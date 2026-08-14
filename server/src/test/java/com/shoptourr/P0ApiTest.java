package com.shoptourr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class P0ApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void authTripPurchaseHomeAndStatsFlow() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@voyage.test";
        String requestId = UUID.randomUUID().toString();

        MvcResult register = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-Id", requestId)
                        .content("""
                                {
                                  "displayName": "Dima",
                                  "email": "%s",
                                  "password": "secret1",
                                  "locale": "ru"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Request-Id", requestId))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.displayName").value("Dima"))
                .andReturn();

        String access = json(register, "accessToken");
        String refresh = json(register, "refreshToken");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "secret1",
                                  "deviceName": "Pixel"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(email));

        mockMvc.perform(get("/api/me").header("Authorization", bearer(access)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.preferredCurrency").value("RUB"))
                .andExpect(jsonPath("$.premiumPlan").value("FREE"))
                .andExpect(jsonPath("$.stats.tripsCount").value(0));

        mockMvc.perform(patch("/api/me/preferences")
                        .header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "preferredCurrency": "EUR",
                                  "theme": "DARK"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredCurrency").value("EUR"))
                .andExpect(jsonPath("$.theme").value("DARK"));

        mockMvc.perform(get("/api/me/app-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minAndroidBuild").value(greaterThan(0)))
                .andExpect(jsonPath("$.flags.exportPdf").isBoolean());

        String tripIdempotency = UUID.randomUUID().toString();
        MvcResult createdTrip = mockMvc.perform(post("/api/trips")
                        .header("Authorization", bearer(access))
                        .header("Idempotency-Key", tripIdempotency)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "city": "Lisbon",
                                  "country": "Portugal",
                                  "countryCode": "PT",
                                  "startDate": "2026-08-10",
                                  "endDate": "2026-08-20",
                                  "budget": { "amount": "1000.00", "currency": "EUR" }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.city").value("Lisbon"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.budget.amount").value("1000.00"))
                .andExpect(jsonPath("$.spent.amount").value("0.00"))
                .andExpect(jsonPath("$.defaultVatRatePercent").value("23.00"))
                .andExpect(jsonPath("$.travelers", hasSize(1)))
                .andExpect(jsonPath("$.travelers[0].isOwner").value(true))
                .andReturn();

        String tripId = json(createdTrip, "id");

        mockMvc.perform(post("/api/trips")
                        .header("Authorization", bearer(access))
                        .header("Idempotency-Key", tripIdempotency)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "city": "Lisbon",
                                  "country": "Portugal",
                                  "countryCode": "PT",
                                  "startDate": "2026-08-10",
                                  "endDate": "2026-08-20",
                                  "budget": { "amount": "1000.00", "currency": "EUR" }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tripId));

        mockMvc.perform(get("/api/trips/" + tripId).header("Authorization", bearer(access)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("Portugal"));

        MvcResult purchase = mockMvc.perform(post("/api/trips/" + tripId + "/purchases")
                        .header("Authorization", bearer(access))
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Pastel de nata",
                                  "category": "FOOD",
                                  "amount": { "amount": "23.00", "currency": "EUR" },
                                  "vatIncluded": true,
                                  "place": "Belem"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pastel de nata"))
                .andExpect(jsonPath("$.amount.amount").value("23.00"))
                .andExpect(jsonPath("$.vat.net").value("18.70"))
                .andExpect(jsonPath("$.vat.vat").value("4.30"))
                .andExpect(jsonPath("$.vat.gross").value("23.00"))
                .andReturn();

        String purchaseId = json(purchase, "id");

        mockMvc.perform(get("/api/trips/" + tripId + "/purchases")
                        .header("Authorization", bearer(access)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentTotal.amount").value("23.00"))
                .andExpect(jsonPath("$.remaining.amount").value("977.00"))
                .andExpect(jsonPath("$.days", hasSize(1)))
                .andExpect(jsonPath("$.days[0].items[0].id").value(purchaseId));

        mockMvc.perform(get("/api/home").header("Authorization", bearer(access)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.currentTrip.id").value(tripId))
                .andExpect(jsonPath("$.currentTrip.spent.amount").value("23.00"));

        mockMvc.perform(get("/api/trips/" + tripId + "/stats")
                        .header("Authorization", bearer(access)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent.amount").value("23.00"))
                .andExpect(jsonPath("$.onBudget").value(true))
                .andExpect(jsonPath("$.topCategory").value("FOOD"))
                .andExpect(jsonPath("$.byCategory[0].category").value("FOOD"));

        mockMvc.perform(patch("/api/trips/" + tripId + "/purchases/" + purchaseId)
                        .header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Pastel de nata x2" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pastel de nata x2"));

        mockMvc.perform(delete("/api/trips/" + tripId + "/purchases/" + purchaseId)
                        .header("Authorization", bearer(access)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/trips/" + tripId).header("Authorization", bearer(access)))
                .andExpect(jsonPath("$.spent.amount").value("0.00"))
                .andExpect(jsonPath("$.purchaseCount").value(0));

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(refresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andReturn();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(json(refreshed, "refreshToken"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/me").header("Authorization", bearer(json(refreshed, "accessToken"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailConflicts() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@voyage.test";
        String body = """
                { "displayName": "Ana", "email": "%s", "password": "secret1" }
                """.formatted(email);
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void loginRejectsBadPassword() throws Exception {
        String email = "bad-" + UUID.randomUUID() + "@voyage.test";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "displayName": "Ana", "email": "%s", "password": "secret1" }
                                """.formatted(email)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "nope" }
                                """.formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String json(MvcResult result, String field) throws Exception {
        String body = result.getResponse().getContentAsString();
        String needle = "\"" + field + "\":\"";
        int start = body.indexOf(needle);
        if (start < 0) {
            throw new IllegalStateException("Missing " + field + " in " + body);
        }
        start += needle.length();
        int end = body.indexOf('"', start);
        return body.substring(start, end);
    }
}
