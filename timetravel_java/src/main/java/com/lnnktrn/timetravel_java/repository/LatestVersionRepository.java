package com.lnnktrn.timetravel_java.repository;

import com.lnnktrn.timetravel_java.entity.LatestVersionEntity;
import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LatestVersionRepository extends JpaRepository<LatestVersionEntity, Long> {

    @Query("""
        select r
        from RecordEntity r
        join LatestVersionEntity l
          on l.id = r.recordId.id
         and l.version = r.recordId.version
        where l.id = :id
    """)
    Optional<RecordEntity> findLatestRecordById(Long id);
}