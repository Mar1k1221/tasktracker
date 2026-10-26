package com.example.tasktracker.model;

import jakarta.persistence.*;


import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,length = 50)
    private String username;
    @Column(nullable = false,length = 100,name = "password_hash")
    private String passwordHash;
    @Column(nullable = false,length = 20)
    private String role;
    @Column(nullable = false,name = "created_at")
    private OffsetDateTime createdAt;
    protected AppUser(){

    }

    public AppUser(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }
    @PrePersist
    protected void onCreate(){
        if (createdAt == null){
            createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
