package ma.project.echallenge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long candidateId;
    private String candidateName;
    private String email;
    private Long sessionId;
}