package ma.project.echallenge.exception;

import ma.project.echallenge.dto.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        // Configuration initiale si nécessaire
    }

    // ==================== RESOURCE NOT FOUND EXCEPTION ====================

    @Test
    void handleResourceNotFoundException_ShouldReturn404() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Ressource non trouvée");

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleResourceNotFoundException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Ressource non trouvée", response.getBody().getMessage());
    }

    @Test
    void handleResourceNotFoundException_ShouldContainCorrectMessage() {
        // Arrange
        String message = "Candidat non trouvé";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleResourceNotFoundException(exception);

        // Assert
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void handleResourceNotFoundException_ShouldHandleEmptyMessage() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("");

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleResourceNotFoundException(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("", response.getBody().getMessage());
    }

    // ==================== BAD REQUEST EXCEPTION ====================

    @Test
    void handleBadRequestException_ShouldReturn400() {
        // Arrange
        BadRequestException exception = new BadRequestException("Requête invalide");

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleBadRequestException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Requête invalide", response.getBody().getMessage());
    }

    @Test
    void handleBadRequestException_ShouldContainCorrectMessage() {
        // Arrange
        String message = "Email déjà utilisé";
        BadRequestException exception = new BadRequestException(message);

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleBadRequestException(exception);

        // Assert
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void handleBadRequestException_ShouldHandleSpecialCharacters() {
        // Arrange
        String message = "Créneau déjà réservé!";
        BadRequestException exception = new BadRequestException(message);

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleBadRequestException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(message, response.getBody().getMessage());
    }

    // ==================== BAD CREDENTIALS EXCEPTION ====================

    @Test
    void handleBadCredentialsException_ShouldReturn401() {
        // Arrange
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleBadCredentialsException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Email ou mot de passe incorrect", response.getBody().getMessage());
    }

    @Test
    void handleBadCredentialsException_ShouldReturnCustomMessage() {
        // Arrange
        BadCredentialsException exception = new BadCredentialsException("Bad password");

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleBadCredentialsException(exception);

        // Assert
        assertEquals("Email ou mot de passe incorrect", response.getBody().getMessage());
    }

    // ==================== ACCESS DENIED EXCEPTION ====================

    @Test
    void handleAccessDeniedException_ShouldReturn403() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleAccessDeniedException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Accès refusé", response.getBody().getMessage());
    }

    @Test
    void handleAccessDeniedException_ShouldReturnCustomMessage() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Forbidden");

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleAccessDeniedException(exception);

        // Assert
        assertEquals("Accès refusé", response.getBody().getMessage());
    }

    // ==================== METHOD ARGUMENT NOT VALID EXCEPTION ====================

    @Test
    void handleValidationExceptions_ShouldReturn400WithFieldErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("registerRequest", "email", "Email invalide");
        FieldError fieldError2 = new FieldError("registerRequest", "password", "Mot de passe trop court");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        ResponseEntity<ApiResponse<Map<String, String>>> response =
                globalExceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Erreur de validation", response.getBody().getMessage());

        Map<String, String> errors = response.getBody().getData();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertEquals("Email invalide", errors.get("email"));
        assertEquals("Mot de passe trop court", errors.get("password"));
    }

    @Test
    void handleValidationExceptions_ShouldHandleSingleFieldError() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("testRequest", "title", "Titre requis");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<ApiResponse<Map<String, String>>> response =
                globalExceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Titre requis", response.getBody().getData().get("title"));
    }

    @Test
    void handleValidationExceptions_ShouldHandleEmptyErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of());

        // Act
        ResponseEntity<ApiResponse<Map<String, String>>> response =
                globalExceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Erreur de validation", response.getBody().getMessage());
        assertTrue(response.getBody().getData().isEmpty());
    }

    // ==================== GENERIC EXCEPTION ====================

    @Test
    void handleGenericException_ShouldReturn500() {
        // Arrange
        Exception exception = new Exception("Erreur interne");

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Une erreur interne s'est produite"));
        assertTrue(response.getBody().getMessage().contains("Erreur interne"));
    }

    @Test
    void handleGenericException_ShouldIncludeExceptionMessage() {
        // Arrange
        String errorMessage = "Database connection failed";
        Exception exception = new Exception(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertTrue(response.getBody().getMessage().contains(errorMessage));
    }

    @Test
    void handleGenericException_ShouldHandleNullPointerException() {
        // Arrange
        NullPointerException exception = new NullPointerException("Null value");

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Null value"));
    }

    @Test
    void handleGenericException_ShouldHandleRuntimeException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Runtime error");

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Runtime error"));
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    void allHandlers_ShouldReturnNonNullResponses() {
        // Arrange
        ResourceNotFoundException notFoundEx = new ResourceNotFoundException("Not found");
        BadRequestException badRequestEx = new BadRequestException("Bad request");
        BadCredentialsException badCredsEx = new BadCredentialsException("Bad creds");
        AccessDeniedException accessDeniedEx = new AccessDeniedException("Denied");
        Exception genericEx = new Exception("Generic error");

        // Act & Assert
        assertNotNull(globalExceptionHandler.handleResourceNotFoundException(notFoundEx).getBody());
        assertNotNull(globalExceptionHandler.handleBadRequestException(badRequestEx).getBody());
        assertNotNull(globalExceptionHandler.handleBadCredentialsException(badCredsEx).getBody());
        assertNotNull(globalExceptionHandler.handleAccessDeniedException(accessDeniedEx).getBody());
        assertNotNull(globalExceptionHandler.handleGenericException(genericEx).getBody());
    }

    @Test
    void allErrorResponses_ShouldHaveSuccessFalse() {
        // Arrange
        ResourceNotFoundException notFoundEx = new ResourceNotFoundException("Not found");
        BadRequestException badRequestEx = new BadRequestException("Bad request");

        // Act
        ResponseEntity<ApiResponse<Object>> response1 =
                globalExceptionHandler.handleResourceNotFoundException(notFoundEx);
        ResponseEntity<ApiResponse<Object>> response2 =
                globalExceptionHandler.handleBadRequestException(badRequestEx);

        // Assert
        assertFalse(response1.getBody().isSuccess());
        assertFalse(response2.getBody().isSuccess());
    }

    // ==================== UNAUTHORIZED EXCEPTION ====================

    @Test
    void handleUnauthorizedException_ShouldReturn401() {
        // Arrange
        UnauthorizedException exception = new UnauthorizedException("Non autorisé");

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleUnauthorizedException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Non autorisé", response.getBody().getMessage());
    }

    @Test
    void handleUnauthorizedException_ShouldContainCorrectMessage() {
        // Arrange
        String message = "Token expiré";
        UnauthorizedException exception = new UnauthorizedException(message);

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleUnauthorizedException(exception);

        // Assert
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void handleUnauthorizedException_ShouldHandleInvalidToken() {
        // Arrange
        String message = "Token JWT invalide";
        UnauthorizedException exception = new UnauthorizedException(message);

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleUnauthorizedException(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(message, response.getBody().getMessage());
    }
}