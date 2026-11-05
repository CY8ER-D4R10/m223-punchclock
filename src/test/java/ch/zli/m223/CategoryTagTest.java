package ch.zli.m223;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * Tests for categories, tags and their assignment to entries.
 *
 * Every test creates what it needs and removes it again, so the tests stay
 * independent of each other and of the execution order. Clean-up happens in
 * reverse order: the entry first, then category and tag — a category that is
 * still in use cannot be deleted.
 */
@QuarkusTest
public class CategoryTagTest {

    private Long createCategory(String title) {
        return given().contentType("application/json")
            .body("{\"title\":\"" + title + "\"}")
            .when().post("/categories")
            .then().statusCode(201).extract().jsonPath().getLong("id");
    }

    private Long createTag(String title) {
        return given().contentType("application/json")
            .body("{\"title\":\"" + title + "\"}")
            .when().post("/tags")
            .then().statusCode(201).extract().jsonPath().getLong("id");
    }

    private Long createEntry(String body) {
        return given().contentType("application/json").body(body)
            .when().post("/entries")
            .then().statusCode(200).extract().jsonPath().getLong("id");
    }

    // --- Categories ---

    @Test
    @DisplayName("Category: create, read, update and delete")
    public void categoryLifecycle() {
        Long id = createCategory("Projektarbeit");

        given().when().get("/categories/" + id)
            .then().statusCode(200).body("title", is("Projektarbeit"));

        given().contentType("application/json")
            .body("""
                {"title":"Kundenarbeit"}
                """)
            .when().put("/categories/" + id)
            .then().statusCode(200).body("title", is("Kundenarbeit"));

        given().when().delete("/categories/" + id).then().statusCode(204);
        given().when().get("/categories/" + id).then().statusCode(404);
    }

    @Test
    @DisplayName("Category without a title is rejected with 400")
    public void categoryWithoutTitleIsRejected() {
        given().contentType("application/json").body("{}")
            .when().post("/categories")
            .then().statusCode(400);
    }

    @Test
    @DisplayName("Unknown category id answers 404 on GET, PUT and DELETE")
    public void unknownCategoryAnswers404() {
        given().when().get("/categories/99999999").then().statusCode(404);
        given().contentType("application/json").body("""
                {"title":"x"}
                """)
            .when().put("/categories/99999999").then().statusCode(404);
        given().when().delete("/categories/99999999").then().statusCode(404);
    }

    // --- Tags ---

    @Test
    @DisplayName("Tag: create, read, update and delete")
    public void tagLifecycle() {
        Long id = createTag("remote");

        given().when().get("/tags/" + id)
            .then().statusCode(200).body("title", is("remote"));

        given().contentType("application/json")
            .body("""
                {"title":"homeoffice"}
                """)
            .when().put("/tags/" + id)
            .then().statusCode(200).body("title", is("homeoffice"));

        given().when().delete("/tags/" + id).then().statusCode(204);
        given().when().get("/tags/" + id).then().statusCode(404);
    }

    @Test
    @DisplayName("Tag without a title is rejected with 400")
    public void tagWithoutTitleIsRejected() {
        given().contentType("application/json").body("{}")
            .when().post("/tags")
            .then().statusCode(400);
    }

    // --- Relations on entries ---

    @Test
    @DisplayName("An entry carries the ids of its category and tags")
    public void entryCarriesRelationIds() {
        Long category = createCategory("Weiterbildung");
        Long tagA = createTag("kurs");
        Long tagB = createTag("intern");

        Long entry = createEntry("""
            {"checkIn":"2026-11-05T08:00:00","checkOut":"2026-11-05T17:00:00",
             "categoryId":%d,"tagIds":[%d,%d]}
            """.formatted(category, tagA, tagB));

        given().when().get("/entries")
            .then().statusCode(200)
            .body("find { it.id == " + entry + " }.categoryId", is(category.intValue()))
            .body("find { it.id == " + entry + " }.tagIds.size()", is(2));

        given().when().delete("/entries/" + entry).then().statusCode(204);
        given().when().delete("/tags/" + tagA).then().statusCode(204);
        given().when().delete("/tags/" + tagB).then().statusCode(204);
        given().when().delete("/categories/" + category).then().statusCode(204);
    }

    @Test
    @DisplayName("An entry without relations has no category and an empty tag list")
    public void entryWithoutRelations() {
        Long entry = createEntry("""
            {"checkIn":"2026-11-06T08:00:00","checkOut":"2026-11-06T12:00:00"}
            """);

        given().when().get("/entry-details/" + entry)
            .then().statusCode(200)
            .body("category", nullValue())
            .body("tags.size()", is(0));

        given().when().delete("/entries/" + entry).then().statusCode(204);
    }

