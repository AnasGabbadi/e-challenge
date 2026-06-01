package ma.project.echallenge.controller;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.SessionResponse;
import ma.project.echallenge.service.TestSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestSessionController {

    private final TestSessionService testSessionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> createSession(
            @RequestParam Long candidateId,
            @RequestParam Long testId,
            @RequestParam Long timeSlotId) {
        return ResponseEntity.ok(testSessionService.createSession(candidateId, testId, timeSlotId));
    }

    @PostMapping("/start/{sessionCode}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<SessionResponse>> startSession(@PathVariable String sessionCode) {
        return ResponseEntity.ok(testSessionService.startSession(sessionCode));
    }

    @PostMapping("/complete/{sessionId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<String>> completeSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(testSessionService.completeSession(sessionId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getAllSessions() {
        return ResponseEntity.ok(testSessionService.getAllSessions());
    }

    @GetMapping("/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApiResponse<SessionResponse>> getSessionById(@PathVariable Long sessionId) {
        return ResponseEntity.ok(testSessionService.getSessionById(sessionId));
    }
}