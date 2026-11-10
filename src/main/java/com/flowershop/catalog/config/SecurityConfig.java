package com.flowershop.catalog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowershop.common.security.ApiSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Xem danh mục hoa: ai cũng xem được (khách chưa đăng nhập vẫn duyệt sản phẩm).
 * Thêm/sửa/ngừng bán: cần permission flower:write (khai báo bằng @PreAuthorize ở FlowerController).
 *
 * Service này TỰ kiểm tra JWT dù gateway đã kiểm tra rồi: ai gọi thẳng vào cổng 8081 (bỏ qua gateway)
 * vẫn bị chặn. Đó là "zero trust": không tin request chỉ vì nó đến từ mạng nội bộ.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        ApiSecurity.configure(http, objectMapper);
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/v1/flowers", "/api/v1/flowers/**").permitAll()
                // Chặn ở tầng URL (trong filter) TRƯỚC khi vào controller. Nếu chỉ có @PreAuthorize thì Spring
                // validate body trước rồi mới kiểm tra quyền: người không có quyền gửi body sai sẽ nhận 400
                // (biết được luật validate) thay vì 403. @PreAuthorize ở controller vẫn giữ làm lớp thứ hai.
                .requestMatchers("/api/v1/flowers", "/api/v1/flowers/**").hasAuthority("flower:write")
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
