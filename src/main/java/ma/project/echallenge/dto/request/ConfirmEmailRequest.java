package ma.project.echallenge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmEmailRequest {

    @NotBlank(message = "Email est obligatoire")
    private String email;

    @NotBlank(message = "Code de confirmation est obligatoire")
    private String confirmationCode;
}