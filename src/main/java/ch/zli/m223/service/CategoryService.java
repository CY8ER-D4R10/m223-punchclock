package ch.zli.m223.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import ch.zli.m223.model.Category;
import ch.zli.m223.repository.CategoryRepository;
import ch.zli.m223.repository.EntryRepository;

/** Business logic for categories. */
@ApplicationScoped
public class CategoryService {

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    EntryRepository entryRepository;

    public List<Category> findAll() {
        return categoryRepository.listAll();
    }

    /** @return the category, or {@code null} if no category has that id */
    public Category findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional
    public Category createCategory(Category category) {
        categoryRepository.persist(category);
        return category;
    }

    /**
     * Replaces the title of an existing category.
     *
     * No explicit persist: the loaded entity is managed, Hibernate writes the
     * UPDATE on commit (dirty checking).
     *
     * @return the updated category, or {@code null} if no category has that id
     */
    @Transactional
    public Category updateCategory(Long id, Category changes) {
        Category existing = categoryRepository.findById(id);
        if (existing == null) {
            return null;
        }
        existing.setTitle(changes.getTitle());
        return existing;
    }

    /**
     * A category that is still assigned to entries must not be deleted — the
     * entries would be left pointing at a row that no longer exists.
     */
    public boolean isInUse(Long id) {
        // category is a ToOne relation, so the path can be navigated directly
        // in JPQL without an explicit join.
        return entryRepository.count("category.id = ?1", id) > 0;
    }

    @Transactional
    public boolean deleteCategory(Long id) {
        return categoryRepository.deleteById(id);
    }
}
