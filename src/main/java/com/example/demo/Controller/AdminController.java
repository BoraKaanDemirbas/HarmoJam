package com.example.demo.Controller;

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

        return ResponseEntity.ok(stats);
    }
}