    @Test
    @DisplayName("An unknown category id on an entry answers 404")
    public void unknownCategoryOnEntryAnswers404() {
        given().contentType("application/json")
            .body("""
                {"checkIn":"2026-11-05T08:00:00","checkOut":"2026-11-05T17:00:00",
                 "categoryId":99999999}
                """)
            .when().post("/entries")
            .then().statusCode(404);
    }

    @Test
    @DisplayName("An unknown tag id on an entry answers 404")
    public void unknownTagOnEntryAnswers404() {
        given().contentType("application/json")
            .body("""
                {"checkIn":"2026-11-05T08:00:00","checkOut":"2026-11-05T17:00:00",
                 "tagIds":[99999999]}
                """)
            .when().post("/entries")
            .then().statusCode(404);
    }

    @Test
    @DisplayName("PUT replaces the relations: null removes the category, an empty list removes the tags")
    public void updateReplacesRelations() {
        Long category = createCategory("Support");
        Long tag = createTag("ticket");

        Long entry = createEntry("""
            {"checkIn":"2026-11-07T08:00:00","checkOut":"2026-11-07T17:00:00",
             "categoryId":%d,"tagIds":[%d]}
            """.formatted(category, tag));

        given().contentType("application/json")
            .body("""
                {"checkIn":"2026-11-07T09:00:00","checkOut":"2026-11-07T18:00:00",
                 "categoryId":null,"tagIds":[]}
                """)
            .when().put("/entries/" + entry)
            .then().statusCode(200)
            .body("categoryId", nullValue())
            .body("tagIds.size()", is(0));

        given().when().delete("/entries/" + entry).then().statusCode(204);
        given().when().delete("/tags/" + tag).then().statusCode(204);
        given().when().delete("/categories/" + category).then().statusCode(204);
    }

    // --- Deleting something that is still in use ---

    @Test
    @DisplayName("A category that is still assigned cannot be deleted")
    public void categoryInUseCannotBeDeleted() {
        Long category = createCategory("InBenutzung");
        Long entry = createEntry("""
            {"checkIn":"2026-11-08T08:00:00","checkOut":"2026-11-08T17:00:00",
             "categoryId":%d}
            """.formatted(category));

        given().when().delete("/categories/" + category).then().statusCode(409);

        given().when().delete("/entries/" + entry).then().statusCode(204);
        given().when().delete("/categories/" + category).then().statusCode(204);
    }

    @Test
    @DisplayName("A tag that is still assigned cannot be deleted")
    public void tagInUseCannotBeDeleted() {
        Long tag = createTag("inbenutzung");
        Long entry = createEntry("""
            {"checkIn":"2026-11-09T08:00:00","checkOut":"2026-11-09T17:00:00",
             "tagIds":[%d]}
            """.formatted(tag));

        given().when().delete("/tags/" + tag).then().statusCode(409);

        given().when().delete("/entries/" + entry).then().statusCode(204);
        given().when().delete("/tags/" + tag).then().statusCode(204);
    }

    // --- Nested representation ---

    @Test
    @DisplayName("GET /entry-details nests category and tags with id and title")
    public void entryDetailsNestsRelations() {
        Long category = createCategory("Entwicklung");
        Long tag = createTag("backend");

        Long entry = createEntry("""
            {"checkIn":"2026-11-10T08:00:00","checkOut":"2026-11-10T17:00:00",
             "categoryId":%d,"tagIds":[%d]}
            """.formatted(category, tag));

        given().when().get("/entry-details/" + entry)
            .then().statusCode(200)
            .body("id", is(entry.intValue()))
            .body("category.id", is(category.intValue()))
            .body("category.title", is("Entwicklung"))
            .body("tags.size()", is(1))
            .body("tags[0].title", is("backend"));

        given().when().get("/entry-details")
            .then().statusCode(200)
            .body("find { it.id == " + entry + " }.category.title", is("Entwicklung"));

        given().when().delete("/entries/" + entry).then().statusCode(204);
        given().when().delete("/tags/" + tag).then().statusCode(204);
        given().when().delete("/categories/" + category).then().statusCode(204);
    }

    @Test
    @DisplayName("GET /entry-details/{id} answers 404 for an unknown id")
    public void entryDetailsUnknownIdAnswers404() {
        given().when().get("/entry-details/99999999").then().statusCode(404);
    }
}
