package ch.zli.m223.mapper;

import java.util.Comparator;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.zli.m223.dto.EntryDetailDto;
import ch.zli.m223.dto.EntryDto;
import ch.zli.m223.dto.TagDto;
import ch.zli.m223.model.Entry;
import ch.zli.m223.model.Tag;

/**
 * Converts between the entry DTOs and {@link Entry}.
 *
 * The mapper only copies <b>fields</b>. Resolving {@code categoryId} and
 * {@code tagIds} into real entities needs repository access and therefore
 * belongs to the service.
 */
@ApplicationScoped
public class EntryMapper {

    @Inject
    CategoryMapper categoryMapper;

    @Inject
    TagMapper tagMapper;

    /**
     * Builds an entity from a DTO.
     *
     * Neither the id nor the relations are copied: the id is assigned by the
     * database or taken from the URL path, and the relations are resolved by
     * the service.
     */
    public Entry toEntity(EntryDto dto) {
        Entry entry = new Entry();
        entry.setCheckIn(dto.getCheckIn());
        entry.setCheckOut(dto.getCheckOut());
        return entry;
    }

    /** Flat representation: relations as ids. */
    public EntryDto toDto(Entry entry) {
        EntryDto dto = new EntryDto();
        dto.setId(entry.getId());
        dto.setCheckIn(entry.getCheckIn());
        dto.setCheckOut(entry.getCheckOut());
        dto.setCategoryId(entry.getCategory() == null ? null : entry.getCategory().getId());
        dto.setTagIds(sortedTags(entry).stream().map(Tag::getId).toList());
        return dto;
    }

    /** Nested representation: relations as objects with id and title. */
    public EntryDetailDto toDetailDto(Entry entry) {
        EntryDetailDto dto = new EntryDetailDto();
        dto.setId(entry.getId());
        dto.setCheckIn(entry.getCheckIn());
        dto.setCheckOut(entry.getCheckOut());
        dto.setCategory(categoryMapper.toDto(entry.getCategory()));
        List<TagDto> tags = sortedTags(entry).stream().map(tagMapper::toDto).toList();
        dto.setTags(tags);
        return dto;
    }

    /**
     * Tags are stored in a {@code Set}, which has no defined order. Sorting by
     * id keeps the JSON output stable — otherwise the same entry could come
     * back with its tags in a different order on every request, which makes
     * both tests and clients unnecessarily brittle.
     */
    private List<Tag> sortedTags(Entry entry) {
        return entry.getTags().stream()
                .sorted(Comparator.comparing(Tag::getId))
                .toList();
    }
}
