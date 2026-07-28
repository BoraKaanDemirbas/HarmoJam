package com.example.demo.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Her giriş yapılan cihaz/tarayıcı için ayrı bir token satırı oluşur.
// Böylece bir cihazda logout olmak diğer cihazlardaki oturumu bozmaz,
// ve "hangi cihazdan girerse girsin" isteği doğal olarak karşılanır.
@Entity
@Table(name = "auth_token")
public class AuthToken {

    @Id
    @Column(length = 100)
    private String token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public AuthToken() {
    }

    public AuthToken(String token, Long userId) {
        this.token = token;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
