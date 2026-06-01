package ma.project.echallenge.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    private String description;

    @Min(value = 1, message = "La durée doit être au moins 1 minute")
    private Integer duration;

    private Long themeId;

    private Integer questionDuration;

    @Min(value = 0, message = "Le score minimum doit être entre 0 et 100")
    private Double passingScore;
}
