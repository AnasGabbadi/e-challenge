package ma.project.echallenge.service;

import ma.project.echallenge.dto.request.TestRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.TestResponse;
import ma.project.echallenge.entity.Test;
import ma.project.echallenge.entity.Theme;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.TestRepository;
import ma.project.echallenge.repository.ThemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceTest {

    @Mock
    private TestRepository testRepository;

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private TestService testService;

    private Theme theme;
    private Test test;
    private TestRequest testRequest;

    @BeforeEach
    void setUp() {
        theme = new Theme();
        theme.setId(1L);
        theme.setName("Java");
        theme.setDescription("Java Programming");

        test = new Test();
        test.setId(1L);
        test.setTitle("Java Basics Test");
        test.setDescription("Test on Java fundamentals");
        test.setDuration(60);
        test.setQuestionDuration(120);
        test.setPassingScore(70.0);
        test.setTheme(theme);
        test.setQuestions(Collections.emptyList());

        testRequest = new TestRequest();
        testRequest.setTitle("Java Basics Test");
        testRequest.setDescription("Test on Java fundamentals");
        testRequest.setDuration(60);
        testRequest.setQuestionDuration(120);
        testRequest.setPassingScore(70.0);
        testRequest.setThemeId(1L);
    }

    // ==================== CREATE TESTS ====================

    @org.junit.jupiter.api.Test
    void createTest_ShouldReturnSuccess_WhenValidRequest() {
        // Arrange
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(testRepository.save(any(Test.class))).thenReturn(test);

        // Act
        ApiResponse<TestResponse> response = testService.createTest(testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Test créé avec succès", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("Java Basics Test", response.getData().getTitle());
        assertEquals(60, response.getData().getDuration());
        assertEquals(70.0, response.getData().getPassingScore());

        verify(themeRepository, times(1)).findById(1L);
        verify(testRepository, times(1)).save(any(Test.class));
    }

    @org.junit.jupiter.api.Test
    void createTest_ShouldThrowException_WhenThemeNotFound() {
        // Arrange
        when(themeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> testService.createTest(testRequest)
        );

        assertEquals("Thème non trouvé", exception.getMessage());

        verify(themeRepository, times(1)).findById(1L);
        verify(testRepository, never()).save(any(Test.class));
    }

    @org.junit.jupiter.api.Test
    void createTest_ShouldSetAllFields() {
        // Arrange
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(testRepository.save(any(Test.class))).thenAnswer(invocation -> {
            Test saved = invocation.getArgument(0);
            assertEquals("Java Basics Test", saved.getTitle());
            assertEquals("Test on Java fundamentals", saved.getDescription());
            assertEquals(60, saved.getDuration());
            assertEquals(120, saved.getQuestionDuration());
            assertEquals(70.0, saved.getPassingScore());
            assertEquals(theme, saved.getTheme());
            return saved;
        });

        // Act
        testService.createTest(testRequest);

        // Assert
        verify(testRepository, times(1)).save(any(Test.class));
    }

    @org.junit.jupiter.api.Test
    void createTest_ShouldHandleLongTitle() {
        // Arrange
        String longTitle = "A".repeat(255);
        testRequest.setTitle(longTitle);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(testRepository.save(any(Test.class))).thenReturn(test);

        // Act
        ApiResponse<TestResponse> response = testService.createTest(testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(testRepository, times(1)).save(any(Test.class));
    }

    @org.junit.jupiter.api.Test
    void createTest_ShouldHandleZeroPassingScore() {
        // Arrange
        testRequest.setPassingScore(0.0);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(testRepository.save(any(Test.class))).thenReturn(test);

        // Act
        ApiResponse<TestResponse> response = testService.createTest(testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @org.junit.jupiter.api.Test
    void createTest_ShouldHandleMaxPassingScore() {
        // Arrange
        testRequest.setPassingScore(100.0);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(testRepository.save(any(Test.class))).thenReturn(test);

        // Act
        ApiResponse<TestResponse> response = testService.createTest(testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    // ==================== READ TESTS ====================

    @org.junit.jupiter.api.Test
    void getAllTests_ShouldReturnList_WhenTestsExist() {
        // Arrange
        Test test2 = new Test();
        test2.setId(2L);
        test2.setTitle("Python Test");
        test2.setDescription("Python basics");
        test2.setDuration(45);
        test2.setQuestionDuration(90);
        test2.setPassingScore(65.0);
        test2.setTheme(theme);
        test2.setQuestions(Collections.emptyList());

        when(testRepository.findAll()).thenReturn(Arrays.asList(test, test2));

        // Act
        ApiResponse<List<TestResponse>> response = testService.getAllTests();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Tests récupérés", response.getMessage());
        assertEquals(2, response.getData().size());
        assertEquals("Java Basics Test", response.getData().get(0).getTitle());
        assertEquals("Python Test", response.getData().get(1).getTitle());

        verify(testRepository, times(1)).findAll();
    }

    @org.junit.jupiter.api.Test
    void getAllTests_ShouldReturnEmptyList_WhenNoTests() {
        // Arrange
        when(testRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<TestResponse>> response = testService.getAllTests();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());

        verify(testRepository, times(1)).findAll();
    }

    @org.junit.jupiter.api.Test
    void getTestById_ShouldReturnTest_WhenExists() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));

        // Act
        ApiResponse<TestResponse> response = testService.getTestById(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Test récupéré", response.getMessage());
        assertEquals("Java Basics Test", response.getData().getTitle());
        assertEquals(60, response.getData().getDuration());
        assertEquals("Java", response.getData().getThemeName());

        verify(testRepository, times(1)).findById(1L);
    }

    @org.junit.jupiter.api.Test
    void getTestById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(testRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> testService.getTestById(999L)
        );

        assertEquals("Test non trouvé", exception.getMessage());

        verify(testRepository, times(1)).findById(999L);
    }

    @org.junit.jupiter.api.Test
    void getTestById_ShouldReturnTestWithCorrectTheme() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));

        // Act
        ApiResponse<TestResponse> response = testService.getTestById(1L);

        // Assert
        assertEquals("Java", response.getData().getThemeName());
        assertEquals(1L, response.getData().getThemeId());
    }

    // ==================== UPDATE TESTS ====================

    @org.junit.jupiter.api.Test
    void updateTest_ShouldReturnSuccess_WhenValidRequest() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(testRepository.save(any(Test.class))).thenReturn(test);

        testRequest.setTitle("Updated Java Test");
        testRequest.setDuration(90);
        testRequest.setPassingScore(75.0);

        // Act
        ApiResponse<TestResponse> response = testService.updateTest(1L, testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Test mis à jour", response.getMessage());

        verify(testRepository, times(1)).findById(1L);
        verify(themeRepository, times(1)).findById(1L);
        verify(testRepository, times(1)).save(any(Test.class));
    }

    @org.junit.jupiter.api.Test
    void updateTest_ShouldThrowException_WhenTestNotFound() {
        // Arrange
        when(testRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> testService.updateTest(999L, testRequest)
        );

        assertEquals("Test non trouvé", exception.getMessage());

        verify(testRepository, times(1)).findById(999L);
        verify(testRepository, never()).save(any(Test.class));
    }

    @org.junit.jupiter.api.Test
    void updateTest_ShouldThrowException_WhenThemeNotFound() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> testService.updateTest(1L, testRequest)
        );

        assertEquals("Thème non trouvé", exception.getMessage());

        verify(testRepository, times(1)).findById(1L);
        verify(themeRepository, times(1)).findById(1L);
        verify(testRepository, never()).save(any(Test.class));
    }

    @org.junit.jupiter.api.Test
    void updateTest_ShouldUpdateAllFields() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(testRepository.save(any(Test.class))).thenAnswer(invocation -> {
            Test updated = invocation.getArgument(0);
            assertEquals("Java Basics Test", updated.getTitle());
            assertEquals("Test on Java fundamentals", updated.getDescription());
            assertEquals(60, updated.getDuration());
            assertEquals(120, updated.getQuestionDuration());
            assertEquals(70.0, updated.getPassingScore());
            return updated;
        });

        // Act
        testService.updateTest(1L, testRequest);

        // Assert
        verify(testRepository, times(1)).save(any(Test.class));
    }

    @org.junit.jupiter.api.Test
    void updateTest_ShouldAllowChangingTheme() {
        // Arrange
        Theme newTheme = new Theme();
        newTheme.setId(2L);
        newTheme.setName("Python");

        testRequest.setThemeId(2L);

        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(themeRepository.findById(2L)).thenReturn(Optional.of(newTheme));
        when(testRepository.save(any(Test.class))).thenAnswer(invocation -> {
            Test updated = invocation.getArgument(0);
            assertEquals(newTheme, updated.getTheme());
            return updated;
        });

        // Act
        testService.updateTest(1L, testRequest);

        // Assert
        verify(themeRepository, times(1)).findById(2L);
        verify(testRepository, times(1)).save(any(Test.class));
    }

    // ==================== DELETE TESTS ====================

    @org.junit.jupiter.api.Test
    void deleteTest_ShouldReturnSuccess_WhenExists() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        doNothing().when(testRepository).delete(any(Test.class));

        // Act
        ApiResponse<String> response = testService.deleteTest(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Test supprimé avec succès", response.getMessage());

        verify(testRepository, times(1)).findById(1L);
        verify(testRepository, times(1)).delete(test);
    }

    @org.junit.jupiter.api.Test
    void deleteTest_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(testRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> testService.deleteTest(999L)
        );

        assertEquals("Test non trouvé", exception.getMessage());

        verify(testRepository, times(1)).findById(999L);
        verify(testRepository, never()).delete(any(Test.class));
    }

    // ==================== EDGE CASES ====================

    @org.junit.jupiter.api.Test
    void createTest_ShouldHandleNullDescription() {
        // Arrange
        testRequest.setDescription(null);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(testRepository.save(any(Test.class))).thenReturn(test);

        // Act
        ApiResponse<TestResponse> response = testService.createTest(testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @org.junit.jupiter.api.Test
    void createTest_ShouldHandleMinimumDuration() {
        // Arrange
        testRequest.setDuration(1);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(testRepository.save(any(Test.class))).thenReturn(test);

        // Act
        ApiResponse<TestResponse> response = testService.createTest(testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @org.junit.jupiter.api.Test
    void createTest_ShouldHandleMaximumDuration() {
        // Arrange
        testRequest.setDuration(999);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(testRepository.save(any(Test.class))).thenReturn(test);

        // Act
        ApiResponse<TestResponse> response = testService.createTest(testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @org.junit.jupiter.api.Test
    void mapToResponse_ShouldIncludeQuestionCount() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));

        // Act
        ApiResponse<TestResponse> response = testService.getTestById(1L);

        // Assert
        assertNotNull(response.getData().getQuestionCount());
        assertEquals(0, response.getData().getQuestionCount());
    }
}