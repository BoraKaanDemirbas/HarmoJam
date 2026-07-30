package com.example.demo.Repository;

import com.example.demo.Model.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    List<PlaylistSong> findByPlaylistIdAndOwnerIdOrderByAddedAtDesc(Long playlistId, String ownerId);

    Optional<PlaylistSong> findByPlaylistIdAndSongIdAndOwnerId(Long playlistId, String songId, String ownerId);

    long countByPlaylistIdAndOwnerId(Long playlistId, String ownerId);

    void deleteByPlaylistIdAndSongIdAndOwnerId(Long playlistId, String songId, String ownerId);

    // Bir playlist tamamen silindiğinde, içindeki tüm şarkı bağlantılarını da temizlemek için
    void deleteByPlaylistIdAndOwnerId(Long playlistId, String ownerId);

    // Bir şarkı favorilerden tamamen çıkarıldığında, bulunduğu tüm playlist'lerden de düşmesi için
    void deleteBySongIdAndOwnerId(String songId, String ownerId);
}
