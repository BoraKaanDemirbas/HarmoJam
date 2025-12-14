package com.example.demo.Service;

import com.example.demo.Model.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MusicManagerService {
    @Autowired
    private SpotifyService spotifyService;
    //bağlantılar
    @Autowired
    private LastFmService lastFmService;

    @Autowired
    private DeezerService deezerService;


    //arama metodu
    public List<Song> searchSong(String query) {

        System.out.println("MANAGER: Spotify araması yapılıyor -> " + query);//terminal Kontrol

        List<Song> spotifySongs = spotifyService.searchSong(query);
        for (Song s : spotifySongs) {
            try {
                Song deezerMatch = deezerService.findTrack(s.getIsim(), s.getSarkici());

                if (deezerMatch != null && deezerMatch.getMuzikUrl() != null) {
                    s.setMuzikUrl(deezerMatch.getMuzikUrl());
                }
            } catch (Exception e) {
                System.out.println("Audio eşleştirme hatası: " + e.getMessage());//terminal Kontrol
            }
        }

        return spotifySongs;
    }

    public List<Song> getRecommendation(String trackName, String artistName) {
        List<Song> finalRecommendations = new ArrayList<>();

        List<String[]> similarTracks = lastFmService.getRecommendation(trackName, artistName);

        int limit = 0;
        for (String[] trackInfo : similarTracks) {
            if (limit >= 12) break;

            String name = trackInfo[0];
            String artist = trackInfo[1];

            Song song = deezerService.findTrack(name, artist);

            if (song != null) {
                finalRecommendations.add(song);
                limit++;
            }
        }

        return finalRecommendations;
    }//sarki öneri
}
