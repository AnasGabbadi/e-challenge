package ma.project.echallenge.service;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.request.TestRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.TestResponse;
import ma.project.echallenge.entity.Test;
import ma.project.echallenge.entity.Theme;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.TestRepository;
import ma.project.echallenge.repository.ThemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public ApiResponse<TestResponse> createTest(TestRequest request) {
        Theme theme = themeRepository.findById(request.getThemeId())
                .orElseThrow(() -> new ResourceNotFoundException("Thème non trouvé"));

        Test test = new Test();
        test.setTitle(request.getTitle());
        test.setDescription(request.getDescription());
        test.setDuration(request.getDuration());
        test.setQuestionDuration(request.getQuestionDuration());
        test.setPassingScore(request.getPassingScore());
        test.setTheme(theme);

        Test savedTest = testRepository.save(test);

        return ApiResponse.success("Test créé avec succès", mapToResponse(savedTest));
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<TestResponse>> getAllTests() {
        List<TestResponse> tests = testRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Tests récupérés", tests);
    }

    @Transactional(readOnly = true)
    public ApiResponse<TestResponse> getTestById(Long id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test non trouvé"));

        return ApiResponse.success("Test récupéré", mapToResponse(test));
    }

    @Transactional
    public ApiResponse<TestResponse> updateTest(Long id, TestRequest request) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test non trouvé"));

        Theme theme = themeRepository.findById(request.getThemeId())
                .orElseThrow(() -> new ResourceNotFoundException("Thème non trouvé"));

        test.setTitle(request.getTitle());
        test.setDescription(request.getDescription());
        test.setDuration(request.getDuration());
        test.setQuestionDuration(request.getQuestionDuration());
        test.setPassingScore(request.getPassingScore());
        test.setTheme(theme);

        Test updatedTest = testRepository.save(test);

        return ApiResponse.success("Test mis à jour", mapToResponse(updatedTest));
    }

    @Transactional
    public ApiResponse<String> deleteTest(Long id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test non trouvé"));

        testRepository.delete(test);

        return ApiResponse.success("Test supprimé avec succès", null);
    }

    private TestResponse mapToResponse(Test test) {
        return new TestResponse(
                test.getId(),
                test.getTitle(),
                test.getDescription(),
                test.getDuration(),
                test.getQuestionDuration(),
                test.getPassingScore(),
                test.getTheme().getName(),
                test.getTheme().getId(),
                test.getQuestions().size(),
                test.getCreatedAt()
        );
    }
}