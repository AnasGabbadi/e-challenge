package ma.project.echallenge.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.request.QuestionRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.QuestionResponse;
import ma.project.echallenge.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(@Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.createQuestion(request));
    }

    @GetMapping("/test/{testId}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsByTest(
            @PathVariable Long testId,
            @RequestParam(defaultValue = "false") boolean includeCorrect) {
        return ResponseEntity.ok(questionService.getQuestionsByTestId(testId, includeCorrect));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.deleteQuestion(id));
    }
}