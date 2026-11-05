package ch.zli.m223;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

/**
 * Tests for the update and delete endpoints.
 *
 * Every test creates the entry it works on and removes it again, so the tests
 * do not depend on each other and do not rely on the table being empty.
 */
@QuarkusTest
public class EntryModificationTest {

    private Long createEntry(String checkIn, String checkOut) {
        return given()
            .contentType("application/json")
            .body("{\"checkIn\":\"" + checkIn + "\",\"checkOut\":\"" + checkOut + "\"}")
            .when().post("/entries")
            .then()
                .statusCode(200)
                .extract().jsonPath().getLong("id");
    }

    @Test
    @DisplayName("PUT /entries/{id} replaces check-in and check-out")
    public void updateReplacesValues() {
        Long id = createEntry("2026-09-29T08:00:00", "2026-09-29T17:00:00");

        given()
          .contentType("application/json")
          .body("""
              {"checkIn":"2026-09-29T09:15:00","checkOut":"2026-09-29T18:30:00"}
              """)
          .when().put("/entries/" + id)
          .then()
             .statusCode(200)
             .body("id", is(id.intValue()))
             .body("checkIn", is("2026-09-29T09:15:00"))
             .body("checkOut", is("2026-09-29T18:30:00"));

        // The change must be persisted, not only echoed back.
        given()
          .when().get("/entries")
          .then()
             .statusCode(200)
             .body("find { it.id == " + id + " }.checkIn", is("2026-09-29T09:15:00"))
             .body("find { it.id == " + id + " }.checkOut", is("2026-09-29T18:30:00"));

        given().when().delete("/entries/" + id).then().statusCode(204);
    }

    @Test
    @DisplayName("PUT /entries/{id} returns 404 for an unknown id")
    public void updateUnknownIdReturnsNotFound() {
        given()
          .contentType("application/json")
          .body("""
              {"checkIn":"2026-09-29T08:00:00","checkOut":"2026-09-29T17:00:00"}
              """)
          .when().put("/entries/99999999")
          .then()
             .statusCode(404);
    }

    @Test
    @DisplayName("DELETE /entries/{id} removes the entry and answers 204")
    public void deleteRemovesEntry() {
        Long id = createEntry("2026-10-01T08:00:00", "2026-10-01T16:00:00");

        given()
          .when().delete("/entries/" + id)
          .then()
             .statusCode(204);

        given()
          .when().get("/entries")
          .then()
             .statusCode(200)
             .body("findAll { it.id == " + id + " }.size()", is(0));
    }

    @Test
    @DisplayName("DELETE /entries/{id} returns 404 for an unknown id")
    public void deleteUnknownIdReturnsNotFound() {
        given()
          .when().delete("/entries/99999999")
          .then()
             .statusCode(404);
    }

    @Test
    @DisplayName("Deleting the same entry twice returns 404 on the second call")
    public void deleteTwiceReturnsNotFound() {
        Long id = createEntry("2026-10-02T07:30:00", "2026-10-02T12:00:00");

        given().when().delete("/entries/" + id).then().statusCode(204);
        given().when().delete("/entries/" + id).then().statusCode(404);
    }
}
