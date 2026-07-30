package com.example.demo.Controller;

import com.example.demo.Model.FavoriteEventLog;
import com.example.demo.Model.Song;
import com.example.demo.Repository.FavoriteEventLogRepository;
import com.example.demo.Repository.SongRepository;
import com.example.demo.Service.OwnerResolverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/favorites")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FavoriteController {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private OwnerResolverService ownerResolver;

    @Autowired
    private FavoriteEventLogRepository favoriteEventLogRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addFavorite(@RequestBody Song song,
              @RequestHeader(value = "Authorization", required = false) String authHeader,
              @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);

            if (song.getId() == null || song.getId().isEmpty()) {
                song.setId(java.util.UUID.randomUUID().toString());
            }
            song.setOwnerId(ownerId);

            song.setAddedAt(java.time.LocalDateTime.now());//

            System.out.println("Kaydedilen Şarkı: " + song.getIsim() + " (owner: " + ownerId + ")");
            Song saved = songRepository.save(song);

            favoriteEventLogRepository.save(new FavoriteEventLog(
                    song.getId(), song.getIsim(), song.getSarkici(), "ADD", java.time.LocalDateTime.now()));

            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllFavorites
            (@RequestHeader(value = "Authorization", required = false) String authHeader,
             @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);
            return ResponseEntity.ok(songRepository.findByOwnerIdOrderByAddedAtDesc(ownerId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    @Transactional
    public ResponseEntity<?> deleteFavorite(@PathVariable String id,
             @RequestHeader(value = "Authorization", required = false) String authHeader,
             @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            String ownerId = ownerResolver.resolve(authHeader, deviceId);

            Optional<Song> existing = songRepository.findByIdAndOwnerId(id, ownerId);
            existing.ifPresent(song -> favoriteEventLogRepository.save(new FavoriteEventLog(
                    song.getId(), song.getIsim(), song.getSarkici(), "REMOVE",
                    java.time.LocalDateTime.now())));

            songRepository.deleteByIdAndOwnerIdOrderByAddedAtDesc(id, ownerId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
