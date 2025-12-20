package com.cbap.api.service;

import com.cbap.persistence.entity.WorkflowDefinition;
import com.cbap.persistence.entity.WorkflowState;
import com.cbap.persistence.entity.WorkflowTransition;
import com.cbap.persistence.repository.WorkflowDefinitionRepository;
import com.cbap.persistence.repository.WorkflowStateRepository;
import com.cbap.persistence.repository.WorkflowTransitionRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing workflow metadata.
 */
@Service
public class WorkflowMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowMetadataService.class);

    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowStateRepository workflowStateRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;

    public WorkflowMetadataService(
            WorkflowDefinitionRepository workflowDefinitionRepository,
            WorkflowStateRepository workflowStateRepository,
            WorkflowTransitionRepository workflowTransitionRepository) {
        this.workflowDefinitionRepository = workflowDefinitionRepository;
        this.workflowStateRepository = workflowStateRepository;
        this.workflowTransitionRepository = workflowTransitionRepository;
    }

    /**
     * Get all workflow definitions.
     */
    @Transactional(readOnly = true)
    public List<WorkflowDefinitionDTO> getAllWorkflows() {
        List<WorkflowDefinition> workflows = workflowDefinitionRepository.findAllByOrderByNameAsc();
        // Initialize lazy collections for each workflow separately to avoid MultipleBagFetchException
        // Using Hibernate.initialize() is safe even with orphanRemoval=true because we're not replacing the collection
        for (WorkflowDefinition workflow : workflows) {
            Hibernate.initialize(workflow.getStates());
            Hibernate.initialize(workflow.getTransitions());
        }
        return workflows.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get workflow definition by ID with states and transitions.
     */
    @Transactional(readOnly = true)
    public WorkflowDefinitionDTO getWorkflowById(String workflowId) {
        WorkflowDefinition workflow = workflowDefinitionRepository
                .findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + workflowId));
        
        // Initialize lazy collections using Hibernate.initialize() to avoid MultipleBagFetchException
        // This is safe even with orphanRemoval=true because we're not replacing the collection
        Hibernate.initialize(workflow.getStates());
        Hibernate.initialize(workflow.getTransitions());
        
        return toDTO(workflow);
    }

    /**
     * Convert WorkflowDefinition entity to DTO.
     */
    private WorkflowDefinitionDTO toDTO(WorkflowDefinition workflow) {
        WorkflowDefinitionDTO dto = new WorkflowDefinitionDTO();
        dto.setWorkflowId(workflow.getWorkflowId());
        dto.setName(workflow.getName());
        dto.setDescription(workflow.getDescription());
        dto.setInitialState(workflow.getInitialState());
        dto.setMetadataJson(workflow.getMetadataJson());

        // Convert states
        if (workflow.getStates() != null) {
            dto.setStates(workflow.getStates().stream()
                    .map(this::stateToDTO)
                    .collect(Collectors.toList()));
        }

        // Convert transitions
        if (workflow.getTransitions() != null) {
            dto.setTransitions(workflow.getTransitions().stream()
                    .map(this::transitionToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * Convert WorkflowState entity to DTO.
     */
    private WorkflowStateDTO stateToDTO(WorkflowState state) {
        WorkflowStateDTO dto = new WorkflowStateDTO();
        dto.setStateId(state.getStateId().toString());
        dto.setStateName(state.getStateName());
        dto.setLabel(state.getLabel());
        dto.setLabelKey(state.getLabelKey());
        dto.setDescription(state.getDescription());
        dto.setInitial(state.getIsInitial());
        dto.setFinal(state.getIsFinal());
        dto.setMetadataJson(state.getMetadataJson());
        return dto;
    }

    /**
     * Convert WorkflowTransition entity to DTO.
     */
    private WorkflowTransitionDTO transitionToDTO(WorkflowTransition transition) {
        WorkflowTransitionDTO dto = new WorkflowTransitionDTO();
        dto.setTransitionId(transition.getTransitionId().toString());
        dto.setFromState(transition.getFromState());
        dto.setToState(transition.getToState());
        dto.setActionLabel(transition.getActionLabel());
        dto.setLabelKey(transition.getLabelKey());
        dto.setDescription(transition.getDescription());
        dto.setConditionsJson(transition.getConditionsJson());
        dto.setAllowedRoles(transition.getAllowedRoles());
        dto.setPreTransitionRules(transition.getPreTransitionRules());
        dto.setMetadataJson(transition.getMetadataJson());
        return dto;
    }

    /**
     * Workflow Definition DTO.
     */
    public static class WorkflowDefinitionDTO {
        private String workflowId;
        private String name;
        private String description;
        private String initialState;
        private java.util.Map<String, Object> metadataJson;
        private List<WorkflowStateDTO> states;
        private List<WorkflowTransitionDTO> transitions;

        // Getters and Setters
        public String getWorkflowId() { return workflowId; }
        public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getInitialState() { return initialState; }
        public void setInitialState(String initialState) { this.initialState = initialState; }
        public java.util.Map<String, Object> getMetadataJson() { return metadataJson; }
        public void setMetadataJson(java.util.Map<String, Object> metadataJson) { this.metadataJson = metadataJson; }
        public List<WorkflowStateDTO> getStates() { return states; }
        public void setStates(List<WorkflowStateDTO> states) { this.states = states; }
        public List<WorkflowTransitionDTO> getTransitions() { return transitions; }
        public void setTransitions(List<WorkflowTransitionDTO> transitions) { this.transitions = transitions; }
    }

    /**
     * Workflow State DTO.
     */
    public static class WorkflowStateDTO {
        private String stateId;
        private String stateName;
        private String label;
        private String labelKey;
        private String description;
        private Boolean initial;
        private Boolean final_;
        private java.util.Map<String, Object> metadataJson;

        // Getters and Setters
        public String getStateId() { return stateId; }
        public void setStateId(String stateId) { this.stateId = stateId; }
        public String getStateName() { return stateName; }
        public void setStateName(String stateName) { this.stateName = stateName; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getLabelKey() { return labelKey; }
        public void setLabelKey(String labelKey) { this.labelKey = labelKey; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Boolean getInitial() { return initial; }
        public void setInitial(Boolean initial) { this.initial = initial; }
        public Boolean getFinal() { return final_; }
        public void setFinal(Boolean final_) { this.final_ = final_; }
        public java.util.Map<String, Object> getMetadataJson() { return metadataJson; }
        public void setMetadataJson(java.util.Map<String, Object> metadataJson) { this.metadataJson = metadataJson; }
    }

    /**
     * Workflow Transition DTO.
     */
    public static class WorkflowTransitionDTO {
        private String transitionId;
        private String fromState;
        private String toState;
        private String actionLabel;
        private String labelKey;
        private String description;
        private java.util.Map<String, Object> conditionsJson;
        private List<String> allowedRoles;
        private java.util.Map<String, Object> preTransitionRules;
        private java.util.Map<String, Object> metadataJson;

        // Getters and Setters
        public String getTransitionId() { return transitionId; }
        public void setTransitionId(String transitionId) { this.transitionId = transitionId; }
        public String getFromState() { return fromState; }
        public void setFromState(String fromState) { this.fromState = fromState; }
        public String getToState() { return toState; }
        public void setToState(String toState) { this.toState = toState; }
        public String getActionLabel() { return actionLabel; }
        public void setActionLabel(String actionLabel) { this.actionLabel = actionLabel; }
        public String getLabelKey() { return labelKey; }
        public void setLabelKey(String labelKey) { this.labelKey = labelKey; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public java.util.Map<String, Object> getConditionsJson() { return conditionsJson; }
        public void setConditionsJson(java.util.Map<String, Object> conditionsJson) { this.conditionsJson = conditionsJson; }
        public List<String> getAllowedRoles() { return allowedRoles; }
        public void setAllowedRoles(List<String> allowedRoles) { this.allowedRoles = allowedRoles; }
        public java.util.Map<String, Object> getPreTransitionRules() { return preTransitionRules; }
        public void setPreTransitionRules(java.util.Map<String, Object> preTransitionRules) { this.preTransitionRules = preTransitionRules; }
        public java.util.Map<String, Object> getMetadataJson() { return metadataJson; }
        public void setMetadataJson(java.util.Map<String, Object> metadataJson) { this.metadataJson = metadataJson; }
    }
}
