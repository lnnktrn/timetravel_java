package com.lnnktrn.timetravel_java.controller.v2;

import com.lnnktrn.timetravel_java.dto.RecordDto;
import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/records")
public class RecordController {

    @Autowired
    RecordService recordService;

    // GET /api/v2/records/{id}
    @GetMapping("/{id}/latest")
    public ResponseEntity<String> getLatestRecord(
            @PathVariable Long id
    ) {

        return recordService.getLatestRecord(id)
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

        return recordService.getRecord(id, version)
                .map(entity -> ResponseEntity.ok(entity.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/v2/records/{id}/versions
    @GetMapping(value = "/{id}/versions")
    public ResponseEntity<List<RecordDto>> listVersions(@PathVariable Long id) {
        List<RecordEntity> records = recordService.listVersions(id);

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

        return recordService.upsertByIdAndVersion(id, version, data)
                ? ResponseEntity.noContent().build()                 // updated
                : ResponseEntity.status(HttpStatus.CREATED).build(); // created
    }
}