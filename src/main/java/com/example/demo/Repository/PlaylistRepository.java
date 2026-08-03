package com.example.demo.Repository;

import com.example.demo.Model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findByOwnerIdOrderByCreatedAtAsc(String ownerId);

    Optional<Playlist> findByIdAndOwnerId(Long id, String ownerId);

    // Herkese açık paylaşım linkindeki token ile listeyi bulmak için (sahiplik kontrolü YAPILMAZ,
    // çünkü bu link kasıtlı olarak login gerektirmeyen ziyaretçiler için)
    Optional<Playlist> findByShareToken(String shareToken);
}
