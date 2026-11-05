package ewm.compilations.repository;

import ewm.compilations.model.Compilation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("""
            SELECT c
            FROM Compilation c
            WHERE (:pinned IS NULL OR c.pinned = :pinned)
            """)
    List<Compilation> findAllByPinned(@Param("pinned") Boolean pinned, Pageable pageable);
}
