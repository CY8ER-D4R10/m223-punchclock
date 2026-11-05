package ch.zli.m223.controller;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ch.zli.m223.dto.EntryDetailDto;
import ch.zli.m223.mapper.EntryMapper;
import ch.zli.m223.model.Entry;
import ch.zli.m223.service.EntryService;

/**
 * Entries with their relations nested instead of referenced by id.
 *
 * Read-only on purpose: writing stays with {@code /entries}, which keeps a
 * single place that accepts changes. Two endpoints that both create entries in
 * slightly different shapes would be a source of inconsistency.
 */
@Path("/entry-details")
@Tag(name = "Entry details", description = "Entries including category and tags")
@Produces(MediaType.APPLICATION_JSON)
public class EntryDetailController {

    @Inject
    EntryService entryService;

    @Inject
    EntryMapper entryMapper;

    @GET
    @Operation(summary = "Index all entries with their relations.",
               description = "Category is null when none is assigned, tags is an empty list.")
    public List<EntryDetailDto> index() {
        return entryService.findAll().stream().map(entryMapper::toDetailDto).toList();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Shows one entry with its relations.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The entry with category and tags."),
        @APIResponse(responseCode = "404", description = "No entry exists with the given id.")
    })
    public Response show(@PathParam("id") Long id) {
        Entry entry = entryService.findById(id);
        if (entry == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(entryMapper.toDetailDto(entry)).build();
    }
}
