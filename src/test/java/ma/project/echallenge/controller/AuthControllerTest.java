package ma.project.echallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.project.echallenge.dto.request.ConfirmEmailRequest;
import ma.project.echallenge.dto.request.LoginRequest;
import ma.project.echallenge.dto.request.RegisterRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.AuthResponse;
import ma.project.echallenge.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // Désactive tous les filtres de sécurité
@ActiveProfiles("test")  // Utilise le profil test
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private ConfirmEmailRequest validConfirmRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setFirstName("John");
        validRegisterRequest.setLastName("Doe");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        validConfirmRequest = new ConfirmEmailRequest();
        validConfirmRequest.setEmail("test@example.com");
        validConfirmRequest.setConfirmationCode("123456");

        authResponse = new AuthResponse(
                "mock-jwt-token",
                1L,
                "John Doe",
                "test@example.com",
                null
        );
    }

    // Tests d'inscription
    @Test
    void register_ShouldReturnAuthResponse_WhenRequestValid() throws Exception {
        ApiResponse response = ApiResponse.success("Inscription réussie", null);
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void register_ShouldReturn400_WhenEmailMissing() throws Exception {
        validRegisterRequest.setEmail("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void register_ShouldReturn400_WhenPasswordMissing() throws Exception {
        validRegisterRequest.setPassword("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void register_ShouldReturn400_WhenFirstNameMissing() throws Exception {
        validRegisterRequest.setFirstName("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void register_ShouldReturn400_WhenLastNameMissing() throws Exception {
        validRegisterRequest.setLastName("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void register_ShouldReturn400_WhenEmailInvalid() throws Exception {
        validRegisterRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void register_ShouldCallAuthService_WithCorrectData() throws Exception {
        ApiResponse response = ApiResponse.success("Inscription réussie", null);
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        verify(authService).register(argThat(request ->
                request.getEmail().equals("test@example.com") &&
                        request.getFirstName().equals("John") &&
                        request.getLastName().equals("Doe")
        ));
    }

    // Tests de connexion
    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsValid() throws Exception {
        ApiResponse response = ApiResponse.success("Connexion réussie", authResponse);
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void login_ShouldReturn400_WhenEmailMissing() throws Exception {
        validLoginRequest.setEmail("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void login_ShouldReturn400_WhenPasswordMissing() throws Exception {
        validLoginRequest.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void login_ShouldReturn400_WhenEmailInvalid() throws Exception {
        validLoginRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void login_ShouldCallAuthService_WithCorrectCredentials() throws Exception {
        ApiResponse response = ApiResponse.success("Connexion réussie", authResponse);
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk());

        verify(authService).login(argThat(request ->
                request.getEmail().equals("test@example.com") &&
                        request.getPassword().equals("password123")
        ));
    }

    // Tests de confirmation d'email
    @Test
    void confirmEmail_ShouldReturnSuccess_WhenCodeValid() throws Exception {
        ApiResponse response = ApiResponse.success("Email confirmé avec succès", null);
        when(authService.confirmEmail(any(ConfirmEmailRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validConfirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService, times(1)).confirmEmail(any(ConfirmEmailRequest.class));
    }

    @Test
    void confirmEmail_ShouldReturn400_WhenEmailMissing() throws Exception {
        validConfirmRequest.setEmail("");

        mockMvc.perform(post("/api/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validConfirmRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).confirmEmail(any());
    }

    @Test
    void confirmEmail_ShouldReturn400_WhenConfirmationCodeMissing() throws Exception {
        validConfirmRequest.setConfirmationCode("");

        mockMvc.perform(post("/api/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validConfirmRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).confirmEmail(any());
    }

    @Test
    void confirmEmail_ShouldCallAuthService_WithCorrectData() throws Exception {
        ApiResponse response = ApiResponse.success("Email confirmé avec succès", null);
        when(authService.confirmEmail(any(ConfirmEmailRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validConfirmRequest)))
                .andExpect(status().isOk());

        verify(authService).confirmEmail(argThat(request ->
                request.getEmail().equals("test@example.com") &&
                        request.getConfirmationCode().equals("123456")
        ));
    }

    // Tests de renvoi de code
    @Test
    void resendCode_ShouldReturnSuccess_WhenEmailValid() throws Exception {
        ApiResponse response = ApiResponse.success("Code de confirmation renvoyé", null);
        when(authService.resendConfirmationCode(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/auth/resend-code")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService, times(1)).resendConfirmationCode("test@example.com");
    }

    @Test
    void resendCode_ShouldReturn400_WhenEmailParamMissing() throws Exception {
        mockMvc.perform(post("/api/auth/resend-code"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).resendConfirmationCode(anyString());
    }

    @Test
    void resendCode_ShouldCallAuthService_WithCorrectEmail() throws Exception {
        ApiResponse response = ApiResponse.success("Code de confirmation renvoyé", null);
        when(authService.resendConfirmationCode(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/auth/resend-code")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk());

        verify(authService).resendConfirmationCode("test@example.com");
    }

    // Test supplémentaire
    @Test
    void authController_ShouldHandleMultipleRequests() throws Exception {
        ApiResponse registerResponse = ApiResponse.success("Inscription réussie", null);
        ApiResponse loginResponse = ApiResponse.success("Connexion réussie", authResponse);

        when(authService.register(any(RegisterRequest.class))).thenReturn(registerResponse);
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Inscription
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        // Connexion
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk());

        verify(authService).register(any());
        verify(authService).login(any());
    }
}