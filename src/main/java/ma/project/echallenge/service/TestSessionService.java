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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestSessionService {

    private final TestSessionRepository sessionRepository;
    private final CandidateRepository candidateRepository;
    private final TestRepository testRepository;
    private final TimeSlotRepository timeSlotRepository;

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
        session.setStatus(TestSession.SessionStatus.NOT_STARTED);

        timeSlot.setBooked(true);
        timeSlotRepository.save(timeSlot);

        sessionRepository.save(session);

        return ApiResponse.success("Session créée avec succès", sessionCode);
    }

    @Transactional
    public ApiResponse<SessionResponse> startSession(String sessionCode) {
        TestSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée"));

        if (session.getStatus() != TestSession.SessionStatus.NOT_STARTED) {
            throw new BadRequestException("Cette session a déjà été démarrée ou est terminée");
        }

        session.setStatus(TestSession.SessionStatus.STARTED);
        session.setStartTime(LocalDateTime.now());

        TestSession updated = sessionRepository.save(session);

        return ApiResponse.success("Session démarrée", mapToResponse(updated));
    }

    @Transactional
    public ApiResponse<String> completeSession(Long sessionId) {
        TestSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée"));

        if (session.getStatus() != TestSession.SessionStatus.STARTED) {
            throw new BadRequestException("Cette session n'est pas en cours");
        }

        session.setStatus(TestSession.SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());

        sessionRepository.save(session);

        return ApiResponse.success("Session terminée", null);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<SessionResponse>> getAllSessions() {
        List<TestSession> sessions = sessionRepository.findAll();
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

    private String generateSessionCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private SessionResponse mapToResponse(TestSession session) {
        return new SessionResponse(
                session.getId(),
                session.getCandidate().getId(),
                session.getCandidate().getFirstName() + " " + session.getCandidate().getLastName(),
                session.getTest().getId(),
                session.getTest().getTitle(),
                session.getSessionCode(),
                session.getStatus().name(),
                session.getRegistrationDate(),
                session.getStartTime(),
                session.getEndTime()
        );
    }
}