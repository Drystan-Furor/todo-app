package com.example.todo.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        AuthUserResponse user
) {
}
