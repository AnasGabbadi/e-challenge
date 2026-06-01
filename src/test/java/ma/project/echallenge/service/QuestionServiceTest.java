package ma.project.echallenge.service;

import ma.project.echallenge.dto.request.QuestionRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.QuestionResponse;
import ma.project.echallenge.entity.Question;
import ma.project.echallenge.entity.QuestionOption;
import ma.project.echallenge.entity.QuestionType;
import ma.project.echallenge.entity.Test;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.QuestionRepository;
import ma.project.echallenge.repository.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
    private QuestionRequest questionRequest;
    private List<QuestionRequest.OptionRequest> optionRequests;

    @BeforeEach
    void setUp() {
        test = new Test();
        test.setId(1L);
        test.setTitle("Java Test");
        test.setDuration(60);

        QuestionOption option1 = new QuestionOption();
        option1.setId(1L);
        option1.setOptionText("Option A");
        option1.setIsCorrect(true);

        QuestionOption option2 = new QuestionOption();
        option2.setId(2L);
        option2.setOptionText("Option B");
        option2.setIsCorrect(false);

        question = new Question();
        question.setId(1L);
        question.setText("What is Java?");
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setTest(test);
        question.setOptions(Arrays.asList(option1, option2));

        // Create option requests
        QuestionRequest.OptionRequest optionReq1 = new QuestionRequest.OptionRequest();
        optionReq1.setOptionText("Option A");
        optionReq1.setIsCorrect(true);

        QuestionRequest.OptionRequest optionReq2 = new QuestionRequest.OptionRequest();
        optionReq2.setOptionText("Option B");
        optionReq2.setIsCorrect(false);

        optionRequests = Arrays.asList(optionReq1, optionReq2);

        questionRequest = new QuestionRequest();
        questionRequest.setText("What is Java?");
        questionRequest.setType(QuestionType.SINGLE_CHOICE);
        questionRequest.setTestId(1L);
        questionRequest.setOptions(optionRequests);
    }

    // ==================== CREATE TESTS ====================

    @org.junit.jupiter.api.Test
    void createQuestion_ShouldReturnSuccess_WhenValidRequest() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        // Act
        ApiResponse<QuestionResponse> response = questionService.createQuestion(questionRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Question créée avec succès", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("What is Java?", response.getData().getText());
        assertEquals(QuestionType.SINGLE_CHOICE, response.getData().getType());

        verify(testRepository, times(1)).findById(1L);
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @org.junit.jupiter.api.Test
    void createQuestion_ShouldThrowException_WhenTestNotFound() {
        // Arrange
        when(testRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> questionService.createQuestion(questionRequest)
        );

        assertEquals("Test non trouvé", exception.getMessage());

        verify(testRepository, times(1)).findById(1L);
        verify(questionRepository, never()).save(any(Question.class));
    }

    @org.junit.jupiter.api.Test
    void createQuestion_ShouldCreateWithOptions_WhenOptionsProvided() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question saved = invocation.getArgument(0);
            assertNotNull(saved.getOptions());
            assertEquals(2, saved.getOptions().size());
            assertEquals("Option A", saved.getOptions().get(0).getOptionText());
            assertTrue(saved.getOptions().get(0).getIsCorrect());
            assertEquals("Option B", saved.getOptions().get(1).getOptionText());
            assertFalse(saved.getOptions().get(1).getIsCorrect());
            return saved;
        });

        // Act
        questionService.createQuestion(questionRequest);

        // Assert
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @org.junit.jupiter.api.Test
    void createQuestion_ShouldCreateWithoutOptions_WhenOptionsNull() {
        // Arrange
        questionRequest.setOptions(null);
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        // Act
        ApiResponse<QuestionResponse> response = questionService.createQuestion(questionRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @org.junit.jupiter.api.Test
    void createQuestion_ShouldSetQuestionReference_InOptions() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question saved = invocation.getArgument(0);
            if (saved.getOptions() != null) {
                for (QuestionOption option : saved.getOptions()) {
                    assertEquals(saved, option.getQuestion());
                }
            }
            return saved;
        });

        // Act
        questionService.createQuestion(questionRequest);

        // Assert
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @org.junit.jupiter.api.Test
    void createQuestion_ShouldHandleAllQuestionTypes() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        // Test SINGLE_CHOICE
        questionRequest.setType(QuestionType.SINGLE_CHOICE);
        assertDoesNotThrow(() -> questionService.createQuestion(questionRequest));

        // Test MULTIPLE_CHOICE
        questionRequest.setType(QuestionType.MULTIPLE_CHOICE);
        assertDoesNotThrow(() -> questionService.createQuestion(questionRequest));

        // Test TRUE_FALSE
        questionRequest.setType(QuestionType.TRUE_FALSE);
        assertDoesNotThrow(() -> questionService.createQuestion(questionRequest));
    }

    @org.junit.jupiter.api.Test
    void createQuestion_ShouldHandleLongQuestionText() {
        // Arrange
        String longText = "Q".repeat(1000);
        questionRequest.setText(longText);
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        // Act
        ApiResponse<QuestionResponse> response = questionService.createQuestion(questionRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    // ==================== READ TESTS ====================

    @org.junit.jupiter.api.Test
    void getQuestionsByTestId_ShouldReturnList_WhenQuestionsExist() {
        // Arrange
        Question question2 = new Question();
        question2.setId(2L);
        question2.setText("What is OOP?");
        question2.setType(QuestionType.MULTIPLE_CHOICE);
        question2.setTest(test);
        question2.setOptions(new ArrayList<>());

        when(questionRepository.findByTestIdWithOptions(1L))
                .thenReturn(Arrays.asList(question, question2));

        // Act
        ApiResponse<List<QuestionResponse>> response = questionService.getQuestionsByTestId(1L, true);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Questions récupérées", response.getMessage());
        assertEquals(2, response.getData().size());
        assertEquals("What is Java?", response.getData().get(0).getText());
        assertEquals("What is OOP?", response.getData().get(1).getText());

        verify(questionRepository, times(1)).findByTestIdWithOptions(1L);
    }

    @org.junit.jupiter.api.Test
    void getQuestionsByTestId_ShouldReturnEmptyList_WhenNoQuestions() {
        // Arrange
        when(questionRepository.findByTestIdWithOptions(1L)).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<QuestionResponse>> response = questionService.getQuestionsByTestId(1L, true);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());

        verify(questionRepository, times(1)).findByTestIdWithOptions(1L);
    }

    @org.junit.jupiter.api.Test
    void getQuestionsByTestId_ShouldIncludeCorrectAnswers_WhenIncludeCorrectIsTrue() {
        // Arrange
        when(questionRepository.findByTestIdWithOptions(1L)).thenReturn(Arrays.asList(question));

        // Act
        ApiResponse<List<QuestionResponse>> response = questionService.getQuestionsByTestId(1L, true);

        // Assert
        assertNotNull(response.getData().get(0).getOptions());
        QuestionResponse.OptionResponse firstOption = response.getData().get(0).getOptions().get(0);
        assertNotNull(firstOption.getIsCorrect());
        assertTrue(firstOption.getIsCorrect());
    }

    @org.junit.jupiter.api.Test
    void getQuestionsByTestId_ShouldHideCorrectAnswers_WhenIncludeCorrectIsFalse() {
        // Arrange
        when(questionRepository.findByTestIdWithOptions(1L)).thenReturn(Arrays.asList(question));

        // Act
        ApiResponse<List<QuestionResponse>> response = questionService.getQuestionsByTestId(1L, false);

        // Assert
        assertNotNull(response.getData().get(0).getOptions());
        QuestionResponse.OptionResponse firstOption = response.getData().get(0).getOptions().get(0);
        assertNull(firstOption.getIsCorrect());
    }

    @org.junit.jupiter.api.Test
    void getQuestionsByTestId_ShouldMapAllOptions() {
        // Arrange
        when(questionRepository.findByTestIdWithOptions(1L)).thenReturn(Arrays.asList(question));

        // Act
        ApiResponse<List<QuestionResponse>> response = questionService.getQuestionsByTestId(1L, true);

        // Assert
        assertEquals(2, response.getData().get(0).getOptions().size());
        assertEquals("Option A", response.getData().get(0).getOptions().get(0).getOptionText());
        assertEquals("Option B", response.getData().get(0).getOptions().get(1).getOptionText());
    }

    // ==================== DELETE TESTS ====================

    @org.junit.jupiter.api.Test
    void deleteQuestion_ShouldReturnSuccess_WhenExists() {
        // Arrange
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        doNothing().when(questionRepository).delete(any(Question.class));

        // Act
        ApiResponse<String> response = questionService.deleteQuestion(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Question supprimée avec succès", response.getMessage());

        verify(questionRepository, times(1)).findById(1L);
        verify(questionRepository, times(1)).delete(question);
    }

    @org.junit.jupiter.api.Test
    void deleteQuestion_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(questionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> questionService.deleteQuestion(999L)
        );

        assertEquals("Question non trouvée", exception.getMessage());

        verify(questionRepository, times(1)).findById(999L);
        verify(questionRepository, never()).delete(any(Question.class));
    }

    // ==================== EDGE CASES ====================

    @org.junit.jupiter.api.Test
    void createQuestion_ShouldHandleEmptyOptionsList() {
        // Arrange
        questionRequest.setOptions(Collections.emptyList());
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        // Act
        ApiResponse<QuestionResponse> response = questionService.createQuestion(questionRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @org.junit.jupiter.api.Test
    void createQuestion_ShouldHandleManyOptions() {
        // Arrange
        List<QuestionRequest.OptionRequest> manyOptions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            QuestionRequest.OptionRequest opt = new QuestionRequest.OptionRequest();
            opt.setOptionText("Option " + i);
            opt.setIsCorrect(i == 0);
            manyOptions.add(opt);
        }
        questionRequest.setOptions(manyOptions);

        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        // Act
        ApiResponse<QuestionResponse> response = questionService.createQuestion(questionRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @org.junit.jupiter.api.Test
    void getQuestionsByTestId_ShouldHandleQuestionsWithoutOptions() {
        // Arrange
        question.setOptions(Collections.emptyList());
        when(questionRepository.findByTestIdWithOptions(1L)).thenReturn(Arrays.asList(question));

        // Act
        ApiResponse<List<QuestionResponse>> response = questionService.getQuestionsByTestId(1L, true);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().get(0).getOptions().isEmpty());
    }

    @org.junit.jupiter.api.Test
    void createQuestion_ShouldHandleMultipleCorrectAnswers() {
        // Arrange
        QuestionRequest.OptionRequest opt1 = new QuestionRequest.OptionRequest();
        opt1.setOptionText("Correct 1");
        opt1.setIsCorrect(true);

        QuestionRequest.OptionRequest opt2 = new QuestionRequest.OptionRequest();
        opt2.setOptionText("Correct 2");
        opt2.setIsCorrect(true);

        questionRequest.setType(QuestionType.MULTIPLE_CHOICE);
        questionRequest.setOptions(Arrays.asList(opt1, opt2));

        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        // Act
        ApiResponse<QuestionResponse> response = questionService.createQuestion(questionRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @org.junit.jupiter.api.Test
    void getQuestionsByTestId_ShouldReturnQuestionsInOrder() {
        // Arrange
        Question q1 = new Question();
        q1.setId(1L);
        q1.setText("Question 1");
        q1.setType(QuestionType.SINGLE_CHOICE);
        q1.setOptions(new ArrayList<>());

        Question q2 = new Question();
        q2.setId(2L);
        q2.setText("Question 2");
        q2.setType(QuestionType.SINGLE_CHOICE);
        q2.setOptions(new ArrayList<>());

        Question q3 = new Question();
        q3.setId(3L);
        q3.setText("Question 3");
        q3.setType(QuestionType.SINGLE_CHOICE);
        q3.setOptions(new ArrayList<>());

        when(questionRepository.findByTestIdWithOptions(1L))
                .thenReturn(Arrays.asList(q1, q2, q3));

        // Act
        ApiResponse<List<QuestionResponse>> response = questionService.getQuestionsByTestId(1L, true);

        // Assert
        assertEquals(3, response.getData().size());
        assertEquals("Question 1", response.getData().get(0).getText());
        assertEquals("Question 2", response.getData().get(1).getText());
        assertEquals("Question 3", response.getData().get(2).getText());
    }
}