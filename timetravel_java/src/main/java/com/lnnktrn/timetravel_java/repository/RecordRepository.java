package com.lnnktrn.timetravel_java.repository;

import com.lnnktrn.timetravel_java.entity.RecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {
}