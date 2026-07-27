//package com.example.demo.Repository;
//
//import org.springframework.stereotype.Repository;
//import com.example.demo.Model.Song;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//@Repository
//public interface SongRepository extends JpaRepository<Song, String>{
//
//}

package com.example.demo.Repository;

import org.springframework.stereotype.Repository;
import com.example.demo.Model.Song;
import com.example.demo.Model.SongId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, SongId>{

    // Sadece belirli bir cihaza (kullanıcıya) ait favorileri getir
    List<Song> findByDeviceId(String deviceId);

    // Sadece belirli bir cihaza ait favoriyi sil (başka kullanıcının favorisini silmesin diye)
    void deleteByIdAndDeviceId(String id, String deviceId);
}