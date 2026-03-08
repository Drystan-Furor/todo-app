package com.example.todo.todo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Todo> findByIdAndOwnerId(Long id, Long ownerId);
}
