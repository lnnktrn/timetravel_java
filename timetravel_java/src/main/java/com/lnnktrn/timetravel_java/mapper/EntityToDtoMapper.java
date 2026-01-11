package com.lnnktrn.timetravel_java.mapper;

import com.lnnktrn.timetravel_java.dto.RecordDto;
import com.lnnktrn.timetravel_java.entity.RecordEntity;

public class EntityToDtoMapper {
    public static RecordDto map(RecordEntity recordEntity) {
       return new RecordDto(
                recordEntity.getRecordId().getId(),
                recordEntity.getRecordId().getVersion(),
                recordEntity.getData().toString(),
                recordEntity.getCreatedAt(),   // убери если нет поля
                recordEntity.getUpdatedAt());
    }
}
