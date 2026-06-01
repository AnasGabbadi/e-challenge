package ma.project.echallenge.controller;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.ResultResponse;
import ma.project.echallenge.service.ResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResultController {

    private final ResultService resultService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApiResponse<ResultResponse>> getResultById(@PathVariable Long id) {
        return ResponseEntity.ok(resultService.getResultById(id));
    }

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApiResponse<ResultResponse>> getResultBySessionId(@PathVariable Long sessionId) {
        return ResponseEntity.ok(resultService.getResultBySessionId(sessionId));
    }

    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getResultsByCandidateId(@PathVariable Long candidateId) {
        return ResponseEntity.ok(resultService.getResultsByCandidateId(candidateId));
    }

    @GetMapping("/passed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getPassedResults() {
        return ResponseEntity.ok(resultService.getPassedResults());
    }

    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getFailedResults() {
        return ResponseEntity.ok(resultService.getFailedResults());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getAllResults() {
        return ResponseEntity.ok(resultService.getAllResults());
    }
}