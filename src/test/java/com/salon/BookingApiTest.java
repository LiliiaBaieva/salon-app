package com.salon;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingApiTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    /** Унікальна дата для кожного тесту, щоб тести не заважали один одному. */
    private static String day(int offset) {
        return LocalDate.now().plusDays(offset).toString();
    }

    private static Map<String, Object> body(String phone, long serviceId, String date, String time) {
        Map<String, Object> m = new HashMap<>();
        m.put("lastName", "Коваленко");
        m.put("firstName", "Олена");
        m.put("phone", phone);
        m.put("email", "olena@example.com");
        m.put("serviceId", serviceId);
        m.put("date", date);
        m.put("time", time);
        return m;
    }

    private long serviceId(String name) {
        return given().get("/api/services").then().statusCode(200)
                .extract().jsonPath().getLong("find { it.name == '" + name + "' }.id");
    }

    @Test
    void catalogContainsFourServices() {
        given().get("/api/services")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(4))
                .body("name", hasItems("Чоловіча стрижка", "Жіноча стрижка", "Укладка", "Стрижка і укладка"));
    }

    @Test
    void emptyDayHasFortyThreeSlots() {
        long id = serviceId("Чоловіча стрижка");
        given().queryParam("serviceId", id).queryParam("date", day(100))
                .get("/api/slots")
                .then().statusCode(200)
                .body("size()", equalTo(43))
                .body("[0]", equalTo("09:00"));
    }

    @Test
    void createBookingReturnsConfirmationWithoutDiscount() {
        long id = serviceId("Чоловіча стрижка");
        given().contentType(ContentType.JSON).body(body("+380671110001", id, day(101), "10:00"))
                .post("/api/bookings")
                .then().statusCode(201)
                .body("startTime", equalTo("10:00"))
                .body("endTime", equalTo("10:30"))
                .body("discountPercent", equalTo(0))
                .body("finalPrice", comparesEqualTo(300.00f));
    }

    @Test
    void overlappingBookingIsRejected() {
        long id = serviceId("Стрижка і укладка");
        String date = day(102);
        given().contentType(ContentType.JSON).body(body("+380671110002", id, date, "10:00"))
                .post("/api/bookings").then().statusCode(201);
        given().contentType(ContentType.JSON).body(body("+380671110003", id, date, "10:30"))
                .post("/api/bookings").then().statusCode(409);
        given().contentType(ContentType.JSON).body(body("+380671110003", id, date, "11:00"))
                .post("/api/bookings").then().statusCode(201);
    }

    @Test
    void invalidPhoneIsRejected() {
        long id = serviceId("Укладка");
        given().contentType(ContentType.JSON).body(body("0501234567", id, day(103), "10:00"))
                .post("/api/bookings")
                .then().statusCode(400)
                .body("message", containsString("Телефон"));
    }

    @Test
    void pastDateIsRejected() {
        long id = serviceId("Укладка");
        given().contentType(ContentType.JSON).body(body("+380671110004", id, day(-1), "10:00"))
                .post("/api/bookings").then().statusCode(400);
    }

    @Test
    void timeAfterClosingIsRejected() {
        long id = serviceId("Стрижка і укладка");
        given().contentType(ContentType.JSON).body(body("+380671110005", id, day(104), "19:30"))
                .post("/api/bookings").then().statusCode(400);
    }

    @Test
    void fifthBookingOfClientGetsThreePercentDiscount() {
        long id = serviceId("Чоловіча стрижка");
        String phone = "+380671110006";
        String[] times = {"09:00", "09:30", "10:00", "10:30"};
        for (int i = 0; i < 4; i++) {
            given().contentType(ContentType.JSON).body(body(phone, id, day(105), times[i]))
                    .post("/api/bookings")
                    .then().statusCode(201).body("discountPercent", equalTo(0));
        }
        given().contentType(ContentType.JSON).body(body(phone, id, day(105), "11:00"))
                .post("/api/bookings")
                .then().statusCode(201)
                .body("discountPercent", equalTo(3))
                .body("finalPrice", comparesEqualTo(291.00f));
    }

    @Test
    void adminEndpointsRequireLogin() {
        given().get("/api/admin/bookings").then().statusCode(401);
        given().auth().preemptive().basic("admin", "wrong").get("/api/admin/bookings").then().statusCode(401);
        given().auth().preemptive().basic("admin", "admin123").get("/api/admin/bookings").then().statusCode(200);
    }

    @Test
    void adminCanEditAndDeleteBooking() {
        long id = serviceId("Укладка");
        String date = day(106);
        int bookingId = given().contentType(ContentType.JSON).body(body("+380671110007", id, date, "12:00"))
                .post("/api/bookings").then().statusCode(201).extract().path("id");

        given().auth().preemptive().basic("admin", "admin123")
                .contentType(ContentType.JSON).body(body("+380671110007", id, date, "13:00"))
                .put("/api/admin/bookings/" + bookingId)
                .then().statusCode(200).body("startTime", equalTo("13:00"));

        given().auth().preemptive().basic("admin", "admin123")
                .delete("/api/admin/bookings/" + bookingId)
                .then().statusCode(204);

        given().auth().preemptive().basic("admin", "admin123")
                .get("/api/admin/bookings/" + bookingId)
                .then().statusCode(404);
    }

    @Test
    void historyFiltersByDateAndClient() {
        long id = serviceId("Жіноча стрижка");
        String date = day(107);
        given().contentType(ContentType.JSON).body(body("+380671110008", id, date, "15:00"))
                .post("/api/bookings").then().statusCode(201);

        given().auth().preemptive().basic("admin", "admin123")
                .queryParam("from", date).queryParam("to", date).queryParam("client", "+380671110008")
                .get("/api/admin/bookings")
                .then().statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].serviceName", equalTo("Жіноча стрижка"));
    }

    @Test
    void serviceWithBookingsCannotBeDeleted() {
        long id = serviceId("Укладка");
        given().contentType(ContentType.JSON).body(body("+380671110009", id, day(108), "09:00"))
                .post("/api/bookings").then().statusCode(201);
        given().auth().preemptive().basic("admin", "admin123")
                .delete("/api/admin/services/" + id)
                .then().statusCode(409);
    }

    @Test
    void adminCanAddService() {
        Map<String, Object> s = new HashMap<>();
        s.put("name", "Фарбування " + System.nanoTime());
        s.put("durationMin", 60);
        s.put("price", 1200);
        given().auth().preemptive().basic("admin", "admin123")
                .contentType(ContentType.JSON).body(s)
                .post("/api/admin/services")
                .then().statusCode(201).body("durationMin", equalTo(60));
    }
}
