package com.example.demo.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Kayıt sırasında (ya da "tekrar gönder" ile) üretilen e-posta doğrulama linkindeki token.
// Bir kullanıcının aynı anda sadece bir aktif token'ı olur — yeniden gönderilince
// (ya da doğrulama başarılı olunca) eskisi silinir. 24 saat sonra süresi dolar.
@Entity
@Table(name = "email_verification_token")
public class EmailVerificationToken {

    @Id
    @Column(length = 100)
    private String token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public EmailVerificationToken() {
    }

    public EmailVerificationToken(String token, Long userId, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.token = token;
        this.userId = userId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
