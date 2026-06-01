package ma.project.echallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.ResultResponse;
import ma.project.echallenge.service.ResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResultService resultService;

    private ResultResponse resultResponse1;
    private ResultResponse resultResponse2;

    @BeforeEach
    void setUp() {
        // Setup result responses
        resultResponse1 = new ResultResponse();
        resultResponse1.setId(1L);
        resultResponse1.setSessionId(1L);
        resultResponse1.setCandidateName("John Doe");
        resultResponse1.setTestTitle("Java Basics Test");
        resultResponse1.setScore(85.5);
        resultResponse1.setTotalQuestions(20);
        resultResponse1.setCorrectAnswers(17);
        resultResponse1.setPassed(true);
        resultResponse1.setCompletedAt(LocalDateTime.of(2026, 1, 8, 10, 30));

        resultResponse2 = new ResultResponse();
        resultResponse2.setId(2L);
        resultResponse2.setSessionId(2L);
        resultResponse2.setCandidateName("Jane Smith");
        resultResponse2.setTestTitle("Spring Framework Test");
        resultResponse2.setScore(65.0);
        resultResponse2.setTotalQuestions(20);
        resultResponse2.setCorrectAnswers(13);
        resultResponse2.setPassed(true);
        resultResponse2.setCompletedAt(LocalDateTime.of(2026, 1, 8, 11, 0));
    }

    // ==================== GET RESULT BY ID TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getResultById_ShouldReturnResult_WhenIdExists_AsAdmin() throws Exception {
        ApiResponse response = ApiResponse.success("Résultat récupéré avec succès", resultResponse1);
        when(resultService.getResultById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/results/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.candidateName").value("John Doe"))
                .andExpect(jsonPath("$.data.score").value(85.5));

        verify(resultService, times(1)).getResultById(1L);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getResultById_ShouldReturnResult_WhenIdExists_AsCandidate() throws Exception {
        ApiResponse response = ApiResponse.success("Résultat récupéré avec succès", resultResponse1);
        when(resultService.getResultById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/results/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(resultService, times(1)).getResultById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getResultById_ShouldCallServiceWithCorrectId() throws Exception {
        ApiResponse response = ApiResponse.success("Résultat récupéré avec succès", resultResponse2);
        when(resultService.getResultById(2L)).thenReturn(response);

        mockMvc.perform(get("/api/results/2"))
                .andExpect(status().isOk());

        verify(resultService, times(1)).getResultById(2L);
    }

    // ==================== GET RESULT BY SESSION ID TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getResultBySessionId_ShouldReturnResult_WhenSessionExists_AsAdmin() throws Exception {
        ApiResponse response = ApiResponse.success("Résultat récupéré avec succès", resultResponse1);
        when(resultService.getResultBySessionId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/results/session/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(1));

        verify(resultService, times(1)).getResultBySessionId(1L);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getResultBySessionId_ShouldReturnResult_WhenSessionExists_AsCandidate() throws Exception {
        ApiResponse response = ApiResponse.success("Résultat récupéré avec succès", resultResponse2);
        when(resultService.getResultBySessionId(2L)).thenReturn(response);

        mockMvc.perform(get("/api/results/session/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(resultService, times(1)).getResultBySessionId(2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getResultBySessionId_ShouldCallServiceWithCorrectSessionId() throws Exception {
        ApiResponse response = ApiResponse.success("Résultat récupéré avec succès", resultResponse1);
        when(resultService.getResultBySessionId(3L)).thenReturn(response);

        mockMvc.perform(get("/api/results/session/3"))
                .andExpect(status().isOk());

        verify(resultService, times(1)).getResultBySessionId(3L);
    }

    // ==================== GET RESULTS BY CANDIDATE ID TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getResultsByCandidateId_ShouldReturnResults_WhenCandidateExists_AsAdmin() throws Exception {
        List<ResultResponse> results = Arrays.asList(resultResponse1, resultResponse2);
        ApiResponse response = ApiResponse.success("Résultats récupérés avec succès", results);
        when(resultService.getResultsByCandidateId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/results/candidate/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(resultService, times(1)).getResultsByCandidateId(1L);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getResultsByCandidateId_ShouldReturnResults_WhenCandidateExists_AsCandidate() throws Exception {
        List<ResultResponse> results = Arrays.asList(resultResponse1);
        ApiResponse response = ApiResponse.success("Résultats récupérés avec succès", results);
        when(resultService.getResultsByCandidateId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/results/candidate/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(resultService, times(1)).getResultsByCandidateId(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getResultsByCandidateId_ShouldReturnEmptyList_WhenNoResults() throws Exception {
        ApiResponse response = ApiResponse.success("Résultats récupérés avec succès", Arrays.asList());
        when(resultService.getResultsByCandidateId(999L)).thenReturn(response);

        mockMvc.perform(get("/api/results/candidate/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(resultService, times(1)).getResultsByCandidateId(999L);
    }

    // ==================== GET PASSED RESULTS TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPassedResults_ShouldReturnPassedResults_AsAdmin() throws Exception {
        List<ResultResponse> passedResults = Arrays.asList(resultResponse1, resultResponse2);
        ApiResponse response = ApiResponse.success("Résultats réussis récupérés avec succès", passedResults);
        when(resultService.getPassedResults()).thenReturn(response);

        mockMvc.perform(get("/api/results/passed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(resultService, times(1)).getPassedResults();
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getPassedResults_ShouldReturn403_WhenUserIsCandidate() throws Exception {
        mockMvc.perform(get("/api/results/passed"))
                .andExpect(status().isForbidden());

        verify(resultService, never()).getPassedResults();
    }

    // ==================== GET FAILED RESULTS TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getFailedResults_ShouldReturnFailedResults_AsAdmin() throws Exception {
        ResultResponse failedResult = new ResultResponse();
        failedResult.setId(3L);
        failedResult.setPassed(false);
        failedResult.setScore(45.0);

        List<ResultResponse> failedResults = Arrays.asList(failedResult);
        ApiResponse response = ApiResponse.success("Résultats échoués récupérés avec succès", failedResults);
        when(resultService.getFailedResults()).thenReturn(response);

        mockMvc.perform(get("/api/results/failed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(resultService, times(1)).getFailedResults();
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getFailedResults_ShouldReturn403_WhenUserIsCandidate() throws Exception {
        mockMvc.perform(get("/api/results/failed"))
                .andExpect(status().isForbidden());

        verify(resultService, never()).getFailedResults();
    }

    // ==================== GET ALL RESULTS TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllResults_ShouldReturnAllResults_AsAdmin() throws Exception {
        List<ResultResponse> allResults = Arrays.asList(resultResponse1, resultResponse2);
        ApiResponse response = ApiResponse.success("Tous les résultats récupérés avec succès", allResults);
        when(resultService.getAllResults()).thenReturn(response);

        mockMvc.perform(get("/api/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(resultService, times(1)).getAllResults();
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getAllResults_ShouldReturn403_WhenUserIsCandidate() throws Exception {
        mockMvc.perform(get("/api/results"))
                .andExpect(status().isForbidden());

        verify(resultService, never()).getAllResults();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllResults_ShouldReturnEmptyList_WhenNoResults() throws Exception {
        ApiResponse response = ApiResponse.success("Tous les résultats récupérés avec succès", Arrays.asList());
        when(resultService.getAllResults()).thenReturn(response);

        mockMvc.perform(get("/api/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(resultService, times(1)).getAllResults();
    }
}