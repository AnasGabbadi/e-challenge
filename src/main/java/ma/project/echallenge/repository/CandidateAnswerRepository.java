package ma.project.echallenge.repository;

import ma.project.echallenge.entity.CandidateAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateAnswerRepository extends JpaRepository<CandidateAnswer, Long> {

    Optional<CandidateAnswer> findByTestSessionIdAndQuestionId(Long testSessionId, Long questionId);

    List<CandidateAnswer> findByTestSessionId(Long testSessionId);

    long countByTestSessionId(Long testSessionId);

    boolean existsByTestSessionIdAndQuestionId(Long testSessionId, Long questionId);
}