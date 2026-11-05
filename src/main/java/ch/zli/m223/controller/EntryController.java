package ch.zli.m223.controller;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ch.zli.m223.dto.EntryDto;
import ch.zli.m223.mapper.EntryMapper;
import ch.zli.m223.model.Entry;
import ch.zli.m223.service.EntryService;

/**
 * REST interface for entries.
 *
 * The interface exposes {@link EntryDto}, never the entity. Incoming DTOs are
 * validated by {@code @Valid} before the method runs; a violated rule is
 * answered by Quarkus with 400 Bad Request.
 */
@Path("/entries")
@Tag(name = "Entries", description = "Handling of entries")
public class EntryController {

    @Inject
    EntryService entryService;

    @Inject
    EntryMapper entryMapper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Index all Entries.", description = "Returns a list of all entries.")
    public List<EntryDto> index() {
        return entryService.findAll().stream()
                .map(entryMapper::toDto)
                .toList();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Creates a new entry.", description = "Creates a new entry and returns the newly added entry.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The created entry.",
            content = @Content(schema = @Schema(implementation = EntryDto.class))),
        @APIResponse(responseCode = "400", description = "The request body is missing or violates a validation rule.")
    })
    public EntryDto create(
            @RequestBody(description = "The entry to create.")
            @NotNull @Valid EntryDto dto) {
        Entry created = entryService.createEntry(entryMapper.toEntity(dto));
        return entryMapper.toDto(created);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Updates an entry.", description = "Replaces check-in and check-out of the entry with the given id.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The updated entry.",
            content = @Content(schema = @Schema(implementation = EntryDto.class))),
        @APIResponse(responseCode = "400", description = "The request body is missing or violates a validation rule."),
        @APIResponse(responseCode = "404", description = "No entry exists with the given id.")
    })
    public Response update(
            @PathParam("id") Long id,
            @RequestBody(description = "The new values for the entry.")
            @NotNull @Valid EntryDto dto) {
        Entry updated = entryService.updateEntry(id, entryMapper.toEntity(dto));
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(entryMapper.toDto(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deletes an entry.", description = "Deletes the entry with the given id.")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "The entry was deleted."),
        @APIResponse(responseCode = "404", description = "No entry exists with the given id.")
    })
    public Response delete(@PathParam("id") Long id) {
        if (!entryService.deleteEntry(id)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }
}
