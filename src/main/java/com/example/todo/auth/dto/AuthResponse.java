package com.example.todo.auth.dto;

public record AuthResponse(
        String token,
        String accessToken,
        String refreshToken,
        String tokenType,
        AuthUserResponse user
) {
}
