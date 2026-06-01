package ma.project.echallenge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "phone")
    private String phone;

    @Column(nullable = false)
    private boolean emailConfirmed = false;

    private String confirmationCode;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<TestSession> testSessions = new ArrayList<>();

    // ✅ Méthode helper pour récupérer toutes les réponses
    public List<CandidateAnswer> getAllAnswers() {
        List<CandidateAnswer> allAnswers = new ArrayList<>();
        if (testSessions != null) {
            for (TestSession session : testSessions) {
                if (session.getCandidateAnswers() != null) {
                    allAnswers.addAll(session.getCandidateAnswers());
                }
            }
        }
        return allAnswers;
    }
}