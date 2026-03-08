package com.example.todo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 255) String password
) {
}
