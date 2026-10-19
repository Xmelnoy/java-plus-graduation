package ewm.categories.mapper;

import org.mapstruct.Mapper;
import ewm.categories.dto.CategoryDto;
import ewm.categories.dto.NewCategoryDto;
import ewm.categories.model.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toCategoryDto(Category category);

    Category toCategory(NewCategoryDto newCategoryDto);

    List<CategoryDto> toCategoryDtoList(List<Category> categories);
}