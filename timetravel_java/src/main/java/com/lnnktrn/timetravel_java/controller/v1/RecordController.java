package com.lnnktrn.timetravel_java.controller.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.lnnktrn.timetravel_java.dto.RecordDto;
import com.lnnktrn.timetravel_java.mapper.EntityToDtoMapper;
import com.lnnktrn.timetravel_java.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/records")
public class RecordController {
    @Autowired
    RecordService recordService;

    /**
     * Get latest record version for given id.
     *
     * @param id
     * @return entityDto if record exists, 404 otherwise
     */
    // Backwards compatibility
    @GetMapping("/{id}")
    public ResponseEntity<RecordDto> getLatestRecord(
            @PathVariable Long id
    ) {
        var entity = recordService.getLatestRecord(id);
        var dto = EntityToDtoMapper.map(entity);
        return ResponseEntity.ok(dto);
    }

    /**
     * Creates or updates a record
     *
     * @param id   record id
     * @param data data to be updated
     * @return If record id is null or id<0 returns 400
     * If record does not exist creates a new record with given id and data
     * If record exists creates next version with given data and returns 201
     * If record does not exist creates new record vith version 1 and returns 200
     */
    // Apply updates to the latest version while preserving history
    @PostMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> updateLatestVersionById(
            @PathVariable Long id,
            @RequestBody JsonNode data
    ) {
        if (!(isPositive(id))) {
            return ResponseEntity.badRequest().build();
        }
        boolean isUpdated = recordService.UpsertLatestVersion(id, data);
        return isUpdated ? ResponseEntity.status(HttpStatus.OK).build()
                : ResponseEntity.status(HttpStatus.CREATED).build();// created
    }

    private boolean isPositive(Long value) {
        return value != null && value > 0;
    }
}
