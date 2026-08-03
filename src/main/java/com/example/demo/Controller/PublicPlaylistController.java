package com.example.demo.Controller;

import com.example.demo.Model.Playlist;
import com.example.demo.Model.PlaylistSong;
import com.example.demo.Model.Song;
import com.example.demo.Repository.PlaylistRepository;
import com.example.demo.Repository.PlaylistSongRepository;
import com.example.demo.Repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

// Link ile paylaşılan listeleri gösteren, KİMLİK DOĞRULAMA GEREKTİRMEYEN uç noktalar.
// PlaylistController'daki diğer her şeyin aksine burada ownerResolver / X-Device-Id / Authorization
// hiç kullanılmıyor — ziyaretçi giriş yapmamış olsa bile, elindeki linkteki shareToken tek başına yeterli.
@RestController
@RequestMapping("/public/playlists")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PublicPlaylistController {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistSongRepository playlistSongRepository;

    @Autowired
    private SongRepository songRepository;

    @GetMapping("/{shareToken}")
    public ResponseEntity<?> getSharedPlaylist(@PathVariable String shareToken) {
        Optional<Playlist> playlistOpt = playlistRepository.findByShareToken(shareToken);
        if (playlistOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Bu paylaşım linki geçersiz ya da liste artık paylaşılmıyor."));
        }

        Playlist playlist = playlistOpt.get();
        // Şarkı detaylarını (Song tablosu ownerId'ye göre ayrıştığı için) listeyi PAYLAŞAN
        // kişinin ownerId'siyle okuyoruz — ziyaretçinin kendi ownerId'si (varsa) burada devreye girmez.
        String ownerId = playlist.getOwnerId();

        List<PlaylistSong> entries = playlistSongRepository.findByPlaylistIdAndOwnerIdOrderByAddedAtDesc(playlist.getId(), ownerId);

        List<Song> songs = entries.stream()
                .map(entry -> songRepository.findByIdAndOwnerId(entry.getSongId(), ownerId).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("name", playlist.getName());
        result.put("songs", songs);
        return ResponseEntity.ok(result);
    }
}
