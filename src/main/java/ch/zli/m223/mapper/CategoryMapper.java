package ch.zli.m223.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import ch.zli.m223.dto.CategoryDto;
import ch.zli.m223.model.Category;

/** Converts between {@link CategoryDto} and {@link Category}. */
@ApplicationScoped
public class CategoryMapper {

    public Category toEntity(CategoryDto dto) {
        Category category = new Category();
        category.setTitle(dto.getTitle());
        return category;
    }

    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDto(category.getId(), category.getTitle());
    }
}
