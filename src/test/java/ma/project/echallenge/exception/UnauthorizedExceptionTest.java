package ma.project.echallenge.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedExceptionTest {

    @Test
    void constructor_ShouldCreateException_WithMessage() {
        // Arrange
        String message = "Non autorisé";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_ShouldBeRuntimeException() {
        // Arrange & Act
        UnauthorizedException exception = new UnauthorizedException("Test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void getMessage_ShouldReturnCorrectMessage() {
        // Arrange
        String message = "Token expiré";
        UnauthorizedException exception = new UnauthorizedException(message);

        // Act
        String result = exception.getMessage();

        // Assert
        assertEquals(message, result);
    }

    @Test
    void constructor_ShouldHandleEmptyMessage() {
        // Arrange
        String message = "";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_ShouldHandleNullMessage() {
        // Arrange & Act
        UnauthorizedException exception = new UnauthorizedException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void constructor_ShouldHandleLongMessage() {
        // Arrange
        String message = "C".repeat(1000);

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(1000, exception.getMessage().length());
    }

    @Test
    void constructor_ShouldHandleSpecialCharacters() {
        // Arrange
        String message = "Accès refusé: Token invalide! @#$%";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_ShouldBeThrowable() {
        // Arrange
        String message = "Unauthorized access";

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            throw new UnauthorizedException(message);
        });
    }

    @Test
    void thrownException_ShouldContainCorrectMessage() {
        // Arrange
        String message = "Token invalide ou expiré";

        // Act & Assert
        UnauthorizedException thrown = assertThrows(
                UnauthorizedException.class,
                () -> {
                    throw new UnauthorizedException(message);
                }
        );

        assertEquals(message, thrown.getMessage());
    }

    @Test
    void multipleInstances_ShouldBeIndependent() {
        // Arrange & Act
        UnauthorizedException exception1 = new UnauthorizedException("Unauthorized 1");
        UnauthorizedException exception2 = new UnauthorizedException("Unauthorized 2");

        // Assert
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertEquals("Unauthorized 1", exception1.getMessage());
        assertEquals("Unauthorized 2", exception2.getMessage());
    }

    @Test
    void exception_ShouldBeUsedForTokenExpired() {
        // Arrange
        String message = "Le token JWT a expiré";

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> validateToken("expired-token")
        );

        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_ShouldBeUsedForInvalidToken() {
        // Arrange
        String message = "Token JWT invalide";

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> validateToken("invalid-token")
        );

        assertEquals(message, exception.getMessage());
    }

    // Helper methods to simulate JWT scenarios
    private void validateToken(String token) {
        if ("expired-token".equals(token)) {
            throw new UnauthorizedException("Le token JWT a expiré");
        } else if ("invalid-token".equals(token)) {
            throw new UnauthorizedException("Token JWT invalide");
        }
    }
}