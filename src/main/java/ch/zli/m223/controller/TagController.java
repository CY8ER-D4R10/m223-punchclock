package ch.zli.m223.controller;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import ch.zli.m223.dto.TagDto;
import ch.zli.m223.mapper.TagMapper;
import ch.zli.m223.model.Tag;
import ch.zli.m223.service.TagService;

/** CRUD for tags. */
@Path("/tags")
@org.eclipse.microprofile.openapi.annotations.tags.Tag(
        name = "Tags", description = "Handling of tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TagController {

    @Inject
    TagService tagService;

    @Inject
    TagMapper tagMapper;

    @GET
    @Operation(summary = "Index all tags.", description = "Returns a list of all tags.")
    public List<TagDto> index() {
        return tagService.findAll().stream().map(tagMapper::toDto).toList();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Shows a tag.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The tag."),
        @APIResponse(responseCode = "404", description = "No tag exists with the given id.")
    })
    public Response show(@PathParam("id") Long id) {
        Tag tag = tagService.findById(id);
        if (tag == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(tagMapper.toDto(tag)).build();
    }

    @POST
    @Operation(summary = "Creates a new tag.")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "The created tag."),
        @APIResponse(responseCode = "400", description = "The request body is missing or the title is blank.")
    })
    public Response create(@NotNull @Valid TagDto dto) {
        Tag created = tagService.createTag(tagMapper.toEntity(dto));
        return Response.status(Response.Status.CREATED).entity(tagMapper.toDto(created)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Updates a tag.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The updated tag."),
        @APIResponse(responseCode = "400", description = "The request body is missing or the title is blank."),
        @APIResponse(responseCode = "404", description = "No tag exists with the given id.")
    })
    public Response update(@PathParam("id") Long id, @NotNull @Valid TagDto dto) {
        Tag updated = tagService.updateTag(id, tagMapper.toEntity(dto));
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(tagMapper.toDto(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deletes a tag.",
               description = "Only possible while no entry references the tag.")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "The tag was deleted."),
        @APIResponse(responseCode = "404", description = "No tag exists with the given id."),
        @APIResponse(responseCode = "409", description = "The tag is still assigned to entries.")
    })
    public Response delete(@PathParam("id") Long id) {
        if (tagService.findById(id) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (tagService.isInUse(id)) {
            return Response.status(Response.Status.CONFLICT).build();
        }
        tagService.deleteTag(id);
        return Response.noContent().build();
    }
}
