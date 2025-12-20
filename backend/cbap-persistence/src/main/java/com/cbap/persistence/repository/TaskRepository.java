package com.cbap.persistence.repository;

import com.cbap.persistence.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for tasks.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Find tasks assigned to a user.
     */
    @Query("SELECT t FROM Task t WHERE t.assignee.userId = :userId ORDER BY t.createdAt DESC")
    Page<Task> findByAssigneeId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find tasks assigned to a user with specific status.
     */
    @Query("SELECT t FROM Task t WHERE t.assignee.userId = :userId AND t.status = :status ORDER BY t.createdAt DESC")
    Page<Task> findByAssigneeIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") Task.TaskStatus status,
            Pageable pageable);

    /**
     * Find tasks for a specific record.
     */
    @Query("SELECT t FROM Task t WHERE t.record.recordId = :recordId ORDER BY t.createdAt DESC")
    List<Task> findByRecordId(@Param("recordId") UUID recordId);

    /**
     * Find tasks for a specific entity.
     */
    @Query("SELECT t FROM Task t WHERE t.entity.entityId = :entityId ORDER BY t.createdAt DESC")
    Page<Task> findByEntityId(@Param("entityId") String entityId, Pageable pageable);

    /**
     * Find task by ID.
     */
    @Query("SELECT t FROM Task t WHERE t.taskId = :taskId")
    Optional<Task> findByTaskId(@Param("taskId") UUID taskId);

    /**
     * Count open tasks for a user.
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.userId = :userId AND t.status IN ('OPEN', 'IN_PROGRESS')")
    long countOpenTasksByAssigneeId(@Param("userId") UUID userId);
}
