package ma.project.echallenge.service;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.ResultResponse;
import ma.project.echallenge.entity.Result;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.ResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;

    @Transactional(readOnly = true)
    public ApiResponse<ResultResponse> getResultById(Long id) {
        Result result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Résultat non trouvé"));

        return ApiResponse.success("Résultat récupéré", mapToResponse(result));
    }

    @Transactional(readOnly = true)
    public ApiResponse<ResultResponse> getResultBySessionId(Long sessionId) {
        Result result = resultRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Résultat non trouvé pour cette session"));

        return ApiResponse.success("Résultat récupéré", mapToResponse(result));
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ResultResponse>> getResultsByCandidateId(Long candidateId) {
        List<Result> results = resultRepository.findByCandidateId(candidateId);

        List<ResultResponse> responses = results.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Résultats du candidat récupérés", responses);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ResultResponse>> getPassedResults() {
        List<Result> results = resultRepository.findByPassedTrue();

        List<ResultResponse> responses = results.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Résultats réussis récupérés", responses);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ResultResponse>> getFailedResults() {
        List<Result> results = resultRepository.findByPassedFalse();

        List<ResultResponse> responses = results.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Résultats échoués récupérés", responses);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ResultResponse>> getAllResults() {
        List<Result> results = resultRepository.findAll();

        List<ResultResponse> responses = results.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Tous les résultats récupérés", responses);
    }

    private ResultResponse mapToResponse(Result result) {
        return new ResultResponse(
                result.getId(),
                result.getSession().getId(),
                result.getCandidate().getFirstName() + " " + result.getCandidate().getLastName(),
                result.getSession().getTest().getTitle(),
                result.getScore(),
                result.getTotalQuestions(),
                result.getCorrectAnswers(),
                result.getPassed(),
                result.getCompletedAt()
        );
    }
}