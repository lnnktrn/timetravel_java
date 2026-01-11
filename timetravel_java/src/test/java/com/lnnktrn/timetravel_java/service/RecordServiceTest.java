package com.lnnktrn.timetravel_java.service;

import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import com.lnnktrn.timetravel_java.exception.NoSuchRecordException;
import com.lnnktrn.timetravel_java.repository.LatestVersionRepository;
import com.lnnktrn.timetravel_java.repository.RecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    RecordRepository recordRepository;

    @Mock
    LatestVersionRepository latestVersionRepository;

    @InjectMocks
    RecordService service;

    @Test
    void getLatestRecord_returnsEntity_whenFound() {
        Long id = 42L;
        RecordEntity entity = RecordEntity.builder()
                .recordId(RecordId.builder().id(id).version(7L).build())
                .data("{\"k\":\"v\"}")
                .build();

        when(latestVersionRepository.findLatestRecordById(id))
                .thenReturn(Optional.of(entity));

        RecordEntity result = service.getLatestRecord(id);

        assertNotNull(result);
        assertEquals(42L, result.getRecordId().getId());
        assertEquals(7L, result.getRecordId().getVersion());
        verify(latestVersionRepository).findLatestRecordById(id);
        verifyNoInteractions(recordRepository);
    }

    @Test
    void getLatestRecord_throws_whenNotFound() {
        Long id = 42L;

        when(latestVersionRepository.findLatestRecordById(id))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchRecordException.class, () -> service.getLatestRecord(id));
        verify(latestVersionRepository).findLatestRecordById(id);
        verifyNoInteractions(recordRepository);
    }


    @Test
    void getRecord_returnsEntity_whenFound() {
        Long id = 5L;
        Long version = 2L;
        RecordId key = RecordId.builder().id(id).version(version).build();

        RecordEntity entity = RecordEntity.builder()
                .recordId(key)
                .data("{\"x\":\"y\"}")
                .build();

        when(recordRepository.findById(eq(key))).thenReturn(Optional.of(entity));

        RecordEntity result = service.getRecord(id, version);

        assertEquals(id, result.getRecordId().getId());
        assertEquals(version, result.getRecordId().getVersion());
        verify(recordRepository).findById(eq(key));
        verifyNoInteractions(latestVersionRepository);
    }

    @Test
    void getRecord_throws_whenNotFound() {
        Long id = 5L;
        Long version = 2L;
        RecordId key = RecordId.builder().id(id).version(version).build();

        when(recordRepository.findById(eq(key))).thenReturn(Optional.empty());

        assertThrows(NoSuchRecordException.class, () -> service.getRecord(id, version));
        verify(recordRepository).findById(eq(key));
        verifyNoInteractions(latestVersionRepository);
    }

    @Test
    void listVersions_returnsList_whenNotEmpty() {
        Long id = 7L;

        List<RecordEntity> list = List.of(
                RecordEntity.builder()
                        .recordId(RecordId.builder().id(id).version(1L).build())
                        .data("{\"v\":\"1\"}")
                        .build(),
                RecordEntity.builder()
                        .recordId(RecordId.builder().id(id).version(2L).build())
                        .data("{\"v\":\"2\"}")
                        .build()
        );

        when(recordRepository.findAllByRecordId_IdOrderByRecordId_VersionAsc(id))
                .thenReturn(list);

        List<RecordEntity> result = service.listVersions(id);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getRecordId().getVersion());
        assertEquals(2L, result.get(1).getRecordId().getVersion());
        verify(recordRepository).findAllByRecordId_IdOrderByRecordId_VersionAsc(id);
        verifyNoInteractions(latestVersionRepository);
    }

    @Test
    void listVersions_throws_whenEmpty() {
        Long id = 7L;

        when(recordRepository.findAllByRecordId_IdOrderByRecordId_VersionAsc(id))
                .thenReturn(List.of());

        assertThrows(NoSuchRecordException.class, () -> service.listVersions(id));
        verify(recordRepository).findAllByRecordId_IdOrderByRecordId_VersionAsc(id);
        verifyNoInteractions(latestVersionRepository);
    }

    @Test
    void updateLatestVersionById_savesNewEntityWithIncrementedVersion_whenExisting() {
        Long id = 100L;
        String data = "{\"n\":\"v\"}";

        RecordEntity existing = RecordEntity.builder()
                .recordId(RecordId.builder().id(id).version(7L).build())
                .data("{\"old\":\"data\"}")
                .build();

        when(latestVersionRepository.findLatestRecordById(id)).thenReturn(Optional.of(existing));
        when(recordRepository.save(any(RecordEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.updateLatestVersionById(id, data);

        ArgumentCaptor<RecordEntity> captor = ArgumentCaptor.forClass(RecordEntity.class);
        verify(recordRepository).save(captor.capture());

        RecordEntity saved = captor.getValue();
        assertEquals(id, saved.getRecordId().getId());
        assertEquals(8L, saved.getRecordId().getVersion()); // +1
        assertEquals(data, saved.getData());

        verify(latestVersionRepository).findLatestRecordById(id);
        verifyNoMoreInteractions(latestVersionRepository);
    }

    @Test
    void updateLatestVersionById_throws_whenNoExisting() {
        Long id = 100L;

        when(latestVersionRepository.findLatestRecordById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchRecordException.class, () -> service.updateLatestVersionById(id, "{\"x\":\"y\"}"));

        verify(latestVersionRepository).findLatestRecordById(id);
        verifyNoInteractions(recordRepository);
    }

    @Test
    void upsertLatestVersion_returnsTrue_whenExistingAndUpdatesPointer() {
        Long id = 1L;
        String data = "{\"a\":\"b\"}";

        RecordEntity existing = RecordEntity.builder()
                .recordId(RecordId.builder().id(id).version(3L).build())
                .data("{\"old\":\"data\"}")
                .build();

        when(latestVersionRepository.findLatestRecordById(id)).thenReturn(Optional.of(existing));
        when(recordRepository.save(any(RecordEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(latestVersionRepository.updateVersionById(eq(id), anyLong())).thenReturn(1);

        boolean existed = service.UpsertLatestVersion(id, data);

        assertTrue(existed);

        // saved record should be version 4
        ArgumentCaptor<RecordEntity> captor = ArgumentCaptor.forClass(RecordEntity.class);
        verify(recordRepository).save(captor.capture());
        assertEquals(4L, captor.getValue().getRecordId().getVersion());

        verify(latestVersionRepository).updateVersionById(id, 4L);
    }

    @Test
    void upsertLatestVersion_returnsFalse_whenNoExisting_startsAtVersion1() {
        Long id = 1L;
        String data = "{\"a\":\"b\"}";

        when(latestVersionRepository.findLatestRecordById(id)).thenReturn(Optional.empty());
        when(recordRepository.save(any(RecordEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(latestVersionRepository.updateVersionById(eq(id), anyLong())).thenReturn(0);

        boolean existed = service.UpsertLatestVersion(id, data);

        assertFalse(existed);

        ArgumentCaptor<RecordEntity> captor = ArgumentCaptor.forClass(RecordEntity.class);
        verify(recordRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getRecordId().getVersion());

        verify(latestVersionRepository).updateVersionById(id, 1L);
    }


}