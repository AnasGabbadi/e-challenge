package ma.project.echallenge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private TestSession session;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer correctAnswers;

    @Column(nullable = false)
    private Boolean passed;

    @Column(nullable = false)
    private LocalDateTime completedAt = LocalDateTime.now();
}