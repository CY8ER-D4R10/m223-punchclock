package ch.zli.m223.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Schnittstellenmodell für {@link ch.zli.m223.model.Tag}.
 *
 * Wie {@link CategoryDto} ohne Rückverweis auf die Einträge.
 */
public class TagDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(readOnly = true)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    public TagDto() {
    }

    public TagDto(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
