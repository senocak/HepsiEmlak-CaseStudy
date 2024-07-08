package com.github.senocak.domain;

import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;
import org.springframework.data.couchbase.repository.Collection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document
@Collection("user-collection")
public class User extends BaseDomain {
    @Field private String name;
    @Field private String email;
    @Field private String password;
    @Field private List<String> roles = new ArrayList<>();
    @Field private Date emailActivatedAt;

    public User(String name, String email, String password, List<String> roles, Date emailActivatedAt) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.emailActivatedAt = emailActivatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Date getEmailActivatedAt() {
        return emailActivatedAt;
    }

    public void setEmailActivatedAt(Date emailActivatedAt) {
        this.emailActivatedAt = emailActivatedAt;
    }
}
