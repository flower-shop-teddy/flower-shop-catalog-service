package com.flowershop.catalog;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Giả lập request mang JWT hợp lệ của quản lý cửa hàng (có flower:write).
     * jwt() của spring-security-test bỏ qua bước kiểm tra chữ ký: test không cần identity-service đang chạy.
     */
    private static RequestPostProcessor manager() {
        return jwt().jwt(j -> j.subject("manager-uuid"))
                .authorities(new SimpleGrantedAuthority("flower:write"));
    }

    private static String createJson(String sku, String price) {
        return """
                {"sku":"%s","name":"Hoa hồng đỏ","description":"Bó 10 bông",
                 "category":"ROSE","unitPrice":%s,"currency":"VND"}
                """.formatted(sku, price);
    }

    @Test
    void create_returns201_withStandardResponse() throws Exception {
        mockMvc.perform(post("/api/v1/flowers").with(manager()).contentType(APPLICATION_JSON).content(createJson("ROSE-RED-01", "350000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("00"))
                .andExpect(jsonPath("$.data.sku").value("ROSE-RED-01"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void create_duplicateSku_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/flowers").with(manager()).contentType(APPLICATION_JSON).content(createJson("LILY-01", "200000")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/flowers").with(manager()).contentType(APPLICATION_JSON).content(createJson("LILY-01", "200000")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CAT-409-001"));
    }

    @Test
    void create_negativePrice_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/flowers").with(manager()).contentType(APPLICATION_JSON).content(createJson("TULIP-BAD", "-5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COM-400-001"))
                .andExpect(jsonPath("$.data[0].field").value("unitPrice"));
    }

    @Test
    void update_withStaleVersion_returns409() throws Exception {
        String body = mockMvc.perform(post("/api/v1/flowers").with(manager()).contentType(APPLICATION_JSON).content(createJson("ORCHID-01", "500000")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(body, "$.data.id");

        String staleUpdate = """
                {"name":"Lan hồ điệp","category":"ORCHID","unitPrice":550000,"version":99}
                """;
        mockMvc.perform(put("/api/v1/flowers/" + id).with(manager()).contentType(APPLICATION_JSON).content(staleUpdate))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COM-409-001"));
    }

    // ---------- Phân quyền ----------

    @Test
    void list_isPublic() throws Exception {
        mockMvc.perform(get("/api/v1/flowers"))
                .andExpect(status().isOk());
    }

    @Test
    void create_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/flowers").contentType(APPLICATION_JSON).content(createJson("NO-TOKEN-01", "1000")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COM-401-001"));
    }

    @Test
    void create_withoutPermission_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/flowers").with(jwt().authorities(new SimpleGrantedAuthority("customer:read")))
                        .contentType(APPLICATION_JSON).content(createJson("NO-PERM-01", "1000")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COM-403-001"));
    }

    @Test
    void create_withoutPermission_invalidBody_returns403Not400() throws Exception {
        mockMvc.perform(post("/api/v1/flowers").with(jwt()).contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_recordsCreatedByFromToken() throws Exception {
        mockMvc.perform(post("/api/v1/flowers").with(manager()).contentType(APPLICATION_JSON)
                        .content(createJson("AUDIT-01", "1000")))
                .andExpect(status().isCreated());
        String createdBy = jdbcTemplate.queryForObject("select created_by from flower where sku = 'AUDIT-01'", String.class);
        assertThat(createdBy).isEqualTo("manager-uuid");
    }

    @Test
    void getBySku_isPublic_andReturnsInactiveToo() throws Exception {
        String body = mockMvc.perform(post("/api/v1/flowers").with(manager()).contentType(APPLICATION_JSON)
                        .content(createJson("TULIP-SKU-01", "150000")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Integer id = com.jayway.jsonpath.JsonPath.read(body, "$.data.id");

        mockMvc.perform(get("/api/v1/flowers/sku/TULIP-SKU-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(delete("/api/v1/flowers/" + id).with(manager())).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/flowers/sku/TULIP-SKU-01"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        mockMvc.perform(get("/api/v1/flowers/sku/KHONG-CO"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CAT-404-001"));
    }
}
