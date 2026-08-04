package com.example.demo.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Bir kullanıcının (ownerId) Spotify hesabıyla bağlantısını (OAuth token'larını) saklar.
@Entity
@Table(name = "spotify_connections")
public class SpotifyConnection {

    @Id
    @Column(length = 100)
    private String ownerId;

    @Column(length = 500)
    private String accessToken;

    @Column(length = 500)
    private String refreshToken;

    private LocalDateTime expiresAt;

    public SpotifyConnection() {
    }

    public SpotifyConnection(String ownerId, String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.ownerId = ownerId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}