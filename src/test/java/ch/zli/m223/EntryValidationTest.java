package ch.zli.m223;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

/**
 * Tests for the DTO validation rules.
 *
 * Required fields are checked by @NotNull, the order of the two timestamps by
 * @AssertTrue. A violated rule is answered by Quarkus with 400 Bad Request.
 *
 * Tests that create an entry remove it again, so they stay independent of the
 * execution order.
 */
@QuarkusTest
public class EntryValidationTest {

    private static final String VALID = """
        {"checkIn":"2026-11-05T08:00:00","checkOut":"2026-11-05T17:00:00"}
        """;

    @Test
    @DisplayName("A valid entry is accepted")
    public void validEntryIsAccepted() {
        Long id = given()
            .contentType("application/json").body(VALID)
            .when().post("/entries")
            .then().statusCode(200).extract().jsonPath().getLong("id");

        given().when().delete("/entries/" + id).then().statusCode(204);
    }

    @Test
    @DisplayName("Missing check-in is rejected with 400")
    public void missingCheckInIsRejected() {
        given()
          .contentType("application/json")
          .body("""
              {"checkOut":"2026-11-05T17:00:00"}
              """)
          .when().post("/entries")
          .then().statusCode(400);
    }

    @Test
    @DisplayName("Missing check-out is rejected with 400")
    public void missingCheckOutIsRejected() {
        given()
          .contentType("application/json")
          .body("""
              {"checkIn":"2026-11-05T08:00:00"}
              """)
          .when().post("/entries")
          .then().statusCode(400);
    }

    @Test
    @DisplayName("check-in explicitly null is rejected with 400")
    public void explicitNullCheckInIsRejected() {
        given()
          .contentType("application/json")
          .body("""
              {"checkIn":null,"checkOut":"2026-11-05T17:00:00"}
              """)
          .when().post("/entries")
          .then().statusCode(400);
    }

    @Test
    @DisplayName("check-in after check-out is rejected with 400")
    public void checkInAfterCheckOutIsRejected() {
        given()
          .contentType("application/json")
          .body("""
              {"checkIn":"2026-11-05T18:00:00","checkOut":"2026-11-05T08:00:00"}
              """)
          .when().post("/entries")
          .then().statusCode(400);
    }

    @Test
    @DisplayName("check-in equal to check-out is rejected with 400")
    public void equalTimestampsAreRejected() {
        given()
          .contentType("application/json")
          .body("""
              {"checkIn":"2026-11-05T08:00:00","checkOut":"2026-11-05T08:00:00"}
              """)
          .when().post("/entries")
          .then().statusCode(400);
    }

    @Test
    @DisplayName("An empty request body is rejected with 400")
    public void emptyBodyIsRejected() {
        given()
          .contentType("application/json")
          .body("{}")
          .when().post("/entries")
          .then().statusCode(400);
    }

    @Test
    @DisplayName("PUT validates the same rules")
    public void updateValidatesTheSameRules() {
        Long id = given()
            .contentType("application/json").body(VALID)
            .when().post("/entries")
            .then().statusCode(200).extract().jsonPath().getLong("id");

        given()
          .contentType("application/json")
          .body("""
              {"checkIn":"2026-11-05T20:00:00","checkOut":"2026-11-05T09:00:00"}
              """)
          .when().put("/entries/" + id)
          .then().statusCode(400);

        given().when().delete("/entries/" + id).then().statusCode(204);
    }

    @Test
    @DisplayName("An id sent by the client is ignored")
    public void clientSuppliedIdIsIgnored() {
        Long id = given()
            .contentType("application/json")
            .body("""
                {"id":999999,"checkIn":"2026-11-05T08:00:00","checkOut":"2026-11-05T17:00:00"}
                """)
            .when().post("/entries")
            .then()
                .statusCode(200)
                .body("id", not(is(999999)))
                .extract().jsonPath().getLong("id");

        given().when().delete("/entries/" + id).then().statusCode(204);
    }
}
