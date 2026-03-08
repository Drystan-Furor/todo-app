package com.example.todo.todo;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("""
            select t
            from Todo t
            where t.owner.id = :ownerId
              and (:completed is null or t.completed = :completed)
              and (
                    :query is null
                    or lower(t.title) like lower(concat('%', :query, '%'))
                    or lower(coalesce(t.description, '')) like lower(concat('%', :query, '%'))
              )
            """)
    Page<Todo> findPageByOwnerIdAndFilters(
            @Param("ownerId") Long ownerId,
            @Param("completed") Boolean completed,
            @Param("query") String query,
            Pageable pageable
    );

    Optional<Todo> findByIdAndOwnerId(Long id, Long ownerId);
}
