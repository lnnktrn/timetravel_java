package com.lnnktrn.timetravel_java.service;

import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import com.lnnktrn.timetravel_java.exception.NoSuchRecordException;
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
    RecordRepository recordRepository;
    @Autowired
    LatestVersionRepository latestVersionRepository;

    public RecordEntity getLatestRecord(Long id) {
        return latestVersionRepository.findLatestRecordById(id)
                .orElseThrow(() -> new NoSuchRecordException(id));
    }


    public RecordEntity getRecord(Long id, Long version) {
        RecordId recordId = RecordId.builder().id(id).version(version).build();
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchRecordException(id, version));
    }

    public List<RecordEntity> listVersions(Long id) {
       List<RecordEntity> entities =  recordRepository.findAllByRecordId_IdOrderByRecordId_VersionAsc(id);
       if (entities.isEmpty()) {
          throw new NoSuchRecordException(id);
       }
       return entities;
    }

    @Transactional
    public boolean UpsertLatestVersion(Long id, String data){
        Optional<RecordEntity> existingRecord = latestVersionRepository.findLatestRecordById(id);
        Long newVersion;
        if (existingRecord.isPresent()) {
           var existingVersion = existingRecord.get().getRecordId().getVersion();
           newVersion = existingVersion + 1L;
        } else {
            newVersion= 1L;
        }
        RecordId recordId = RecordId.builder().id(id).version(newVersion).build();
        var newRecord = RecordEntity.builder()
                .recordId(recordId)
                .data(data)
                .build();
        recordRepository.save(newRecord);
        latestVersionRepository.updateVersionById(id, newVersion);
        return existingRecord.isPresent();
       }
}
