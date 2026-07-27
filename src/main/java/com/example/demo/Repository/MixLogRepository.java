package com.example.demo.Repository;

import com.example.demo.Model.MixLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MixLogRepository extends JpaRepository<MixLog, Long> {
}