package com.example.demo.Controller;

import com.example.demo.Model.Song;
import com.example.demo.Repository.PlaylistRepository;
import com.example.demo.Repository.PlaylistSongRepository;
import com.example.demo.Repository.SongRepository;
import com.example.demo.Service.OwnerResolverService;
import com.example.demo.Service.SpotifyExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/spotify-export")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SpotifyExportController {

    @Autowired private SpotifyExportService exportService;
    @Autowired private OwnerResolverService ownerResolver;
    @Autowired private SongRepository songRepository;
    @Autowired private PlaylistRepository playlistRepository;
    @Autowired private PlaylistSongRepository playlistSongRepository;

    // Tarayıcı direkt navigasyonuyla (fetch değil) çağrıldığı için header yerine
    // query param ile kimlik bilgisi alıyoruz (Authorization/X-Device-Id header'ları
    // normal sayfa yönlendirmelerinde gönderilemiyor).
    @GetMapping("/login")
    public void login(@RequestParam(required = false) String authToken,
                      @RequestParam(required = false) String deviceId,
                      HttpServletResponse response) throws IOException {
        String ownerId = ownerResolver.resolve(
                authToken != null ? "Bearer " + authToken : null, deviceId);
        response.sendRedirect(exportService.buildAuthorizeUrl(ownerId));
    }

    @GetMapping("/callback")
    public void callback(@RequestParam String code,
                         @RequestParam String state,
                         HttpServletResponse response) throws IOException {
        String ownerId = exportService.decodeState(state);
        try {
            exportService.handleCallback(code, ownerId);
            response.sendRedirect("/?spotifyConnected=1");
        } catch (Exception e) {
            response.sendRedirect("/?spotifyConnected=0");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                    @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);
            return ResponseEntity.ok(Map.of("connected", exportService.isConnected(ownerId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                        @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);
            exportService.disconnect(ownerId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/favorites")
    public ResponseEntity<?> exportFavorites(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);
            List<Song> songs = songRepository.findByOwnerIdOrderByAddedAtDesc(ownerId);
            return ResponseEntity.ok(exportService.exportSongs(ownerId, "HarmoJam Favorites", songs));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Export failed: " + e.getMessage()));
        }
    }

    @PostMapping("/playlists/{playlistId}")
    public ResponseEntity<?> exportPlaylist(@PathVariable Long playlistId,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader,
                                            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);

            var playlist = playlistRepository.findByIdAndOwnerId(playlistId, ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Liste bulunamadı."));

            List<Song> songs = playlistSongRepository.findByPlaylistIdAndOwnerIdOrderByAddedAtDesc(playlistId, ownerId)
                    .stream()
                    .map(entry -> songRepository.findByIdAndOwnerId(entry.getSongId(), ownerId).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(exportService.exportSongs(ownerId, playlist.getName(), songs));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Export failed: " + e.getMessage()));
        }
    }
}