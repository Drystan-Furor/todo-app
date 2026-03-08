package com.example.todo.todo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TodoRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        Boolean completed,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dueDate
) {
}
