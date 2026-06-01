package ma.project.echallenge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean booked;
    private Long testId;
    private String testTitle;
}
