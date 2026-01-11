package com.lnnktrn.timetravel_java.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lnnktrn.timetravel_java.controller.v2.RecordVersionController;
import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import com.lnnktrn.timetravel_java.exception.NoSuchRecordException;
import com.lnnktrn.timetravel_java.service.RecordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecordVersionController.class)
class RecordVersionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean RecordService recordService;

    private RecordEntity makeEntity(long id, long version, String jsonData, Instant createdAt, Instant updatedAt) {
        ObjectNode node = objectMapper.createObjectNode();
        try {
            node = (ObjectNode) objectMapper.readTree(jsonData);
        } catch (Exception e) {
            // fallback
            node.put("raw", jsonData);
        }

        RecordId recordId = RecordId.builder().id(id).version(version).build(); // предполагается, что у тебя есть такой конструктор
        return RecordEntity.builder()
                .recordId(recordId)
                .data(node)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    @Test
    @DisplayName("GET /api/v2/records/{id} without version -> 200 and JSON DTO")
    void getLatest_ok_returnsDtoJson() throws Exception {
        long id = 10L;
        Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2025-01-02T00:00:00Z");

        RecordEntity entity = makeEntity(id, 7L, "{\"a\":1}", createdAt, updatedAt);
        Mockito.when(recordService.getLatestRecord(eq(id))).thenReturn(entity);

        mockMvc.perform(get("/api/v2/records/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.version").value(7))
                .andExpect(jsonPath("$.data").value("{\"a\":1}"))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.updatedAt").value(updatedAt.toString()));

        Mockito.verify(recordService).getLatestRecord(eq(id));
        Mockito.verify(recordService, Mockito.never()).getRecord(anyLong(), anyLong());
    }

    @Test
    @DisplayName("GET /api/v2/records/{id}?version=3 -> 200 and JSON DTO")
    void getByVersion_ok_returnsDtoJson() throws Exception {
        long id = 10L;
        long version = 3L;

        Instant createdAt = Instant.parse("2025-02-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2025-02-02T00:00:00Z");

        RecordEntity entity = makeEntity(id, version, "{\"x\":\"y\"}", createdAt, updatedAt);
        Mockito.when(recordService.getRecord(eq(id), eq(version))).thenReturn(entity);

        mockMvc.perform(get("/api/v2/records/{id}", id)
                        .queryParam("version", String.valueOf(version)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.version").value(3))
                .andExpect(jsonPath("$.data").value("{\"x\":\"y\"}"))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.updatedAt").value(updatedAt.toString()));

        Mockito.verify(recordService).getRecord(eq(id), eq(version));
        Mockito.verify(recordService, Mockito.never()).getLatestRecord(anyLong());
    }

    @Test
    @DisplayName("GET /api/v2/records/{id} with id<=0 -> 400 service is not being called")
    void get_badId_400() throws Exception {
        mockMvc.perform(get("/api/v2/records/{id}", 0))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(recordService);
    }

    @Test
    @DisplayName("GET /api/v2/records/{id}/history -> 200 and DTO list")
    void history_ok_returnsList() throws Exception {
        long id = 10L;

        Instant t1 = Instant.parse("2025-03-01T00:00:00Z");
        Instant t2 = Instant.parse("2025-03-02T00:00:00Z");

        RecordEntity v1 = makeEntity(id, 1L, "{\"v\":1}", t1, t1);
        RecordEntity v2 = makeEntity(id, 2L, "{\"v\":2}", t2, t2);

        Mockito.when(recordService.listVersions(eq(id))).thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/api/v2/records/{id}/history", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].version").value(1))
                .andExpect(jsonPath("$[0].data").value("{\"v\":1}"))
                .andExpect(jsonPath("$[1].version").value(2))
                .andExpect(jsonPath("$[1].data").value("{\"v\":2}"));

        Mockito.verify(recordService).listVersions(eq(id));
    }

    @Test
    @DisplayName("GET /api/v2/records/{id}/history -> 404 if service throws an exception")
    void history_empty_404() throws Exception {
        long id = 10L;

        Mockito.when(recordService.listVersions(eq(id))).thenThrow(new NoSuchRecordException(id));

        mockMvc.perform(get("/api/v2/records/{id}/history", id))
                .andExpect(status().isNotFound());

        Mockito.verify(recordService).listVersions(eq(id));
    }

    @Test
    @DisplayName("GET /api/v2/records/{id}/history с id<=0 -> 400")
    void history_badId_400() throws Exception {
        mockMvc.perform(get("/api/v2/records/{id}/history", 0))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(recordService);
    }

    @Test
    @DisplayName("POST /api/v2/records/{id} -> 200 if UpsertLatestVersion updated a record")
    void post_updated_true_returns200() throws Exception {
        long id = 10L;
        ObjectNode body = objectMapper.createObjectNode().put("k", "v");

        Mockito.when(recordService.upsertLatestVersion(eq(id), any())).thenReturn(RecordEntity.builder()
                .recordId(RecordId.builder().id(id).version(1L).build())
                .build());

        mockMvc.perform(post("/api/v2/records/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        Mockito.verify(recordService).upsertLatestVersion(eq(id), any());
    }

    @Test
    @DisplayName("POST /api/v2/records/{id} -> 201 if UpsertLatestVersion created a record")
    void post_created_false_returns201() throws Exception {
        long id = 10L;
        ObjectNode body = objectMapper.createObjectNode().put("k", "v");

        Mockito.when(recordService.upsertLatestVersion(eq(id), any())).thenReturn(RecordEntity.builder()
                .recordId(RecordId.builder().id(id).version(1L).build())
                .build());

        mockMvc.perform(post("/api/v2/records/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        Mockito.verify(recordService).upsertLatestVersion(eq(id), any());
    }

    @Test
    @DisplayName("POST /api/v2/records/{id} с id<=0 -> 400")
    void post_badId_400() throws Exception {
        ObjectNode body = objectMapper.createObjectNode().put("k", "v");

        mockMvc.perform(post("/api/v2/records/{id}", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(recordService);
    }

    @Test
    @DisplayName("GET /api/v2/records/{id}?version=0 -> 400")
    void get_badVersion_expected400() throws Exception {
        long id = 10L;
        mockMvc.perform(get("/api/v2/records/{id}", id)
                        .queryParam("version", "0"))
                .andExpect(status().isBadRequest());
    }
}
