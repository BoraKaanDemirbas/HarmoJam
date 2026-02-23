package com.example.demo.Controller;

import com.example.demo.Model.Song;
import com.example.demo.Service.MusicManagerService;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;//

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class Controller {

    @Autowired
    private MusicManagerService musicManager;

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
    public List<Song> searchMusic(@RequestBody String query) {

        System.out.println("Gelen Arama: " + query);

        return musicManager.searchSong(query);
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

        return musicManager.getRecommendation(track, artist);
    }


}
