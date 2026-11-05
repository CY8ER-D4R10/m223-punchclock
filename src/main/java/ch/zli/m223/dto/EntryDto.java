package ch.zli.m223.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Data Transfer Object for {@link ch.zli.m223.model.Entry}.
 *
 * The DTO is the model of the REST interface, the entity is the model of the
 * database. Keeping them apart means a change to the database schema does not
 * automatically change the API, and it is the DTO that carries the validation
 * rules.
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
