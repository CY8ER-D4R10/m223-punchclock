package ch.zli.m223.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry with its relations spelled out instead of referenced by id.
 *
 * Where {@link EntryDto} carries {@code categoryId} and {@code tagIds}, this
 * one nests the full {@link CategoryDto} and {@link TagDto}. A client that only
 * wants to display an entry therefore needs a single request instead of one per
 * referenced object.
 *
 * <p>The nested DTOs deliberately carry no back reference to entries — that
 * would turn the JSON into an endless loop.</p>
 *
 * <p>This DTO is read-only: it is never accepted as a request body, which is
 * why it needs no validation annotations.</p>
 */
public class EntryDetailDto {

    private Long id;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;

    /** {@code null} when the entry has no category. */
    private CategoryDto category;

    /** Empty list when the entry has no tags, never {@code null}. */
    private List<TagDto> tags = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDateTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }

    public CategoryDto getCategory() {
        return category;
    }

    public void setCategory(CategoryDto category) {
        this.category = category;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags == null ? new ArrayList<>() : tags;
    }
}
