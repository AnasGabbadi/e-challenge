package ma.project.echallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.SessionResponse;
import ma.project.echallenge.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SessionService sessionService;

    private SessionResponse sessionResponse1;
    private SessionResponse sessionResponse2;

    @BeforeEach
    void setUp() {
        // Setup session responses
        sessionResponse1 = new SessionResponse();
        sessionResponse1.setId(1L);
        sessionResponse1.setSessionCode("ABC123");
        sessionResponse1.setCandidateId(1L);
        sessionResponse1.setTestId(1L);

        sessionResponse2 = new SessionResponse();
        sessionResponse2.setId(2L);
        sessionResponse2.setSessionCode("XYZ789");
        sessionResponse2.setCandidateId(2L);
        sessionResponse2.setTestId(1L);
    }

    // ==================== CREATE SESSION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSession_ShouldReturnCreatedSession_WhenParamsValid() throws Exception {
        ApiResponse response = ApiResponse.success("Session créée avec succès", sessionResponse1);
        when(sessionService.createSession(1L, 1L, 1L)).thenReturn(response);

        mockMvc.perform(post("/api/sessions")
                        .param("candidateId", "1")
                        .param("testId", "1")
                        .param("timeSlotId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionCode").value("ABC123"));

        verify(sessionService, times(1)).createSession(1L, 1L, 1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSession_ShouldCallServiceWithCorrectParameters() throws Exception {
        ApiResponse response = ApiResponse.success("Session créée avec succès", sessionResponse2);
        when(sessionService.createSession(2L, 3L, 4L)).thenReturn(response);

        mockMvc.perform(post("/api/sessions")
                        .param("candidateId", "2")
                        .param("testId", "3")
                        .param("timeSlotId", "4"))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).createSession(2L, 3L, 4L);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void createSession_ShouldReturn403_WhenUserIsCandidate() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .param("candidateId", "1")
                        .param("testId", "1")
                        .param("timeSlotId", "1"))
                .andExpect(status().isForbidden());

        verify(sessionService, never()).createSession(anyLong(), anyLong(), anyLong());
    }

    // ==================== CHECK SESSION STATUS TESTS ====================

    @Test
    @WithMockUser
    void checkSessionStatus_ShouldReturnStatus_WhenSessionCodeValid() throws Exception {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("canStart", true);
        statusData.put("status", "SCHEDULED");

        ApiResponse response = ApiResponse.success("Statut de la session récupéré", statusData);
        when(sessionService.checkSessionStatus("ABC123")).thenReturn(response);

        mockMvc.perform(get("/api/sessions/check/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.canStart").value(true))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));

        verify(sessionService, times(1)).checkSessionStatus("ABC123");
    }

    @Test
    @WithMockUser
    void checkSessionStatus_ShouldCallServiceWithCorrectCode() throws Exception {
        Map<String, Object> statusData = new HashMap<>();
        ApiResponse response = ApiResponse.success("Statut de la session récupéré", statusData);
        when(sessionService.checkSessionStatus("XYZ789")).thenReturn(response);

        mockMvc.perform(get("/api/sessions/check/XYZ789"))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).checkSessionStatus("XYZ789");
    }

    // ==================== START SESSION TESTS ====================

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void startSession_ShouldReturnStartedSession_WhenCodeValid() throws Exception {
        ApiResponse response = ApiResponse.success("Session démarrée avec succès", sessionResponse1);
        when(sessionService.startSession("ABC123")).thenReturn(response);

        mockMvc.perform(post("/api/sessions/start/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionCode").value("ABC123"));

        verify(sessionService, times(1)).startSession("ABC123");
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void startSession_ShouldCallServiceWithCorrectCode() throws Exception {
        ApiResponse response = ApiResponse.success("Session démarrée avec succès", sessionResponse2);
        when(sessionService.startSession("XYZ789")).thenReturn(response);

        mockMvc.perform(post("/api/sessions/start/XYZ789"))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).startSession("XYZ789");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void startSession_ShouldReturn403_WhenUserIsAdmin() throws Exception {
        mockMvc.perform(post("/api/sessions/start/ABC123"))
                .andExpect(status().isForbidden());

        verify(sessionService, never()).startSession(anyString());
    }

    // ==================== COMPLETE SESSION TESTS ====================

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void completeSession_ShouldReturnCompletedSession_WhenCodeValid() throws Exception {
        ApiResponse response = ApiResponse.success("Session terminée avec succès", sessionResponse1);
        when(sessionService.completeSession("ABC123")).thenReturn(response);

        mockMvc.perform(post("/api/sessions/complete/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(sessionService, times(1)).completeSession("ABC123");
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void completeSession_ShouldCallServiceWithCorrectCode() throws Exception {
        ApiResponse response = ApiResponse.success("Session terminée avec succès", sessionResponse2);
        when(sessionService.completeSession("XYZ789")).thenReturn(response);

        mockMvc.perform(post("/api/sessions/complete/XYZ789"))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).completeSession("XYZ789");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void completeSession_ShouldReturn403_WhenUserIsAdmin() throws Exception {
        mockMvc.perform(post("/api/sessions/complete/ABC123"))
                .andExpect(status().isForbidden());

        verify(sessionService, never()).completeSession(anyString());
    }

    // ==================== CANCEL SESSION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelSession_ShouldReturnSuccess_WhenIdExists() throws Exception {
        ApiResponse response = ApiResponse.success("Session annulée avec succès", sessionResponse1);
        when(sessionService.cancelSession(1L)).thenReturn(response);

        mockMvc.perform(put("/api/sessions/cancel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(sessionService, times(1)).cancelSession(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelSession_ShouldCallServiceWithCorrectId() throws Exception {
        ApiResponse response = ApiResponse.success("Session annulée avec succès", sessionResponse2);
        when(sessionService.cancelSession(2L)).thenReturn(response);

        mockMvc.perform(put("/api/sessions/cancel/2"))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).cancelSession(2L);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void cancelSession_ShouldReturn403_WhenUserIsCandidate() throws Exception {
        mockMvc.perform(put("/api/sessions/cancel/1"))
                .andExpect(status().isForbidden());

        verify(sessionService, never()).cancelSession(anyLong());
    }

    // ==================== GET SESSIONS BY CANDIDATE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSessionsByCandidateId_ShouldReturnSessions_WhenCandidateExists() throws Exception {
        List<SessionResponse> sessions = Arrays.asList(sessionResponse1, sessionResponse2);
        ApiResponse response = ApiResponse.success("Sessions récupérées avec succès", sessions);
        when(sessionService.getSessionsByCandidateId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/sessions/candidate/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(sessionService, times(1)).getSessionsByCandidateId(1L);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getSessionsByCandidateId_ShouldReturnSessions_WhenUserIsCandidate() throws Exception {
        List<SessionResponse> sessions = Arrays.asList(sessionResponse1);
        ApiResponse response = ApiResponse.success("Sessions récupérées avec succès", sessions);
        when(sessionService.getSessionsByCandidateId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/sessions/candidate/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(sessionService, times(1)).getSessionsByCandidateId(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSessionsByCandidateId_ShouldReturnEmptyList_WhenNoSessions() throws Exception {
        ApiResponse response = ApiResponse.success("Sessions récupérées avec succès", Arrays.asList());
        when(sessionService.getSessionsByCandidateId(3L)).thenReturn(response);

        mockMvc.perform(get("/api/sessions/candidate/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(sessionService, times(1)).getSessionsByCandidateId(3L);
    }

    // ==================== GET SESSION BY ID TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSessionById_ShouldReturnSession_WhenIdExists() throws Exception {
        ApiResponse response = ApiResponse.success("Session récupérée avec succès", sessionResponse1);
        when(sessionService.getSessionById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.sessionCode").value("ABC123"));

        verify(sessionService, times(1)).getSessionById(1L);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getSessionById_ShouldReturnSession_WhenUserIsCandidate() throws Exception {
        ApiResponse response = ApiResponse.success("Session récupérée avec succès", sessionResponse2);
        when(sessionService.getSessionById(2L)).thenReturn(response);

        mockMvc.perform(get("/api/sessions/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(sessionService, times(1)).getSessionById(2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSessionById_ShouldCallServiceWithCorrectId() throws Exception {
        ApiResponse response = ApiResponse.success("Session récupérée avec succès", sessionResponse2);
        when(sessionService.getSessionById(2L)).thenReturn(response);

        mockMvc.perform(get("/api/sessions/2"))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).getSessionById(2L);
    }
}