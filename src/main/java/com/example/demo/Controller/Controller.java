package com.example.demo.Controller;

import com.example.demo.Model.SearchLog;
import com.example.demo.Model.Song;
import com.example.demo.Repository.SearchLogRepository;
import com.example.demo.Service.MusicManagerService;
import com.example.demo.Service.SearchQueryParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;//

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", exposedHeaders = {"X-Detected-Genre", "X-Detected-Year-From", "X-Detected-Year-To"})
public class Controller {

    @Autowired
    private com.example.demo.Repository.MixLogRepository mixLogRepository;

    @Autowired
    private SearchLogRepository searchLogRepository;

    @Autowired
    private MusicManagerService musicManager;

    @Autowired
    private SearchQueryParser searchQueryParser;

 /*   @Autowired//controller ile spotify data servis birbirine bağlama
    private SpotifyDataService dataService;*/
/*
    @GetMapping("/get-token")//token alma uzantısı
    public String getSpotifyToken() {
        return spotifyService.getSpotifyToken();
    }*/

    /*
    @GetMapping("/search")//search uzantısı
    public List<Song> searchSong(@RequestParam String q){

        return dataService.searchSong(q);
    }*/

    /**//*@GetMapping("/search")
    public List<Song> search(@RequestParam String q) {
        System.out.println("CONTROLLER: Arama isteği geldi -> " + q);
        String Q = java.net.URLDecoder.decode(q, java.nio.charset.StandardCharsets.UTF_8);//
        return musicManager.searchSong(Q);
    }*/

    @PostMapping("/search")
    public ResponseEntity<List<Song>> searchMusic(@RequestBody String query, @RequestParam(defaultValue = "0") int offset) {

        // "Load More" ile gelen sonraki sayfaları istatistiklerde tekrar tekrar loglamayalım,
        // sadece aramanın ilk sayfasını (offset=0) kaydediyoruz.
        // Not: istatistiklere kullanıcının yazdığı orijinal metni kaydediyoruz, ayrıştırılmış
        // Spotify sorgusunu değil.
        if (offset == 0) {
            SearchLog log = new SearchLog(query, LocalDateTime.now());
            searchLogRepository.save(log);
        }

        // "1980'lerin rock müzikleri" gibi doğal dil ifadelerini Spotify'ın
        // year:/genre: filtrelerine çeviriyoruz. Filtre bulunamazsa sorgu
        // aynen (düz metin arama olarak) geri döner.
        SearchQueryParser.ParsedQuery parsed = searchQueryParser.parse(query);

        System.out.println("Gelen Arama: " + query + " -> Spotify sorgusu: " + parsed.spotifyQuery
                + " (offset=" + offset + ")");

        List<Song> results = musicManager.searchSong(parsed.spotifyQuery, offset);

        // Frontend'in "1980'ler · rock algılandı" gibi bir ipucu gösterebilmesi için
        // ayrıştırılan filtreleri header üzerinden yolluyoruz. JSON gövdesini
        // (List<Song>) bozmadan geriye dönük uyumlu kalıyoruz.
        HttpHeaders headers = new HttpHeaders();
        if (parsed.genre != null) {
            headers.add("X-Detected-Genre", parsed.genre);
        }
        if (parsed.yearFrom != null) {
            headers.add("X-Detected-Year-From", String.valueOf(parsed.yearFrom));
            headers.add("X-Detected-Year-To", String.valueOf(parsed.yearTo));
        }

        return ResponseEntity.ok().headers(headers).body(results);
    }
/*
    @GetMapping("/recommend")
    public List<Song> recommend(@RequestParam String name, @RequestParam String artist) {
        //servise isim ve sarkici yolluyoruz
        return dataService.getRecommendation(name, artist);
    }*/

    @GetMapping("/recommend")
    public List<Song> recommend(@RequestParam String track, @RequestParam String artist) {
        System.out.println("CONTROLLER: Öneri isteği geldi -> " + track);

        com.example.demo.Model.MixLog mixLog = new com.example.demo.Model.MixLog(track, artist, java.time.LocalDateTime.now());
        mixLogRepository.save(mixLog);

        return musicManager.getRecommendation(track, artist);
    }


}
