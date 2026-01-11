package com.lnnktrn.timetravel_java.controller;

import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/records")
public class RecordController {

    @Autowired
    RecordRepository repo;

    // GET /api/v1/records/{id}
    @GetMapping("/{id}")
    public ResponseEntity<String> getRecord(@PathVariable Long id) {
        return repo.findById(id)
                .map(record -> ResponseEntity.ok(record.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/v1/records/{id}
    @PostMapping("/{id}")
    public ResponseEntity<Void> upsertRecord(
            @PathVariable Long id,
            @RequestBody String data
    ) {
        RecordEntity entity = repo.findById(id)
                .orElseGet(() -> RecordEntity.builder().id(id).data("{}").build());

        entity.setData(data); // или merge-логика, если нужно
        repo.save(entity);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}