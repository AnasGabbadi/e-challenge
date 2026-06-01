package ma.project.echallenge;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.project.echallenge.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    protected String adminToken;
    protected String candidateToken;

    @BeforeEach
    void setUpBase() {
        adminToken = "Bearer " + jwtTokenProvider.generateToken(
                "admin@test.com",
                1L,
                "ROLE_ADMIN"
        );

        candidateToken = "Bearer " + jwtTokenProvider.generateToken(
                "candidate@test.com",
                2L,
                "ROLE_CANDIDATE"
        );
    }
}