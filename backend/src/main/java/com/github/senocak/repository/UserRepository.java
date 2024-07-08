package com.github.senocak.repository;

import com.github.senocak.domain.User;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository extends CouchbaseRepository<User, UUID> {
    User findByEmail(String email);
    boolean existsByEmail(String email);
}