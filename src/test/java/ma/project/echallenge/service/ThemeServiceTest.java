package ma.project.echallenge.service;

import ma.project.echallenge.dto.request.ThemeRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.entity.Theme;
import ma.project.echallenge.exception.BadRequestException;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.ThemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        theme.setName("Java");
        theme.setDescription("Java Programming Language");

        themeRequest = new ThemeRequest();
        themeRequest.setName("Java");
        themeRequest.setDescription("Java Programming Language");
    }

    // ==================== CREATE TESTS ====================

    @Test
    void createTheme_ShouldReturnSuccess_WhenValidRequest() {
        // Arrange
        when(themeRepository.existsByName(anyString())).thenReturn(false);
        when(themeRepository.save(any(Theme.class))).thenReturn(theme);

        // Act
        ApiResponse<Theme> response = themeService.createTheme(themeRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Thème créé avec succès", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("Java", response.getData().getName());

        verify(themeRepository, times(1)).existsByName("Java");
        verify(themeRepository, times(1)).save(any(Theme.class));
    }

    @Test
    void createTheme_ShouldThrowException_WhenNameAlreadyExists() {
        // Arrange
        when(themeRepository.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> themeService.createTheme(themeRequest)
        );

        assertEquals("Un thème avec ce nom existe déjà", exception.getMessage());

        verify(themeRepository, times(1)).existsByName("Java");
        verify(themeRepository, never()).save(any(Theme.class));
    }

    @Test
    void createTheme_ShouldSetAllFields() {
        // Arrange
        when(themeRepository.existsByName(anyString())).thenReturn(false);
        when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> {
            Theme saved = invocation.getArgument(0);
            assertEquals("Java", saved.getName());
            assertEquals("Java Programming Language", saved.getDescription());
            return saved;
        });

        // Act
        themeService.createTheme(themeRequest);

        // Assert
        verify(themeRepository, times(1)).save(any(Theme.class));
    }

    @Test
    void createTheme_ShouldHandleNullDescription() {
        // Arrange
        themeRequest.setDescription(null);
        when(themeRepository.existsByName(anyString())).thenReturn(false);
        when(themeRepository.save(any(Theme.class))).thenReturn(theme);

        // Act
        ApiResponse<Theme> response = themeService.createTheme(themeRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @Test
    void createTheme_ShouldHandleLongName() {
        // Arrange
        String longName = "A".repeat(255);
        themeRequest.setName(longName);
        when(themeRepository.existsByName(anyString())).thenReturn(false);
        when(themeRepository.save(any(Theme.class))).thenReturn(theme);

        // Act
        ApiResponse<Theme> response = themeService.createTheme(themeRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    // ==================== READ TESTS ====================

    @Test
    void getAllThemes_ShouldReturnList_WhenThemesExist() {
        // Arrange
        Theme theme2 = new Theme();
        theme2.setId(2L);
        theme2.setName("Python");
        theme2.setDescription("Python Programming");

        when(themeRepository.findAll()).thenReturn(Arrays.asList(theme, theme2));

        // Act
        ApiResponse<List<Theme>> response = themeService.getAllThemes();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Thèmes récupérés", response.getMessage());
        assertEquals(2, response.getData().size());
        assertEquals("Java", response.getData().get(0).getName());
        assertEquals("Python", response.getData().get(1).getName());

        verify(themeRepository, times(1)).findAll();
    }

    @Test
    void getAllThemes_ShouldReturnEmptyList_WhenNoThemes() {
        // Arrange
        when(themeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<Theme>> response = themeService.getAllThemes();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());

        verify(themeRepository, times(1)).findAll();
    }

    @Test
    void getThemeById_ShouldReturnTheme_WhenExists() {
        // Arrange
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));

        // Act
        ApiResponse<Theme> response = themeService.getThemeById(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Thème récupéré", response.getMessage());
        assertEquals("Java", response.getData().getName());
        assertEquals("Java Programming Language", response.getData().getDescription());

        verify(themeRepository, times(1)).findById(1L);
    }

    @Test
    void getThemeById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(themeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> themeService.getThemeById(999L)
        );

        assertEquals("Thème non trouvé", exception.getMessage());

        verify(themeRepository, times(1)).findById(999L);
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void updateTheme_ShouldReturnSuccess_WhenValidRequest() {
        // Arrange
        themeRequest.setName("Java Advanced");
        themeRequest.setDescription("Advanced Java Programming");

        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(themeRepository.existsByName(anyString())).thenReturn(false);
        when(themeRepository.save(any(Theme.class))).thenReturn(theme);

        // Act
        ApiResponse<Theme> response = themeService.updateTheme(1L, themeRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Thème mis à jour", response.getMessage());

        verify(themeRepository, times(1)).findById(1L);
        verify(themeRepository, times(1)).save(any(Theme.class));
    }

    @Test
    void updateTheme_ShouldThrowException_WhenThemeNotFound() {
        // Arrange
        when(themeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> themeService.updateTheme(999L, themeRequest)
        );

        assertEquals("Thème non trouvé", exception.getMessage());

        verify(themeRepository, times(1)).findById(999L);
        verify(themeRepository, never()).save(any(Theme.class));
    }

    @Test
    void updateTheme_ShouldThrowException_WhenNewNameAlreadyExists() {
        // Arrange
        themeRequest.setName("Python");

        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(themeRepository.existsByName("Python")).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> themeService.updateTheme(1L, themeRequest)
        );

        assertEquals("Un thème avec ce nom existe déjà", exception.getMessage());

        verify(themeRepository, times(1)).findById(1L);
        verify(themeRepository, never()).save(any(Theme.class));
    }

    @Test
    void updateTheme_ShouldAllowKeepingSameName() {
        // Arrange
        themeRequest.setName("Java");
        themeRequest.setDescription("Updated description");

        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(themeRepository.save(any(Theme.class))).thenReturn(theme);

        // Act
        ApiResponse<Theme> response = themeService.updateTheme(1L, themeRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());

        verify(themeRepository, times(1)).findById(1L);
        verify(themeRepository, never()).existsByName(anyString());
        verify(themeRepository, times(1)).save(any(Theme.class));
    }

    @Test
    void updateTheme_ShouldUpdateAllFields() {
        // Arrange
        themeRequest.setName("Java Advanced");
        themeRequest.setDescription("Advanced Java Programming");

        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(themeRepository.existsByName(anyString())).thenReturn(false);
        when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> {
            Theme updated = invocation.getArgument(0);
            assertEquals("Java Advanced", updated.getName());
            assertEquals("Advanced Java Programming", updated.getDescription());
            return updated;
        });

        // Act
        themeService.updateTheme(1L, themeRequest);

        // Assert
        verify(themeRepository, times(1)).save(any(Theme.class));
    }

    // ==================== DELETE TESTS ====================

    @Test
    void deleteTheme_ShouldReturnSuccess_WhenExists() {
        // Arrange
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        doNothing().when(themeRepository).delete(any(Theme.class));

        // Act
        ApiResponse<String> response = themeService.deleteTheme(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Thème supprimé avec succès", response.getMessage());

        verify(themeRepository, times(1)).findById(1L);
        verify(themeRepository, times(1)).delete(theme);
    }

    @Test
    void deleteTheme_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(themeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> themeService.deleteTheme(999L)
        );

        assertEquals("Thème non trouvé", exception.getMessage());

        verify(themeRepository, times(1)).findById(999L);
        verify(themeRepository, never()).delete(any(Theme.class));
    }

    // ==================== EDGE CASES ====================

    @Test
    void createTheme_ShouldHandleEmptyDescription() {
        // Arrange
        themeRequest.setDescription("");
        when(themeRepository.existsByName(anyString())).thenReturn(false);
        when(themeRepository.save(any(Theme.class))).thenReturn(theme);

        // Act
        ApiResponse<Theme> response = themeService.createTheme(themeRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @Test
    void getAllThemes_ShouldReturnMultipleThemes() {
        // Arrange
        Theme theme2 = new Theme();
        theme2.setId(2L);
        theme2.setName("Python");

        Theme theme3 = new Theme();
        theme3.setId(3L);
        theme3.setName("JavaScript");

        Theme theme4 = new Theme();
        theme4.setId(4L);
        theme4.setName("C++");

        when(themeRepository.findAll())
                .thenReturn(Arrays.asList(theme, theme2, theme3, theme4));

        // Act
        ApiResponse<List<Theme>> response = themeService.getAllThemes();

        // Assert
        assertEquals(4, response.getData().size());
        assertEquals("Java", response.getData().get(0).getName());
        assertEquals("Python", response.getData().get(1).getName());
        assertEquals("JavaScript", response.getData().get(2).getName());
        assertEquals("C++", response.getData().get(3).getName());
    }

    @Test
    void updateTheme_ShouldHandleSpecialCharacters() {
        // Arrange
        themeRequest.setName("C++");
        themeRequest.setDescription("C++ & Object-Oriented Programming");

        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(themeRepository.existsByName(anyString())).thenReturn(false);
        when(themeRepository.save(any(Theme.class))).thenReturn(theme);

        // Act
        ApiResponse<Theme> response = themeService.updateTheme(1L, themeRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @Test
    void createTheme_ShouldBeCaseInsensitiveForNameCheck() {
        // Arrange
        themeRequest.setName("JAVA");
        when(themeRepository.existsByName("JAVA")).thenReturn(false);
        when(themeRepository.save(any(Theme.class))).thenReturn(theme);

        // Act
        ApiResponse<Theme> response = themeService.createTheme(themeRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(themeRepository, times(1)).existsByName("JAVA");
    }

    @Test
    void updateTheme_ShouldHandleNullDescription() {
        // Arrange
        themeRequest.setName("Java");
        themeRequest.setDescription(null);

        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(themeRepository.save(any(Theme.class))).thenReturn(theme);

        // Act
        ApiResponse<Theme> response = themeService.updateTheme(1L, themeRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }
}