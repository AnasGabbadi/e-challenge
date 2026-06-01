package ma.project.echallenge.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_ShouldCreateException_WithMessage() {
        // Arrange
        String message = "Ressource non trouvée";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_ShouldBeRuntimeException() {
        // Arrange & Act
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void getMessage_ShouldReturnCorrectMessage() {
        // Arrange
        String message = "Candidat non trouvé";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

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
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_ShouldHandleNullMessage() {
        // Arrange & Act
        ResourceNotFoundException exception = new ResourceNotFoundException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void constructor_ShouldHandleLongMessage() {
        // Arrange
        String message = "B".repeat(1000);

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(1000, exception.getMessage().length());
    }

    @Test
    void constructor_ShouldHandleSpecialCharacters() {
        // Arrange
        String message = "Erreur 404: Élément introuvable! @#$%";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_ShouldBeThrowable() {
        // Arrange
        String message = "Test not found";

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException(message);
        });
    }

    @Test
    void thrownException_ShouldContainCorrectMessage() {
        // Arrange
        String message = "Session non trouvée";

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    throw new ResourceNotFoundException(message);
                }
        );

        assertEquals(message, thrown.getMessage());
    }

    @Test
    void multipleInstances_ShouldBeIndependent() {
        // Arrange & Act
        ResourceNotFoundException exception1 = new ResourceNotFoundException("Resource 1 not found");
        ResourceNotFoundException exception2 = new ResourceNotFoundException("Resource 2 not found");

        // Assert
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertEquals("Resource 1 not found", exception1.getMessage());
        assertEquals("Resource 2 not found", exception2.getMessage());
    }

    @Test
    void exception_ShouldBeUsedInTypicalScenario() {
        // Arrange
        String message = "Test non trouvé";

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> simulateRepositoryFindById(999L)
        );

        assertEquals(message, exception.getMessage());
    }

    // Helper method to simulate service behavior
    private void simulateRepositoryFindById(Long id) {
        if (id == 999L) {
            throw new ResourceNotFoundException("Test non trouvé");
        }
    }
}