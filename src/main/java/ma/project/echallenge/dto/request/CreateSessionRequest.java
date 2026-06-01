package ma.project.echallenge.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {

    @NotNull(message = "L'ID du candidat est obligatoire")
    private Long candidateId;

    @NotNull(message = "L'ID du test est obligatoire")
    private Long testId;

    @NotNull(message = "L'ID du créneau horaire est obligatoire")
    private Long timeSlotId;
}