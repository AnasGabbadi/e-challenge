package ma.project.echallenge.service;

import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.SessionResponse;
import ma.project.echallenge.entity.*;
import ma.project.echallenge.exception.BadRequestException;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.CandidateRepository;
import ma.project.echallenge.repository.TestRepository;
import ma.project.echallenge.repository.TestSessionRepository;
import ma.project.echallenge.repository.TimeSlotRepository;
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
class TestSessionServiceTest {

    @Mock
    private TestSessionRepository sessionRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private TestRepository testRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private TestSessionService testSessionService;

    private Candidate candidate;
    private ma.project.echallenge.entity.Test test;
    private TimeSlot timeSlot;
    private TestSession session;

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
        timeSlot.setStartTime(LocalDateTime.now().plusDays(1));
        timeSlot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        timeSlot.setBooked(false);
        timeSlot.setTest(test);

        session = new TestSession(candidate, test, timeSlot, "ABC12345");
        session.setId(1L);
        session.setStatus(TestSession.SessionStatus.NOT_STARTED);
    }

    // ==================== CREATE SESSION TESTS ====================

    @Test
    void createSession_ShouldReturnSuccess_WhenValidRequest() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));
        when(sessionRepository.save(any(TestSession.class))).thenReturn(session);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);

        // Act
        ApiResponse<String> response = testSessionService.createSession(1L, 1L, 1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Session créée avec succès", response.getMessage());
        assertNotNull(response.getData());

        verify(candidateRepository, times(1)).findById(1L);
        verify(testRepository, times(1)).findById(1L);
        verify(timeSlotRepository, times(1)).findById(1L);
        verify(sessionRepository, times(1)).save(any(TestSession.class));
        verify(timeSlotRepository, times(1)).save(any(TimeSlot.class));
    }

    @Test
    void createSession_ShouldThrowException_WhenCandidateNotFound() {
        // Arrange
        when(candidateRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> testSessionService.createSession(999L, 1L, 1L)
        );

        assertEquals("Candidat non trouvé", exception.getMessage());

        verify(candidateRepository, times(1)).findById(999L);
        verify(sessionRepository, never()).save(any(TestSession.class));
    }

    @Test
    void createSession_ShouldThrowException_WhenTestNotFound() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(testRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> testSessionService.createSession(1L, 999L, 1L)
        );

        assertEquals("Test non trouvé", exception.getMessage());

        verify(candidateRepository, times(1)).findById(1L);
        verify(testRepository, times(1)).findById(999L);
        verify(sessionRepository, never()).save(any(TestSession.class));
    }

    @Test
    void createSession_ShouldThrowException_WhenTimeSlotNotFound() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> testSessionService.createSession(1L, 1L, 999L)
        );

        assertEquals("Créneau horaire non trouvé", exception.getMessage());

        verify(sessionRepository, never()).save(any(TestSession.class));
    }

    @Test
    void createSession_ShouldThrowException_WhenTimeSlotAlreadyBooked() {
        // Arrange
        timeSlot.setBooked(true);
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> testSessionService.createSession(1L, 1L, 1L)
        );

        assertEquals("Ce créneau est déjà réservé", exception.getMessage());

        verify(sessionRepository, never()).save(any(TestSession.class));
    }

    @Test
    void createSession_ShouldMarkTimeSlotAsBooked() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));
        when(sessionRepository.save(any(TestSession.class))).thenReturn(session);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenAnswer(invocation -> {
            TimeSlot saved = invocation.getArgument(0);
            assertTrue(saved.isBooked());
            return saved;
        });

        // Act
        testSessionService.createSession(1L, 1L, 1L);

        // Assert
        verify(timeSlotRepository, times(1)).save(any(TimeSlot.class));
    }

    @Test
    void createSession_ShouldSetSessionStatusToNotStarted() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));
        when(sessionRepository.save(any(TestSession.class))).thenAnswer(invocation -> {
            TestSession saved = invocation.getArgument(0);
            assertEquals(TestSession.SessionStatus.NOT_STARTED, saved.getStatus());
            return saved;
        });
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);

        // Act
        testSessionService.createSession(1L, 1L, 1L);

        // Assert
        verify(sessionRepository, times(1)).save(any(TestSession.class));
    }

    // ==================== START SESSION TESTS ====================

    @Test
    void startSession_ShouldReturnSuccess_WhenValidSessionCode() {
        // Arrange
        when(sessionRepository.findBySessionCode("ABC12345")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TestSession.class))).thenReturn(session);

        // Act
        ApiResponse<SessionResponse> response = testSessionService.startSession("ABC12345");

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Session démarrée", response.getMessage());
        assertNotNull(response.getData());

        verify(sessionRepository, times(1)).findBySessionCode("ABC12345");
        verify(sessionRepository, times(1)).save(any(TestSession.class));
    }

    @Test
    void startSession_ShouldThrowException_WhenSessionNotFound() {
        // Arrange
        when(sessionRepository.findBySessionCode(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> testSessionService.startSession("INVALID")
        );

        assertEquals("Session non trouvée", exception.getMessage());

        verify(sessionRepository, times(1)).findBySessionCode("INVALID");
    }

    @Test
    void startSession_ShouldThrowException_WhenSessionAlreadyStarted() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.STARTED);
        when(sessionRepository.findBySessionCode("ABC12345")).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> testSessionService.startSession("ABC12345")
        );

        assertEquals("Cette session a déjà été démarrée ou est terminée", exception.getMessage());
    }

    @Test
    void startSession_ShouldThrowException_WhenSessionCompleted() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.COMPLETED);
        when(sessionRepository.findBySessionCode("ABC12345")).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> testSessionService.startSession("ABC12345")
        );

        assertEquals("Cette session a déjà été démarrée ou est terminée", exception.getMessage());
    }

    @Test
    void startSession_ShouldSetStartTime() {
        // Arrange
        when(sessionRepository.findBySessionCode("ABC12345")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TestSession.class))).thenAnswer(invocation -> {
            TestSession saved = invocation.getArgument(0);
            assertNotNull(saved.getStartTime());
            return saved;
        });

        // Act
        testSessionService.startSession("ABC12345");

        // Assert
        verify(sessionRepository, times(1)).save(any(TestSession.class));
    }

    @Test
    void startSession_ShouldSetStatusToStarted() {
        // Arrange
        when(sessionRepository.findBySessionCode("ABC12345")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TestSession.class))).thenAnswer(invocation -> {
            TestSession saved = invocation.getArgument(0);
            assertEquals(TestSession.SessionStatus.STARTED, saved.getStatus());
            return saved;
        });

        // Act
        testSessionService.startSession("ABC12345");

        // Assert
        verify(sessionRepository, times(1)).save(any(TestSession.class));
    }

    // ==================== COMPLETE SESSION TESTS ====================

    @Test
    void completeSession_ShouldReturnSuccess_WhenValidSessionId() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.STARTED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TestSession.class))).thenReturn(session);

        // Act
        ApiResponse<String> response = testSessionService.completeSession(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Session terminée", response.getMessage());

        verify(sessionRepository, times(1)).findById(1L);
        verify(sessionRepository, times(1)).save(any(TestSession.class));
    }

    @Test
    void completeSession_ShouldThrowException_WhenSessionNotFound() {
        // Arrange
        when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> testSessionService.completeSession(999L)
        );

        assertEquals("Session non trouvée", exception.getMessage());

        verify(sessionRepository, times(1)).findById(999L);
    }

    @Test
    void completeSession_ShouldThrowException_WhenSessionNotStarted() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.NOT_STARTED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> testSessionService.completeSession(1L)
        );

        assertEquals("Cette session n'est pas en cours", exception.getMessage());
    }

    @Test
    void completeSession_ShouldThrowException_WhenSessionAlreadyCompleted() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.COMPLETED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> testSessionService.completeSession(1L)
        );

        assertEquals("Cette session n'est pas en cours", exception.getMessage());
    }

    @Test
    void completeSession_ShouldSetEndTime() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.STARTED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TestSession.class))).thenAnswer(invocation -> {
            TestSession saved = invocation.getArgument(0);
            assertNotNull(saved.getEndTime());
            return saved;
        });

        // Act
        testSessionService.completeSession(1L);

        // Assert
        verify(sessionRepository, times(1)).save(any(TestSession.class));
    }

    @Test
    void completeSession_ShouldSetStatusToCompleted() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.STARTED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TestSession.class))).thenAnswer(invocation -> {
            TestSession saved = invocation.getArgument(0);
            assertEquals(TestSession.SessionStatus.COMPLETED, saved.getStatus());
            return saved;
        });

        // Act
        testSessionService.completeSession(1L);

        // Assert
        verify(sessionRepository, times(1)).save(any(TestSession.class));
    }

    // ==================== GET ALL SESSIONS TESTS ====================

    @Test
    void getAllSessions_ShouldReturnList_WhenSessionsExist() {
        // Arrange
        TestSession session2 = new TestSession(candidate, test, timeSlot, "XYZ78910");
        session2.setId(2L);

        when(sessionRepository.findAll()).thenReturn(Arrays.asList(session, session2));

        // Act
        ApiResponse<List<SessionResponse>> response = testSessionService.getAllSessions();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Sessions récupérées", response.getMessage());
        assertEquals(2, response.getData().size());

        verify(sessionRepository, times(1)).findAll();
    }

    @Test
    void getAllSessions_ShouldReturnEmptyList_WhenNoSessions() {
        // Arrange
        when(sessionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<SessionResponse>> response = testSessionService.getAllSessions();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());

        verify(sessionRepository, times(1)).findAll();
    }

    // ==================== GET SESSION BY ID TESTS ====================

    @Test
    void getSessionById_ShouldReturnSession_WhenExists() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // Act
        ApiResponse<SessionResponse> response = testSessionService.getSessionById(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Session récupérée", response.getMessage());
        assertNotNull(response.getData());

        verify(sessionRepository, times(1)).findById(1L);
    }

    @Test
    void getSessionById_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> testSessionService.getSessionById(999L)
        );

        assertEquals("Session non trouvée", exception.getMessage());

        verify(sessionRepository, times(1)).findById(999L);
    }

    // ==================== EDGE CASES ====================

    @Test
    void createSession_ShouldGenerateUniqueSessionCode() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));
        when(sessionRepository.save(any(TestSession.class))).thenAnswer(invocation -> {
            TestSession saved = invocation.getArgument(0);
            assertNotNull(saved.getSessionCode());
            assertTrue(saved.getSessionCode().length() > 0);
            return saved;
        });
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);

        // Act
        testSessionService.createSession(1L, 1L, 1L);

        // Assert
        verify(sessionRepository, times(1)).save(any(TestSession.class));
    }

    @Test
    void getAllSessions_ShouldHandleMultipleSessions() {
        // Arrange
        TestSession session2 = new TestSession(candidate, test, timeSlot, "CODE2");
        TestSession session3 = new TestSession(candidate, test, timeSlot, "CODE3");

        when(sessionRepository.findAll())
                .thenReturn(Arrays.asList(session, session2, session3));

        // Act
        ApiResponse<List<SessionResponse>> response = testSessionService.getAllSessions();

        // Assert
        assertEquals(3, response.getData().size());
    }
}