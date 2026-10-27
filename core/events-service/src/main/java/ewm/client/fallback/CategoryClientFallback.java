package ewm.client.fallback;

import dto.CategoryDto;
import ewm.client.CategoryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CategoryClientFallback implements CategoryClient {

    @Override
    public CategoryDto getCategoryById(Long id) {
        log.warn("category-service недоступен. Возвращаем заглушку для категории id={}", id);
        CategoryDto fallback = new CategoryDto();
        fallback.setId(id);
        fallback.setName("Unknown Category");
        return fallback;
    }

    @Override
    public List<CategoryDto> getCategoriesByIds(List<Long> ids) {
        log.warn("category-service недоступен. Возвращаем заглушки для категорий");
        return ids.stream()
                .map(id -> {
                    CategoryDto category = new CategoryDto();
                    category.setId(id);
                    category.setName("Unknown Category");
                    return category;
                })
                .toList();
    }
}