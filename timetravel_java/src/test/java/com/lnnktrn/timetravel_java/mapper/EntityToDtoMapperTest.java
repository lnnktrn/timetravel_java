package com.lnnktrn.timetravel_java.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lnnktrn.timetravel_java.dto.RecordDto;
import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityToDtoMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void map_copiesAllFields() throws Exception {
        long id = 123L;
        long version = 7L;

        ObjectNode data = objectMapper.createObjectNode()
                .put("a", 1)
                .put("b", "x");

        Instant createdAt = Instant.parse("2026-01-11T10:15:30Z");

        RecordEntity entity = RecordEntity.builder()
                .recordId(RecordId.builder().id(id).version(version).build())
                .data(data)
                .build();

        setField(entity, "createdAt", createdAt);

        RecordDto dto = EntityToDtoMapper.map(entity);

        assertEquals(id, dto.id());
        assertEquals(version, dto.version());
        assertEquals(data.toString(), dto.data());
        assertEquals(createdAt, dto.createdAt());
    }

    @Test
    void map_usesJsonNodeToStringAsData() throws Exception {
        long id = 1L;
        long version = 1L;

        ObjectNode data = objectMapper.createObjectNode()
                .set("nested", objectMapper.createObjectNode().put("k", "v"));

        RecordEntity entity = RecordEntity.builder()
                .recordId(RecordId.builder().id(id).version(version).build())
                .data(data)
                .build();

        setField(entity, "createdAt", Instant.EPOCH);

        RecordDto dto = EntityToDtoMapper.map(entity);

        assertEquals("{\"nested\":{\"k\":\"v\"}}", dto.data());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}