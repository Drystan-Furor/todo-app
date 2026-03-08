package com.example.todo.todo;

import com.example.todo.auth.AuthenticatedUser;
import com.example.todo.todo.dto.TodoPageResponse;
import com.example.todo.todo.dto.TodoRequest;
import com.example.todo.todo.dto.TodoResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    public TodoResponse create(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody TodoRequest request
    ) {
        return todoService.create(currentUser.id(), request);
    }

    @GetMapping
    public TodoPageResponse list(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return todoService.list(currentUser.id(), completed, q, page, size);
    }

    @GetMapping("/{id}")
    public TodoResponse get(@AuthenticationPrincipal AuthenticatedUser currentUser, @PathVariable Long id) {
        return todoService.get(currentUser.id(), id);
    }

    @PutMapping("/{id}")
    public TodoResponse update(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody TodoRequest request
    ) {
        return todoService.update(currentUser.id(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal AuthenticatedUser currentUser, @PathVariable Long id) {
        todoService.delete(currentUser.id(), id);
    }
}
