package ma.project.echallenge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionResponse {
    private Long id;
    private Long candidateId;
    private String candidateName;
    private Long testId;
    private String testTitle;
    private String sessionCode;
    private String status;
    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}