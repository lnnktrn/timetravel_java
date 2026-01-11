package com.lnnktrn.timetravel_java.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "latest_versions")
public class LatestVersionEntity {
    @Id
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
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

    private LatestVersionEntity(Long id, Long version) {
        this.id = id;
        this.version = version;
    }

    protected LatestVersionEntity() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long version;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public LatestVersionEntity build() {
            LatestVersionEntity entity = new LatestVersionEntity();
            entity.id = this.id;
            entity.version = this.version;
            return entity;
        }
    }

}
