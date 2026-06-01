package ma.project.echallenge.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Set test values using reflection (simulating @Value injection)
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "mySecretKeyForTestingPurposesOnlyMinimum256BitsLongForHS256Algorithm");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 3600000L); // 1 hour
    }

    // ==================== GENERATE TOKEN TESTS ====================

    @Test
    void generateToken_ShouldReturnValidToken_WhenValidParameters() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        String role = "CANDIDATE";

        // Act
        String token = jwtTokenProvider.generateToken(email, userId, role);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void generateToken_ShouldContainEmail_WhenGenerated() {
        // Arrange
        String email = "candidate@test.com";
        Long userId = 2L;
        String role = "CANDIDATE";

        // Act
        String token = jwtTokenProvider.generateToken(email, userId, role);
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    void generateToken_ShouldContainUserId_WhenGenerated() {
        // Arrange
        String email = "user@test.com";
        Long userId = 123L;
        String role = "ADMIN";

        // Act
        String token = jwtTokenProvider.generateToken(email, userId, role);
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(userId, extractedUserId);
    }

    @Test
    void generateToken_ShouldHandleDifferentEmails() {
        // Arrange & Act
        String token1 = jwtTokenProvider.generateToken("user1@test.com", 1L, "CANDIDATE");
        String token2 = jwtTokenProvider.generateToken("user2@test.com", 2L, "CANDIDATE");

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_ShouldHandleDifferentUserIds() {
        // Arrange & Act
        String token1 = jwtTokenProvider.generateToken("test@test.com", 1L, "CANDIDATE");
        String token2 = jwtTokenProvider.generateToken("test@test.com", 2L, "CANDIDATE");

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_ShouldHandleDifferentRoles() {
        // Arrange & Act
        String token1 = jwtTokenProvider.generateToken("test@test.com", 1L, "CANDIDATE");
        String token2 = jwtTokenProvider.generateToken("test@test.com", 1L, "ADMIN");

        // Assert
        assertNotEquals(token1, token2);
    }

    // ==================== GET EMAIL FROM TOKEN TESTS ====================

    @Test
    void getEmailFromToken_ShouldReturnCorrectEmail_WhenValidToken() {
        // Arrange
        String email = "john.doe@example.com";
        String token = jwtTokenProvider.generateToken(email, 1L, "CANDIDATE");

        // Act
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    void getEmailFromToken_ShouldHandleSpecialCharactersInEmail() {
        // Arrange
        String email = "test+user@example.com";
        String token = jwtTokenProvider.generateToken(email, 1L, "CANDIDATE");

        // Act
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    void getEmailFromToken_ShouldThrowException_WhenInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(JwtException.class, () -> {
            jwtTokenProvider.getEmailFromToken(invalidToken);
        });
    }

    // ==================== GET USER ID FROM TOKEN TESTS ====================

    @Test
    void getUserIdFromToken_ShouldReturnCorrectUserId_WhenValidToken() {
        // Arrange
        Long userId = 42L;
        String token = jwtTokenProvider.generateToken("test@example.com", userId, "CANDIDATE");

        // Act
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(userId, extractedUserId);
    }

    @Test
    void getUserIdFromToken_ShouldHandleLargeUserId() {
        // Arrange
        Long userId = 999999999L;
        String token = jwtTokenProvider.generateToken("test@example.com", userId, "CANDIDATE");

        // Act
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(userId, extractedUserId);
    }

    @Test
    void getUserIdFromToken_ShouldThrowException_WhenInvalidToken() {
        // Arrange
        String invalidToken = "not.a.valid.token";

        // Act & Assert
        assertThrows(JwtException.class, () -> {
            jwtTokenProvider.getUserIdFromToken(invalidToken);
        });
    }

    // ==================== VALIDATE TOKEN TESTS ====================

    @Test
    void validateToken_ShouldReturnTrue_WhenValidToken() {
        // Arrange
        String token = jwtTokenProvider.generateToken("test@example.com", 1L, "CANDIDATE");

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenInvalidToken() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenEmptyToken() {
        // Arrange
        String emptyToken = "";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenNullToken() {
        // Arrange
        String nullToken = null;

        // Act
        boolean isValid = jwtTokenProvider.validateToken(nullToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenMalformedToken() {
        // Arrange
        String malformedToken = "this.is.not.a.jwt";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenExpiredToken() {
        // Arrange - Create token with negative expiration (already expired)
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredProvider, "jwtSecret", "mySecretKeyForTestingPurposesOnlyMinimum256BitsLongForHS256Algorithm");
        ReflectionTestUtils.setField(expiredProvider, "jwtExpiration", -1000L); // Already expired

        String expiredToken = expiredProvider.generateToken("test@example.com", 1L, "CANDIDATE");

        // Wait a moment to ensure expiration
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    void fullJwtWorkflow_ShouldWork_WhenGenerateValidateAndExtract() {
        // Arrange
        String email = "integration@test.com";
        Long userId = 100L;
        String role = "ADMIN";

        // Act - Generate token
        String token = jwtTokenProvider.generateToken(email, userId, role);

        // Act - Validate token
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Act - Extract claims
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertTrue(isValid);
        assertEquals(email, extractedEmail);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void generateToken_ShouldCreateDifferentTokens_WhenDifferentParameters() {
        // Arrange
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";
        Long userId = 1L;
        String role = "CANDIDATE";

        // Act
        String token1 = jwtTokenProvider.generateToken(email1, userId, role);
        String token2 = jwtTokenProvider.generateToken(email2, userId, role);

        // Assert
        assertNotEquals(token1, token2); // Different because of different emails
    }

    @Test
    void tokenValidation_ShouldWork_WithDifferentSigningKeys() {
        // Arrange - Create another provider with different secret
        JwtTokenProvider otherProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(otherProvider, "jwtSecret", "differentSecretKeyForTestingMinimum256BitsLongForHS256AlgorithmDifferent");
        ReflectionTestUtils.setField(otherProvider, "jwtExpiration", 3600000L);

        String tokenFromOtherProvider = otherProvider.generateToken("test@example.com", 1L, "CANDIDATE");

        // Act - Try to validate with original provider
        boolean isValid = jwtTokenProvider.validateToken(tokenFromOtherProvider);

        // Assert - Should be invalid because of different signing key
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldWork_ForRecentlyGeneratedTokens() {
        // Arrange
        String token1 = jwtTokenProvider.generateToken("user1@test.com", 1L, "CANDIDATE");
        String token2 = jwtTokenProvider.generateToken("user2@test.com", 2L, "ADMIN");
        String token3 = jwtTokenProvider.generateToken("user3@test.com", 3L, "CANDIDATE");

        // Act & Assert
        assertTrue(jwtTokenProvider.validateToken(token1));
        assertTrue(jwtTokenProvider.validateToken(token2));
        assertTrue(jwtTokenProvider.validateToken(token3));
    }
}