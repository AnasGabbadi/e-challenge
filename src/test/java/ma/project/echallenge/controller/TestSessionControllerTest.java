package ma.project.echallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.project.echallenge.config.TestSecurityConfig;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.SessionResponse;
import ma.project.echallenge.service.TestSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TestSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestSessionService testSessionService;

    // ===== CREATE SESSION TESTS =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSession_ShouldReturnSuccess() throws Exception {
        // Arrange
        Long candidateId = 1L;
        Long testId = 1L;
        Long timeSlotId = 1L;
        String sessionCode = "ABC12345";

        when(testSessionService.createSession(candidateId, testId, timeSlotId))
                .thenReturn(ApiResponse.success("Session créée avec succès", sessionCode));

        // Act & Assert
        mockMvc.perform(post("/api/test-sessions")
                        .param("candidateId", String.valueOf(candidateId))
                        .param("testId", String.valueOf(testId))
                        .param("timeSlotId", String.valueOf(timeSlotId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Session créée avec succès"))
                .andExpect(jsonPath("$.data").value(sessionCode));

        verify(testSessionService).createSession(candidateId, testId, timeSlotId);
    }

    @Test
    void createSession_WithMissingParams_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/test-sessions")
                        .param("candidateId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(testSessionService, never()).createSession(anyLong(), anyLong(), anyLong());
    }

    // ===== START SESSION TESTS =====

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void startSession_ShouldReturnSessionResponse() throws Exception {
        // Arrange
        String sessionCode = "ABC12345";
        SessionResponse sessionResponse = new SessionResponse(
                1L, 1L, "John Doe", 1L, "Java Test",
                sessionCode, "STARTED",
                LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(testSessionService.startSession(sessionCode))
                .thenReturn(ApiResponse.success("Session démarrée", sessionResponse));

        // Act & Assert
        mockMvc.perform(post("/api/test-sessions/start/{sessionCode}", sessionCode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionCode").value(sessionCode))
                .andExpect(jsonPath("$.data.status").value("STARTED"));

        verify(testSessionService).startSession(sessionCode);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void startSession_WithInvalidCode_ShouldReturnError() throws Exception {
        String sessionCode = "INVALID";

        when(testSessionService.startSession(sessionCode))
                .thenThrow(new RuntimeException("Session non trouvée"));

        mockMvc.perform(post("/api/test-sessions/start/{sessionCode}", sessionCode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(testSessionService).startSession(sessionCode);
    }

    // ===== COMPLETE SESSION TESTS =====

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void completeSession_ShouldReturnSuccess() throws Exception {
        // Arrange
        Long sessionId = 1L;

        when(testSessionService.completeSession(sessionId))
                .thenReturn(ApiResponse.success("Session terminée", null));

        // Act & Assert
        mockMvc.perform(post("/api/test-sessions/complete/{sessionId}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Session terminée"));

        verify(testSessionService).completeSession(sessionId);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void completeSession_WithInvalidId_ShouldReturnError() throws Exception {
        Long sessionId = 999L;

        when(testSessionService.completeSession(sessionId))
                .thenThrow(new RuntimeException("Session non trouvée"));

        mockMvc.perform(post("/api/test-sessions/complete/{sessionId}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(testSessionService).completeSession(sessionId);
    }

    // ===== GET ALL SESSIONS TESTS =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSessions_ShouldReturnListOfSessions() throws Exception {
        // Arrange
        SessionResponse session1 = new SessionResponse(
                1L, 1L, "John Doe", 1L, "Java Test",
                "ABC123", "STARTED",
                LocalDateTime.now(), LocalDateTime.now(), null
        );
        SessionResponse session2 = new SessionResponse(
                2L, 2L, "Jane Smith", 2L, "Python Test",
                "DEF456", "COMPLETED",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        List<SessionResponse> sessions = Arrays.asList(session1, session2);

        when(testSessionService.getAllSessions())
                .thenReturn(ApiResponse.success("Sessions récupérées", sessions));

        // Act & Assert
        mockMvc.perform(get("/api/test-sessions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].sessionCode").value("ABC123"))
                .andExpect(jsonPath("$.data[1].sessionCode").value("DEF456"));

        verify(testSessionService).getAllSessions();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSessions_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        when(testSessionService.getAllSessions())
                .thenReturn(ApiResponse.success("Aucune session", List.of()));

        mockMvc.perform(get("/api/test-sessions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(testSessionService).getAllSessions();
    }

    // ===== GET SESSION BY ID TESTS =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSessionById_ShouldReturnSession() throws Exception {
        // Arrange
        Long sessionId = 1L;
        SessionResponse sessionResponse = new SessionResponse(
                sessionId, 1L, "John Doe", 1L, "Java Test",
                "ABC123", "STARTED",
                LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(testSessionService.getSessionById(sessionId))
                .thenReturn(ApiResponse.success("Session récupérée", sessionResponse));

        // Act & Assert
        mockMvc.perform(get("/api/test-sessions/{sessionId}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(sessionId))
                .andExpect(jsonPath("$.data.sessionCode").value("ABC123"))
                .andExpect(jsonPath("$.data.status").value("STARTED"));

        verify(testSessionService).getSessionById(sessionId);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getSessionById_WithInvalidId_ShouldReturnError() throws Exception {
        Long sessionId = 999L;

        when(testSessionService.getSessionById(sessionId))
                .thenThrow(new RuntimeException("Session non trouvée"));

        mockMvc.perform(get("/api/test-sessions/{sessionId}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(testSessionService).getSessionById(sessionId);
    }
}
