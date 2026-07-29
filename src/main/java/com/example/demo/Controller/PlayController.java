package com.example.demo.Controller;

import com.example.demo.Model.PlayLog;
import com.example.demo.Repository.PlayLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/plays")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PlayController {

    @Autowired
    private PlayLogRepository playLogRepository;

    // Frontend, bir şarkının preview'ını çalmaya başladığında bu uca istek atar.
    @PostMapping("/log")
    public ResponseEntity<?> logPlay(@RequestBody Map<String, String> body) {
        String songId = body.get("id");
        String isim = body.get("isim");
        String sarkici = body.get("sarkici");

        PlayLog log = new PlayLog(songId, isim, sarkici, LocalDateTime.now());
        playLogRepository.save(log);

        return ResponseEntity.ok().build();
    }
}
