package com.example.demo.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Bir şarkının hangi playlist'e eklendiğini tutan ara tablo.
// Bir şarkı bir playlist'e eklenebilmesi için önce favorilerde (favoriler tablosunda) olmalı.
@Entity
@Table(name = "playlist_songs")
public class PlaylistSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long playlistId;

    @Column(length = 600)
    private String songId;

    @Column(length = 100)
    private String ownerId;

    private LocalDateTime addedAt;

    public PlaylistSong() {
    }

    public PlaylistSong(Long playlistId, String songId, String ownerId, LocalDateTime addedAt) {
        this.playlistId = playlistId;
        this.songId = songId;
        this.ownerId = ownerId;
        this.addedAt = addedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlaylistId() { return playlistId; }
    public void setPlaylistId(Long playlistId) { this.playlistId = playlistId; }

    public String getSongId() { return songId; }
    public void setSongId(String songId) { this.songId = songId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
