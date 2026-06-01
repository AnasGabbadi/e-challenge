package ma.project.echallenge.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BadRequestExceptionTest {

    @Test
    void constructor_ShouldCreateException_WithMessage() {
        // Arrange
        String message = "Requête invalide";

        // Act
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_ShouldBeRuntimeException() {
        // Arrange & Act
        BadRequestException exception = new BadRequestException("Test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void getMessage_ShouldReturnCorrectMessage() {
        // Arrange
        String message = "Email déjà utilisé";
        BadRequestException exception = new BadRequestException(message);

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
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_ShouldHandleNullMessage() {
        // Arrange & Act
        BadRequestException exception = new BadRequestException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void constructor_ShouldHandleLongMessage() {
        // Arrange
        String message = "A".repeat(1000);

        // Act
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(1000, exception.getMessage().length());
    }

    @Test
    void constructor_ShouldHandleSpecialCharacters() {
        // Arrange
        String message = "Erreur: données invalides! @#$%^&*()";

        // Act
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_ShouldBeThrowable() {
        // Arrange
        String message = "Test exception";

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            throw new BadRequestException(message);
        });
    }

    @Test
    void thrownException_ShouldContainCorrectMessage() {
        // Arrange
        String message = "Creneau déjà réservé";

        // Act & Assert
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> {
                    throw new BadRequestException(message);
                }
        );

        assertEquals(message, thrown.getMessage());
    }

    @Test
    void multipleInstances_ShouldBeIndependent() {
        // Arrange & Act
        BadRequestException exception1 = new BadRequestException("Message 1");
        BadRequestException exception2 = new BadRequestException("Message 2");

        // Assert
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertEquals("Message 1", exception1.getMessage());
        assertEquals("Message 2", exception2.getMessage());
    }
}
