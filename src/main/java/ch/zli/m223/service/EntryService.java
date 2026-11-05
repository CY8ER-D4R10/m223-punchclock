package ch.zli.m223.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import ch.zli.m223.model.Category;
import ch.zli.m223.model.Entry;
import ch.zli.m223.model.Tag;
import ch.zli.m223.repository.CategoryRepository;
import ch.zli.m223.repository.EntryRepository;
import ch.zli.m223.repository.TagRepository;

@ApplicationScoped
public class EntryService {

    /**
     * Why a relation could not be applied. The controller turns this into a
     * status code; the service itself stays free of HTTP.
     */
    public enum RelationResult {
        OK,
        UNKNOWN_CATEGORY,
        UNKNOWN_TAG,
        UNKNOWN_ENTRY
    }

    public record EntryResult(RelationResult result, Entry entry) {
        static EntryResult failure(RelationResult result) {
            return new EntryResult(result, null);
        }
    }

    @Inject
    private EntryRepository entryRepository;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    TagRepository tagRepository;

    @Transactional
    public List<Entry> findAll() {
        return entryRepository.listAll();
    }

    /** @return the entry, or {@code null} if no entry has that id */
    @Transactional
    public Entry findById(Long id) {
        return entryRepository.findById(id);
    }

    /**
     * Creates an entry and assigns the referenced category and tags.
     *
     * @return the created entry, or the reason why a referenced id is unknown
     */
    @Transactional
    public EntryResult createEntry(Entry entry, Long categoryId, List<Long> tagIds) {
        RelationResult relations = applyRelations(entry, categoryId, tagIds);
        if (relations != RelationResult.OK) {
            return EntryResult.failure(relations);
        }
        entryRepository.persist(entry);
        return new EntryResult(RelationResult.OK, entry);
    }

    /**
     * Updates an existing entry.
     *
     * The entry loaded inside the transaction is managed by Hibernate. Changing
     * its fields is enough; the UPDATE statement is written automatically when
     * the transaction commits (dirty checking). An explicit persist() would be
     * wrong here, because the entity already exists.
     *
     * <p>PUT replaces the whole resource, so the given relations <b>replace</b>
     * the existing ones: {@code categoryId: null} removes the category and an
     * empty {@code tagIds} removes all tags.</p>
     */
    @Transactional
    public EntryResult updateEntry(Long id, Entry changes, Long categoryId, List<Long> tagIds) {
        Entry existing = entryRepository.findById(id);
        if (existing == null) {
            return EntryResult.failure(RelationResult.UNKNOWN_ENTRY);
        }
        RelationResult relations = applyRelations(existing, categoryId, tagIds);
        if (relations != RelationResult.OK) {
            return EntryResult.failure(relations);
        }
        existing.setCheckIn(changes.getCheckIn());
        existing.setCheckOut(changes.getCheckOut());
        return new EntryResult(RelationResult.OK, existing);
    }

    /**
     * Deletes the entry with the given id.
     *
     * @return {@code true} if an entry was deleted, {@code false} if none existed
     */
    @Transactional
    public boolean deleteEntry(Long id) {
        return entryRepository.deleteById(id);
    }

    /**
     * Resolves the given ids into managed entities and assigns them.
     *
     * Nothing is written here — the entry is either new or managed, so the
     * changes are persisted when the surrounding transaction commits.
     */
    private RelationResult applyRelations(Entry entry, Long categoryId, List<Long> tagIds) {
        if (categoryId == null) {
            entry.setCategory(null);
        } else {
            Category category = categoryRepository.findById(categoryId);
            if (category == null) {
                return RelationResult.UNKNOWN_CATEGORY;
            }
            entry.setCategory(category);
        }

        Set<Tag> tags = new HashSet<>();
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                Tag tag = tagRepository.findById(tagId);
                if (tag == null) {
                    return RelationResult.UNKNOWN_TAG;
                }
                tags.add(tag);
            }
        }
        entry.setTags(tags);
        return RelationResult.OK;
    }
}
