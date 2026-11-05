package ch.zli.m223.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import ch.zli.m223.dto.EntryDto;
import ch.zli.m223.model.Entry;

/**
 * Converts between {@link EntryDto} and {@link Entry}.
 *
 * The mapping lives in its own class rather than in the controller, so the
 * controller stays limited to HTTP concerns. As an {@code @ApplicationScoped}
 * bean it is injected like any other dependency.
 */
@ApplicationScoped
public class EntryMapper {

    /**
     * Builds an entity from a DTO.
     *
     * The id is deliberately not copied: for a new entry the database assigns
     * it, and for an update the id comes from the URL path, not from the body.
     */
    public Entry toEntity(EntryDto dto) {
        Entry entry = new Entry();
        entry.setCheckIn(dto.getCheckIn());
        entry.setCheckOut(dto.getCheckOut());
        return entry;
    }

    public EntryDto toDto(Entry entry) {
        EntryDto dto = new EntryDto();
        dto.setId(entry.getId());
        dto.setCheckIn(entry.getCheckIn());
        dto.setCheckOut(entry.getCheckOut());
        return dto;
    }
}
