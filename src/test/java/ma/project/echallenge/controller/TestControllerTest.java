package ma.project.echallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.project.echallenge.dto.request.TestRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.TestResponse;
import ma.project.echallenge.service.TestService;
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
@WithMockUser(roles = "ADMIN")
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestService testService;

    private TestRequest validTestRequest;
    private TestResponse testResponse1;
    private TestResponse testResponse2;

    @BeforeEach
    void setUp() {
        // Setup valid test request
        validTestRequest = new TestRequest();
        validTestRequest.setTitle("Java Basics Test");
        validTestRequest.setDescription("A comprehensive test on Java basics");
        validTestRequest.setDuration(60);
        validTestRequest.setThemeId(1L);
        validTestRequest.setQuestionDuration(5);
        validTestRequest.setPassingScore(70.0);

        // Setup test responses
        testResponse1 = new TestResponse();
        testResponse1.setId(1L);
        testResponse1.setTitle("Java Basics Test");
        testResponse1.setDuration(60);

        testResponse2 = new TestResponse();
        testResponse2.setId(2L);
        testResponse2.setTitle("Spring Framework Test");
        testResponse2.setDuration(90);
    }

    // ==================== CREATE TEST TESTS ====================

    @Test
    void createTest_ShouldReturnCreatedTest_WhenRequestValid() throws Exception {
        ApiResponse response = ApiResponse.success("Test créé avec succès", testResponse1);
        when(testService.createTest(any(TestRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Java Basics Test"));

        verify(testService, times(1)).createTest(any(TestRequest.class));
    }

    @Test
    void createTest_ShouldReturn400_WhenTitleIsBlank() throws Exception {
        validTestRequest.setTitle("");

        mockMvc.perform(post("/api/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isBadRequest());

        verify(testService, never()).createTest(any());
    }

    @Test
    void createTest_ShouldReturn400_WhenTitleIsNull() throws Exception {
        validTestRequest.setTitle(null);

        mockMvc.perform(post("/api/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isBadRequest());

        verify(testService, never()).createTest(any());
    }

    @Test
    void createTest_ShouldReturn400_WhenDurationIsZero() throws Exception {
        validTestRequest.setDuration(0);

        mockMvc.perform(post("/api/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isBadRequest());

        verify(testService, never()).createTest(any());
    }

    @Test
    void createTest_ShouldReturn400_WhenDurationIsNegative() throws Exception {
        validTestRequest.setDuration(-10);

        mockMvc.perform(post("/api/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isBadRequest());

        verify(testService, never()).createTest(any());
    }

    @Test
    void createTest_ShouldReturn400_WhenPassingScoreIsNegative() throws Exception {
        validTestRequest.setPassingScore(-5.0);

        mockMvc.perform(post("/api/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isBadRequest());

        verify(testService, never()).createTest(any());
    }

    @Test
    void createTest_ShouldAcceptNullDescription() throws Exception {
        validTestRequest.setDescription(null);
        ApiResponse response = ApiResponse.success("Test créé avec succès", testResponse1);
        when(testService.createTest(any(TestRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isOk());

        verify(testService, times(1)).createTest(any());
    }

    @Test
    void createTest_ShouldAcceptNullThemeId() throws Exception {
        validTestRequest.setThemeId(null);
        ApiResponse response = ApiResponse.success("Test créé avec succès", testResponse1);
        when(testService.createTest(any(TestRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isOk());

        verify(testService, times(1)).createTest(any());
    }

    // ==================== GET ALL TESTS ====================

    @Test
    void getAllTests_ShouldReturnListOfTests() throws Exception {
        List<TestResponse> tests = Arrays.asList(testResponse1, testResponse2);
        ApiResponse response = ApiResponse.success("Tests récupérés avec succès", tests);
        when(testService.getAllTests()).thenReturn(response);

        mockMvc.perform(get("/api/tests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Java Basics Test"))
                .andExpect(jsonPath("$.data[1].title").value("Spring Framework Test"));

        verify(testService, times(1)).getAllTests();
    }

    @Test
    void getAllTests_ShouldReturnEmptyList_WhenNoTests() throws Exception {
        ApiResponse response = ApiResponse.success("Tests récupérés avec succès", Arrays.asList());
        when(testService.getAllTests()).thenReturn(response);

        mockMvc.perform(get("/api/tests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(testService, times(1)).getAllTests();
    }

    // ==================== GET TEST BY ID TESTS ====================

    @Test
    void getTestById_ShouldReturnTest_WhenIdExists() throws Exception {
        ApiResponse response = ApiResponse.success("Test récupéré avec succès", testResponse1);
        when(testService.getTestById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/tests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Java Basics Test"));

        verify(testService, times(1)).getTestById(1L);
    }

    @Test
    void getTestById_ShouldCallServiceWithCorrectId() throws Exception {
        ApiResponse response = ApiResponse.success("Test récupéré avec succès", testResponse2);
        when(testService.getTestById(2L)).thenReturn(response);

        mockMvc.perform(get("/api/tests/2"))
                .andExpect(status().isOk());

        verify(testService, times(1)).getTestById(2L);
    }

    // ==================== UPDATE TEST TESTS ====================

    @Test
    void updateTest_ShouldReturnUpdatedTest_WhenRequestValid() throws Exception {
        TestResponse updatedTest = new TestResponse();
        updatedTest.setId(1L);
        updatedTest.setTitle("Updated Java Test");
        updatedTest.setDuration(120);

        ApiResponse response = ApiResponse.success("Test mis à jour avec succès", updatedTest);
        when(testService.updateTest(eq(1L), any(TestRequest.class))).thenReturn(response);

        validTestRequest.setTitle("Updated Java Test");
        validTestRequest.setDuration(120);

        mockMvc.perform(put("/api/tests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Java Test"));

        verify(testService, times(1)).updateTest(eq(1L), any(TestRequest.class));
    }

    @Test
    void updateTest_ShouldReturn400_WhenTitleIsBlank() throws Exception {
        validTestRequest.setTitle("");

        mockMvc.perform(put("/api/tests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isBadRequest());

        verify(testService, never()).updateTest(anyLong(), any());
    }

    @Test
    void updateTest_ShouldReturn400_WhenDurationIsZero() throws Exception {
        validTestRequest.setDuration(0);

        mockMvc.perform(put("/api/tests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isBadRequest());

        verify(testService, never()).updateTest(anyLong(), any());
    }

    @Test
    void updateTest_ShouldReturn400_WhenPassingScoreIsNegative() throws Exception {
        validTestRequest.setPassingScore(-10.0);

        mockMvc.perform(put("/api/tests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isBadRequest());

        verify(testService, never()).updateTest(anyLong(), any());
    }

    @Test
    void updateTest_ShouldCallServiceWithCorrectIdAndData() throws Exception {
        ApiResponse response = ApiResponse.success("Test mis à jour avec succès", testResponse1);
        when(testService.updateTest(eq(1L), any(TestRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/tests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTestRequest)))
                .andExpect(status().isOk());

        verify(testService).updateTest(eq(1L), argThat(request ->
                request.getTitle().equals("Java Basics Test") &&
                        request.getDuration().equals(60)
        ));
    }

    // ==================== DELETE TEST TESTS ====================

    @Test
    void deleteTest_ShouldReturnSuccess_WhenIdExists() throws Exception {
        ApiResponse response = ApiResponse.success("Test supprimé avec succès", null);
        when(testService.deleteTest(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/tests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(testService, times(1)).deleteTest(1L);
    }

    @Test
    void deleteTest_ShouldCallServiceWithCorrectId() throws Exception {
        ApiResponse response = ApiResponse.success("Test supprimé avec succès", null);
        when(testService.deleteTest(2L)).thenReturn(response);

        mockMvc.perform(delete("/api/tests/2"))
                .andExpect(status().isOk());

        verify(testService, times(1)).deleteTest(2L);
    }

    @Test
    void deleteTest_ShouldHandleMultipleDeleteRequests() throws Exception {
        ApiResponse response = ApiResponse.success("Test supprimé avec succès", null);
        when(testService.deleteTest(anyLong())).thenReturn(response);

        mockMvc.perform(delete("/api/tests/1"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/tests/2"))
                .andExpect(status().isOk());

        verify(testService, times(1)).deleteTest(1L);
        verify(testService, times(1)).deleteTest(2L);
    }
}