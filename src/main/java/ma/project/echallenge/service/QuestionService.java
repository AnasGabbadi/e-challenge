package ma.project.echallenge.service;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.request.QuestionRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.QuestionResponse;
import ma.project.echallenge.entity.Question;
import ma.project.echallenge.entity.QuestionOption;
import ma.project.echallenge.entity.Test;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.QuestionRepository;
import ma.project.echallenge.repository.TestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TestRepository testRepository;

    @Transactional
    public ApiResponse<QuestionResponse> createQuestion(QuestionRequest request) {
        Test test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new ResourceNotFoundException("Test non trouvé"));

        Question question = new Question();
        question.setText(request.getText());
        question.setType(request.getType());
        question.setTest(test);

        // Add options
        if (request.getOptions() != null) {
            List<QuestionOption> options = request.getOptions().stream()
                    .map(opt -> {
                        QuestionOption option = new QuestionOption();
                        option.setOptionText(opt.getOptionText());
                        option.setIsCorrect(opt.getIsCorrect());
                        option.setQuestion(question);
                        return option;
                    })
                    .collect(Collectors.toList());

            question.setOptions(options);
        }

        Question savedQuestion = questionRepository.save(question);

        return ApiResponse.success("Question créée avec succès", mapToResponse(savedQuestion, true));
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<QuestionResponse>> getQuestionsByTestId(Long testId, boolean includeCorrect) {
        List<Question> questions = questionRepository.findByTestIdWithOptions(testId);

        List<QuestionResponse> responses = questions.stream()
                .map(q -> mapToResponse(q, includeCorrect))
                .collect(Collectors.toList());

        return ApiResponse.success("Questions récupérées", responses);
    }

    @Transactional
    public ApiResponse<String> deleteQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question non trouvée"));

        questionRepository.delete(question);

        return ApiResponse.success("Question supprimée avec succès", null);
    }

    private QuestionResponse mapToResponse(Question question, boolean includeCorrect) {
        List<QuestionResponse.OptionResponse> options = question.getOptions().stream()
                .map(opt -> new QuestionResponse.OptionResponse(
                        opt.getId(),
                        opt.getOptionText(),
                        includeCorrect ? opt.getIsCorrect() : null
                ))
                .collect(Collectors.toList());

        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getType(),
                options
        );
    }
}