package com.lnnktrn.timetravel_java.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "records")
public class RecordEntity {

    @EmbeddedId
    private RecordId recordId;
    
    @Column(nullable = false)
    private String data;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public RecordEntity(RecordId recordId, String data, Instant createdAt, Instant updatedAt) {
        this.recordId = recordId;
        this.data = data;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public RecordEntity() {
    }

    public RecordId getRecordId() {
        return recordId;
    }

    public void setRecordId(RecordId id) {
        this.recordId = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }


    public static RecordEntityBuilder builder() {
        return new RecordEntityBuilder();
    }

    public static class RecordEntityBuilder {
        private RecordId recordId;
        private String data;
        private Instant createdAt;
        private Instant updatedAt;

        RecordEntityBuilder() {
        }

        public RecordEntityBuilder recordId(RecordId recordId) {
            this.recordId = recordId;
            return this;
        }

        public RecordEntityBuilder data(String data) {
            this.data = data;
            return this;
        }

        public RecordEntityBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RecordEntityBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public RecordEntity build() {
            return new RecordEntity(recordId, data, createdAt, updatedAt);
        }
    }
}