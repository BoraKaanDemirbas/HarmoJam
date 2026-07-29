package com.example.demo.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Favorilere ekleme/çıkarma olaylarının kalıcı kaydı.
// Not: "favoriler" tablosundan bir şarkı çıkarıldığında satır tamamen silindiği için,
// "en çok eklenen" / "en çok çıkarılan" gibi istatistikleri hesaplayabilmek için
// bu ayrı log tablosunu tutuyoruz (arama/mix loglarıyla aynı mantık).
@Entity
@Table(name = "favorite_event_logs")
public class FavoriteEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String songId;
    private String isim;
    private String sarkici;

    // "ADD" veya "REMOVE"
    private String eventType;

    private LocalDateTime eventTime;

    public FavoriteEventLog() {
    }

    public FavoriteEventLog(String songId, String isim, String sarkici, String eventType, LocalDateTime eventTime) {
        this.songId = songId;
        this.isim = isim;
        this.sarkici = sarkici;
        this.eventType = eventType;
        this.eventTime = eventTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSongId() { return songId; }
    public void setSongId(String songId) { this.songId = songId; }

    public String getIsim() { return isim; }
    public void setIsim(String isim) { this.isim = isim; }

    public String getSarkici() { return sarkici; }
    public void setSarkici(String sarkici) { this.sarkici = sarkici; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
}
