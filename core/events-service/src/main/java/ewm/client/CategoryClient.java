package ewm.client;

import dto.CategoryDto;
import ewm.client.fallback.CategoryClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "category-service", path = "/categories", fallback = CategoryClientFallback.class)
public interface CategoryClient {

    @GetMapping("/{id}")
    CategoryDto getCategoryById(@PathVariable("id") Long id);

    @GetMapping
    List<CategoryDto> getCategoriesByIds(@RequestParam("ids") List<Long> ids);
}