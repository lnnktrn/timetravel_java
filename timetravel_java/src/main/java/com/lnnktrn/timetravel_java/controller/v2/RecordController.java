package com.lnnktrn.timetravel_java.controller.v2;

import com.lnnktrn.timetravel_java.dto.RecordDto;
import com.lnnktrn.timetravel_java.mapper.EntityToDtoMapper;
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
    private RecordService recordService;

    /**
     * Get record version by given id.
     *
     * @param id      - record id
     * @param version - record version
     * @return entityDto if record exists, 404 otherwise
     * If version is null or version<0 returns 400
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecordDto> getRecordByVersion(
            @PathVariable Long id,
            @RequestParam Long version
    ) {
        if (!(isPositive(version))) {
            return ResponseEntity.badRequest().build();
        }
        var entity = recordService.getRecord(id, version);
        var dto = EntityToDtoMapper.map(entity);
        return ResponseEntity.ok(dto);
    }

    /**
     * Get record version by given id.
     *
     * @param version - record version
     * @return entityDto if record exists, 404 otherwise
     * If version is null or version<0 returns 400
     */
    @GetMapping("/")
    public ResponseEntity<List<RecordDto>> getRecordsByVersion(
            @RequestParam Long version
    ) {
        if (!(isPositive(version))) {
            return ResponseEntity.badRequest().build();
        }
        var entities = recordService.getRecordsByVersion(version);
        var dtos = entities.stream().map(EntityToDtoMapper::map).toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all record versions for given id.
     *
     * @param id
     * @return List of entityDto if records exist, 404 otherwise
     * If id is null or id<0 returns 400
     */
    @GetMapping(value = "/{id}/versions")
    // List all available versions of a record
    public ResponseEntity<List<RecordDto>> listVersions(@PathVariable Long id) {
        if (!(isPositive(id))) {
            return ResponseEntity.badRequest().build();
        }
        var records = recordService.listVersions(id);
        if (records.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
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
    @PostMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> updateLatestVersionById(
            @PathVariable Long id,
            @RequestBody String data
    ) {
        if (!(isPositive(id))) {
            return ResponseEntity.badRequest().build();
        }
        recordService.updateLatestVersionById(id, data);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // created
    }

    private boolean isPositive(Long value){
        return value != null && value > 0;
    }

}