package com.lnnktrn.timetravel_java.service;

import com.lnnktrn.timetravel_java.entity.RecordEntity;
import com.lnnktrn.timetravel_java.entity.RecordId;
import com.lnnktrn.timetravel_java.repository.LatestVersionRepository;
import com.lnnktrn.timetravel_java.repository.RecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    RecordRepository repo;

    @Mock
    LatestVersionRepository latestVersionRepository;

    @InjectMocks
    RecordService service;

    @Test
    void getLatestRecord_delegatesToLatestVersionRepository() {
        Long id = 42L;
        RecordEntity entity = RecordEntity.builder()
                .recordId(RecordId.builder().id(id).version(7L).build())
                .data("{\"a\":\"1\"}")
                .build();

        when(latestVersionRepository.findLatestRecordById(id)).thenReturn(Optional.of(entity));

        Optional<RecordEntity> result = service.getLatestRecord(id);

        assertTrue(result.isPresent());
        assertEquals(7L, result.get().getRecordId().getVersion());
        verify(latestVersionRepository).findLatestRecordById(id);
        verifyNoInteractions(repo);
    }
}