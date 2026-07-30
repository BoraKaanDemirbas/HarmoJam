package com.example.demo.Repository;

import com.example.demo.Model.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {


    // Veritabanında en çok tekrar eden (aratılan) ilk 5 kelimeyi ve sayısını getirir
    @Query(value = "SELECT query, COUNT(query) as count FROM search_logs GROUP BY query ORDER BY count DESC LIMIT 5"
            , nativeQuery = true)
    List<Object[]> findTop5Searches();
}