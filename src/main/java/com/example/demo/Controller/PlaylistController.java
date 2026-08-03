package com.example.demo.Controller;

import com.example.demo.Model.Playlist;
import com.example.demo.Model.PlaylistSong;
import com.example.demo.Model.Song;
import com.example.demo.Repository.PlaylistRepository;
import com.example.demo.Repository.PlaylistSongRepository;
import com.example.demo.Repository.SongRepository;
import com.example.demo.Service.OwnerResolverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/playlists")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PlaylistController {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistSongRepository playlistSongRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private OwnerResolverService ownerResolver;

    // Kullanıcının (varsayılan "Favoriler" hariç) oluşturduğu tüm özel listeleri getirir
    @GetMapping
    public ResponseEntity<?> getPlaylists(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);

            List<Playlist> playlists = playlistRepository.findByOwnerIdOrderByCreatedAtAsc(ownerId);

            List<Map<String, Object>> result = playlists.stream().map(p -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("name", p.getName());
                m.put("songCount", playlistSongRepository.countByPlaylistIdAndOwnerId(p.getId(), ownerId));
                m.put("shareToken", p.getShareToken()); // frontend liste zaten paylaşılıyor mu diye anlayabilsin
                return m;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // Yeni bir liste oluşturur (örn. "Yaz Tatili", "Antreman")
    @PostMapping
    public ResponseEntity<?> createPlaylist(@RequestBody Map<String, String> body,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);

            String name = body.get("name");
            if (name == null || name.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Liste adı boş olamaz."));
            }
            if (name.length() > 200) {
                name = name.substring(0, 200);
            }

            Playlist playlist = new Playlist(ownerId, name.trim(), LocalDateTime.now());
            Playlist saved = playlistRepository.save(playlist);

            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // Bir listeyi (ve içindeki bağlantıları) siler — şarkılar favorilerden silinmez, sadece listeden çıkar
    @DeleteMapping("/{playlistId}")
    @Transactional
    public ResponseEntity<?> deletePlaylist(@PathVariable Long playlistId,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);

            Optional<Playlist> playlist = playlistRepository.findByIdAndOwnerId(playlistId, ownerId);
            if (playlist.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Liste bulunamadı."));
            }

            playlistSongRepository.deleteByPlaylistIdAndOwnerId(playlistId, ownerId);
            playlistRepository.delete(playlist.get());

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // Bir listenin içindeki şarkıları (tam Song bilgisiyle) getirir
    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<?> getPlaylistSongs(@PathVariable Long playlistId,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader,
                                               @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);

            if (playlistRepository.findByIdAndOwnerId(playlistId, ownerId).isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Liste bulunamadı."));
            }

            List<PlaylistSong> entries = playlistSongRepository.findByPlaylistIdAndOwnerIdOrderByAddedAtDesc(playlistId, ownerId);

            List<Song> songs = entries.stream()
                    .map(entry -> songRepository.findByIdAndOwnerId(entry.getSongId(), ownerId).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(songs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // Favorilerdeki bir şarkıyı listeye ekler (şarkı önce favorilerde olmak zorunda)
    @PostMapping("/{playlistId}/songs")
    public ResponseEntity<?> addSongToPlaylist(@PathVariable Long playlistId,
                                                @RequestBody Map<String, String> body,
                                                @RequestHeader(value = "Authorization", required = false) String authHeader,
                                                @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);
            String songId = body.get("songId");

            if (songId == null || songId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "songId gerekli."));
            }
            if (playlistRepository.findByIdAndOwnerId(playlistId, ownerId).isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Liste bulunamadı."));
            }
            if (songRepository.findByIdAndOwnerId(songId, ownerId).isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bu şarkı favorilerde değil, önce favoriye eklemelisin."));
            }
            if (playlistSongRepository.findByPlaylistIdAndSongIdAndOwnerId(playlistId, songId, ownerId).isPresent()) {
                return ResponseEntity.ok().build(); // zaten ekli, no-op
            }

            playlistSongRepository.save(new PlaylistSong(playlistId, songId, ownerId, LocalDateTime.now()));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // Bir şarkıyı listeden çıkarır (favorilerden silmez, sadece o listeden çıkarır)
    @DeleteMapping("/{playlistId}/songs/{songId}")
    @Transactional
    public ResponseEntity<?> removeSongFromPlaylist(@PathVariable Long playlistId,
                                                      @PathVariable String songId,
                                                      @RequestHeader(value = "Authorization", required = false) String authHeader,
                                                      @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);
            playlistSongRepository.deleteByPlaylistIdAndSongIdAndOwnerId(playlistId, songId, ownerId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // Bir listeyi link ile paylaşılabilir hale getirir.
    // Liste zaten paylaşılıyorsa (shareToken varsa) AYNI token'ı döner — böylece daha önce
    // dağıtılmış bir link, tekrar "Share" tıklanınca bozulmaz/değişmez.
    @PostMapping("/{playlistId}/share")
    public ResponseEntity<?> sharePlaylist(@PathVariable Long playlistId,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader,
                                            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);

            Optional<Playlist> playlistOpt = playlistRepository.findByIdAndOwnerId(playlistId, ownerId);
            if (playlistOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Liste bulunamadı."));
            }

            Playlist playlist = playlistOpt.get();
            if (playlist.getShareToken() == null) {
                playlist.setShareToken(UUID.randomUUID().toString());
                playlistRepository.save(playlist);
            }

            return ResponseEntity.ok(Map.of("shareToken", playlist.getShareToken()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // Paylaşımı iptal eder: token null'lanır, eski link artık hiçbir listeye çıkmaz
    @DeleteMapping("/{playlistId}/share")
    public ResponseEntity<?> unsharePlaylist(@PathVariable Long playlistId,
                                              @RequestHeader(value = "Authorization", required = false) String authHeader,
                                              @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);

            Optional<Playlist> playlistOpt = playlistRepository.findByIdAndOwnerId(playlistId, ownerId);
            if (playlistOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Liste bulunamadı."));
            }

            Playlist playlist = playlistOpt.get();
            playlist.setShareToken(null);
            playlistRepository.save(playlist);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
