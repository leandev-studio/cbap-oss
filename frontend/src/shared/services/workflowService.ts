import apiClient from '../api/client';

/**
 * Available Transition DTO
 */
export interface AvailableTransition {
  transitionId: string;
  fromState: string;
  toState: string;
  actionLabel: string;
  labelKey?: string;
  description?: string;
  allowedRoles?: string[];
}

/**
 * Workflow Audit Log Entry DTO
 */
export interface WorkflowAuditLogEntry {
  auditId: string;
  entityId: string;
  recordId: string;
  workflowId: string;
  fromState: string;
  toState: string;
  transitionId: string;
  transitionLabel: string;
  performedBy: string;
  performedByUsername?: string;
  performedAt: string;
  comments?: string;
  metadataJson?: Record<string, any>;
}

/**
 * Transition Request
 */
export interface TransitionRequest {
  comments?: string;
}

/**
 * Transition Result
 */
export interface TransitionResult {
  entityId: string;
  recordId: string;
  fromState: string;
  toState: string;
  transitionId: string;
  transitionLabel: string;
  performedBy: string;
  performedAt: string;
  comments?: string;
}

/**
 * Workflow Service
 * 
 * Handles workflow-related API calls.
 */
export async function getAvailableTransitions(
  entityId: string,
  recordId: string
): Promise<AvailableTransition[]> {
  const response = await apiClient.get<{
    entityId: string;
    recordId: string;
    transitions: AvailableTransition[];
    count: number;
  }>(`/entities/${entityId}/records/${recordId}/transitions`);
  return response.data.transitions;
}

export async function executeTransition(
  entityId: string,
  recordId: string,
  transitionId: string,
  comments?: string
): Promise<TransitionResult> {
  const response = await apiClient.post<TransitionResult>(
    `/entities/${entityId}/records/${recordId}/transitions/${transitionId}`,
    { comments } as TransitionRequest
  );
  return response.data;
}

export async function getWorkflowAuditLog(
  entityId: string,
  recordId: string
): Promise<WorkflowAuditLogEntry[]> {
  const response = await apiClient.get<{
    entityId: string;
    recordId: string;
    auditLog: WorkflowAuditLogEntry[];
    count: number;
  }>(`/entities/${entityId}/records/${recordId}/workflow-audit`);
  return response.data.auditLog;
}
