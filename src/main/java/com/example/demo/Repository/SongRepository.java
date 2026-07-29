package com.example.demo.Repository;

import org.springframework.stereotype.Repository;
import com.example.demo.Model.Song;
import com.example.demo.Model.SongId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song, SongId>{

    List<Song> findByOwnerId(String ownerId);

    List<Song> findByOwnerIdOrderByAddedAtDesc(String ownerId);

    // Silmeden önce şarkının isim/sanatçı bilgisini alabilmek için (istatistik logu amacıyla)
    Optional<Song> findByIdAndOwnerId(String id, String ownerId);

    void deleteByIdAndOwnerIdOrderByAddedAtDesc(String id, String ownerId);
}
