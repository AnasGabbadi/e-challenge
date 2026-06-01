package ma.project.echallenge.controller;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.SessionResponse;
import ma.project.echallenge.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SessionController {

    private final SessionService sessionService;

    // Créer session AVEC créneau horaire (OBLIGATOIRE selon cahier des charges)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> createSession(
            @RequestParam Long candidateId,
            @RequestParam Long testId,
            @RequestParam Long timeSlotId) {
        return ResponseEntity.ok(sessionService.createSession(candidateId, testId, timeSlotId));
    }

    // Vérifier le statut de la session et si le candidat peut démarrer
    @GetMapping("/check/{sessionCode}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSessionStatus(
            @PathVariable String sessionCode) {
        return ResponseEntity.ok(sessionService.checkSessionStatus(sessionCode));
    }

    @PostMapping("/start/{sessionCode}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<SessionResponse>> startSession(@PathVariable String sessionCode) {
        return ResponseEntity.ok(sessionService.startSession(sessionCode));
    }

    @PostMapping("/complete/{sessionCode}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<String>> completeSession(@PathVariable String sessionCode) {
        return ResponseEntity.ok(sessionService.completeSession(sessionCode));
    }

    @PutMapping("/cancel/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> cancelSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.cancelSession(sessionId));
    }

    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getSessionsByCandidateId(
            @PathVariable Long candidateId) {
        return ResponseEntity.ok(sessionService.getSessionsByCandidateId(candidateId));
    }

    @GetMapping("/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApiResponse<SessionResponse>> getSessionById(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.getSessionById(sessionId));
    }
}