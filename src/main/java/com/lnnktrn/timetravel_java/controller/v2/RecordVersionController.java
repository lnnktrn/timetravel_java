package com.lnnktrn.timetravel_java.controller.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.lnnktrn.timetravel_java.dto.RecordDto;
import com.lnnktrn.timetravel_java.mapper.EntityToDtoMapper;
import com.lnnktrn.timetravel_java.service.RecordService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/records")
public class RecordVersionController {

    @Autowired
    private RecordService recordService;

    /**
     * Get record by given id.
     *
     * @param id      - record id
     * @param version (optional) - record version. If not empty, then returns a specific version of a record. If empty - returns latest version.
     * @return entityDto if record exists, 404 otherwise
     */
    // GET /api/v2/records/{id}?version={version}
    @GetMapping("/{id}")
    public ResponseEntity<RecordDto> getLatestOrByVersion(
            @PathVariable @Min(1) Long id,
            @RequestParam(required = false) @Min(1) Long version
    ) {
        var entity = (version == null)
                ? recordService.getLatestRecord(id)
                : recordService.getRecord(id, version);

        return ResponseEntity.ok(EntityToDtoMapper.map(entity));
    }

    /**
     * Get all record versions for given id.
     *
     * @param id
     * @return List of entityDto if records exist, 404 otherwise
     * If id is null or id<0 returns 400
     */
    // GET /api/v2/records/{id}/history
    @GetMapping(value = "/{id}/history")
    // List all available versions of a record
    public ResponseEntity<List<RecordDto>> listVersions(@PathVariable @Min(1) Long id) {
        var records = recordService.listVersions(id);
        var dtos = records.stream().map(EntityToDtoMapper::map).toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Applies updates to the latest version while preserving history
     * @param id record id
     * @param data data to be updated
     * @return
     * If record id is null or id<0 returns 400
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