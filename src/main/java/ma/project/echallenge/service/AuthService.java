package ma.project.echallenge.service;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.request.ConfirmEmailRequest;
import ma.project.echallenge.dto.request.LoginRequest;
import ma.project.echallenge.dto.request.RegisterRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.AuthResponse;
import ma.project.echallenge.entity.Candidate;
import ma.project.echallenge.exception.BadRequestException;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.CandidateRepository;
import ma.project.echallenge.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Transactional
    public ApiResponse<String> register(RegisterRequest request) {
        // Check if email exists
        if (candidateRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email déjà utilisé");
        }

        // Create candidate
        Candidate candidate = new Candidate();
        candidate.setFirstName(request.getFirstName());
        candidate.setLastName(request.getLastName());
        candidate.setEmail(request.getEmail());
        candidate.setPassword(passwordEncoder.encode(request.getPassword()));
        candidate.setPhone(request.getPhone());
        candidate.setEmailConfirmed(false);
        candidate.setConfirmationCode(generateConfirmationCode());

        candidateRepository.save(candidate);

        // Send confirmation email
        emailService.sendConfirmationEmail(candidate.getEmail(), candidate.getConfirmationCode());

        return ApiResponse.success("Inscription réussie. Vérifiez votre email pour confirmer.", null);
    }

    @Transactional
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        Candidate candidate = candidateRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Email ou mot de passe incorrect"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), candidate.getPassword())) {
            throw new BadRequestException("Email ou mot de passe incorrect");
        }

        // Check email confirmation
        if (!candidate.isEmailConfirmed()) {
            throw new BadRequestException("Email non confirmé. Vérifiez votre boîte mail.");
        }

        // Generate token
        String token = jwtTokenProvider.generateToken(
                candidate.getEmail(),
                candidate.getId(),
                "CANDIDATE"
        );

        AuthResponse response = new AuthResponse(
                token,
                candidate.getId(),
                candidate.getFirstName() + " " + candidate.getLastName(),
                candidate.getEmail(),
                null
        );

        return ApiResponse.success("Connexion réussie", response);
    }

    @Transactional
    public ApiResponse<String> confirmEmail(ConfirmEmailRequest request) {
        Candidate candidate = candidateRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé"));

        if (!candidate.getConfirmationCode().equals(request.getConfirmationCode())) {
            throw new BadRequestException("Code de confirmation invalide");
        }

        candidate.setEmailConfirmed(true);
        candidate.setConfirmationCode(null);
        candidateRepository.save(candidate);

        return ApiResponse.success("Email confirmé avec succès", null);
    }

    @Transactional
    public ApiResponse<String> resendConfirmationCode(String email) {
        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé"));

        if (candidate.isEmailConfirmed()) {
            throw new BadRequestException("Email déjà confirmé");
        }

        String newCode = generateConfirmationCode();
        candidate.setConfirmationCode(newCode);
        candidateRepository.save(candidate);

        emailService.sendConfirmationEmail(candidate.getEmail(), newCode);

        return ApiResponse.success("Code de confirmation renvoyé", null);
    }

    private String generateConfirmationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}