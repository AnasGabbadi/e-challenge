package ma.project.echallenge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.project.echallenge.entity.QuestionType;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    private Long id;
    private String text;
    private QuestionType type;
    private List<OptionResponse> options;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionResponse {
        private Long id;
        private String optionText;
        private Boolean isCorrect; // Seulement pour admin
    }
}