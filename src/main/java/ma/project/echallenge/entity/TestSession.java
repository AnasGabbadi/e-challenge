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
@Table(name = "test_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestSession implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    @JsonIgnoreProperties({"testSessions"})
    private Candidate candidate;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    @JsonIgnoreProperties({"sessions", "questions"})
    private Test test;

    @Column(nullable = false, unique = true)
    private String sessionCode;

    @ManyToOne
    @JoinColumn(name = "timeslot_id", nullable = false)
    @JsonIgnoreProperties({"sessions"})
    private TimeSlot timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @OneToMany(mappedBy = "testSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"testSession"})
    private List<CandidateAnswer> candidateAnswers = new ArrayList<>();

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"session"})
    private Result result;

    public enum SessionStatus {
        NOT_STARTED,  // Candidat inscrit, test pas encore démarré
        STARTED,      // Test en cours
        COMPLETED,    // Test complété
        EXPIRED,      // Créneau dépassé
        CANCELLED     // Session annulée
    }

    // Constructor simplifié pour création
    public TestSession(Candidate candidate, Test test, TimeSlot timeSlot, String sessionCode) {
        this.candidate = candidate;
        this.test = test;
        this.timeSlot = timeSlot;
        this.sessionCode = sessionCode;
        this.status = SessionStatus.NOT_STARTED;
        this.registrationDate = LocalDateTime.now();
    }
}