package ma.project.echallenge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean emailConfirmed;
    private LocalDateTime createdAt;
}