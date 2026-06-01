package ma.project.echallenge.repository;

import ma.project.echallenge.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTestId(Long testId);

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.test.id = :testId")
    List<Question> findByTestIdWithOptions(Long testId);
}