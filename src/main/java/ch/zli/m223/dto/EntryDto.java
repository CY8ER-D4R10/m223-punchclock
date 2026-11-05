package ch.zli.m223.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Data Transfer Object for {@link ch.zli.m223.model.Entry}.
 *
 * The DTO is the model of the REST interface, the entity is the model of the
 * database. Relations are exchanged as <b>ids</b>, not as nested objects: that
 * keeps the payload small and avoids back references. The nested form is
 * offered separately by {@link EntryDetailDto}.
 */
public class EntryDto {

    /**
     * The id is assigned by the database. READ_ONLY means it is written to the
     * JSON response but ignored when a request is read, so a client cannot set
     * or change it.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(readOnly = true)
    private Long id;

    @NotNull(message = "Check-in is required")
    private LocalDateTime checkIn;

    @NotNull(message = "Check-out is required")
    private LocalDateTime checkOut;

    /** Optional. {@code null} means the entry has no category. */
    private Long categoryId;

    /**
     * Ids of the assigned tags. Never {@code null} in a response; an empty list
     * means the entry has no tags. On PUT the given list <b>replaces</b> all
     * assignments.
     */
    private List<Long> tagIds = new ArrayList<>();

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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds == null ? new ArrayList<>() : tagIds;
    }

    /**
     * Rule across two fields: check-in has to be before check-out.
     *
     * {@code @AssertTrue} requires this method to return true.
     * {@code @JsonIgnore} keeps it out of the JSON, otherwise the response would
     * contain an extra field "checkInBeforeCheckOut".
     *
     * Missing values return true on purpose - those are already reported by
     * {@code @NotNull}, and reporting them twice would show the user two errors
     * for one problem.
     */
    @AssertTrue(message = "Check-in must be before check-out")
    @JsonIgnore
    public boolean isCheckInBeforeCheckOut() {
        if (checkIn == null || checkOut == null) {
            return true;
        }
        return checkIn.isBefore(checkOut);
    }
}
