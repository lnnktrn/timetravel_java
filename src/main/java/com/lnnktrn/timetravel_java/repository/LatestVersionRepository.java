package com.lnnktrn.timetravel_java.repository;

import com.lnnktrn.timetravel_java.entity.LatestVersionEntity;
import com.lnnktrn.timetravel_java.entity.RecordEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LatestVersionEntity l where l.id = :id")
    Optional<LatestVersionEntity> findByIdForUpdate(@Param("id") Long id);

}