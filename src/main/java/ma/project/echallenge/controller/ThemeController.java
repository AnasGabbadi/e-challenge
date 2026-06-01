package ma.project.echallenge.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.request.ThemeRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.entity.Theme;
import ma.project.echallenge.service.ThemeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ThemeController {

    private final ThemeService themeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Theme>> createTheme(@Valid @RequestBody ThemeRequest request) {
        return ResponseEntity.ok(themeService.createTheme(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Theme>>> getAllThemes() {
        return ResponseEntity.ok(themeService.getAllThemes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Theme>> getThemeById(@PathVariable Long id) {
        return ResponseEntity.ok(themeService.getThemeById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Theme>> updateTheme(
            @PathVariable Long id,
            @Valid @RequestBody ThemeRequest request) {
        return ResponseEntity.ok(themeService.updateTheme(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteTheme(@PathVariable Long id) {
        return ResponseEntity.ok(themeService.deleteTheme(id));
    }
}