package com.lnnktrn.timetravel_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Service
public class RecordService {

    @Autowired
    private RecordRepository recordRepository;
    @Autowired
    private LatestVersionRepository latestVersionRepository;
    @Autowired
    private JsonMergePatchUtil jsonMergePatchUtil;
    @Autowired
    private ObjectMapper objectMapper;

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
    public RecordEntity upsertLatestVersion(Long id, JsonNode patch) {
        var latestOpt = latestVersionRepository.findByIdForUpdate(id);

        long newVersion;
        JsonNode baseData;
        LatestVersionEntity latest;

        if (latestOpt.isPresent()) {
            latest = latestOpt.get();
            baseData = getRecord(id, latest.getVersion()).getData();
            newVersion = latest.getVersion() + 1;
        } else {
            baseData = objectMapper.createObjectNode();
            newVersion = 1;
            latest = LatestVersionEntity.builder().id(id).version(newVersion).build();
        }

        JsonNode newData = jsonMergePatchUtil.applyMergePatch(baseData, patch);

        var saved = recordRepository.save(
                RecordEntity.builder()
                        .recordId(RecordId.builder().id(id).version(newVersion).build())
                        .data(newData)
                        .build()
        );

        latest.setVersion(newVersion);
        latestVersionRepository.save(latest);

        return saved;
    }
    
}
