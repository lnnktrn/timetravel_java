package com.lnnktrn.timetravel_java.mapper;

import com.lnnktrn.timetravel_java.dto.RecordDto;
import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class EntityToDtoMapperTest {

    @Test
    void testEntityToDto() {
        Instant now = Instant.now();
        String data = "{\"a\":\"1\"}";
        RecordEntity recordEntity = RecordEntity.builder()
                .recordId(RecordId.builder().id(1L).version(2L).build())
                .createdAt(now)
                .updatedAt(now)
                .data(data)
                .build();
        RecordDto recordDto = EntityToDtoMapper.map(recordEntity);
        Assertions.assertEquals(1L, recordDto.id());
        Assertions.assertEquals(2L, recordDto.version());
        Assertions.assertEquals(now, recordDto.createdAt());
        Assertions.assertEquals(now, recordDto.updatedAt());
        Assertions.assertEquals(data, recordDto.data());
    }
}