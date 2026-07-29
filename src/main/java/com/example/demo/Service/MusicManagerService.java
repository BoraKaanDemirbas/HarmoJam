package com.example.demo.Service;

import com.example.demo.Model.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class MusicManagerService {
    @Autowired
    private SpotifyService spotifyService;
    //bağlantılar
    @Autowired
    private LastFmService lastFmService;

    @Autowired
    private DeezerService deezerService;

    // Deezer eşleştirmelerini (audio preview bulma) paralel yapmak için havuz.
    // Önceden her şarkı için sırayla (birbiri ardına) Deezer'a istek atılıyordu,
    // bu da 10+ şarkı için toplamda saniyelerce sürüyordu. Artık hepsi aynı anda gidiyor.
    private final ExecutorService deezerExecutor = Executors.newFixedThreadPool(10);


    //arama metodu
    public List<Song> searchSong(String query) {

        System.out.println("MANAGER: Spotify araması yapılıyor -> " + query);//terminal Kontrol

        List<Song> spotifySongs = spotifyService.searchSong(query);

        // Her şarkı için Deezer eşleştirmesini paralel olarak başlatıyoruz
        List<CompletableFuture<Void>> futures = spotifySongs.stream()
                .map(s -> CompletableFuture.runAsync(() -> {
                    try {
                        Song deezerMatch = deezerService.findTrack(s.getIsim(), s.getSarkici());

                        if (deezerMatch != null && deezerMatch.getMuzikUrl() != null) {
                            s.setMuzikUrl(deezerMatch.getMuzikUrl());
                        }
                    } catch (Exception e) {
                        System.out.println("Audio eşleştirme hatası: " + e.getMessage());//terminal Kontrol
                    }
                }, deezerExecutor))
                .collect(Collectors.toList());

        // Hepsinin bitmesini bekliyoruz (ama artık paralel çalıştıkları için toplam süre
        // en yavaş tekil istek kadar sürüyor, hepsinin toplamı kadar değil)
        futures.forEach(CompletableFuture::join);

        return spotifySongs;
    }

    public List<Song> getRecommendation(String trackName, String artistName) {
        List<String[]> similarTracks = lastFmService.getRecommendation(trackName, artistName);

        List<String[]> limited = similarTracks.stream()
                .limit(12)
                .collect(Collectors.toList());

        // Last.fm'den gelen benzer şarkılar için Deezer eşleştirmesini paralel yapıyoruz
        List<CompletableFuture<Song>> futures = limited.stream()
                .map(trackInfo -> CompletableFuture.supplyAsync(
                        () -> deezerService.findTrack(trackInfo[0], trackInfo[1]), deezerExecutor))
                .collect(Collectors.toList());

        List<Song> finalRecommendations = new ArrayList<>();
        for (CompletableFuture<Song> future : futures) {
            Song song = future.join();
            if (song != null) {
                finalRecommendations.add(song);
            }
        }

        return finalRecommendations;
    }//sarki öneri
}
