package com.example.todo.todo;

import com.example.todo.common.ApiException;
import com.example.todo.todo.dto.TodoRequest;
import com.example.todo.todo.dto.TodoResponse;
import com.example.todo.user.User;
import com.example.todo.user.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public TodoService(TodoRepository todoRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TodoResponse create(Long ownerId, TodoRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

        Todo todo = new Todo();
        applyRequest(todo, request);
        todo.setOwner(owner);
        return TodoResponse.from(todoRepository.save(todo));
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> list(Long ownerId) {
        return todoRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(TodoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TodoResponse get(Long ownerId, Long todoId) {
        return TodoResponse.from(findOwnedTodo(ownerId, todoId));
    }

    @Transactional
    public TodoResponse update(Long ownerId, Long todoId, TodoRequest request) {
        Todo todo = findOwnedTodo(ownerId, todoId);
        applyRequest(todo, request);
        return TodoResponse.from(todo);
    }

    @Transactional
    public void delete(Long ownerId, Long todoId) {
        Todo todo = findOwnedTodo(ownerId, todoId);
        todoRepository.delete(todo);
    }

    private Todo findOwnedTodo(Long ownerId, Long todoId) {
        return todoRepository.findByIdAndOwnerId(todoId, ownerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Todo not found"));
    }

    private void applyRequest(Todo todo, TodoRequest request) {
        todo.setTitle(request.title());
        todo.setDescription(request.description());
        todo.setCompleted(Boolean.TRUE.equals(request.completed()));
        todo.setDueDate(request.dueDate());
    }
}
