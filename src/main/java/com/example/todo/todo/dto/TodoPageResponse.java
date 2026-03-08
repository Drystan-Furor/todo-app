package com.example.todo.todo.dto;

import com.example.todo.todo.Todo;
import java.util.List;
import org.springframework.data.domain.Page;

public record TodoPageResponse(
        List<TodoResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static TodoPageResponse from(Page<Todo> todoPage) {
        return new TodoPageResponse(
                todoPage.getContent().stream()
                        .map(TodoResponse::from)
                        .toList(),
                todoPage.getNumber(),
                todoPage.getSize(),
                todoPage.getTotalElements(),
                todoPage.getTotalPages()
        );
    }
}
