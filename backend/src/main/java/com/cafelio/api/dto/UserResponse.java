package com.cafelio.api.dto;

import com.cafelio.api.model.User;

import java.time.Instant;
import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private boolean emailVerified;
    private Instant createdAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.emailVerified = user.isEmailVerified();
        this.createdAt = user.getCreatedAt();
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isEmailVerified() { return emailVerified; }
    public Instant getCreatedAt() { return createdAt; }
}
