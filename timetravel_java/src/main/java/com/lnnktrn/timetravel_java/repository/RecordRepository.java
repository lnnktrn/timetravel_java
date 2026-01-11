package com.lnnktrn.timetravel_java.repository;

import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecordRepository extends JpaRepository<RecordEntity, RecordId> {
    List<RecordEntity> findAllByRecordId_IdOrderByRecordId_VersionAsc(Long id);
}