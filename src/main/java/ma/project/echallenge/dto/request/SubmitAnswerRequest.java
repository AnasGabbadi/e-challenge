package ma.project.echallenge.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequest {

    @NotNull(message = "L'ID de la session est obligatoire")
    private Long sessionId;

    @NotNull(message = "L'ID de la question est obligatoire")
    private Long questionId;

    @NotEmpty(message = "Au moins une option doit être sélectionnée")
    private List<Long> selectedOptionIds;

    // Optionnel : temps passé sur la question
    private Long timeSpentSeconds;
}