package com.lnnktrn.timetravel_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.lnnktrn.timetravel_java.entity.LatestVersionEntity;
import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import com.lnnktrn.timetravel_java.exception.NoSuchRecordException;
import com.lnnktrn.timetravel_java.helper.JsonMergePatchUtil;
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
    private RecordRepository recordRepository;
    @Autowired
    private LatestVersionRepository latestVersionRepository;
    @Autowired
    private JsonMergePatchUtil jsonMergePatchUtil;


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
        List<RecordEntity> entities = recordRepository.findAllByRecordId_IdOrderByRecordId_VersionAsc(id);
        if (entities.isEmpty()) {
            throw new NoSuchRecordException(id);
        }
        return entities;
    }

    @Transactional
    public boolean UpsertLatestVersion(Long id, JsonNode patch) {
        Optional<RecordEntity> existingRecord = latestVersionRepository.findLatestRecordById(id);
        Long newVersion;
        JsonNode newData;
        if (existingRecord.isPresent()) {
            var existingVersion = existingRecord.get().getRecordId().getVersion();
            newVersion = existingVersion + 1L;
            latestVersionRepository.updateVersionById(id, newVersion);
            JsonNode existingData = existingRecord.get().getData();
            newData = jsonMergePatchUtil.applyMergePatch(existingData, patch);
        } else {
            newVersion = 1L;
            newData = patch;
            var newLatestRecord = LatestVersionEntity.builder()
                    .id(id)
                    .version(newVersion)
                    .build();
            latestVersionRepository.save(newLatestRecord);
        }
        RecordId recordId = RecordId.builder().id(id).version(newVersion).build();
        var newRecord = RecordEntity.builder()
                .recordId(recordId)
                .data(newData)
                .build();
        recordRepository.save(newRecord);

        return existingRecord.isPresent();
    }
}
