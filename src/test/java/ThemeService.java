package ma.customer.project;

import ma.customer.project.echallenge.dto.request.ThemeRequest;
import ma.customer.project.echallenge.dto.response.ApiResponse;
import ma.customer.project.echallenge.entity.Theme;
import ma.customer.project.echallenge.exception.BadRequestException;
import ma.customer.project.echallenge.exception.ResourceNotFoundException;
import ma.customer.project.echallenge.repository.ThemeRepository;
import ma.customer.project.echallenge.service.ThemeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ThemeService tests")
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeService themeService;

    private Theme theme;
    private ThemeRequest themeRequest;

    @BeforeEach
    void setUp() {
        theme = new Theme();
        theme.setId(1L);
        theme.setName("Test Theme");
        theme.setDescription("Test Description");

        themeRequest = new ThemeRequest();
        themeRequest.setName("New Theme");
        themeRequest.setDescription("New Description");
    }

    @Test
    @DisplayName("createTheme(themeRequest) should create a new theme successfully")
    void createTheme_shouldCreateNewThemeSuccessfully() {
        // Given
        when(themeRepository.existsByName(anyString())).thenReturn(false);
        when(themeRepository.save(any(Theme.class))).thenReturn(theme);

        // When
        ApiResponse<Theme> response = themeService.createTheme(themeRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Thème créé avec succès", response.getMessage());
        assertEquals(theme, response.getData());

        verify(themeRepository, times(1)).existsByName(anyString());
        verify(themeRepository, times(1)).save(any(Theme.class));
    }

    @Test
    @DisplayName("createTheme(themeRequest) should throw BadRequestException when theme name already exists")
    void createTheme_shouldThrowBadRequestExceptionWhenThemeNameExists() {
        // Given
        when(themeRepository.existsByName(anyString())).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () -> themeService.createTheme(themeRequest));

        verify(themeRepository, times(1)).existsByName(anyString());
        verify(themeRepository, never()).save(any(Theme.class));
    }

    @Test
    @DisplayName("getAllThemes() should return all themes successfully")
    void getAllThemes_shouldReturnAllThemesSuccessfully() {
        // Given
        when(themeRepository.findAll()).thenReturn(List.of(theme));

        // When
        ApiResponse<List<Theme>> response = themeService.getAllThemes();

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Thèmes récupérés", response.getMessage());
        assertEquals(1, response.getData().size());
        assertEquals(theme, response.getData().get(0));

        verify(themeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllThemes() should return empty list when no themes exist")
    void getAllThemes_shouldReturnEmptyListWhenNoThemesExist() {
        // Given
        when(themeRepository.findAll()).thenReturn(List.of());

        // When
        ApiResponse<List<Theme>> response = themeService.getAllThemes();

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Thèmes récupérés", response.getMessage());
        assertTrue(response.getData().isEmpty());

        verify(themeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getThemeById(id) should return theme when it exists")
    void getThemeById_shouldReturnThemeWhenItExists() {
        // Given
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));

        // When
        ApiResponse<Theme> response = themeService.getThemeById(1L);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Thème récupéré", response.getMessage());
        assertEquals(theme, response.getData());

        verify(themeRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("getThemeById(id) should throw ResourceNotFoundException when theme does not exist")
    void getThemeById_shouldThrowResourceNotFoundExceptionWhenThemeDoesNotExist() {
        // Given
        when(themeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> themeService.getThemeById(1L));

        verify(themeRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("updateTheme(id, themeRequest) should update theme successfully when it exists")
    void updateTheme_shouldUpdateThemeSuccessfullyWhenItExists() {
        // Given
        ThemeRequest updateRequest = new ThemeRequest();
        updateRequest.setName("Updated Theme");
        updateRequest.setDescription("Updated Description");

        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));
        when(themeRepository.existsByName(anyString())).thenReturn(false);
        when(themeRepository.save(any(Theme.class))).thenReturn(theme);

        // When
        ApiResponse<Theme> response = themeService.updateTheme(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Thème mis à jour", response.getMessage());
        assertEquals(theme, response.getData());

        verify(themeRepository, times(1)).findById(anyLong());
        verify(themeRepository, times(1)).existsByName(anyString());
        verify(themeRepository, times(1)).save(any(Theme.class));
    }

    @Test
    @DisplayName("updateTheme(id, themeRequest) should throw ResourceNotFoundException when theme does not exist")
    void updateTheme_shouldThrowResourceNotFoundExceptionWhenThemeDoesNotExist() {
        // Given
        when(themeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> themeService.updateTheme(1L, themeRequest));

        verify(themeRepository, times(1)).findById(anyLong());
        verify(themeRepository, never()).existsByName(anyString());
        verify(themeRepository, never()).save(any(Theme.class));
    }

    @Test
    @DisplayName("updateTheme(id, themeRequest) should throw BadRequestException when new theme name already exists")
    void updateTheme_shouldThrowBadRequestExceptionWhenNewThemeNameAlreadyExists() {
        // Given
        ThemeRequest updateRequest = new ThemeRequest();
        updateRequest.setName("Existing Theme");
        updateRequest.setDescription("Updated Description");

        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));
        when(themeRepository.existsByName(anyString())).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () -> themeService.updateTheme(1L, updateRequest));

        verify(themeRepository, times(1)).findById(anyLong());
        verify(themeRepository, times(1)).existsByName(anyString());
        verify(themeRepository, never()).save(any(Theme.class));
    }

    @Test
    @DisplayName("deleteTheme(id) should delete theme successfully when it exists")
    void deleteTheme_shouldDeleteThemeSuccessfullyWhenItExists() {
        // Given
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));

        // When
        ApiResponse<String> response = themeService.deleteTheme(1L);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Thème supprimé avec succès", response.getMessage());
        assertNull(response.getData());

        verify(themeRepository, times(1)).findById(anyLong());
        verify(themeRepository, times(1)).delete(any(Theme.class));
    }

    @Test
    @DisplayName("deleteTheme(id) should throw ResourceNotFoundException when theme does not exist")
    void deleteTheme_shouldThrowResourceNotFoundExceptionWhenThemeDoesNotExist() {
        // Given
        when(themeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> themeService.deleteTheme(1L));

        verify(themeRepository, times(1)).findById(anyLong());
        verify(themeRepository, never()).delete(any(Theme.class));
    }
}