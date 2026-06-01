package ma.project.echallenge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResponse {
    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private Integer questionDuration;
    private Double passingScore;
    private String themeName;
    private Long themeId;
    private Integer questionCount;
    private LocalDateTime createdAt;
}