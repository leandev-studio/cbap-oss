package com.cbap.persistence.repository;

import com.cbap.persistence.entity.WorkflowState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for workflow states.
 */
@Repository
public interface WorkflowStateRepository extends JpaRepository<WorkflowState, java.util.UUID> {

    /**
     * Find all states for a workflow.
     */
    @Query("SELECT s FROM WorkflowState s WHERE s.workflow.workflowId = :workflowId ORDER BY s.stateName ASC")
    List<WorkflowState> findByWorkflowId(@Param("workflowId") String workflowId);

    /**
     * Find state by workflow ID and state name.
     */
    @Query("SELECT s FROM WorkflowState s WHERE s.workflow.workflowId = :workflowId AND s.stateName = :stateName")
    Optional<WorkflowState> findByWorkflowIdAndStateName(
            @Param("workflowId") String workflowId,
            @Param("stateName") String stateName);
}
