package ma.project.echallenge.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidate_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAnswer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_session_id", nullable = false)
    @JsonIgnoreProperties({"answers", "candidate", "test"})
    private TestSession testSession;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"options", "theme", "type"})
    private Question question;

    @ManyToMany
    @JoinTable(
            name = "candidate_answer_options",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    @JsonIgnoreProperties("question")
    private List<QuestionOption> selectedOptions = new ArrayList<>();

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "time_spent_seconds")
    private Long timeSpentSeconds;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    public CandidateAnswer(TestSession testSession, Question question) {
        this.testSession = testSession;
        this.question = question;
        this.answeredAt = LocalDateTime.now();
    }
}