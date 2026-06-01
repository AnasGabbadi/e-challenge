package ma.project.echallenge.service;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.request.SubmitAnswerRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.entity.CandidateAnswer;
import ma.project.echallenge.entity.Question;
import ma.project.echallenge.entity.QuestionOption;
import ma.project.echallenge.entity.TestSession;
import ma.project.echallenge.exception.BadRequestException;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.CandidateAnswerRepository;
import ma.project.echallenge.repository.QuestionOptionRepository;
import ma.project.echallenge.repository.QuestionRepository;
import ma.project.echallenge.repository.TestSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final CandidateAnswerRepository answerRepository;
    private final TestSessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;

    @Transactional
    public ApiResponse<CandidateAnswer> submitAnswer(SubmitAnswerRequest request) {
        TestSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée"));

        if (session.getStatus() != TestSession.SessionStatus.STARTED) {
            throw new BadRequestException("La session n'est pas en cours");
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question non trouvée"));

        List<QuestionOption> selectedOptions = optionRepository.findAllById(request.getSelectedOptionIds());

        if (selectedOptions.isEmpty()) {
            throw new BadRequestException("Aucune option sélectionnée");
        }

        CandidateAnswer answer = answerRepository
                .findByTestSessionIdAndQuestionId(request.getSessionId(), request.getQuestionId())
                .orElse(null);

        if (answer != null) {
            answer.getSelectedOptions().clear();
            answer.getSelectedOptions().addAll(selectedOptions);
            answer.setAnsweredAt(LocalDateTime.now());

            CandidateAnswer updated = answerRepository.save(answer);
            return ApiResponse.success("Réponse mise à jour", updated);
        } else {
            CandidateAnswer newAnswer = new CandidateAnswer();
            newAnswer.setTestSession(session);
            newAnswer.setQuestion(question);
            newAnswer.setSelectedOptions(selectedOptions);
            newAnswer.setAnsweredAt(LocalDateTime.now());

            CandidateAnswer saved = answerRepository.save(newAnswer);
            return ApiResponse.success("Réponse enregistrée", saved);
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<CandidateAnswer>> getAnswersBySession(Long sessionId) {
        TestSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée"));

        if (session.getStatus() != TestSession.SessionStatus.STARTED &&
                session.getStatus() != TestSession.SessionStatus.COMPLETED) {
            throw new BadRequestException("Aucune réponse disponible pour cette session");
        }

        List<CandidateAnswer> answers = answerRepository.findByTestSessionId(sessionId);
        return ApiResponse.success("Réponses récupérées", answers);
    }

    @Transactional(readOnly = true)
    public ApiResponse<CandidateAnswer> getAnswerBySessionAndQuestion(Long sessionId, Long questionId) {
        CandidateAnswer answer = answerRepository
                .findByTestSessionIdAndQuestionId(sessionId, questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Réponse non trouvée"));

        return ApiResponse.success("Réponse récupérée", answer);
    }
}