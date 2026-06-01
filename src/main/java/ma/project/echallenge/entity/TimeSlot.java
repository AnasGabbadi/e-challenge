package ma.project.echallenge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "timeslots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private boolean booked = false;

    @Column(nullable = false)
    private int maxCandidates = 1;

    // Calculate end time automatically
    public void calculateEndTime() {
        if (startTime != null) {
            endTime = startTime.plusMinutes(durationMinutes);
        }
    }
}