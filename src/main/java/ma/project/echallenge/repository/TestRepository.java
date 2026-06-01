package ma.project.echallenge.repository;

import ma.project.echallenge.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByThemeId(Long themeId);

    @Query("SELECT t FROM Test t LEFT JOIN FETCH t.questions WHERE t.id = :id")
    Optional<Test> findByIdWithQuestions(Long id);
}