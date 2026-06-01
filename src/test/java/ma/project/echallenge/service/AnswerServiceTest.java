package ma.project.echallenge.service;

import ma.project.echallenge.dto.request.SubmitAnswerRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.entity.*;
import ma.project.echallenge.exception.BadRequestException;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.CandidateAnswerRepository;
import ma.project.echallenge.repository.QuestionOptionRepository;
import ma.project.echallenge.repository.QuestionRepository;
import ma.project.echallenge.repository.TestSessionRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private CandidateAnswerRepository answerRepository;

    @Mock
    private TestSessionRepository sessionRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionOptionRepository optionRepository;

    @InjectMocks
    private AnswerService answerService;

    private TestSession session;
    private Question question;
    private QuestionOption correctOption;
    private QuestionOption incorrectOption;
    private CandidateAnswer candidateAnswer;
    private SubmitAnswerRequest submitRequest;
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

        timeSlot = new TimeSlot();
        timeSlot.setId(1L);
        timeSlot.setStartTime(LocalDateTime.now().minusMinutes(30));
        timeSlot.setEndTime(LocalDateTime.now().plusMinutes(30));

        session = new TestSession(candidate, test, timeSlot, "ABC123");
        session.setId(1L);
        session.setStatus(TestSession.SessionStatus.STARTED);

        correctOption = new QuestionOption();
        correctOption.setId(1L);
        correctOption.setOptionText("Correct Answer");
        correctOption.setIsCorrect(true);

        incorrectOption = new QuestionOption();
        incorrectOption.setId(2L);
        incorrectOption.setOptionText("Incorrect Answer");
        incorrectOption.setIsCorrect(false);

        question = new Question();
        question.setId(1L);
        question.setText("What is Java?");
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setTest(test);
        question.setOptions(Arrays.asList(correctOption, incorrectOption));

        correctOption.setQuestion(question);
        incorrectOption.setQuestion(question);

        candidateAnswer = new CandidateAnswer();
        candidateAnswer.setId(1L);
        candidateAnswer.setTestSession(session);
        candidateAnswer.setQuestion(question);

        submitRequest = new SubmitAnswerRequest();
        submitRequest.setSessionId(1L);
        submitRequest.setQuestionId(1L);
        submitRequest.setSelectedOptionIds(Collections.singletonList(1L));
    }

    // ==================== SUBMIT ANSWER TESTS ====================

    @Test
    void submitAnswer_ShouldReturnSuccess_WhenValidRequest() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(optionRepository.findAllById(anyList())).thenReturn(Collections.singletonList(correctOption));
        when(answerRepository.findByTestSessionIdAndQuestionId(1L, 1L)).thenReturn(Optional.empty());
        when(answerRepository.save(any(CandidateAnswer.class))).thenReturn(candidateAnswer);

        // Act
        ApiResponse<CandidateAnswer> response = answerService.submitAnswer(submitRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Réponse enregistrée", response.getMessage());

        verify(sessionRepository, times(1)).findById(1L);
        verify(questionRepository, times(1)).findById(1L);
        verify(optionRepository, times(1)).findAllById(anyList());
        verify(answerRepository, times(1)).save(any(CandidateAnswer.class));
    }

    @Test
    void submitAnswer_ShouldThrowException_WhenSessionNotFound() {
        // Arrange
        when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> answerService.submitAnswer(submitRequest)
        );

        assertEquals("Session non trouvée", exception.getMessage());

        verify(sessionRepository, times(1)).findById(1L);
        verify(answerRepository, never()).save(any(CandidateAnswer.class));
    }

    @Test
    void submitAnswer_ShouldThrowException_WhenQuestionNotFound() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(questionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> answerService.submitAnswer(submitRequest)
        );

        assertEquals("Question non trouvée", exception.getMessage());

        verify(sessionRepository, times(1)).findById(1L);
        verify(questionRepository, times(1)).findById(1L);
        verify(answerRepository, never()).save(any(CandidateAnswer.class));
    }

    @Test
    void submitAnswer_ShouldThrowException_WhenSessionNotStarted() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.NOT_STARTED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> answerService.submitAnswer(submitRequest)
        );

        assertEquals("La session n'est pas en cours", exception.getMessage());

        verify(sessionRepository, times(1)).findById(1L);
        verify(answerRepository, never()).save(any(CandidateAnswer.class));
    }

    @Test
    void submitAnswer_ShouldThrowException_WhenSessionCompleted() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.COMPLETED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> answerService.submitAnswer(submitRequest)
        );

        assertEquals("La session n'est pas en cours", exception.getMessage());

        verify(sessionRepository, times(1)).findById(1L);
        verify(answerRepository, never()).save(any(CandidateAnswer.class));
    }

    @Test
    void submitAnswer_ShouldThrowException_WhenNoOptionSelected() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(optionRepository.findAllById(anyList())).thenReturn(Collections.emptyList());

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> answerService.submitAnswer(submitRequest)
        );

        assertEquals("Aucune option sélectionnée", exception.getMessage());

        verify(sessionRepository, times(1)).findById(1L);
        verify(questionRepository, times(1)).findById(1L);
        verify(answerRepository, never()).save(any(CandidateAnswer.class));
    }

    @Test
    void submitAnswer_ShouldUpdateExistingAnswer_WhenAlreadyAnswered() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(optionRepository.findAllById(anyList())).thenReturn(Collections.singletonList(correctOption));
        when(answerRepository.findByTestSessionIdAndQuestionId(1L, 1L))
                .thenReturn(Optional.of(candidateAnswer));
        when(answerRepository.save(any(CandidateAnswer.class))).thenReturn(candidateAnswer);

        // Act
        ApiResponse<CandidateAnswer> response = answerService.submitAnswer(submitRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Réponse mise à jour", response.getMessage());

        verify(answerRepository, times(1)).findByTestSessionIdAndQuestionId(1L, 1L);
        verify(answerRepository, times(1)).save(any(CandidateAnswer.class));
    }

    @Test
    void submitAnswer_ShouldHandleMultipleOptions() {
        // Arrange
        submitRequest.setSelectedOptionIds(Arrays.asList(1L, 2L));
        question.setType(QuestionType.MULTIPLE_CHOICE);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(optionRepository.findAllById(anyList()))
                .thenReturn(Arrays.asList(correctOption, incorrectOption));
        when(answerRepository.findByTestSessionIdAndQuestionId(1L, 1L)).thenReturn(Optional.empty());
        when(answerRepository.save(any(CandidateAnswer.class))).thenReturn(candidateAnswer);

        // Act
        ApiResponse<CandidateAnswer> response = answerService.submitAnswer(submitRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());

        verify(optionRepository, times(1)).findAllById(Arrays.asList(1L, 2L));
    }

    // ==================== GET ANSWERS TESTS ====================

    @Test
    void getAnswersBySession_ShouldReturnList_WhenAnswersExist() {
        // Arrange
        CandidateAnswer answer2 = new CandidateAnswer();
        answer2.setId(2L);
        answer2.setTestSession(session);

        Question question2 = new Question();
        question2.setId(2L);
        question2.setText("What is OOP?");
        answer2.setQuestion(question2);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(answerRepository.findByTestSessionId(1L))
                .thenReturn(Arrays.asList(candidateAnswer, answer2));

        // Act
        ApiResponse<List<CandidateAnswer>> response = answerService.getAnswersBySession(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Réponses récupérées", response.getMessage());
        assertEquals(2, response.getData().size());

        verify(answerRepository, times(1)).findByTestSessionId(1L);
    }

    @Test
    void getAnswersBySession_ShouldReturnEmptyList_WhenNoAnswers() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(answerRepository.findByTestSessionId(1L)).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<CandidateAnswer>> response = answerService.getAnswersBySession(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());

        verify(answerRepository, times(1)).findByTestSessionId(1L);
    }

    @Test
    void getAnswersBySession_ShouldThrowException_WhenSessionNotFound() {
        // Arrange
        when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> answerService.getAnswersBySession(999L)
        );

        assertEquals("Session non trouvée", exception.getMessage());

        verify(sessionRepository, times(1)).findById(999L);
    }

    @Test
    void getAnswersBySession_ShouldThrowException_WhenSessionNotStartedOrCompleted() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.NOT_STARTED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> answerService.getAnswersBySession(1L)
        );

        assertEquals("Aucune réponse disponible pour cette session", exception.getMessage());
    }

    @Test
    void getAnswerBySessionAndQuestion_ShouldReturnAnswer_WhenExists() {
        // Arrange
        when(answerRepository.findByTestSessionIdAndQuestionId(1L, 1L))
                .thenReturn(Optional.of(candidateAnswer));

        // Act
        ApiResponse<CandidateAnswer> response = answerService.getAnswerBySessionAndQuestion(1L, 1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Réponse récupérée", response.getMessage());
        assertNotNull(response.getData());

        verify(answerRepository, times(1)).findByTestSessionIdAndQuestionId(1L, 1L);
    }

    @Test
    void getAnswerBySessionAndQuestion_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(answerRepository.findByTestSessionIdAndQuestionId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> answerService.getAnswerBySessionAndQuestion(1L, 1L)
        );

        assertEquals("Réponse non trouvée", exception.getMessage());

        verify(answerRepository, times(1)).findByTestSessionIdAndQuestionId(1L, 1L);
    }

    // ==================== EDGE CASES ====================

    @Test
    void submitAnswer_ShouldCreateNewAnswer_WhenNotExists() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(optionRepository.findAllById(anyList())).thenReturn(Collections.singletonList(correctOption));
        when(answerRepository.findByTestSessionIdAndQuestionId(1L, 1L)).thenReturn(Optional.empty());
        when(answerRepository.save(any(CandidateAnswer.class))).thenAnswer(invocation -> {
            CandidateAnswer saved = invocation.getArgument(0);
            assertNotNull(saved.getTestSession());
            assertNotNull(saved.getQuestion());
            return saved;
        });

        // Act
        answerService.submitAnswer(submitRequest);

        // Assert
        verify(answerRepository, times(1)).save(any(CandidateAnswer.class));
    }

    @Test
    void getAnswersBySession_ShouldAllowWhenSessionCompleted() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.COMPLETED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(answerRepository.findByTestSessionId(1L)).thenReturn(Collections.singletonList(candidateAnswer));

        // Act
        ApiResponse<List<CandidateAnswer>> response = answerService.getAnswersBySession(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
    }
}