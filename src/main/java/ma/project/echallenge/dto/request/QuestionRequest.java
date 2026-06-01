package ma.project.echallenge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.project.echallenge.entity.QuestionType;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    @NotBlank(message = "Texte de la question est obligatoire")
    private String text;

    @NotNull(message = "Type de question est obligatoire")
    private QuestionType type;

    @NotNull(message = "Test ID est obligatoire")
    private Long testId;

    private List<OptionRequest> options;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionRequest {
        @NotBlank(message = "Texte de l'option est obligatoire")
        private String optionText;

        @NotNull(message = "isCorrect est obligatoire")
        private Boolean isCorrect;
    }
}