package com.github.senocak.repository;

import com.github.senocak.domain.TodoItem;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface TodoItemRepository extends CouchbaseRepository<TodoItem, UUID> {
    public Page<TodoItem> findAllByOwner(UUID owner, Pageable pageable);
}
