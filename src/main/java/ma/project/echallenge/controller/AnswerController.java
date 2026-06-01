package ma.project.echallenge.controller;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.request.SubmitAnswerRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.entity.CandidateAnswer;
import ma.project.echallenge.service.AnswerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<CandidateAnswer>> submitAnswer(
            @Valid @RequestBody SubmitAnswerRequest request) {
        return ResponseEntity.ok(answerService.submitAnswer(request));
    }

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApiResponse<List<CandidateAnswer>>> getAnswersBySession(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(answerService.getAnswersBySession(sessionId));
    }

    @GetMapping("/session/{sessionId}/question/{questionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApiResponse<CandidateAnswer>> getAnswerBySessionAndQuestion(
            @PathVariable Long sessionId,
            @PathVariable Long questionId) {
        return ResponseEntity.ok(answerService.getAnswerBySessionAndQuestion(sessionId, questionId));
    }
}