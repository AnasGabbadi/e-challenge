package ma.project.echallenge.service;

import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.ResultResponse;
import ma.project.echallenge.entity.*;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.ResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @InjectMocks
    private ResultService resultService;

    private Result result;
    private TestSession session;
    private Candidate candidate;
    private ma.project.echallenge.entity.Test test;
    private TimeSlot timeSlot;

    @BeforeEach
    void setUp() {
        candidate = new Candidate();
        candidate.setId(1L);
        candidate.setFirstName("John");
        candidate.setLastName("Doe");
        candidate.setEmail("john@example.com");

        test = new ma.project.echallenge.entity.Test();
        test.setId(1L);
        test.setTitle("Java Test");
        test.setDuration(60);
        test.setPassingScore(70.0);

        timeSlot = new TimeSlot();
        timeSlot.setId(1L);
        timeSlot.setStartTime(LocalDateTime.now().minusHours(2));
        timeSlot.setEndTime(LocalDateTime.now().minusHours(1));

        session = new TestSession(candidate, test, timeSlot, "ABC123");
        session.setId(1L);
        session.setStatus(TestSession.SessionStatus.COMPLETED);

        result = new Result();
        result.setId(1L);
        result.setSession(session);
        result.setCandidate(candidate);
        result.setTotalQuestions(20);
        result.setCorrectAnswers(17);
        result.setScore(85.0);
        result.setPassed(true);
        result.setCompletedAt(LocalDateTime.now().minusHours(1));
    }

    // ==================== GET RESULT BY ID TESTS ====================

    @Test
    void getResultById_ShouldReturnResult_WhenExists() {
        // Arrange
        when(resultRepository.findById(1L)).thenReturn(Optional.of(result));

        // Act
        ApiResponse<ResultResponse> response = resultService.getResultById(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Résultat récupéré", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(85.0, response.getData().getScore());
        assertEquals(20, response.getData().getTotalQuestions());
        assertEquals(17, response.getData().getCorrectAnswers());
        assertTrue(response.getData().getPassed());

        verify(resultRepository, times(1)).findById(1L);
    }

    @Test
    void getResultById_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(resultRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> resultService.getResultById(999L)
        );

        assertEquals("Résultat non trouvé", exception.getMessage());

        verify(resultRepository, times(1)).findById(999L);
    }

    // ==================== GET RESULT BY SESSION ID TESTS ====================

    @Test
    void getResultBySessionId_ShouldReturnResult_WhenExists() {
        // Arrange
        when(resultRepository.findBySessionId(1L)).thenReturn(Optional.of(result));

        // Act
        ApiResponse<ResultResponse> response = resultService.getResultBySessionId(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Résultat récupéré", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(85.0, response.getData().getScore());

        verify(resultRepository, times(1)).findBySessionId(1L);
    }

    @Test
    void getResultBySessionId_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(resultRepository.findBySessionId(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> resultService.getResultBySessionId(999L)
        );

        assertEquals("Résultat non trouvé pour cette session", exception.getMessage());

        verify(resultRepository, times(1)).findBySessionId(999L);
    }

    // ==================== GET RESULTS BY CANDIDATE ID TESTS ====================

    @Test
    void getResultsByCandidateId_ShouldReturnList_WhenResultsExist() {
        // Arrange
        Result result2 = new Result();
        result2.setId(2L);
        result2.setSession(session);
        result2.setCandidate(candidate);
        result2.setTotalQuestions(10);
        result2.setCorrectAnswers(8);
        result2.setScore(80.0);
        result2.setPassed(true);

        when(resultRepository.findByCandidateId(1L))
                .thenReturn(Arrays.asList(result, result2));

        // Act
        ApiResponse<List<ResultResponse>> response = resultService.getResultsByCandidateId(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Résultats du candidat récupérés", response.getMessage());
        assertEquals(2, response.getData().size());

        verify(resultRepository, times(1)).findByCandidateId(1L);
    }

    @Test
    void getResultsByCandidateId_ShouldReturnEmptyList_WhenNoResults() {
        // Arrange
        when(resultRepository.findByCandidateId(1L)).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<ResultResponse>> response = resultService.getResultsByCandidateId(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());

        verify(resultRepository, times(1)).findByCandidateId(1L);
    }

    // ==================== GET PASSED RESULTS TESTS ====================

    @Test
    void getPassedResults_ShouldReturnList_WhenResultsExist() {
        // Arrange
        Result result2 = new Result();
        result2.setId(2L);
        result2.setSession(session);
        result2.setCandidate(candidate);
        result2.setTotalQuestions(15);
        result2.setCorrectAnswers(12);
        result2.setScore(80.0);
        result2.setPassed(true);

        when(resultRepository.findByPassedTrue())
                .thenReturn(Arrays.asList(result, result2));

        // Act
        ApiResponse<List<ResultResponse>> response = resultService.getPassedResults();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Résultats réussis récupérés", response.getMessage());
        assertEquals(2, response.getData().size());

        verify(resultRepository, times(1)).findByPassedTrue();
    }

    @Test
    void getPassedResults_ShouldReturnEmptyList_WhenNoResults() {
        // Arrange
        when(resultRepository.findByPassedTrue()).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<ResultResponse>> response = resultService.getPassedResults();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());

        verify(resultRepository, times(1)).findByPassedTrue();
    }

    // ==================== GET FAILED RESULTS TESTS ====================

    @Test
    void getFailedResults_ShouldReturnList_WhenResultsExist() {
        // Arrange
        Result failedResult = new Result();
        failedResult.setId(2L);
        failedResult.setSession(session);
        failedResult.setCandidate(candidate);
        failedResult.setTotalQuestions(20);
        failedResult.setCorrectAnswers(8);
        failedResult.setScore(40.0);
        failedResult.setPassed(false);

        when(resultRepository.findByPassedFalse())
                .thenReturn(Collections.singletonList(failedResult));

        // Act
        ApiResponse<List<ResultResponse>> response = resultService.getFailedResults();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Résultats échoués récupérés", response.getMessage());
        assertEquals(1, response.getData().size());
        assertFalse(response.getData().get(0).getPassed());

        verify(resultRepository, times(1)).findByPassedFalse();
    }

    @Test
    void getFailedResults_ShouldReturnEmptyList_WhenNoResults() {
        // Arrange
        when(resultRepository.findByPassedFalse()).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<ResultResponse>> response = resultService.getFailedResults();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());

        verify(resultRepository, times(1)).findByPassedFalse();
    }

    // ==================== GET ALL RESULTS TESTS ====================

    @Test
    void getAllResults_ShouldReturnList_WhenResultsExist() {
        // Arrange
        Result result2 = new Result();
        result2.setId(2L);
        result2.setSession(session);
        result2.setCandidate(candidate);
        result2.setTotalQuestions(10);
        result2.setCorrectAnswers(5);
        result2.setScore(50.0);
        result2.setPassed(true);

        when(resultRepository.findAll()).thenReturn(Arrays.asList(result, result2));

        // Act
        ApiResponse<List<ResultResponse>> response = resultService.getAllResults();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Tous les résultats récupérés", response.getMessage());
        assertEquals(2, response.getData().size());

        verify(resultRepository, times(1)).findAll();
    }

    @Test
    void getAllResults_ShouldReturnEmptyList_WhenNoResults() {
        // Arrange
        when(resultRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<ResultResponse>> response = resultService.getAllResults();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());

        verify(resultRepository, times(1)).findAll();
    }

    // ==================== EDGE CASES ====================

    @Test
    void getResultById_ShouldIncludeAllFields() {
        // Arrange
        when(resultRepository.findById(1L)).thenReturn(Optional.of(result));

        // Act
        ApiResponse<ResultResponse> response = resultService.getResultById(1L);

        // Assert
        ResultResponse data = response.getData();
        assertNotNull(data.getId());
        assertNotNull(data.getSessionId());
        assertNotNull(data.getCandidateName());
        assertNotNull(data.getTestTitle());
        assertNotNull(data.getScore());
        assertNotNull(data.getTotalQuestions());
        assertNotNull(data.getCorrectAnswers());
        assertNotNull(data.getPassed());
        assertNotNull(data.getCompletedAt());
    }

    @Test
    void getResultBySessionId_ShouldHandleFailedTest() {
        // Arrange
        Result failedResult = new Result();
        failedResult.setId(2L);
        failedResult.setSession(session);
        failedResult.setCandidate(candidate);
        failedResult.setTotalQuestions(20);
        failedResult.setCorrectAnswers(5);
        failedResult.setScore(25.0);
        failedResult.setPassed(false);

        when(resultRepository.findBySessionId(1L)).thenReturn(Optional.of(failedResult));

        // Act
        ApiResponse<ResultResponse> response = resultService.getResultBySessionId(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(25.0, response.getData().getScore());
        assertFalse(response.getData().getPassed());
    }

    @Test
    void getResultsByCandidateId_ShouldHandleMultipleSessions() {
        // Arrange
        Result result2 = new Result();
        result2.setId(2L);
        result2.setSession(session);
        result2.setCandidate(candidate);
        result2.setScore(70.0);

        Result result3 = new Result();
        result3.setId(3L);
        result3.setSession(session);
        result3.setCandidate(candidate);
        result3.setScore(93.3);

        when(resultRepository.findByCandidateId(1L))
                .thenReturn(Arrays.asList(result, result2, result3));

        // Act
        ApiResponse<List<ResultResponse>> response = resultService.getResultsByCandidateId(1L);

        // Assert
        assertEquals(3, response.getData().size());
        assertEquals(85.0, response.getData().get(0).getScore());
        assertEquals(70.0, response.getData().get(1).getScore());
        assertEquals(93.3, response.getData().get(2).getScore());
    }

    @Test
    void getPassedResults_ShouldHandlePerfectScore() {
        // Arrange
        Result perfectResult = new Result();
        perfectResult.setId(2L);
        perfectResult.setSession(session);
        perfectResult.setCandidate(candidate);
        perfectResult.setTotalQuestions(20);
        perfectResult.setCorrectAnswers(20);
        perfectResult.setScore(100.0);
        perfectResult.setPassed(true);

        when(resultRepository.findByPassedTrue())
                .thenReturn(Collections.singletonList(perfectResult));

        // Act
        ApiResponse<List<ResultResponse>> response = resultService.getPassedResults();

        // Assert
        assertEquals(1, response.getData().size());
        assertEquals(100.0, response.getData().get(0).getScore());
        assertEquals(20, response.getData().get(0).getCorrectAnswers());
    }

    @Test
    void getFailedResults_ShouldHandleZeroScore() {
        // Arrange
        Result zeroResult = new Result();
        zeroResult.setId(2L);
        zeroResult.setSession(session);
        zeroResult.setCandidate(candidate);
        zeroResult.setTotalQuestions(20);
        zeroResult.setCorrectAnswers(0);
        zeroResult.setScore(0.0);
        zeroResult.setPassed(false);

        when(resultRepository.findByPassedFalse())
                .thenReturn(Collections.singletonList(zeroResult));

        // Act
        ApiResponse<List<ResultResponse>> response = resultService.getFailedResults();

        // Assert
        assertEquals(1, response.getData().size());
        assertEquals(0.0, response.getData().get(0).getScore());
        assertFalse(response.getData().get(0).getPassed());
    }

    @Test
    void getAllResults_ShouldIncludeBothPassedAndFailed() {
        // Arrange
        Result passedResult = new Result();
        passedResult.setId(1L);
        passedResult.setSession(session);
        passedResult.setCandidate(candidate);
        passedResult.setPassed(true);

        Result failedResult = new Result();
        failedResult.setId(2L);
        failedResult.setSession(session);
        failedResult.setCandidate(candidate);
        failedResult.setPassed(false);

        when(resultRepository.findAll()).thenReturn(Arrays.asList(passedResult, failedResult));

        // Act
        ApiResponse<List<ResultResponse>> response = resultService.getAllResults();

        // Assert
        assertEquals(2, response.getData().size());
        assertTrue(response.getData().get(0).getPassed());
        assertFalse(response.getData().get(1).getPassed());
    }
}