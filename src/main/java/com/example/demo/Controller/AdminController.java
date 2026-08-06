package com.example.demo.Controller;

import com.example.demo.Model.AppUser;
import com.example.demo.Repository.*;
import jakarta.validation.constraints.Null;
import jakarta.websocket.OnClose;
import org.hibernate.type.descriptor.jdbc.ObjectNullResolvingJdbcType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private AppUserRepository appUserRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<Map<String, Object>> users = appUserRepository.findAll().stream()
                .map(u-> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",u.getId());
                    m.put("username",u.getUsername());
                    m.put("email",u.getEmail());
                    m.put("emailVerified",u.isEmailVerified());
                    m.put("createdAt",u.getCreatedAt());
                    return  m;
                }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public  ResponseEntity<?> verifyUser(@PathVariable Long id){
        return appUserRepository.findById(id)
                .map(user ->{
                    user.setEmailVerified(true);
                    appUserRepository.save(user);
                    return ResponseEntity.ok(Map.of("success",true));

                }).orElse(ResponseEntity.notFound().build());
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/cache/search-terms")
    public ResponseEntity<?> getCachedSearchTerms (){
        Set<String> keys = redisTemplate.keys("searchResults::*");
        if (keys == null) keys = Collections.emptySet();

        List<Map<String, Object>> result = new ArrayList<>();
        for (String key : keys) {
            String raw = key.substring("searchResults::".length());
            int lastUnderScore = raw.lastIndexOf('_');
            String term = lastUnderScore > 0 ? raw.substring(0,lastUnderScore) : raw;
            String offset = lastUnderScore > 0 ? raw.substring(lastUnderScore+1) : "0";

            Map<String, Object> entry = new HashMap<>();
            entry.put("term",term);
            entry.put("offset",offset);
            entry.put("ttlSeconds",redisTemplate.getExpire(key, TimeUnit.SECONDS));
            result.add(entry);
        }
        result.sort((a,b) -> ((String) a.get("term"))
                .compareTo((String) b.get("term")));
        return ResponseEntity.ok(result);
    }

}