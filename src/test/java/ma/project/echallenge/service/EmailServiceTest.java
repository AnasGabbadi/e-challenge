package ma.project.echallenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Configuration initiale si nécessaire
    }

    // ==================== SEND CONFIRMATION EMAIL TESTS ====================

    @Test
    void sendConfirmationEmail_ShouldSendEmail_WhenValidParameters() {
        // Arrange
        String to = "test@example.com";
        String confirmationCode = "123456";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendConfirmationEmail(to, confirmationCode);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage);
        assertNotNull(sentMessage.getTo());
        assertEquals(to, sentMessage.getTo()[0]);
        assertNotNull(sentMessage.getSubject());
        assertTrue(sentMessage.getSubject().contains("Confirmation"));
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains(confirmationCode));
    }

    @Test
    void sendConfirmationEmail_ShouldIncludeConfirmationCode() {
        // Arrange
        String to = "candidate@example.com";
        String confirmationCode = "789456";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendConfirmationEmail(to, confirmationCode);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains(confirmationCode));
    }

    @Test
    void sendConfirmationEmail_ShouldHandleMultipleEmails() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendConfirmationEmail("user1@example.com", "123456");
        emailService.sendConfirmationEmail("user2@example.com", "654321");
        emailService.sendConfirmationEmail("user3@example.com", "111222");

        // Assert
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendConfirmationEmail_ShouldSetCorrectSubject() {
        // Arrange
        String to = "test@example.com";
        String confirmationCode = "123456";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendConfirmationEmail(to, confirmationCode);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("Confirmation de votre email - E-Challenge", sentMessage.getSubject());
    }

    // ==================== SEND SESSION INVITATION TESTS ====================

    @Test
    void sendSessionInvitation_ShouldSendEmail_WhenValidParameters() {
        // Arrange
        String to = "candidate@example.com";
        String testTitle = "Java Programming Test";
        String sessionCode = "SESSION123";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendSessionInvitation(to, testTitle, sessionCode);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage);
        assertNotNull(sentMessage.getTo());
        assertEquals(to, sentMessage.getTo()[0]);
        assertNotNull(sentMessage.getSubject());
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains(sessionCode));
        assertTrue(sentMessage.getText().contains(testTitle));
    }

    @Test
    void sendSessionInvitation_ShouldIncludeTestTitle() {
        // Arrange
        String to = "test@example.com";
        String testTitle = "Spring Boot Advanced";
        String sessionCode = "ABC789";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendSessionInvitation(to, testTitle, sessionCode);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains(testTitle));
    }

    @Test
    void sendSessionInvitation_ShouldIncludeSessionCode() {
        // Arrange
        String to = "candidate@example.com";
        String testTitle = "JavaScript Test";
        String sessionCode = "JS2024";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendSessionInvitation(to, testTitle, sessionCode);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains(sessionCode));
    }

    @Test
    void sendSessionInvitation_ShouldSetCorrectSubject() {
        // Arrange
        String to = "test@example.com";
        String testTitle = "Python Test";
        String sessionCode = "PY123";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendSessionInvitation(to, testTitle, sessionCode);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("Invitation à passer un test - E-Challenge", sentMessage.getSubject());
    }

    // ==================== SEND TEST RESULT TESTS ====================

    @Test
    void sendTestResult_ShouldSendEmail_WhenValidParameters() {
        // Arrange
        String to = "candidate@example.com";
        String testTitle = "Java Test";
        double score = 85.5;
        boolean passed = true;
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendTestResult(to, testTitle, score, passed);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage);
        assertNotNull(sentMessage.getTo());
        assertEquals(to, sentMessage.getTo()[0]);
        assertNotNull(sentMessage.getSubject());
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains(testTitle));
        assertTrue(sentMessage.getText().contains(String.valueOf(score)));
    }

    @Test
    void sendTestResult_ShouldIncludePassedStatus() {
        // Arrange
        String to = "test@example.com";
        String testTitle = "Spring Test";
        double score = 90.0;
        boolean passed = true;
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendTestResult(to, testTitle, score, passed);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains("RÉUSSI"));
    }

    @Test
    void sendTestResult_ShouldIncludeFailedStatus() {
        // Arrange
        String to = "test@example.com";
        String testTitle = "Python Test";
        double score = 45.0;
        boolean passed = false;
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendTestResult(to, testTitle, score, passed);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains("ÉCHOUÉ"));
    }

    @Test
    void sendTestResult_ShouldIncludeScore() {
        // Arrange
        String to = "candidate@example.com";
        String testTitle = "JavaScript Test";
        double score = 75.5;
        boolean passed = true;
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendTestResult(to, testTitle, score, passed);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains("75.5"));
    }

    @Test
    void sendTestResult_ShouldSetCorrectSubject() {
        // Arrange
        String to = "test@example.com";
        String testTitle = "C++ Test";
        double score = 88.0;
        boolean passed = true;
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendTestResult(to, testTitle, score, passed);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("Résultats de votre test - E-Challenge", sentMessage.getSubject());
    }

    // ==================== EDGE CASES ====================

    @Test
    void sendConfirmationEmail_ShouldHandleSpecialCharactersInEmail() {
        // Arrange
        String to = "test+user@example.com";
        String confirmationCode = "123456";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendConfirmationEmail(to, confirmationCode);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getTo());
        assertEquals(to, sentMessage.getTo()[0]);
    }

    @Test
    void sendSessionInvitation_ShouldHandleLongTestTitle() {
        // Arrange
        String to = "test@example.com";
        String testTitle = "Advanced Java Enterprise Edition with Spring Boot Framework and Microservices Architecture";
        String sessionCode = "LONG123";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendSessionInvitation(to, testTitle, sessionCode);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendTestResult_ShouldHandlePerfectScore() {
        // Arrange
        String to = "excellent@example.com";
        String testTitle = "Final Exam";
        double score = 100.0;
        boolean passed = true;
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendTestResult(to, testTitle, score, passed);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains("100.0"));
    }

    @Test
    void sendTestResult_ShouldHandleZeroScore() {
        // Arrange
        String to = "failed@example.com";
        String testTitle = "Difficult Test";
        double score = 0.0;
        boolean passed = false;
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendTestResult(to, testTitle, score, passed);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains("0.0"));
    }

    @Test
    void allEmailMethods_ShouldCallMailSenderSend() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendConfirmationEmail("test1@example.com", "123456");
        emailService.sendSessionInvitation("test2@example.com", "Test Title", "SESSION1");
        emailService.sendTestResult("test3@example.com", "Final Test", 85.0, true);

        // Assert
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }
}