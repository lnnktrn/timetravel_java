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
     * Get record by given id.
     *
     * @param id      - record id
     * @param version (optional) - record version. If not empty, then returns a specific version of a record. If empty - returns latest version.
     * @return entityDto if record exists, 404 otherwise
     */
    // GET /api/v2/records/{id}?version={version}
    @GetMapping("/{id}")
    public ResponseEntity<RecordDto> getLatestOrByVersion(
            @PathVariable Long id,
            @RequestParam(required = false) Long version
    ) {
        if (!isPositive(id)) return ResponseEntity.badRequest().build();

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
    // POST /api/v2/records/{id}
    @PostMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> updateLatestVersionById(
            @PathVariable Long id,
            @RequestBody String data
    ) {
        if (!(isPositive(id))) {
            return ResponseEntity.badRequest().build();
        }
        boolean isUpdated = recordService.UpsertLatestVersion(id, data);
        return isUpdated ? ResponseEntity.status(HttpStatus.OK).build()
                : ResponseEntity.status(HttpStatus.CREATED).build();// created
    }

    private boolean isPositive(Long value){
        return value != null && value > 0;
    }

}