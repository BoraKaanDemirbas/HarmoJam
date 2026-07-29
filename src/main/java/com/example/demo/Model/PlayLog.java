package com.example.demo.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Bir şarkının preview'ının çalınmaya başlandığı her an için kayıt.
@Entity
@Table(name = "play_logs")
public class PlayLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String songId;
    private String isim;
    private String sarkici;
    private LocalDateTime playedAt;

    public PlayLog() {
    }

    public PlayLog(String songId, String isim, String sarkici, LocalDateTime playedAt) {
        this.songId = songId;
        this.isim = isim;
        this.sarkici = sarkici;
        this.playedAt = playedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSongId() { return songId; }
    public void setSongId(String songId) { this.songId = songId; }

    public String getIsim() { return isim; }
    public void setIsim(String isim) { this.isim = isim; }

    public String getSarkici() { return sarkici; }
    public void setSarkici(String sarkici) { this.sarkici = sarkici; }

    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}
