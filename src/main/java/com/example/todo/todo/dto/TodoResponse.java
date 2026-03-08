package com.example.todo.todo.dto;

import com.example.todo.todo.Todo;
import java.time.Instant;
import java.time.LocalDate;

public record TodoResponse(
        Long id,
        String title,
        String description,
        boolean completed,
        LocalDate dueDate,
        Long ownerId,
        Instant createdAt,
        Instant updatedAt
) {

    public static TodoResponse from(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getDueDate(),
                todo.getOwner().getId(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}
