package com.lnnktrn.timetravel_java.controller;

import com.lnnktrn.timetravel_java.dto.RecordDto;
import com.lnnktrn.timetravel_java.entity.LatestVersionEntity;
import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import com.lnnktrn.timetravel_java.repository.LatestVersionRepository;
import com.lnnktrn.timetravel_java.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/v2/records")
public class RecordController {

    @Autowired
    RecordRepository repo;
    @Autowired
    LatestVersionRepository latestVersionRepository;

    // GET /api/v2/records/{id}
    @GetMapping("/{id}/latest")
    public ResponseEntity<String> getLatestRecord(
            @PathVariable Long id
    ) {

        return latestVersionRepository.findLatestRecordById(id)
                .map(entity -> ResponseEntity.ok(entity.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/v2/records/{id}?version=7
    @GetMapping("/{id}")
    public ResponseEntity<String> getRecordByVersion(
            @PathVariable Long id,
            @RequestParam Long version
    ) {
        if (version == null || version <= 0) {
            return ResponseEntity.badRequest().build();
        }
        RecordId recordId = RecordId.builder().id(id).version(version).build();

        return repo.findById(recordId)
                .map(entity -> ResponseEntity.ok(entity.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/v2/records/{id}/versions
    @GetMapping(value = "/{id}/versions")
    public ResponseEntity<List<RecordDto>> listVersions(@PathVariable Long id) {
        List<RecordEntity> records =
                repo.findAllByRecordId_IdOrderByRecordId_VersionAsc(id);

        if (records.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<RecordDto> dto = records.stream()
                .map(r -> new RecordDto(
                        r.getRecordId().getId(),
                        r.getRecordId().getVersion(),
                        r.getData(),
                        r.getCreatedAt(),   // убери если нет поля
                        r.getUpdatedAt()    // убери если нет поля
                ))
                .toList();

        return ResponseEntity.ok(dto);
    }


    // POST /api/v2/records/{id}?version=7
    @PostMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> upsertByIdAndVersion(
            @PathVariable Long id,
            @RequestParam("version") Long version,
            @RequestBody String data
    ) {
        if (version == null || version <= 0) {
            return ResponseEntity.badRequest().build();
        }
        if (data == null) {
            return ResponseEntity.badRequest().build();
        }

        RecordId key =RecordId.builder().id(id).version(version).build();

        boolean existed = repo.existsById(key);

        RecordEntity entity = RecordEntity.builder()
                .recordId(key)
                .data(data)
                .build();

        LatestVersionEntity latestVersionEntity = LatestVersionEntity.builder()
                .id(key.getId())
                .version(key.getVersion())
                .build();
        repo.save(entity);

        return existed
                ? ResponseEntity.noContent().build()                 // updated
                : ResponseEntity.status(HttpStatus.CREATED).build(); // created
    }
}