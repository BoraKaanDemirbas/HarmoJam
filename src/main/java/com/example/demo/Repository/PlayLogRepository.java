package com.example.demo.Repository;

import com.example.demo.Model.PlayLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayLogRepository extends JpaRepository<PlayLog, Long> {

    // En çok preview'ı çalınan ilk 5 şarkıyı getirir
    @Query(value = "SELECT isim, sarkici, COUNT(*) as count " +
            "FROM play_logs " +
            "GROUP BY isim, sarkici " +
            "ORDER BY count DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTop5Played();
}
