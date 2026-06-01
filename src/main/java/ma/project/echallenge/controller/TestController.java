package ma.project.echallenge.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.request.TestRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.TestResponse;
import ma.project.echallenge.service.TestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestController {

    private final TestService testService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TestResponse>> createTest(@Valid @RequestBody TestRequest request) {
        return ResponseEntity.ok(testService.createTest(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TestResponse>>> getAllTests() {
        return ResponseEntity.ok(testService.getAllTests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TestResponse>> getTestById(@PathVariable Long id) {
        return ResponseEntity.ok(testService.getTestById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TestResponse>> updateTest(
            @PathVariable Long id,
            @Valid @RequestBody TestRequest request) {
        return ResponseEntity.ok(testService.updateTest(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteTest(@PathVariable Long id) {
        return ResponseEntity.ok(testService.deleteTest(id));
    }
}