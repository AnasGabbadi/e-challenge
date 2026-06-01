package ma.project.echallenge.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.request.ConfirmEmailRequest;
import ma.project.echallenge.dto.request.LoginRequest;
import ma.project.echallenge.dto.request.RegisterRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.AuthResponse;
import ma.project.echallenge.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<ApiResponse> confirmEmail(@Valid @RequestBody ConfirmEmailRequest request) {
        return ResponseEntity.ok(authService.confirmEmail(request));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<ApiResponse> resendCode(
            @RequestParam @NotBlank(message = "L'email est requis") String email
    ) {
        return ResponseEntity.ok(authService.resendConfirmationCode(email));
    }
}