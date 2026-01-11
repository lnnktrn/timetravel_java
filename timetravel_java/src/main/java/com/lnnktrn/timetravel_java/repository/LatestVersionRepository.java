package com.lnnktrn.timetravel_java.repository;

import com.lnnktrn.timetravel_java.entity.LatestVersionEntity;
import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("""
                select r
                from RecordEntity r
                join LatestVersionEntity l
                  on l.id = r.recordId.id
                 and l.version = r.recordId.version
                where l.version = :version
            """)
    List<RecordEntity> findLatestRecordByVersion(Long version);

    @Modifying
    @Transactional
    @Query("""
        update LatestVersionEntity l
           set l.version = :version
         where l.id = :id
    """)
    int updateVersionById(
            @Param("id") Long id,
            @Param("version") Long version
    );
}