package com.example.demo.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mix_logs")
public class MixLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trackName;
    private String artistName;
    private LocalDateTime mixTime;

    public MixLog() {
    }

    public MixLog(String trackName, String artistName, LocalDateTime mixTime) {
        this.trackName = trackName;
        this.artistName = artistName;
        this.mixTime = mixTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTrackName() { return trackName; }
    public void setTrackName(String trackName) { this.trackName = trackName; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public LocalDateTime getMixTime() { return mixTime; }
    public void setMixTime(LocalDateTime mixTime) { this.mixTime = mixTime; }
}