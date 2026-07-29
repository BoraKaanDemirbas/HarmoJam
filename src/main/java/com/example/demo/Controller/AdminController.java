package com.example.demo.Controller;

import com.example.demo.Repository.FavoriteEventLogRepository;
import com.example.demo.Repository.PlayLogRepository;
import com.example.demo.Repository.SearchLogRepository;
import com.example.demo.Repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private com.example.demo.Repository.MixLogRepository mixLogRepository;

    @Autowired
    private SearchLogRepository searchLogRepository;

    @Autowired
    private SongRepository songRepository; // Favori sayısını çekmek için mevcut repoyu kullanıyoruz

    @Autowired
    private FavoriteEventLogRepository favoriteEventLogRepository;

    @Autowired
    private PlayLogRepository playLogRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Toplam yapılan arama sayısı
        stats.put("totalSearches", searchLogRepository.count());

        // 2. En çok aratılan 5 kelime
        stats.put("topSearches", searchLogRepository.findTop5Searches());

        // 3. Veritabanındaki toplam favori şarkı sayısı
        stats.put("totalFavorites", songRepository.count());

        stats.put("totalMixes", mixLogRepository.count());

        // 4. En çok mixlenen (öneri istenen) ilk 5 şarkı
        stats.put("topMixed", mixLogRepository.findTop5Mixed());

        // 5. En çok favorilere eklenen ilk 5 şarkı
        stats.put("topAddedFavorites", favoriteEventLogRepository.findTop5ByEventType("ADD"));

        // 6. En çok favorilerden çıkarılan ilk 5 şarkı
        stats.put("topRemovedFavorites", favoriteEventLogRepository.findTop5ByEventType("REMOVE"));

        // 7. En çok preview'ı çalınan ilk 5 şarkı
        stats.put("topPlayed", playLogRepository.findTop5Played());

        return ResponseEntity.ok(stats);
    }
}