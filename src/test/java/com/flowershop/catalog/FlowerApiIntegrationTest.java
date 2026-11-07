package com.flowershop.catalog;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test tích hợp với Postgres thật (Testcontainers). Cần Docker đang chạy.
 * Biến DB_* chỉ để thỏa placeholder; kết nối thật do @ServiceConnection cung cấp.
 */
@SpringBootTest(properties = {
        "DB_URL=jdbc:postgresql://unused:5432/unused",
        "DB_USERNAME=unused",
        "DB_PASSWORD=unused",
        "eureka.client.enabled=false"
})
@AutoConfigureMockMvc
@Testcontainers
class FlowerApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    private static String createJson(String sku, String price) {
        return """
                {"sku":"%s","name":"Hoa hồng đỏ","description":"Bó 10 bông",
                 "category":"ROSE","unitPrice":%s,"currency":"VND"}
                """.formatted(sku, price);
    }

    @Test
    void create_returns201_withStandardResponse() throws Exception {
        mockMvc.perform(post("/api/v1/flowers").contentType(APPLICATION_JSON).content(createJson("ROSE-RED-01", "350000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("00"))
                .andExpect(jsonPath("$.data.sku").value("ROSE-RED-01"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void create_duplicateSku_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/flowers").contentType(APPLICATION_JSON).content(createJson("LILY-01", "200000")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/flowers").contentType(APPLICATION_JSON).content(createJson("LILY-01", "200000")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CAT-409-001"));
    }

    @Test
    void create_negativePrice_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/flowers").contentType(APPLICATION_JSON).content(createJson("TULIP-BAD", "-5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COM-400-001"))
                .andExpect(jsonPath("$.data[0].field").value("unitPrice"));
    }

    @Test
    void update_withStaleVersion_returns409() throws Exception {
        String body = mockMvc.perform(post("/api/v1/flowers").contentType(APPLICATION_JSON).content(createJson("ORCHID-01", "500000")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(body, "$.data.id");

        String staleUpdate = """
                {"name":"Lan hồ điệp","category":"ORCHID","unitPrice":550000,"version":99}
                """;
        mockMvc.perform(put("/api/v1/flowers/" + id).contentType(APPLICATION_JSON).content(staleUpdate))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COM-409-001"));
    }
}
