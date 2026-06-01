package ma.project.echallenge.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotRequest {

    @NotNull(message = "L'ID du test est obligatoire")
    private Long testId;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalDateTime startTime;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalDateTime endTime;

    @Positive(message = "Le nombre maximum de candidats doit être positif")
    private Integer maxCandidates;
}