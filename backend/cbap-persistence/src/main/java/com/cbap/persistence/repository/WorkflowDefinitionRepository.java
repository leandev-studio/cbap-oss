package com.cbap.persistence.repository;

import com.cbap.persistence.entity.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for workflow definitions.
 */
@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, String> {

    /**
     * Find workflow by ID with states and transitions.
     */
    @Query("SELECT DISTINCT w FROM WorkflowDefinition w " +
           "LEFT JOIN FETCH w.states s " +
           "LEFT JOIN FETCH w.transitions t " +
           "WHERE w.workflowId = :workflowId")
    Optional<WorkflowDefinition> findByWorkflowIdWithStatesAndTransitions(@Param("workflowId") String workflowId);

    /**
     * Find all workflows.
     */
    List<WorkflowDefinition> findAllByOrderByNameAsc();
}
