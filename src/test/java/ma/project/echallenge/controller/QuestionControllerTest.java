package ma.project.echallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.project.echallenge.dto.request.QuestionRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.QuestionResponse;
import ma.project.echallenge.entity.QuestionType;
import ma.project.echallenge.service.QuestionService;
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
@WithMockUser(roles = "ADMIN")
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuestionService questionService;

    private QuestionRequest validQuestionRequest;
    private QuestionResponse questionResponse1;
    private QuestionResponse questionResponse2;

    @BeforeEach
    void setUp() {
        // Setup valid question request
        validQuestionRequest = new QuestionRequest();
        validQuestionRequest.setText("What is polymorphism?");
        validQuestionRequest.setType(QuestionType.MULTIPLE_CHOICE);
        validQuestionRequest.setTestId(1L);

        // Setup options
        QuestionRequest.OptionRequest option1 = new QuestionRequest.OptionRequest();
        option1.setOptionText("A programming concept");
        option1.setIsCorrect(true);

        QuestionRequest.OptionRequest option2 = new QuestionRequest.OptionRequest();
        option2.setOptionText("A data type");
        option2.setIsCorrect(false);

        validQuestionRequest.setOptions(Arrays.asList(option1, option2));

        // Setup question responses
        questionResponse1 = new QuestionResponse();
        questionResponse1.setId(1L);
        questionResponse1.setText("What is polymorphism?");
        questionResponse1.setType(QuestionType.MULTIPLE_CHOICE);

        questionResponse2 = new QuestionResponse();
        questionResponse2.setId(2L);
        questionResponse2.setText("What is inheritance?");
        questionResponse2.setType(QuestionType.TRUE_FALSE);
    }

    // ==================== CREATE QUESTION TESTS ====================

    @Test
    void createQuestion_ShouldReturnCreatedQuestion_WhenRequestValid() throws Exception {
        ApiResponse response = ApiResponse.success("Question créée avec succès", questionResponse1);
        when(questionService.createQuestion(any(QuestionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validQuestionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.text").value("What is polymorphism?"))
                .andExpect(jsonPath("$.data.type").value("MULTIPLE_CHOICE"));

        verify(questionService, times(1)).createQuestion(any(QuestionRequest.class));
    }

    @Test
    void createQuestion_ShouldReturn400_WhenTextIsBlank() throws Exception {
        validQuestionRequest.setText("");

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validQuestionRequest)))
                .andExpect(status().isBadRequest());

        verify(questionService, never()).createQuestion(any());
    }

    @Test
    void createQuestion_ShouldReturn400_WhenTextIsNull() throws Exception {
        validQuestionRequest.setText(null);

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validQuestionRequest)))
                .andExpect(status().isBadRequest());

        verify(questionService, never()).createQuestion(any());
    }

    @Test
    void createQuestion_ShouldReturn400_WhenTypeIsNull() throws Exception {
        validQuestionRequest.setType(null);

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validQuestionRequest)))
                .andExpect(status().isBadRequest());

        verify(questionService, never()).createQuestion(any());
    }

    @Test
    void createQuestion_ShouldReturn400_WhenTestIdIsNull() throws Exception {
        validQuestionRequest.setTestId(null);

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validQuestionRequest)))
                .andExpect(status().isBadRequest());

        verify(questionService, never()).createQuestion(any());
    }

    @Test
    void createQuestion_ShouldAcceptNullOptions() throws Exception {
        validQuestionRequest.setOptions(null);
        ApiResponse response = ApiResponse.success("Question créée avec succès", questionResponse1);
        when(questionService.createQuestion(any(QuestionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validQuestionRequest)))
                .andExpect(status().isOk());

        verify(questionService, times(1)).createQuestion(any());
    }

    // ==================== GET QUESTIONS BY TEST TESTS ====================

    @Test
    void getQuestionsByTest_ShouldReturnQuestions_WhenTestExists() throws Exception {
        List<QuestionResponse> questions = Arrays.asList(questionResponse1, questionResponse2);
        ApiResponse response = ApiResponse.success("Questions récupérées avec succès", questions);
        when(questionService.getQuestionsByTestId(1L, false)).thenReturn(response);

        mockMvc.perform(get("/api/questions/test/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(questionService, times(1)).getQuestionsByTestId(1L, false);
    }

    @Test
    void getQuestionsByTest_ShouldReturnEmptyList_WhenNoQuestions() throws Exception {
        ApiResponse response = ApiResponse.success("Questions récupérées avec succès", Arrays.asList());
        when(questionService.getQuestionsByTestId(1L, false)).thenReturn(response);

        mockMvc.perform(get("/api/questions/test/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(questionService, times(1)).getQuestionsByTestId(1L, false);
    }

    @Test
    void getQuestionsByTest_ShouldUseDefaultIncludeCorrect_WhenNotProvided() throws Exception {
        ApiResponse response = ApiResponse.success("Questions récupérées avec succès", Arrays.asList());
        when(questionService.getQuestionsByTestId(1L, false)).thenReturn(response);

        mockMvc.perform(get("/api/questions/test/1"))
                .andExpect(status().isOk());

        verify(questionService, times(1)).getQuestionsByTestId(1L, false);
    }

    @Test
    void getQuestionsByTest_ShouldIncludeCorrectAnswers_WhenIncludeCorrectIsTrue() throws Exception {
        ApiResponse response = ApiResponse.success("Questions récupérées avec succès", Arrays.asList());
        when(questionService.getQuestionsByTestId(1L, true)).thenReturn(response);

        mockMvc.perform(get("/api/questions/test/1")
                        .param("includeCorrect", "true"))
                .andExpect(status().isOk());

        verify(questionService, times(1)).getQuestionsByTestId(1L, true);
    }

    @Test
    void getQuestionsByTest_ShouldCallServiceWithCorrectTestId() throws Exception {
        ApiResponse response = ApiResponse.success("Questions récupérées avec succès", Arrays.asList());
        when(questionService.getQuestionsByTestId(2L, false)).thenReturn(response);

        mockMvc.perform(get("/api/questions/test/2"))
                .andExpect(status().isOk());

        verify(questionService, times(1)).getQuestionsByTestId(2L, false);
    }

    // ==================== DELETE QUESTION TESTS ====================

    @Test
    void deleteQuestion_ShouldReturnSuccess_WhenIdExists() throws Exception {
        ApiResponse response = ApiResponse.success("Question supprimée avec succès", null);
        when(questionService.deleteQuestion(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(questionService, times(1)).deleteQuestion(1L);
    }

    @Test
    void deleteQuestion_ShouldCallServiceWithCorrectId() throws Exception {
        ApiResponse response = ApiResponse.success("Question supprimée avec succès", null);
        when(questionService.deleteQuestion(2L)).thenReturn(response);

        mockMvc.perform(delete("/api/questions/2"))
                .andExpect(status().isOk());

        verify(questionService, times(1)).deleteQuestion(2L);
    }

    @Test
    void deleteQuestion_ShouldHandleMultipleDeleteRequests() throws Exception {
        ApiResponse response = ApiResponse.success("Question supprimée avec succès", null);
        when(questionService.deleteQuestion(anyLong())).thenReturn(response);

        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/questions/2"))
                .andExpect(status().isOk());

        verify(questionService, times(1)).deleteQuestion(1L);
        verify(questionService, times(1)).deleteQuestion(2L);
    }
}