package ma.customer.project;

import ma.project.echallenge.dto.request.QuestionRequest;
import ma.project.echallenge.dto.request.QuestionRequest.OptionRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.QuestionResponse;
import ma.project.echallenge.entity.Question;
import ma.project.echallenge.entity.QuestionOption;
import ma.project.echallenge.entity.Test;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.QuestionRepository;
import ma.project.echallenge.repository.TestRepository;
import ma.project.echallenge.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private TestRepository testRepository;

    @InjectMocks
    private QuestionService questionService;

    private Test test;
    private Question question;
    private QuestionOption option;

    @BeforeEach
    void setUp() {
        test = new Test();
        test.setId(1L);

        question = new Question();
        question.setId(1L);
        question.setText("Sample question");
        question.setType("MULTIPLE_CHOICE");
        question.setTest(test);

        option = new QuestionOption();
        option.setId(1L);
        option.setOptionText("Option 1");
        option.setIsCorrect(true);
        option.setQuestion(question);
        question.setOptions(List.of(option));
    }

    @Test
    @DisplayName("createQuestion(validRequest) should create and return question")
    void createQuestion_validRequest_shouldCreateAndReturnQuestion() {
        QuestionRequest request = new QuestionRequest();
        request.setTestId(1L);
        request.setText("Sample question");
        request.setType("MULTIPLE_CHOICE");

        OptionRequest optionRequest = new OptionRequest();
        optionRequest.setOptionText("Option 1");
        optionRequest.setIsCorrect(true);
        request.setOptions(List.of(optionRequest));

        when(testRepository.findById(anyLong())).thenReturn(Optional.of(test));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        ApiResponse<QuestionResponse> response = questionService.createQuestion(request);

        assertEquals("Question créée avec succès", response.getMessage());
        assertEquals(1L, response.getData().getId());
        assertEquals("Sample question", response.getData().getText());
    }

    @Test
    @DisplayName("createQuestion(nullTestId) should throw ResourceNotFoundException")
    void createQuestion_nullTestId_shouldThrowResourceNotFoundException() {
        QuestionRequest request = new QuestionRequest();
        request.setTestId(999L);
        request.setText("Sample question");
        request.setType("MULTIPLE_CHOICE");

        when(testRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> questionService.createQuestion(request));
    }

    @Test
    @DisplayName("getQuestionsByTestId(validTestId) should return questions")
    void getQuestionsByTestId_validTestId_shouldReturnQuestions() {
        when(questionRepository.findByTestIdWithOptions(anyLong())).thenReturn(List.of(question));

        ApiResponse<List<QuestionResponse>> response = questionService.getQuestionsByTestId(1L, true);

        assertEquals("Questions récupérées", response.getMessage());
        assertEquals(1, response.getData().size());
        assertEquals(1L, response.getData().get(0).getId());
    }

    @Test
    @DisplayName("getQuestionsByTestId(zeroTestId) should return empty list")
    void getQuestionsByTestId_zeroTestId_shouldReturnEmptyList() {
        when(questionRepository.findByTestIdWithOptions(0L)).thenReturn(List.of());

        ApiResponse<List<QuestionResponse>> response = questionService.getQuestionsByTestId(0L, true);

        assertEquals("Questions récupérées", response.getMessage());
        assertEquals(0, response.getData().size());
    }

    @Test
    @DisplayName("deleteQuestion(validId) should delete question")
    void deleteQuestion_validId_shouldDeleteQuestion() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(question));

        ApiResponse<String> response = questionService.deleteQuestion(1L);

        assertEquals("Question supprimée avec succès", response.getMessage());
    }

    @Test
    @DisplayName("deleteQuestion(nullId) should throw ResourceNotFoundException")
    void deleteQuestion_nullId_shouldThrowResourceNotFoundException() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> questionService.deleteQuestion(999L));
    }

    @Test
    @DisplayName("mapToResponse(question, true) should include correct options")
    void mapToResponse_questionIncludeCorrect_shouldIncludeCorrectOptions() {
        QuestionResponse response = questionService.mapToResponse(question, true);

        assertEquals(1, response.getOptions().size());
        assertEquals(true, response.getOptions().get(0).getIsCorrect());
    }

    @Test
    @DisplayName("mapToResponse(question, false) should exclude correct options")
    void mapToResponse_questionExcludeCorrect_shouldExcludeCorrectOptions() {
        QuestionResponse response = questionService.mapToResponse(question, false);

        assertEquals(1, response.getOptions().size());
        assertEquals(null, response.getOptions().get(0).getIsCorrect());
    }
}