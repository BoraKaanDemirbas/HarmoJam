package com.example.demo.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Kullanıcının favorileri içinde oluşturduğu özel liste (Spotify'daki "playlist" gibi).
// "Varsayılan Favoriler" listesi ayrı bir tablo satırı DEĞİL — o zaten "favoriler" (Song)
// tablosunun tamamı. Bu tablo sadece EK, özel listeleri temsil eder.
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String ownerId;

    @Column(length = 200)
    private String name;

    private LocalDateTime createdAt;

    // Liste link ile paylaşıldığında üretilen benzersiz kod (UUID).
    // null ise liste paylaşılmıyor demektir; paylaşım kaldırıldığında tekrar null yapılır.
    @Column(length = 100, unique = true)
    private String shareToken;

    public Playlist() {
    }

    public Playlist(String ownerId, String name, LocalDateTime createdAt) {
        this.ownerId = ownerId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getShareToken() { return shareToken; }
    public void setShareToken(String shareToken) { this.shareToken = shareToken; }
}
