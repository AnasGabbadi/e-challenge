package ma.project.echallenge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultResponse {
    private Long id;
    private Long sessionId;
    private String candidateName;
    private String testTitle;
    private Double score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Boolean passed;
    private LocalDateTime completedAt;
}