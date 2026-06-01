package ma.project.echallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.project.echallenge.dto.request.SubmitAnswerRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.entity.CandidateAnswer;
import ma.project.echallenge.service.AnswerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnswerService answerService;

    private SubmitAnswerRequest validSubmitAnswerRequest;
    private CandidateAnswer candidateAnswer;

    @BeforeEach
    void setUp() {
        // Setup valid submit answer request
        validSubmitAnswerRequest = new SubmitAnswerRequest();
        validSubmitAnswerRequest.setSessionId(1L);
        validSubmitAnswerRequest.setQuestionId(1L);
        validSubmitAnswerRequest.setSelectedOptionIds(Arrays.asList(1L, 2L));
        validSubmitAnswerRequest.setTimeSpentSeconds(30L);

        // Setup candidate answer
        candidateAnswer = new CandidateAnswer();
        candidateAnswer.setId(1L);
        candidateAnswer.setIsCorrect(true);
        candidateAnswer.setTimeSpentSeconds(30L);
    }

    // ==================== SUBMIT ANSWER TESTS ====================

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void submitAnswer_ShouldReturnAnswer_WhenRequestValid() throws Exception {
        ApiResponse response = ApiResponse.success("Réponse enregistrée avec succès", candidateAnswer);
        when(answerService.submitAnswer(any(SubmitAnswerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/answers/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSubmitAnswerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isCorrect").value(true));

        verify(answerService, times(1)).submitAnswer(any(SubmitAnswerRequest.class));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void submitAnswer_ShouldReturn400_WhenSessionIdIsNull() throws Exception {
        validSubmitAnswerRequest.setSessionId(null);

        mockMvc.perform(post("/api/answers/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSubmitAnswerRequest)))
                .andExpect(status().isBadRequest());

        verify(answerService, never()).submitAnswer(any());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void submitAnswer_ShouldReturn400_WhenQuestionIdIsNull() throws Exception {
        validSubmitAnswerRequest.setQuestionId(null);

        mockMvc.perform(post("/api/answers/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSubmitAnswerRequest)))
                .andExpect(status().isBadRequest());

        verify(answerService, never()).submitAnswer(any());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void submitAnswer_ShouldReturn400_WhenSelectedOptionIdsIsEmpty() throws Exception {
        validSubmitAnswerRequest.setSelectedOptionIds(Arrays.asList());

        mockMvc.perform(post("/api/answers/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSubmitAnswerRequest)))
                .andExpect(status().isBadRequest());

        verify(answerService, never()).submitAnswer(any());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void submitAnswer_ShouldReturn400_WhenSelectedOptionIdsIsNull() throws Exception {
        validSubmitAnswerRequest.setSelectedOptionIds(null);

        mockMvc.perform(post("/api/answers/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSubmitAnswerRequest)))
                .andExpect(status().isBadRequest());

        verify(answerService, never()).submitAnswer(any());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void submitAnswer_ShouldAcceptNullTimeSpentSeconds() throws Exception {
        validSubmitAnswerRequest.setTimeSpentSeconds(null);
        ApiResponse response = ApiResponse.success("Réponse enregistrée avec succès", candidateAnswer);
        when(answerService.submitAnswer(any(SubmitAnswerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/answers/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSubmitAnswerRequest)))
                .andExpect(status().isOk());

        verify(answerService, times(1)).submitAnswer(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void submitAnswer_ShouldReturn403_WhenUserIsAdmin() throws Exception {
        mockMvc.perform(post("/api/answers/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSubmitAnswerRequest)))
                .andExpect(status().isForbidden());

        verify(answerService, never()).submitAnswer(any());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void submitAnswer_ShouldCallServiceWithCorrectData() throws Exception {
        ApiResponse response = ApiResponse.success("Réponse enregistrée avec succès", candidateAnswer);
        when(answerService.submitAnswer(any(SubmitAnswerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/answers/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSubmitAnswerRequest)))
                .andExpect(status().isOk());

        verify(answerService).submitAnswer(argThat(request ->
                request.getSessionId().equals(1L) &&
                        request.getQuestionId().equals(1L) &&
                        request.getSelectedOptionIds().size() == 2
        ));
    }

    // ==================== GET ANSWERS BY SESSION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAnswersBySession_ShouldReturnAnswers_WhenSessionExists_AsAdmin() throws Exception {
        List<CandidateAnswer> answers = Arrays.asList(candidateAnswer);
        ApiResponse response = ApiResponse.success("Réponses récupérées avec succès", answers);
        when(answerService.getAnswersBySession(1L)).thenReturn(response);

        mockMvc.perform(get("/api/answers/session/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(answerService, times(1)).getAnswersBySession(1L);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getAnswersBySession_ShouldReturnAnswers_WhenSessionExists_AsCandidate() throws Exception {
        List<CandidateAnswer> answers = Arrays.asList(candidateAnswer);
        ApiResponse response = ApiResponse.success("Réponses récupérées avec succès", answers);
        when(answerService.getAnswersBySession(1L)).thenReturn(response);

        mockMvc.perform(get("/api/answers/session/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(answerService, times(1)).getAnswersBySession(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAnswersBySession_ShouldReturnEmptyList_WhenNoAnswers() throws Exception {
        ApiResponse response = ApiResponse.success("Réponses récupérées avec succès", Arrays.asList());
        when(answerService.getAnswersBySession(999L)).thenReturn(response);

        mockMvc.perform(get("/api/answers/session/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(answerService, times(1)).getAnswersBySession(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAnswersBySession_ShouldCallServiceWithCorrectSessionId() throws Exception {
        ApiResponse response = ApiResponse.success("Réponses récupérées avec succès", Arrays.asList());
        when(answerService.getAnswersBySession(2L)).thenReturn(response);

        mockMvc.perform(get("/api/answers/session/2"))
                .andExpect(status().isOk());

        verify(answerService, times(1)).getAnswersBySession(2L);
    }

    // ==================== GET ANSWER BY SESSION AND QUESTION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAnswerBySessionAndQuestion_ShouldReturnAnswer_WhenExists_AsAdmin() throws Exception {
        ApiResponse response = ApiResponse.success("Réponse récupérée avec succès", candidateAnswer);
        when(answerService.getAnswerBySessionAndQuestion(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/answers/session/1/question/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isCorrect").value(true));

        verify(answerService, times(1)).getAnswerBySessionAndQuestion(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getAnswerBySessionAndQuestion_ShouldReturnAnswer_WhenExists_AsCandidate() throws Exception {
        ApiResponse response = ApiResponse.success("Réponse récupérée avec succès", candidateAnswer);
        when(answerService.getAnswerBySessionAndQuestion(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/answers/session/1/question/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(answerService, times(1)).getAnswerBySessionAndQuestion(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAnswerBySessionAndQuestion_ShouldCallServiceWithCorrectIds() throws Exception {
        ApiResponse response = ApiResponse.success("Réponse récupérée avec succès", candidateAnswer);
        when(answerService.getAnswerBySessionAndQuestion(2L, 3L)).thenReturn(response);

        mockMvc.perform(get("/api/answers/session/2/question/3"))
                .andExpect(status().isOk());

        verify(answerService, times(1)).getAnswerBySessionAndQuestion(2L, 3L);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getAnswerBySessionAndQuestion_ShouldHandleMultipleRequests() throws Exception {
        ApiResponse response1 = ApiResponse.success("Réponse récupérée avec succès", candidateAnswer);
        ApiResponse response2 = ApiResponse.success("Réponse récupérée avec succès", candidateAnswer);

        when(answerService.getAnswerBySessionAndQuestion(1L, 1L)).thenReturn(response1);
        when(answerService.getAnswerBySessionAndQuestion(1L, 2L)).thenReturn(response2);

        mockMvc.perform(get("/api/answers/session/1/question/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/answers/session/1/question/2"))
                .andExpect(status().isOk());

        verify(answerService, times(1)).getAnswerBySessionAndQuestion(1L, 1L);
        verify(answerService, times(1)).getAnswerBySessionAndQuestion(1L, 2L);
    }
}