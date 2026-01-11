package com.lnnktrn.timetravel_java.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.lnnktrn.timetravel_java.helper.JsonNodeConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "records")
public class RecordEntity {

    @EmbeddedId
    private RecordId recordId;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "data", columnDefinition = "TEXT")
    private JsonNode data;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public RecordEntity(RecordId recordId, JsonNode data, Instant createdAt) {
        this.recordId = recordId;
        this.data = data;
        this.createdAt = createdAt;
    }

    public RecordEntity() {
    }

    public RecordId getRecordId() {
        return recordId;
    }

    public void setRecordId(RecordId id) {
        this.recordId = id;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }
    

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static RecordEntityBuilder builder() {
        return new RecordEntityBuilder();
    }

    public static class RecordEntityBuilder {
        private RecordId recordId;
        private JsonNode data;
        private Instant createdAt;

        RecordEntityBuilder() {
        }

        public RecordEntityBuilder recordId(RecordId recordId) {
            this.recordId = recordId;
            return this;
        }

        public RecordEntityBuilder data(JsonNode data) {
            this.data = data;
            return this;
        }

        public RecordEntityBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RecordEntity build() {
            return new RecordEntity(recordId, data, createdAt);
        }
    }
}