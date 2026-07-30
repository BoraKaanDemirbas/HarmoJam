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
}
