package com.lnnktrn.timetravel_java.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.Instant;

@Embeddable
public class RecordId implements Serializable {

    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "version", nullable = false)
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    protected RecordId() {
    }

    protected RecordId(Long id, Long version) {
        this.id = id;
        this.version = version;
    }

    public static RecordId.RecordIdBuilder builder() {
        return new RecordId.RecordIdBuilder();
    }

    public static class RecordIdBuilder {
        private Long id;
        private Long version;


        RecordIdBuilder() {
        }

        public RecordId.RecordIdBuilder recordId(Long id, Long version) {
            this.id = id;
            this.version = version;
            return this;
        }

        public RecordId.RecordIdBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RecordId.RecordIdBuilder version(Long version) {
            this.version = version;
            return this;
        }

        public RecordId build() {
            return new RecordId(id, version);
        }
    }


}