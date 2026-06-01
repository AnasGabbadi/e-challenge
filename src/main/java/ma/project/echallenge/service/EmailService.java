package ma.project.echallenge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendConfirmationEmail(String to, String confirmationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Confirmation de votre email - E-Challenge");
            message.setText(
                    "Bienvenue sur E-Challenge !\n\n" +
                            "Votre code de confirmation est : " + confirmationCode + "\n\n" +
                            "Merci de votre inscription !"
            );

            mailSender.send(message);
            log.info("Confirmation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send confirmation email to: {}", to, e);
        }
    }

    @Async
    public void sendSessionInvitation(String to, String testTitle, String sessionCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Invitation à passer un test - E-Challenge");
            message.setText(
                    "Bonjour,\n\n" +
                            "Vous êtes invité(e) à passer le test : " + testTitle + "\n\n" +
                            "Code de session : " + sessionCode + "\n\n" +
                            "Bonne chance !"
            );

            mailSender.send(message);
            log.info("Session invitation sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send session invitation to: {}", to, e);
        }
    }

    @Async
    public void sendTestResult(String to, String testTitle, double score, boolean passed) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Résultats de votre test - E-Challenge");
            message.setText(
                    "Bonjour,\n\n" +
                            "Voici vos résultats pour le test : " + testTitle + "\n\n" +
                            "Score : " + score + "%\n" +
                            "Statut : " + (passed ? "RÉUSSI ✓" : "ÉCHOUÉ ✗") + "\n\n" +
                            "Merci d'avoir participé !"
            );

            mailSender.send(message);
            log.info("Test result email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send result email to: {}", to, e);
        }
    }
}