package com.example.demo.Repository;

import org.springframework.stereotype.Repository;
import com.example.demo.Model.Song;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface SongRepository extends JpaRepository<Song, String>{

}




