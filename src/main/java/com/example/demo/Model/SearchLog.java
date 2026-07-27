package com.example.demo.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_logs")
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String query; // Aratılan kelime

    private LocalDateTime searchTime; // Arama yapılan tam tarih ve saat

    // Boş Constructor (JPA için zorunlu)
    public SearchLog() {
    }

    // Dolu Constructor
    public SearchLog(String query, LocalDateTime searchTime) {
        this.query = query;
        this.searchTime = searchTime;
    }

    // Getter ve Setter metodları
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public LocalDateTime getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(LocalDateTime searchTime) {
        this.searchTime = searchTime;
    }
}