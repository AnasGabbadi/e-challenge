package ma.project.echallenge.service;

import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.SessionResponse;
import ma.project.echallenge.entity.Candidate;
import ma.project.echallenge.entity.Test;
import ma.project.echallenge.entity.TestSession;
import ma.project.echallenge.entity.TimeSlot;
import ma.project.echallenge.exception.BadRequestException;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.CandidateRepository;
import ma.project.echallenge.repository.TestRepository;
import ma.project.echallenge.repository.TestSessionRepository;
import ma.project.echallenge.repository.TimeSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private TestSessionRepository sessionRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private TestRepository testRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SessionService sessionService;

    private Candidate candidate;
    private Test test;
    private TimeSlot timeSlot;
    private TestSession session;
    private LocalDateTime futureStart;
    private LocalDateTime futureEnd;

    @BeforeEach
    void setUp() {
        candidate = new Candidate();
        candidate.setId(1L);
        candidate.setFirstName("John");
        candidate.setLastName("Doe");
        candidate.setEmail("john.doe@example.com");
        candidate.setEmailConfirmed(true);

        test = new Test();
        test.setId(1L);
        test.setTitle("Java Test");
        test.setDuration(60);

        futureStart = LocalDateTime.now().plusDays(1);
        futureEnd = futureStart.plusHours(2);

        timeSlot = new TimeSlot();
        timeSlot.setId(1L);
        timeSlot.setTest(test);
        timeSlot.setStartTime(futureStart);
        timeSlot.setEndTime(futureEnd);
        timeSlot.setBooked(false);

        session = new TestSession(candidate, test, timeSlot, "ABC123");
        session.setId(1L);
        session.setStatus(TestSession.SessionStatus.NOT_STARTED);
    }

    // ==================== CREATE SESSION TESTS ====================

    @org.junit.jupiter.api.Test
    void createSession_ShouldReturnSuccess_WhenValidRequest() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));
        when(sessionRepository.save(any(TestSession.class))).thenReturn(session);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);
        doNothing().when(emailService).sendSessionInvitation(anyString(), anyString(), anyString());

        // Act
        ApiResponse<String> response = sessionService.createSession(1L, 1L, 1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("Session créée avec succès"));
        assertNotNull(response.getData());

        verify(candidateRepository, times(1)).findById(1L);
        verify(testRepository, times(1)).findById(1L);
        verify(timeSlotRepository, times(1)).findById(1L);
        verify(sessionRepository, times(1)).save(any(TestSession.class));
        verify(timeSlotRepository, times(1)).save(any(TimeSlot.class));
        verify(emailService, times(1)).sendSessionInvitation(anyString(), anyString(), anyString());
    }

    @org.junit.jupiter.api.Test
    void createSession_ShouldThrowException_WhenCandidateNotFound() {
        // Arrange
        when(candidateRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> sessionService.createSession(999L, 1L, 1L)
        );

        assertEquals("Candidat non trouvé", exception.getMessage());

        verify(candidateRepository, times(1)).findById(999L);
        verify(sessionRepository, never()).save(any(TestSession.class));
    }

    @org.junit.jupiter.api.Test
    void createSession_ShouldThrowException_WhenTestNotFound() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(testRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> sessionService.createSession(1L, 999L, 1L)
        );

        assertEquals("Test non trouvé", exception.getMessage());

        verify(candidateRepository, times(1)).findById(1L);
        verify(testRepository, times(1)).findById(999L);
        verify(sessionRepository, never()).save(any(TestSession.class));
    }

    @org.junit.jupiter.api.Test
    void createSession_ShouldThrowException_WhenTimeSlotNotFound() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> sessionService.createSession(1L, 1L, 999L)
        );

        assertEquals("Créneau horaire non trouvé", exception.getMessage());

        verify(candidateRepository, times(1)).findById(1L);
        verify(testRepository, times(1)).findById(1L);
        verify(timeSlotRepository, times(1)).findById(999L);
        verify(sessionRepository, never()).save(any(TestSession.class));
    }

    @org.junit.jupiter.api.Test
    void createSession_ShouldThrowException_WhenTimeSlotAlreadyBooked() {
        // Arrange
        timeSlot.setBooked(true);
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> sessionService.createSession(1L, 1L, 1L)
        );

        assertEquals("Ce créneau est déjà réservé", exception.getMessage());

        verify(sessionRepository, never()).save(any(TestSession.class));
    }

    @org.junit.jupiter.api.Test
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
        doNothing().when(emailService).sendSessionInvitation(anyString(), anyString(), anyString());

        // Act
        sessionService.createSession(1L, 1L, 1L);

        // Assert
        verify(timeSlotRepository, times(1)).save(any(TimeSlot.class));
    }

    @org.junit.jupiter.api.Test
    void createSession_ShouldSendEmailInvitation() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));
        when(sessionRepository.save(any(TestSession.class))).thenReturn(session);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);
        doNothing().when(emailService).sendSessionInvitation(anyString(), anyString(), anyString());

        // Act
        sessionService.createSession(1L, 1L, 1L);

        // Assert
        verify(emailService, times(1)).sendSessionInvitation(
                eq("john.doe@example.com"),
                eq("Java Test"),
                anyString()
        );
    }

    // ==================== START SESSION TESTS ====================

    @org.junit.jupiter.api.Test
    void startSession_ShouldReturnSuccess_WhenValidSessionCode() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        timeSlot.setStartTime(now.minusMinutes(5));
        timeSlot.setEndTime(now.plusHours(2));

        when(sessionRepository.findBySessionCode("ABC123")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TestSession.class))).thenReturn(session);

        // Act
        ApiResponse<SessionResponse> response = sessionService.startSession("ABC123");

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Session démarrée", response.getMessage());

        verify(sessionRepository, times(1)).findBySessionCode("ABC123");
        verify(sessionRepository, times(1)).save(any(TestSession.class));
    }

    @org.junit.jupiter.api.Test
    void startSession_ShouldThrowException_WhenSessionNotFound() {
        // Arrange
        when(sessionRepository.findBySessionCode(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> sessionService.startSession("INVALID")
        );

        assertEquals("Session non trouvée", exception.getMessage());

        verify(sessionRepository, times(1)).findBySessionCode("INVALID");
    }

    @org.junit.jupiter.api.Test
    void startSession_ShouldThrowException_WhenTimeSlotExpired() {
        // Arrange
        LocalDateTime pastTime = LocalDateTime.now().minusHours(3);
        timeSlot.setStartTime(pastTime.minusHours(2));
        timeSlot.setEndTime(pastTime);

        when(sessionRepository.findBySessionCode("ABC123")).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> sessionService.startSession("ABC123")
        );

        assertTrue(exception.getMessage().contains("créneau horaire est passé"));

        verify(sessionRepository, times(1)).findBySessionCode("ABC123");
    }

    @org.junit.jupiter.api.Test
    void startSession_ShouldThrowException_WhenTimeSlotNotYetStarted() {
        // Arrange
        LocalDateTime futureTime = LocalDateTime.now().plusHours(2);
        timeSlot.setStartTime(futureTime);
        timeSlot.setEndTime(futureTime.plusHours(2));

        when(sessionRepository.findBySessionCode("ABC123")).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> sessionService.startSession("ABC123")
        );

        assertTrue(exception.getMessage().contains("pas encore atteint"));

        verify(sessionRepository, times(1)).findBySessionCode("ABC123");
    }

    @org.junit.jupiter.api.Test
    void startSession_ShouldThrowException_WhenSessionAlreadyCompleted() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        timeSlot.setStartTime(now.minusMinutes(30));
        timeSlot.setEndTime(now.plusHours(1));
        session.setStatus(TestSession.SessionStatus.COMPLETED);

        when(sessionRepository.findBySessionCode("ABC123")).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> sessionService.startSession("ABC123")
        );

        assertEquals("Cette session est déjà terminée", exception.getMessage());
    }

    @org.junit.jupiter.api.Test
    void startSession_ShouldThrowException_WhenSessionAlreadyStarted() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        timeSlot.setStartTime(now.minusMinutes(30));
        timeSlot.setEndTime(now.plusHours(1));
        session.setStatus(TestSession.SessionStatus.STARTED);

        when(sessionRepository.findBySessionCode("ABC123")).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> sessionService.startSession("ABC123")
        );

        assertEquals("La session a déjà commencé", exception.getMessage());
    }

    @org.junit.jupiter.api.Test
    void startSession_ShouldThrowException_WhenSessionCancelled() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        timeSlot.setStartTime(now.minusMinutes(30));
        timeSlot.setEndTime(now.plusHours(1));
        session.setStatus(TestSession.SessionStatus.CANCELLED);

        when(sessionRepository.findBySessionCode("ABC123")).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> sessionService.startSession("ABC123")
        );

        assertEquals("Cette session a été annulée", exception.getMessage());
    }

    // ==================== CHECK SESSION STATUS TESTS ====================

    @org.junit.jupiter.api.Test
    void checkSessionStatus_ShouldReturnStatus_WhenSessionExists() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        timeSlot.setStartTime(now.minusMinutes(5));
        timeSlot.setEndTime(now.plusHours(2));

        when(sessionRepository.findBySessionCode("ABC123")).thenReturn(Optional.of(session));

        // Act
        ApiResponse<Map<String, Object>> response = sessionService.checkSessionStatus("ABC123");

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Statut de la session", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("ABC123", response.getData().get("sessionCode"));

        verify(sessionRepository, times(1)).findBySessionCode("ABC123");
    }

    @org.junit.jupiter.api.Test
    void checkSessionStatus_ShouldThrowException_WhenSessionNotFound() {
        // Arrange
        when(sessionRepository.findBySessionCode(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> sessionService.checkSessionStatus("INVALID")
        );

        assertEquals("Session non trouvée", exception.getMessage());
    }

    // ==================== GET SESSIONS BY CANDIDATE TESTS ====================

    @org.junit.jupiter.api.Test
    void getSessionsByCandidateId_ShouldReturnList_WhenSessionsExist() {
        // Arrange
        TestSession session2 = new TestSession(candidate, test, timeSlot, "XYZ789");
        session2.setId(2L);

        when(sessionRepository.findByCandidateId(1L))
                .thenReturn(Arrays.asList(session, session2));

        // Act
        ApiResponse<List<SessionResponse>> response = sessionService.getSessionsByCandidateId(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Sessions récupérées", response.getMessage());
        assertEquals(2, response.getData().size());

        verify(sessionRepository, times(1)).findByCandidateId(1L);
    }

    @org.junit.jupiter.api.Test
    void getSessionsByCandidateId_ShouldReturnEmptyList_WhenNoSessions() {
        // Arrange
        when(sessionRepository.findByCandidateId(1L)).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<SessionResponse>> response = sessionService.getSessionsByCandidateId(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());

        verify(sessionRepository, times(1)).findByCandidateId(1L);
    }

    // ==================== GET SESSION BY ID TESTS ====================

    @org.junit.jupiter.api.Test
    void getSessionById_ShouldReturnSession_WhenExists() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // Act
        ApiResponse<SessionResponse> response = sessionService.getSessionById(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Session récupérée", response.getMessage());
        assertNotNull(response.getData());

        verify(sessionRepository, times(1)).findById(1L);
    }

    @org.junit.jupiter.api.Test
    void getSessionById_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> sessionService.getSessionById(999L)
        );

        assertEquals("Session non trouvée", exception.getMessage());

        verify(sessionRepository, times(1)).findById(999L);
    }

    // ==================== COMPLETE SESSION TESTS ====================

    @org.junit.jupiter.api.Test
    void completeSession_ShouldReturnSuccess_WhenValidSessionCode() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.STARTED);
        when(sessionRepository.findBySessionCode("ABC123")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TestSession.class))).thenReturn(session);

        // Act
        ApiResponse<String> response = sessionService.completeSession("ABC123");

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Session terminée avec succès", response.getMessage());

        verify(sessionRepository, times(1)).findBySessionCode("ABC123");
        verify(sessionRepository, times(1)).save(any(TestSession.class));
    }

    @org.junit.jupiter.api.Test
    void completeSession_ShouldThrowException_WhenSessionNotStarted() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.NOT_STARTED);
        when(sessionRepository.findBySessionCode("ABC123")).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> sessionService.completeSession("ABC123")
        );

        assertEquals("La session n'est pas en cours", exception.getMessage());
    }

    // ==================== CANCEL SESSION TESTS ====================

    @org.junit.jupiter.api.Test
    void cancelSession_ShouldReturnSuccess_WhenValidSessionId() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TestSession.class))).thenReturn(session);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);

        // Act
        ApiResponse<String> response = sessionService.cancelSession(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Session annulée", response.getMessage());

        verify(sessionRepository, times(1)).findById(1L);
        verify(timeSlotRepository, times(1)).save(any(TimeSlot.class));
        verify(sessionRepository, times(1)).save(any(TestSession.class));
    }

    @org.junit.jupiter.api.Test
    void cancelSession_ShouldThrowException_WhenSessionCompleted() {
        // Arrange
        session.setStatus(TestSession.SessionStatus.COMPLETED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> sessionService.cancelSession(1L)
        );

        assertEquals("Impossible d'annuler une session terminée", exception.getMessage());
    }

    @org.junit.jupiter.api.Test
    void cancelSession_ShouldFreeTimeSlot() {
        // Arrange
        timeSlot.setBooked(true);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TestSession.class))).thenReturn(session);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenAnswer(invocation -> {
            TimeSlot freed = invocation.getArgument(0);
            assertFalse(freed.isBooked());
            return freed;
        });

        // Act
        sessionService.cancelSession(1L);

        // Assert
        verify(timeSlotRepository, times(1)).save(any(TimeSlot.class));
    }
}