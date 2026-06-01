package ma.project.echallenge.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private Candidate candidate;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        candidate = new Candidate();
        candidate.setId(1L);
        candidate.setFirstName("John");
        candidate.setLastName("Doe");
        candidate.setEmail("john.doe@example.com");
        candidate.setPassword("encodedPassword123");
        candidate.setPhone("0612345678");
        candidate.setEmailConfirmed(true);
        candidate.setConfirmationCode(null);

        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("0612345678");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");
    }

    // ==================== REGISTER TESTS ====================

    @Test
    void register_ShouldReturnSuccess_WhenValidRequest() {
        // Arrange
        when(candidateRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString());

        // Act
        ApiResponse<String> response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Inscription réussie. Vérifiez votre email pour confirmer.", response.getMessage());

        verify(candidateRepository, times(1)).existsByEmail("john.doe@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(candidateRepository, times(1)).save(any(Candidate.class));
        verify(emailService, times(1)).sendConfirmationEmail(anyString(), anyString());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(candidateRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(registerRequest)
        );

        assertEquals("Email déjà utilisé", exception.getMessage());

        verify(candidateRepository, times(1)).existsByEmail("john.doe@example.com");
        verify(candidateRepository, never()).save(any(Candidate.class));
        verify(emailService, never()).sendConfirmationEmail(anyString(), anyString());
    }

    @Test
    void register_ShouldEncodePassword() {
        // Arrange
        when(candidateRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString());

        // Act
        authService.register(registerRequest);

        // Assert
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void register_ShouldGenerateConfirmationCode() {
        // Arrange
        when(candidateRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(invocation -> {
            Candidate saved = invocation.getArgument(0);
            assertNotNull(saved.getConfirmationCode());
            assertTrue(saved.getConfirmationCode().matches("\\d{6}"));
            return saved;
        });
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString());

        // Act
        authService.register(registerRequest);

        // Assert
        verify(candidateRepository, times(1)).save(any(Candidate.class));
    }

    @Test
    void register_ShouldSetEmailConfirmedToFalse() {
        // Arrange
        when(candidateRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(invocation -> {
            Candidate saved = invocation.getArgument(0);
            assertFalse(saved.isEmailConfirmed());
            return saved;
        });
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString());

        // Act
        authService.register(registerRequest);

        // Assert
        verify(candidateRepository, times(1)).save(any(Candidate.class));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void login_ShouldReturnSuccess_WhenValidCredentials() {
        // Arrange
        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(candidate));
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyLong(), anyString())).thenReturn("jwt-token-123");

        // Act
        ApiResponse<AuthResponse> response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Connexion réussie", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("jwt-token-123", response.getData().getToken());
        assertEquals(1L, response.getData().getCandidateId());
        assertEquals("John Doe", response.getData().getCandidateName());
        assertEquals("john.doe@example.com", response.getData().getEmail());

        verify(candidateRepository, times(1)).findByEmail("john.doe@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword123");
        verify(jwtTokenProvider, times(1)).generateToken("john.doe@example.com", 1L, "CANDIDATE");
    }

    @Test
    void login_ShouldThrowException_WhenEmailNotFound() {
        // Arrange
        when(candidateRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Email ou mot de passe incorrect", exception.getMessage());

        verify(candidateRepository, times(1)).findByEmail("john.doe@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIncorrect() {
        // Arrange
        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(candidate));
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(false);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Email ou mot de passe incorrect", exception.getMessage());

        verify(candidateRepository, times(1)).findByEmail("john.doe@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword123");
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void login_ShouldThrowException_WhenEmailNotConfirmed() {
        // Arrange
        candidate.setEmailConfirmed(false);
        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(candidate));
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Email non confirmé. Vérifiez votre boîte mail.", exception.getMessage());

        verify(candidateRepository, times(1)).findByEmail("john.doe@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword123");
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void login_ShouldGenerateJwtToken() {
        // Arrange
        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(candidate));
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(true);
        when(jwtTokenProvider.generateToken("john.doe@example.com", 1L, "CANDIDATE"))
                .thenReturn("jwt-token-123");

        // Act
        ApiResponse<AuthResponse> response = authService.login(loginRequest);

        // Assert
        assertEquals("jwt-token-123", response.getData().getToken());
        verify(jwtTokenProvider, times(1)).generateToken("john.doe@example.com", 1L, "CANDIDATE");
    }

    // ==================== CONFIRM EMAIL TESTS ====================

    @Test
    void confirmEmail_ShouldReturnSuccess_WhenValidCode() {
        // Arrange
        candidate.setEmailConfirmed(false);
        candidate.setConfirmationCode("123456");

        ConfirmEmailRequest request = new ConfirmEmailRequest();
        request.setEmail("john.doe@example.com");
        request.setConfirmationCode("123456");

        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);

        // Act
        ApiResponse<String> response = authService.confirmEmail(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Email confirmé avec succès", response.getMessage());

        verify(candidateRepository, times(1)).findByEmail("john.doe@example.com");
        verify(candidateRepository, times(1)).save(any(Candidate.class));
    }

    @Test
    void confirmEmail_ShouldSetEmailConfirmedToTrue() {
        // Arrange
        candidate.setEmailConfirmed(false);
        candidate.setConfirmationCode("123456");

        ConfirmEmailRequest request = new ConfirmEmailRequest();
        request.setEmail("john.doe@example.com");
        request.setConfirmationCode("123456");

        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(invocation -> {
            Candidate saved = invocation.getArgument(0);
            assertTrue(saved.isEmailConfirmed());
            assertNull(saved.getConfirmationCode());
            return saved;
        });

        // Act
        authService.confirmEmail(request);

        // Assert
        verify(candidateRepository, times(1)).save(any(Candidate.class));
    }

    @Test
    void confirmEmail_ShouldThrowException_WhenCandidateNotFound() {
        // Arrange
        ConfirmEmailRequest request = new ConfirmEmailRequest();
        request.setEmail("notfound@example.com");
        request.setConfirmationCode("123456");

        when(candidateRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.confirmEmail(request)
        );

        assertEquals("Candidat non trouvé", exception.getMessage());

        verify(candidateRepository, times(1)).findByEmail("notfound@example.com");
        verify(candidateRepository, never()).save(any(Candidate.class));
    }

    @Test
    void confirmEmail_ShouldThrowException_WhenCodeInvalid() {
        // Arrange
        candidate.setEmailConfirmed(false);
        candidate.setConfirmationCode("123456");

        ConfirmEmailRequest request = new ConfirmEmailRequest();
        request.setEmail("john.doe@example.com");
        request.setConfirmationCode("999999");

        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(candidate));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.confirmEmail(request)
        );

        assertEquals("Code de confirmation invalide", exception.getMessage());

        verify(candidateRepository, times(1)).findByEmail("john.doe@example.com");
        verify(candidateRepository, never()).save(any(Candidate.class));
    }

    // ==================== RESEND CONFIRMATION CODE TESTS ====================

    @Test
    void resendConfirmationCode_ShouldReturnSuccess_WhenValidEmail() {
        // Arrange
        candidate.setEmailConfirmed(false);
        candidate.setConfirmationCode("123456");

        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString());

        // Act
        ApiResponse<String> response = authService.resendConfirmationCode("john.doe@example.com");

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Code de confirmation renvoyé", response.getMessage());

        verify(candidateRepository, times(1)).findByEmail("john.doe@example.com");
        verify(candidateRepository, times(1)).save(any(Candidate.class));
        verify(emailService, times(1)).sendConfirmationEmail(anyString(), anyString());
    }

    @Test
    void resendConfirmationCode_ShouldThrowException_WhenCandidateNotFound() {
        // Arrange
        when(candidateRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.resendConfirmationCode("notfound@example.com")
        );

        assertEquals("Candidat non trouvé", exception.getMessage());

        verify(candidateRepository, times(1)).findByEmail("notfound@example.com");
        verify(emailService, never()).sendConfirmationEmail(anyString(), anyString());
    }

    @Test
    void resendConfirmationCode_ShouldThrowException_WhenEmailAlreadyConfirmed() {
        // Arrange
        candidate.setEmailConfirmed(true);
        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(candidate));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.resendConfirmationCode("john.doe@example.com")
        );

        assertEquals("Email déjà confirmé", exception.getMessage());

        verify(candidateRepository, times(1)).findByEmail("john.doe@example.com");
        verify(candidateRepository, never()).save(any(Candidate.class));
        verify(emailService, never()).sendConfirmationEmail(anyString(), anyString());
    }

    @Test
    void resendConfirmationCode_ShouldGenerateNewCode() {
        // Arrange
        candidate.setEmailConfirmed(false);
        candidate.setConfirmationCode("123456");

        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(invocation -> {
            Candidate saved = invocation.getArgument(0);
            assertNotNull(saved.getConfirmationCode());
            assertNotEquals("123456", saved.getConfirmationCode());
            assertTrue(saved.getConfirmationCode().matches("\\d{6}"));
            return saved;
        });
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString());

        // Act
        authService.resendConfirmationCode("john.doe@example.com");

        // Assert
        verify(candidateRepository, times(1)).save(any(Candidate.class));
    }

    @Test
    void resendConfirmationCode_ShouldSendEmailWithNewCode() {
        // Arrange
        candidate.setEmailConfirmed(false);
        candidate.setConfirmationCode("123456");

        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString());

        // Act
        authService.resendConfirmationCode("john.doe@example.com");

        // Assert
        verify(emailService, times(1)).sendConfirmationEmail(eq("john.doe@example.com"), anyString());
    }
}