package ma.project.echallenge.repository;

import ma.project.echallenge.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByTestId(Long testId);
    List<TimeSlot> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<TimeSlot> findByBookedFalse();
    List<TimeSlot> findByTestIdAndBookedFalse(Long testId);
}