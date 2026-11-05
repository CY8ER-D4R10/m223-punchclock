package ch.zli.m223.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import ch.zli.m223.model.Entry;
import ch.zli.m223.repository.EntryRepository;

@ApplicationScoped
public class EntryService {
    @Inject
    private EntryRepository entryRepository;

    @Transactional
    public Entry createEntry(Entry entry) {
        entryRepository.persist(entry);
        return entry;
    }

    public List<Entry> findAll() {
        return entryRepository.listAll();
    }

    /**
     * Updates an existing entry.
     *
     * The entry loaded inside the transaction is managed by Hibernate. Changing
     * its fields is enough; the UPDATE statement is written automatically when
     * the transaction commits (dirty checking). An explicit persist() would be
     * wrong here, because the entity already exists.
     *
     * @return the updated entry, or {@code null} if no entry has that id
     */
    @Transactional
    public Entry updateEntry(Long id, Entry changes) {
        Entry existing = entryRepository.findById(id);
        if (existing == null) {
            return null;
        }
        existing.setCheckIn(changes.getCheckIn());
        existing.setCheckOut(changes.getCheckOut());
        return existing;
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
}
