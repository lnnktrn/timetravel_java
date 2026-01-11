package com.lnnktrn.timetravel_java.controller.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.lnnktrn.timetravel_java.dto.RecordDto;
import com.lnnktrn.timetravel_java.mapper.EntityToDtoMapper;
import com.lnnktrn.timetravel_java.service.RecordService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/records")
public class RecordController {
    @Autowired
    RecordService recordService;

    /**
     * Get record latest version by given id.
     *
     * @param id - record id
     * @return entityDto if record exists, 404 otherwise
     */
    // GET /api/v2/records/{id}
    @GetMapping("/{id}")
    public ResponseEntity<RecordDto> getLatestOrByVersion(
            @PathVariable @Min(1) Long id
    ) {
        var entity = recordService.getLatestRecord(id);
        return ResponseEntity.ok(EntityToDtoMapper.map(entity));
    }

    /**
     * Applies updates to the latest version while preserving history
     *
     * @param id   record id
     * @param data data to be updated
     * @return If record id is null or id<0 returns 400
     * If record does not exist returns 404
     */
    // Apply updates to the latest version while preserving history
    // POST /api/v2/raecords/{id}
    @PostMapping("/{id}")
    public ResponseEntity<Void> updateRecordById(
            @PathVariable @Min(1) Long id,
            @RequestBody JsonNode data
    ) {
        recordService.upsertLatestVersion(id, data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
