//package com.example.demo.Controller;
//
//import com.example.demo.Model.Song;
//import com.example.demo.Repository.SongRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/favorites")
//@CrossOrigin(origins = "*")
//public class FavoriteController {
//    @Autowired
//    private SongRepository songRepository;
//
//    @PostMapping("/add")
//    public Song addFavorite(@RequestBody Song song) {// Gelen şarkıyı veritabanına kaydetme
//        if (song.getId() == null || song.getId().isEmpty()) {
//            song.setId(java.util.UUID.randomUUID().toString());
//        }
//        System.out.println("Kaydedilen Şarkı: " + song.getIsim());
//        return songRepository.save(song);
//    }
//
//    @GetMapping("/all")
//    public List<Song> getAllFavorites() {// Veritabanındaki her şeyi getir
//        return songRepository.findAll();
//    }
//
//    @DeleteMapping("/delete/{id}")
//    public void deleteFavorite(@PathVariable String id) {// ID'si verilen şarkıyı sil
//        songRepository.deleteById(id);
//    }
//
//}

package com.example.demo.Controller;

import com.example.demo.Model.Song;
import com.example.demo.Repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FavoriteController {
    @Autowired
    private SongRepository songRepository;

    @PostMapping("/add")
    public Song addFavorite(@RequestBody Song song, @RequestHeader("X-Device-Id") String deviceId) {
        if (song.getId() == null || song.getId().isEmpty()) {
            song.setId(java.util.UUID.randomUUID().toString());
        }
        song.setDeviceId(deviceId);
        System.out.println("Kaydedilen Şarkı: " + song.getIsim() + " (device: " + deviceId + ")");
        return songRepository.save(song);
    }

    @GetMapping("/all")
    public List<Song> getAllFavorites(@RequestHeader("X-Device-Id") String deviceId) {
        return songRepository.findByDeviceId(deviceId);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteFavorite(@PathVariable String id, @RequestHeader("X-Device-Id") String deviceId) {
        songRepository.deleteByIdAndDeviceId(id, deviceId);
    }

}