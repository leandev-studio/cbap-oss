package com.cbap.persistence.repository;

import com.cbap.persistence.entity.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for workflow transitions.
 */
@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, UUID> {

    /**
     * Find all transitions for a workflow.
     */
    @Query("SELECT t FROM WorkflowTransition t WHERE t.workflow.workflowId = :workflowId ORDER BY t.fromState ASC, t.toState ASC")
    List<WorkflowTransition> findByWorkflowId(@Param("workflowId") String workflowId);

    /**
     * Find transitions from a specific state.
     */
    @Query("SELECT t FROM WorkflowTransition t WHERE t.workflow.workflowId = :workflowId AND t.fromState = :fromState ORDER BY t.toState ASC")
    List<WorkflowTransition> findByWorkflowIdAndFromState(
            @Param("workflowId") String workflowId,
            @Param("fromState") String fromState);

    /**
     * Find transition by workflow ID, from state, and to state.
     */
    @Query("SELECT t FROM WorkflowTransition t WHERE t.workflow.workflowId = :workflowId AND t.fromState = :fromState AND t.toState = :toState")
    Optional<WorkflowTransition> findByWorkflowIdAndFromStateAndToState(
            @Param("workflowId") String workflowId,
            @Param("fromState") String fromState,
            @Param("toState") String toState);

    /**
     * Find transition by ID.
     */
    @Query("SELECT t FROM WorkflowTransition t WHERE t.transitionId = :transitionId")
    Optional<WorkflowTransition> findByTransitionId(@Param("transitionId") UUID transitionId);
}
