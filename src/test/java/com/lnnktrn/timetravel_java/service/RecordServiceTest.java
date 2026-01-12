package com.lnnktrn.timetravel_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lnnktrn.timetravel_java.entity.LatestVersionEntity;
import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import com.lnnktrn.timetravel_java.exception.NoSuchRecordException;
import com.lnnktrn.timetravel_java.helper.JsonMergePatchUtil;
import com.lnnktrn.timetravel_java.repository.LatestVersionRepository;
import com.lnnktrn.timetravel_java.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    private RecordRepository recordRepository;
    private LatestVersionRepository latestVersionRepository;
    private JsonMergePatchUtil jsonMergePatchUtil;
    private ObjectMapper objectMapper;

    private RecordService service;

    @BeforeEach
    void setUp() throws Exception {
        recordRepository = mock(RecordRepository.class);
        latestVersionRepository = mock(LatestVersionRepository.class);
        jsonMergePatchUtil = mock(JsonMergePatchUtil.class);
        objectMapper = new ObjectMapper();

        service = new RecordService();
        inject(service, "recordRepository", recordRepository);
        inject(service, "latestVersionRepository", latestVersionRepository);
        inject(service, "jsonMergePatchUtil", jsonMergePatchUtil);
        inject(service, "objectMapper", objectMapper);
    }

    // --- getLatestRecord ---

    @Test
    void getLatestRecord_returnsRecord() {
        long id = 10L;
        RecordEntity record = RecordEntity.builder()
                .recordId(RecordId.builder().id(id).version(3L).build())
                .data(objectMapper.createObjectNode())
                .build();

        when(latestVersionRepository.findLatestRecordById(id)).thenReturn(Optional.of(record));

        var result = service.getLatestRecord(id);

        assertSame(record, result);
        verify(latestVersionRepository).findLatestRecordById(id);
        verifyNoMoreInteractions(latestVersionRepository);
        verifyNoInteractions(recordRepository, jsonMergePatchUtil);
    }

    @Test
    void getLatestRecord_throwsWhenNotFound() {
        long id = 10L;
        when(latestVersionRepository.findLatestRecordById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchRecordException.class, () -> service.getLatestRecord(id));
    }

    // --- getRecord ---

    @Test
    void getRecord_returnsRecord() {
        long id = 7L;
        long version = 2L;
        RecordId rid = RecordId.builder().id(id).version(version).build();

        RecordEntity record = RecordEntity.builder()
                .recordId(rid)
                .data(objectMapper.createObjectNode())
                .build();

        when(recordRepository.findById(rid)).thenReturn(Optional.of(record));

        var result = service.getRecord(id, version);

        assertSame(record, result);
        verify(recordRepository).findById(rid);
        verifyNoInteractions(latestVersionRepository, jsonMergePatchUtil);
    }

    @Test
    void getRecord_throwsWhenNotFound() {
        long id = 7L;
        long version = 2L;
        RecordId rid = RecordId.builder().id(id).version(version).build();

        when(recordRepository.findById(rid)).thenReturn(Optional.empty());

        assertThrows(NoSuchRecordException.class, () -> service.getRecord(id, version));
    }

    // --- listVersions ---

    @Test
    void listVersions_returnsSortedListFromRepo() {
        long id = 5L;
        var e1 = RecordEntity.builder()
                .recordId(RecordId.builder().id(id).version(1L).build())
                .data(objectMapper.createObjectNode())
                .build();
        var e2 = RecordEntity.builder()
                .recordId(RecordId.builder().id(id).version(2L).build())
                .data(objectMapper.createObjectNode())
                .build();

        when(recordRepository.findAllByRecordId_IdOrderByRecordId_VersionAsc(id)).thenReturn(List.of(e1, e2));

        var result = service.listVersions(id);

        assertEquals(2, result.size());
        assertSame(e1, result.get(0));
        assertSame(e2, result.get(1));
        verify(recordRepository).findAllByRecordId_IdOrderByRecordId_VersionAsc(id);
        verifyNoInteractions(latestVersionRepository, jsonMergePatchUtil);
    }

    @Test
    void listVersions_throwsWhenEmpty() {
        long id = 5L;
        when(recordRepository.findAllByRecordId_IdOrderByRecordId_VersionAsc(id)).thenReturn(List.of());

        assertThrows(NoSuchRecordException.class, () -> service.listVersions(id));
    }

    // --- upsertLatestVersion: existing latest ---

    @Test
    void upsertLatestVersion_existingLatest_createsNextVersionAndUpdatesLatest() {
        long id = 42L;

        // latest = v5
        LatestVersionEntity latest = LatestVersionEntity.builder().id(id).version(5L).build();
        when(latestVersionRepository.findByIdForUpdate(id)).thenReturn(Optional.of(latest));

        // current record v5
        ObjectNode baseData = objectMapper.createObjectNode().put("a", 1);
        RecordId currentId = RecordId.builder().id(id).version(5L).build();
        RecordEntity currentRecord = RecordEntity.builder().recordId(currentId).data(baseData).build();
        when(recordRepository.findById(currentId)).thenReturn(Optional.of(currentRecord));

        // patch -> newData
        ObjectNode patch = objectMapper.createObjectNode().put("b", 2);
        ObjectNode newData = objectMapper.createObjectNode().put("a", 1).put("b", 2);
        when(jsonMergePatchUtil.applyMergePatch(baseData, patch)).thenReturn(newData);

        // save record returns entity
        ArgumentCaptor<RecordEntity> recordCaptor = ArgumentCaptor.forClass(RecordEntity.class);
        when(recordRepository.save(any(RecordEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = service.upsertLatestVersion(id, patch);

        // verify record saved with version=6 and data=newData
        verify(recordRepository).save(recordCaptor.capture());
        RecordEntity savedArg = recordCaptor.getValue();
        assertEquals(id, savedArg.getRecordId().getId());
        assertEquals(6L, savedArg.getRecordId().getVersion());
        assertSame(newData, savedArg.getData());

        // verify latest updated to 6 and saved
        ArgumentCaptor<LatestVersionEntity> latestCaptor = ArgumentCaptor.forClass(LatestVersionEntity.class);
        verify(latestVersionRepository).save(latestCaptor.capture());
        assertEquals(6L, latestCaptor.getValue().getVersion());

        // verify merge called correctly
        verify(jsonMergePatchUtil).applyMergePatch(baseData, patch);

        // sanity: method returns what recordRepository.save returned (we returned the arg)
        assertEquals(6L, saved.getRecordId().getVersion());
        assertSame(newData, saved.getData());
    }

    // --- upsertLatestVersion: new record ---

    @Test
    void upsertLatestVersion_noLatest_createsVersion1AndCreatesLatest() {
        long id = 100L;

        when(latestVersionRepository.findByIdForUpdate(id)).thenReturn(Optional.empty());

        ObjectNode patch = objectMapper.createObjectNode().put("x", "y");
        ObjectNode newData = objectMapper.createObjectNode().put("x", "y");

        // baseData will be {} (createObjectNode).
        when(jsonMergePatchUtil.applyMergePatch(any(JsonNode.class), eq(patch))).thenReturn(newData);

        ArgumentCaptor<RecordEntity> recordCaptor = ArgumentCaptor.forClass(RecordEntity.class);
        when(recordRepository.save(any(RecordEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = service.upsertLatestVersion(id, patch);

        verify(recordRepository).save(recordCaptor.capture());
        RecordEntity savedArg = recordCaptor.getValue();
        assertEquals(id, savedArg.getRecordId().getId());
        assertEquals(1L, savedArg.getRecordId().getVersion());
        assertSame(newData, savedArg.getData());

        ArgumentCaptor<LatestVersionEntity> latestCaptor = ArgumentCaptor.forClass(LatestVersionEntity.class);
        verify(latestVersionRepository).save(latestCaptor.capture());
        LatestVersionEntity latestSaved = latestCaptor.getValue();
        assertEquals(id, latestSaved.getId());
        assertEquals(1L, latestSaved.getVersion());

        assertEquals(1L, saved.getRecordId().getVersion());
        assertSame(newData, saved.getData());
    }

    // --- tiny helper: inject private fields (because field injection in service) ---

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}