package ma.project.echallenge.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final TestSessionRepository sessionRepository;
    private final CandidateRepository candidateRepository;
    private final TestRepository testRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final EmailService emailService;

    @Transactional
    public ApiResponse<String> createSession(Long candidateId, Long testId, Long timeSlotId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé"));

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test non trouvé"));

        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Créneau horaire non trouvé"));

        if (timeSlot.isBooked()) {
            throw new BadRequestException("Ce créneau est déjà réservé");
        }

        String sessionCode = generateSessionCode();

        TestSession session = new TestSession(candidate, test, timeSlot, sessionCode);

        timeSlot.setBooked(true);
        timeSlotRepository.save(timeSlot);

        sessionRepository.save(session);

        emailService.sendSessionInvitation(
                candidate.getEmail(),
                test.getTitle(),
                sessionCode
        );

        return ApiResponse.success("Session créée avec succès. Code: " + sessionCode, sessionCode);
    }

    @Transactional
    public ApiResponse<SessionResponse> startSession(String sessionCode) {
        TestSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée"));

        TimeSlot timeSlot = session.getTimeSlot();
        LocalDateTime now = LocalDateTime.now();

        // Vérifier si le créneau est passé
        if (now.isAfter(timeSlot.getEndTime())) {
            throw new BadRequestException("Le créneau horaire est passé. Veuillez choisir un autre créneau.");
        }

        // Vérifier si le créneau n'est pas encore atteint
        if (now.isBefore(timeSlot.getStartTime())) {
            long minutesRemaining = Duration.between(now, timeSlot.getStartTime()).toMinutes();
            throw new BadRequestException("Le créneau horaire n'est pas encore atteint. Veuillez attendre " + minutesRemaining + " minutes.");
        }

        if (session.getStatus() == TestSession.SessionStatus.COMPLETED) {
            throw new BadRequestException("Cette session est déjà terminée");
        }

        if (session.getStatus() == TestSession.SessionStatus.STARTED) {
            throw new BadRequestException("La session a déjà commencé");
        }

        if (session.getStatus() == TestSession.SessionStatus.CANCELLED) {
            throw new BadRequestException("Cette session a été annulée");
        }

        session.setStatus(TestSession.SessionStatus.STARTED);
        session.setStartTime(LocalDateTime.now());

        TestSession savedSession = sessionRepository.save(session);

        return ApiResponse.success("Session démarrée", mapToResponse(savedSession));
    }

    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> checkSessionStatus(String sessionCode) {
        TestSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée"));

        TimeSlot timeSlot = session.getTimeSlot();
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> status = new HashMap<>();
        status.put("sessionCode", sessionCode);
        status.put("status", session.getStatus().name());
        status.put("candidateName", session.getCandidate().getFirstName() + " " + session.getCandidate().getLastName());
        status.put("testTitle", session.getTest().getTitle());
        status.put("startTime", timeSlot.getStartTime());
        status.put("endTime", timeSlot.getEndTime());

        if (now.isAfter(timeSlot.getEndTime())) {
            status.put("canStart", false);
            status.put("message", "Le créneau horaire est passé. Veuillez choisir un autre créneau.");
        } else if (now.isBefore(timeSlot.getStartTime())) {
            long minutesRemaining = Duration.between(now, timeSlot.getStartTime()).toMinutes();
            status.put("canStart", false);
            status.put("message", "Le créneau horaire n'est pas encore atteint. Veuillez attendre.");
            status.put("waitTimeMinutes", minutesRemaining);
        } else {
            if (session.getStatus() == TestSession.SessionStatus.NOT_STARTED) {
                status.put("canStart", true);
                status.put("message", "Vous pouvez démarrer le test maintenant.");
            } else if (session.getStatus() == TestSession.SessionStatus.STARTED) {
                status.put("canStart", false);
                status.put("message", "Le test est déjà en cours.");
            } else if (session.getStatus() == TestSession.SessionStatus.COMPLETED) {
                status.put("canStart", false);
                status.put("message", "Le test est déjà terminé.");
            }
        }

        return ApiResponse.success("Statut de la session", status);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<SessionResponse>> getSessionsByCandidateId(Long candidateId) {
        List<TestSession> sessions = sessionRepository.findByCandidateId(candidateId);

        List<SessionResponse> responses = sessions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Sessions récupérées", responses);
    }

    @Transactional(readOnly = true)
    public ApiResponse<SessionResponse> getSessionById(Long sessionId) {
        TestSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée"));

        return ApiResponse.success("Session récupérée", mapToResponse(session));
    }

    @Transactional
    public ApiResponse<String> completeSession(String sessionCode) {
        TestSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée"));

        if (session.getStatus() != TestSession.SessionStatus.STARTED) {
            throw new BadRequestException("La session n'est pas en cours");
        }

        session.setStatus(TestSession.SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());

        sessionRepository.save(session);

        return ApiResponse.success("Session terminée avec succès", null);
    }

    @Transactional
    public ApiResponse<String> cancelSession(Long sessionId) {
        TestSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée"));

        if (session.getStatus() == TestSession.SessionStatus.COMPLETED) {
            throw new BadRequestException("Impossible d'annuler une session terminée");
        }

        // Libérer le créneau
        TimeSlot timeSlot = session.getTimeSlot();
        if (timeSlot != null) {
            timeSlot.setBooked(false);
            timeSlotRepository.save(timeSlot);
        }

        session.setStatus(TestSession.SessionStatus.CANCELLED);
        sessionRepository.save(session);

        return ApiResponse.success("Session annulée", null);
    }

    private String generateSessionCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (sessionRepository.findBySessionCode(code).isPresent());
        return code;
    }

    private SessionResponse mapToResponse(TestSession session) {
        LocalDateTime scheduledTime = null;
        if (session.getTimeSlot() != null) {
            scheduledTime = session.getTimeSlot().getStartTime();
        }

        return new SessionResponse(
                session.getId(),
                session.getCandidate().getId(),
                session.getCandidate().getFirstName() + " " + session.getCandidate().getLastName(),
                session.getTest().getId(),
                session.getTest().getTitle(),
                session.getSessionCode(),
                session.getStatus().name(),
                scheduledTime,
                session.getStartTime(),
                session.getEndTime()
        );
    }
}