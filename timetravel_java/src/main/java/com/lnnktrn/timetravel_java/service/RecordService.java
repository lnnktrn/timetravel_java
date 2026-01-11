package com.lnnktrn.timetravel_java.service;

import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import com.lnnktrn.timetravel_java.repository.LatestVersionRepository;
import com.lnnktrn.timetravel_java.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RecordService {

    @Autowired
    RecordRepository repo;
    @Autowired
    LatestVersionRepository latestVersionRepository;

    public Optional<RecordEntity> getLatestRecord(Long id) {
        return latestVersionRepository.findLatestRecordById(id);
    }

    public Optional<RecordEntity> getRecord(Long id, Long version) {
        if (version == null || version <= 0) {
            // throw exception
            return Optional.empty();
        }
        RecordId recordId = RecordId.builder().id(id).version(version).build();
        return repo.findById(recordId);
    }

    public List<RecordEntity> listVersions(Long id) {
        return repo.findAllByRecordId_IdOrderByRecordId_VersionAsc(id);
    }

    @Transactional
    public boolean upsertByIdAndVersion(Long id, Long version, String data) {
        RecordId recordId = RecordId.builder().id(id).version(version).build();
        RecordEntity newEntity = RecordEntity.builder()
                .recordId(recordId)
                .data(data)
                .build();
        boolean existed = repo.existsById(recordId);
//        LatestVersionEntity latestVersionEntity = LatestVersionEntity.builder()
//                .id(recordId.getId())
//                .version(recordId.getVersion())
//                .build();
        repo.save(newEntity);

        return existed;
    }
}
