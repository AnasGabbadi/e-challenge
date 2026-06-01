package ma.project.echallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.project.echallenge.dto.request.ThemeRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.entity.Theme;
import ma.project.echallenge.service.ThemeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")  // Appliqué à tous les tests
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ThemeService themeService;

    private ThemeRequest validThemeRequest;
    private Theme theme1;
    private Theme theme2;

    @BeforeEach
    void setUp() {
        // Setup valid request
        validThemeRequest = new ThemeRequest();
        validThemeRequest.setName("Java Programming");
        validThemeRequest.setDescription("Questions about Java programming language");

        // Setup themes
        theme1 = new Theme();
        theme1.setId(1L);
        theme1.setName("Java Programming");
        theme1.setDescription("Questions about Java programming language");

        theme2 = new Theme();
        theme2.setId(2L);
        theme2.setName("Spring Framework");
        theme2.setDescription("Questions about Spring Framework");
    }

    // ==================== CREATE THEME TESTS ====================

    @Test
    void createTheme_ShouldReturnCreatedTheme_WhenRequestValid() throws Exception {
        ApiResponse response = ApiResponse.success("Thème créé avec succès", theme1);
        when(themeService.createTheme(any(ThemeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validThemeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Java Programming"));

        verify(themeService, times(1)).createTheme(any(ThemeRequest.class));
    }

    @Test
    void createTheme_ShouldReturn400_WhenNameIsBlank() throws Exception {
        validThemeRequest.setName("");

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validThemeRequest)))
                .andExpect(status().isBadRequest());

        verify(themeService, never()).createTheme(any());
    }

    @Test
    void createTheme_ShouldReturn400_WhenNameIsNull() throws Exception {
        validThemeRequest.setName(null);

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validThemeRequest)))
                .andExpect(status().isBadRequest());

        verify(themeService, never()).createTheme(any());
    }

    @Test
    void createTheme_ShouldReturn400_WhenNameIsTooShort() throws Exception {
        validThemeRequest.setName("J");

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validThemeRequest)))
                .andExpect(status().isBadRequest());

        verify(themeService, never()).createTheme(any());
    }

    @Test
    void createTheme_ShouldReturn400_WhenNameIsTooLong() throws Exception {
        validThemeRequest.setName("a".repeat(101));

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validThemeRequest)))
                .andExpect(status().isBadRequest());

        verify(themeService, never()).createTheme(any());
    }

    @Test
    void createTheme_ShouldReturn400_WhenDescriptionIsTooLong() throws Exception {
        validThemeRequest.setDescription("a".repeat(501));

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validThemeRequest)))
                .andExpect(status().isBadRequest());

        verify(themeService, never()).createTheme(any());
    }

    @Test
    void createTheme_ShouldAcceptNullDescription() throws Exception {
        validThemeRequest.setDescription(null);
        ApiResponse response = ApiResponse.success("Thème créé avec succès", theme1);
        when(themeService.createTheme(any(ThemeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validThemeRequest)))
                .andExpect(status().isOk());

        verify(themeService, times(1)).createTheme(any());
    }

    // ==================== GET ALL THEMES TESTS ====================

    @Test
    void getAllThemes_ShouldReturnListOfThemes() throws Exception {
        List<Theme> themes = Arrays.asList(theme1, theme2);
        ApiResponse response = ApiResponse.success("Thèmes récupérés avec succès", themes);
        when(themeService.getAllThemes()).thenReturn(response);

        mockMvc.perform(get("/api/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Java Programming"))
                .andExpect(jsonPath("$.data[1].name").value("Spring Framework"));

        verify(themeService, times(1)).getAllThemes();
    }

    @Test
    void getAllThemes_ShouldReturnEmptyList_WhenNoThemes() throws Exception {
        ApiResponse response = ApiResponse.success("Thèmes récupérés avec succès", Arrays.asList());
        when(themeService.getAllThemes()).thenReturn(response);

        mockMvc.perform(get("/api/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(themeService, times(1)).getAllThemes();
    }

    // ==================== GET THEME BY ID TESTS ====================

    @Test
    void getThemeById_ShouldReturnTheme_WhenIdExists() throws Exception {
        ApiResponse response = ApiResponse.success("Thème récupéré avec succès", theme1);
        when(themeService.getThemeById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/themes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Java Programming"));

        verify(themeService, times(1)).getThemeById(1L);
    }

    @Test
    void getThemeById_ShouldCallServiceWithCorrectId() throws Exception {
        ApiResponse response = ApiResponse.success("Thème récupéré avec succès", theme2);
        when(themeService.getThemeById(2L)).thenReturn(response);

        mockMvc.perform(get("/api/themes/2"))
                .andExpect(status().isOk());

        verify(themeService, times(1)).getThemeById(2L);
    }

    // ==================== UPDATE THEME TESTS ====================

    @Test
    void updateTheme_ShouldReturnUpdatedTheme_WhenRequestValid() throws Exception {
        Theme updatedTheme = new Theme();
        updatedTheme.setId(1L);
        updatedTheme.setName("Updated Java");
        updatedTheme.setDescription("Updated description");

        ApiResponse response = ApiResponse.success("Thème mis à jour avec succès", updatedTheme);
        when(themeService.updateTheme(eq(1L), any(ThemeRequest.class))).thenReturn(response);

        validThemeRequest.setName("Updated Java");
        validThemeRequest.setDescription("Updated description");

        mockMvc.perform(put("/api/themes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validThemeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Java"));

        verify(themeService, times(1)).updateTheme(eq(1L), any(ThemeRequest.class));
    }

    @Test
    void updateTheme_ShouldReturn400_WhenNameIsBlank() throws Exception {
        validThemeRequest.setName("");

        mockMvc.perform(put("/api/themes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validThemeRequest)))
                .andExpect(status().isBadRequest());

        verify(themeService, never()).updateTheme(anyLong(), any());
    }

    @Test
    void updateTheme_ShouldReturn400_WhenNameIsTooShort() throws Exception {
        validThemeRequest.setName("J");

        mockMvc.perform(put("/api/themes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validThemeRequest)))
                .andExpect(status().isBadRequest());

        verify(themeService, never()).updateTheme(anyLong(), any());
    }

    @Test
    void updateTheme_ShouldReturn400_WhenNameIsTooLong() throws Exception {
        validThemeRequest.setName("a".repeat(101));

        mockMvc.perform(put("/api/themes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validThemeRequest)))
                .andExpect(status().isBadRequest());

        verify(themeService, never()).updateTheme(anyLong(), any());
    }

    @Test
    void updateTheme_ShouldCallServiceWithCorrectIdAndData() throws Exception {
        ApiResponse response = ApiResponse.success("Thème mis à jour avec succès", theme1);
        when(themeService.updateTheme(eq(1L), any(ThemeRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/themes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validThemeRequest)))
                .andExpect(status().isOk());

        verify(themeService).updateTheme(eq(1L), argThat(request ->
                request.getName().equals("Java Programming")
        ));
    }

    // ==================== DELETE THEME TESTS ====================

    @Test
    void deleteTheme_ShouldReturnSuccess_WhenIdExists() throws Exception {
        ApiResponse response = ApiResponse.success("Thème supprimé avec succès", null);
        when(themeService.deleteTheme(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/themes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(themeService, times(1)).deleteTheme(1L);
    }

    @Test
    void deleteTheme_ShouldCallServiceWithCorrectId() throws Exception {
        ApiResponse response = ApiResponse.success("Thème supprimé avec succès", null);
        when(themeService.deleteTheme(2L)).thenReturn(response);

        mockMvc.perform(delete("/api/themes/2"))
                .andExpect(status().isOk());

        verify(themeService, times(1)).deleteTheme(2L);
    }

    @Test
    void deleteTheme_ShouldHandleMultipleDeleteRequests() throws Exception {
        ApiResponse response = ApiResponse.success("Thème supprimé avec succès", null);
        when(themeService.deleteTheme(anyLong())).thenReturn(response);

        // Delete theme 1
        mockMvc.perform(delete("/api/themes/1"))
                .andExpect(status().isOk());

        // Delete theme 2
        mockMvc.perform(delete("/api/themes/2"))
                .andExpect(status().isOk());

        verify(themeService, times(1)).deleteTheme(1L);
        verify(themeService, times(1)).deleteTheme(2L);
    }
}