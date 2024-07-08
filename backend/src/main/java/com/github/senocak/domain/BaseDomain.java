package com.github.senocak.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.couchbase.core.mapping.Field;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public abstract class BaseDomain implements Serializable {
    @Id private UUID id;
    @Field @CreatedDate private Date createdAt;
    @Field @LastModifiedBy private Date updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
