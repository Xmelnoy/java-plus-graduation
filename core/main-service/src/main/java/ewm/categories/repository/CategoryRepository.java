package ewm.categories.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ewm.categories.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}