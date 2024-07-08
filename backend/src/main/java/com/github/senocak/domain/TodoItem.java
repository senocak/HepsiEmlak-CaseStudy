package com.github.senocak.domain;

import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;
import org.springframework.data.couchbase.repository.Collection;
import java.io.Serializable;
import java.util.UUID;

@Document
@Collection("todo-collection")
public class TodoItem extends BaseDomain implements Serializable {
    @Field private String description;
    @Field private UUID owner;
    @Field private Boolean finished;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }
}