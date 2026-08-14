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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class P1ApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void diaryWishlistTaxFreeAlertsMediaRouteAndExport() throws Exception {
        String access = registerAccess("p1-" + UUID.randomUUID() + "@voyage.test");

        MvcResult createdTrip = mockMvc.perform(post("/api/trips")
                        .header("Authorization", bearer(access))
                        .header("Idempotency-Key", UUID.randomUUID().toString())
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
                .andReturn();
        String tripId = json(createdTrip, "id");

        MvcResult diary = mockMvc.perform(post("/api/trips/" + tripId + "/diary")
                        .header("Authorization", bearer(access))
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "mood": "good", "text": "Pasteis and sun" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mood").value("good"))
                .andExpect(jsonPath("$.tripId").value(tripId))
                .andReturn();
        String diaryId = json(diary, "id");

        mockMvc.perform(get("/api/trips/" + tripId + "/diary").header("Authorization", bearer(access)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days", hasSize(1)))
                .andExpect(jsonPath("$.days[0].entries[0].id").value(diaryId));

        MvcResult wish = mockMvc.perform(post("/api/wishlist")
                        .header("Authorization", bearer(access))
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tile magnet",
                                  "city": "Lisbon",
                                  "targetPrice": { "amount": "8.00", "currency": "EUR" },
                                  "iconEmoji": "🧲"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tile magnet"))
                .andReturn();
        String wishId = json(wish, "id");

        mockMvc.perform(get("/api/wishlist").header("Authorization", bearer(access)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(wishId));

        mockMvc.perform(post("/api/trips/" + tripId + "/purchases")
                        .header("Authorization", bearer(access))
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Watch",
                                  "category": "SOUVENIRS",
                                  "amount": { "amount": "80.00", "currency": "EUR" },
                                  "vatIncluded": true,
                                  "taxRefundEligible": true,
                                  "place": "Belem"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/trips/" + tripId + "/tax-free").header("Authorization", bearer(access)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rules.regionLabel").value("EU"))
                .andExpect(jsonPath("$.eligibleCount").value(1))
                .andExpect(jsonPath("$.eligibleTotal.amount").value("80.00"))
                .andExpect(jsonPath("$.estimatedRefundTotal.amount").value("10.40"))
                .andExpect(jsonPath("$.remainingToMinimum.amount").value("0.00"));

        mockMvc.perform(get("/api/trips/" + tripId + "/alerts").header("Authorization", bearer(access)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerts[*].type", hasItem("DAILY_ALLOWANCE")));

        mockMvc.perform(get("/api/trips/" + tripId + "/route").header("Authorization", bearer(access)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stopCount").value(1))
                .andExpect(jsonPath("$.stops[0].place").value("Belem"))
                .andExpect(jsonPath("$.stops[0].amountSpentHere.amount").value("80.00"));

        MvcResult intent = mockMvc.perform(post("/api/media/upload-intents")
                        .header("Authorization", bearer(access))
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "purpose": "RECEIPT",
                                  "contentType": "image/jpeg",
                                  "byteSize": 4
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_UPLOAD"))
                .andReturn();
        String mediaId = json(intent, "mediaId");
        String uploadUrl = json(intent, "uploadUrl");
        String pathAndQuery = uploadUrl.substring(uploadUrl.indexOf("/api/"));

        mockMvc.perform(put(pathAndQuery)
                        .contentType(MediaType.IMAGE_JPEG)
                        .content(new byte[] {1, 2, 3, 4}))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/media/" + mediaId + "/confirm")
                        .header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "uploaded": true }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));

        mockMvc.perform(get("/api/media/" + mediaId + "/ocr").header("Authorization", bearer(access)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEDIA_NOT_READY"));

        MvcResult exportJob = mockMvc.perform(post("/api/trips/" + tripId + "/exports")
                        .header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "format": "CSV", "includeTaxFree": true, "includeDiary": true }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.format").value("CSV"))
                .andReturn();
        String exportId = json(exportJob, "id");

        mockMvc.perform(get("/api/exports/" + exportId).header("Authorization", bearer(access)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(exportId));

        mockMvc.perform(get("/api/exports/" + exportId + "/file").header("Authorization", bearer(access)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/trips/" + tripId + "/diary/" + diaryId)
                        .header("Authorization", bearer(access)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/wishlist/" + wishId).header("Authorization", bearer(access)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/wishlist").header("Authorization", bearer(access)))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    private String registerAccess(String email) throws Exception {
        MvcResult register = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "P1",
                                  "email": "%s",
                                  "password": "secret1"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();
        return json(register, "accessToken");
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
