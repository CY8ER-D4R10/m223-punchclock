package ch.zli.m223.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import ch.zli.m223.model.Tag;
import ch.zli.m223.repository.EntryRepository;
import ch.zli.m223.repository.TagRepository;

/** Business logic for tags. */
@ApplicationScoped
public class TagService {

    @Inject
    TagRepository tagRepository;

    @Inject
    EntryRepository entryRepository;

    public List<Tag> findAll() {
        return tagRepository.listAll();
    }

    /** @return the tag, or {@code null} if no tag has that id */
    public Tag findById(Long id) {
        return tagRepository.findById(id);
    }

    @Transactional
    public Tag createTag(Tag tag) {
        tagRepository.persist(tag);
        return tag;
    }

    /** @return the updated tag, or {@code null} if no tag has that id */
    @Transactional
    public Tag updateTag(Long id, Tag changes) {
        Tag existing = tagRepository.findById(id);
        if (existing == null) {
            return null;
        }
        existing.setTitle(changes.getTitle());
        return existing;
    }

    /**
     * A tag that is still assigned to entries must not be deleted — the rows in
     * the join table {@code entry_tag} would point at a tag that no longer
     * exists and the database would refuse the delete anyway.
     */
    @Transactional
    public boolean isInUse(Long id) {
        Tag tag = tagRepository.findById(id);
        // getEntries() is the inverse side of the n:m relation and loads lazily,
        // which is why this method needs a transaction. Navigating a ToMany path
        // in JPQL would require an explicit join - the mapped collection is the
        // simpler way here.
        return tag != null && !tag.getEntries().isEmpty();
    }

    @Transactional
    public boolean deleteTag(Long id) {
        return tagRepository.deleteById(id);
    }
}
