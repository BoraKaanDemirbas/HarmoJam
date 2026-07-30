package com.example.demo.Config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

// Admin istatistiklerini (/api/admin/**) korumak için basit ama etkili bir katman.
// Projede henüz tam bir Spring Security kurulumu olmadığı için, mevcut kod stiline
// (AuthService/OwnerResolverService'deki gibi basit token mantığı) uygun, hafif bir
// "paylaşılan anahtar" (shared secret) filtresi kullanıyoruz.
//
// Nasıl çalışır:
// - Render/İsteğe göre ortam değişkeni olarak ADMIN_SECRET_KEY tanımlanır.
// - /api/admin/** altındaki her istekte "X-Admin-Key" header'ı bu değere eşit olmalı.
// - ADMIN_SECRET_KEY tanımlı DEĞİLSE, güvenlik açığı olmasın diye admin paneline
//   erişim TAMAMEN kapatılır (fail-closed) — yani "unutulmuş konfigürasyon" asla
//   "herkese açık admin paneli" anlamına gelmez.
@Configuration
public class AdminSecurityConfig {

    @Value("${ADMIN_SECRET_KEY:}")
    private String adminSecretKey;

    @Bean
    public FilterRegistrationBean<Filter> adminAuthFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AdminAuthFilter(adminSecretKey));
        registrationBean.addUrlPatterns("/api/admin/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    static class AdminAuthFilter implements Filter {
        private final String expectedKey;

        AdminAuthFilter(String expectedKey) {
            this.expectedKey = expectedKey;
        }

        @Override
        public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;

            if (expectedKey == null || expectedKey.isBlank()) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Admin paneli aktif değil: ADMIN_SECRET_KEY ortam değişkeni tanımlanmamış.");
                return;
            }

            String providedKey = request.getHeader("X-Admin-Key");
            if (providedKey == null || !constantTimeEquals(providedKey, expectedKey)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Geçersiz admin anahtarı.");
                return;
            }

            chain.doFilter(req, res);
        }

        // Zamanlama saldırılarına (timing attack) karşı basit sabit-zamanlı karşılaştırma.
        private boolean constantTimeEquals(String a, String b) {
            if (a.length() != b.length()) return false;
            int result = 0;
            for (int i = 0; i < a.length(); i++) {
                result |= a.charAt(i) ^ b.charAt(i);
            }
            return result == 0;
        }
    }
}
