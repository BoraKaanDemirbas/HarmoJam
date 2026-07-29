package com.example.demo.Repository;

import com.example.demo.Model.MixLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface MixLogRepository extends JpaRepository<MixLog, Long> {

    // En cok mixlenen (oneri istenen) ilk 5 sarkiyi getirir
    @Query(value = "SELECT track_name, artist_name, COUNT(*) as count " +
            "FROM mix_logs " +
            "GROUP BY track_name, artist_name " +
            "ORDER BY count DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTop5Mixed();
}