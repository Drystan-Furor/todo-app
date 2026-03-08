package com.example.todo.auth.dto;

import java.time.Instant;

public record AuthUserResponse(
        Long id,
        String email,
        Instant createdAt
) {
}
