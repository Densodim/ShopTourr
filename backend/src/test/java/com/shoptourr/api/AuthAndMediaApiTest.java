package com.shoptourr.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndMediaApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void registerLoginAndEchoRequestId() throws Exception {
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Request-Id", "req-test-1")
                                .content("""
                                        {
                                          "displayName": "Mila",
                                          "email": "mila@voyage.app",
                                          "password": "secret1",
                                          "locale": "ru"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Request-Id", "req-test-1"))
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.user.email", is("mila@voyage.app")));

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "mila@voyage.app",
                                          "password": "secret1"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    void mediaUploadConfirmThenOcr() throws Exception {
        String intentBody = mockMvc.perform(
                        post("/api/media/upload-intents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "purpose": "RECEIPT",
                                          "contentType": "image/jpeg",
                                          "byteSize": 1200
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaId", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String mediaId = intentBody.replaceAll("(?s).*\"mediaId\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(
                        post("/api/media/" + mediaId + "/confirm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "uploaded": true }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("READY")));

        mockMvc.perform(get("/api/media/" + mediaId + "/ocr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedName", is("Pasteis de Belem")))
                .andExpect(jsonPath("$.suggestedAmount", is("4.50")))
                .andExpect(jsonPath("$.confidence", is(0.86)));
    }
}
