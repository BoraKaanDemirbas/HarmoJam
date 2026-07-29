package com.example.demo.Repository;

import com.example.demo.Model.FavoriteEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteEventLogRepository extends JpaRepository<FavoriteEventLog, Long> {

    // Belirli bir olay tipi (ADD/REMOVE) için en çok tekrar eden ilk 5 şarkıyı getirir
    @Query(value = "SELECT isim, sarkici, COUNT(*) as count " +
            "FROM favorite_event_logs " +
            "WHERE event_type = :eventType " +
            "GROUP BY isim, sarkici " +
            "ORDER BY count DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTop5ByEventType(@Param("eventType") String eventType);
}
