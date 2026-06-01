package ma.project.echallenge.repository;

import ma.project.echallenge.entity.TestSession;
import ma.project.echallenge.entity.TestSession.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestSessionRepository extends JpaRepository<TestSession, Long> {
    Optional<TestSession> findBySessionCode(String sessionCode);
    List<TestSession> findByCandidateId(Long candidateId);
    List<TestSession> findByStatus(SessionStatus status);
    List<TestSession> findByCandidateIdAndStatus(Long candidateId, SessionStatus status);
}