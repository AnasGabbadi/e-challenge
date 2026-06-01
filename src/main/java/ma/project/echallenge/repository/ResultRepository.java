package ma.project.echallenge.repository;

import ma.project.echallenge.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    Optional<Result> findBySessionId(Long sessionId);
    List<Result> findByCandidateId(Long candidateId);
    List<Result> findByPassedTrue();
    List<Result> findByPassedFalse();
}