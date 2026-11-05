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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ch.zli.m223.dto.CategoryDto;
import ch.zli.m223.mapper.CategoryMapper;
import ch.zli.m223.model.Category;
import ch.zli.m223.service.CategoryService;

/** CRUD for categories. */
@Path("/categories")
@Tag(name = "Categories", description = "Handling of categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryController {

    @Inject
    CategoryService categoryService;

    @Inject
    CategoryMapper categoryMapper;

    @GET
    @Operation(summary = "Index all categories.", description = "Returns a list of all categories.")
    public List<CategoryDto> index() {
        return categoryService.findAll().stream().map(categoryMapper::toDto).toList();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Shows a category.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The category."),
        @APIResponse(responseCode = "404", description = "No category exists with the given id.")
    })
    public Response show(@PathParam("id") Long id) {
        Category category = categoryService.findById(id);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(categoryMapper.toDto(category)).build();
    }

    @POST
    @Operation(summary = "Creates a new category.")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "The created category."),
        @APIResponse(responseCode = "400", description = "The request body is missing or the title is blank.")
    })
    public Response create(@NotNull @Valid CategoryDto dto) {
        Category created = categoryService.createCategory(categoryMapper.toEntity(dto));
        return Response.status(Response.Status.CREATED)
                .entity(categoryMapper.toDto(created)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Updates a category.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The updated category."),
        @APIResponse(responseCode = "400", description = "The request body is missing or the title is blank."),
        @APIResponse(responseCode = "404", description = "No category exists with the given id.")
    })
    public Response update(@PathParam("id") Long id, @NotNull @Valid CategoryDto dto) {
        Category updated = categoryService.updateCategory(id, categoryMapper.toEntity(dto));
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(categoryMapper.toDto(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deletes a category.",
               description = "Only possible while no entry references the category.")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "The category was deleted."),
        @APIResponse(responseCode = "404", description = "No category exists with the given id."),
        @APIResponse(responseCode = "409", description = "The category is still assigned to entries.")
    })
    public Response delete(@PathParam("id") Long id) {
        if (categoryService.findById(id) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (categoryService.isInUse(id)) {
            return Response.status(Response.Status.CONFLICT).build();
        }
        categoryService.deleteCategory(id);
        return Response.noContent().build();
    }
}
