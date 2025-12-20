import apiClient from '../api/client';

/**
 * Task DTO
 */
export interface Task {
  taskId: string;
  entityId: string;
  recordId: string;
  title: string;
  description?: string;
  assigneeId: string;
  assigneeUsername: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  dueDate?: string;
  workflowState?: string;
  transitionId?: string;
  completedAt?: string;
  completedById?: string;
  decision?: 'APPROVED' | 'REJECTED' | 'REQUEST_CHANGES';
  decisionComments?: string;
  createdAt: string;
  updatedAt: string;
  createdById?: string;
  entityDisplayValue?: string;
}

/**
 * Paginated Tasks Response
 */
export interface PaginatedTasks {
  tasks: Task[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

/**
 * Task Decision Request
 */
export interface TaskDecisionRequest {
  decision: 'APPROVED' | 'REJECTED' | 'REQUEST_CHANGES';
  comments?: string;
}

/**
 * Task Service
 * 
 * Handles task-related API calls.
 */
export async function getUserTasks(
  status?: string,
  page: number = 0,
  size: number = 20
): Promise<PaginatedTasks> {
  const params: Record<string, any> = { page, size };
  if (status) {
    params.status = status;
  }
  
  const response = await apiClient.get<PaginatedTasks>('/tasks', { params });
  return response.data;
}

export async function getTask(taskId: string): Promise<Task> {
  const response = await apiClient.get<Task>(`/tasks/${taskId}`);
  return response.data;
}

export async function completeTask(taskId: string): Promise<Task> {
  await apiClient.post<{
    taskId: string;
    status: string;
    completedAt: string;
  }>(`/tasks/${taskId}/complete`);
  
  // Fetch the updated task to get full details
  return getTask(taskId);
}

export async function submitDecision(
  taskId: string,
  decision: 'APPROVED' | 'REJECTED' | 'REQUEST_CHANGES',
  comments?: string
): Promise<Task> {
  await apiClient.post<{
    taskId: string;
    status: string;
    decision: string;
    decisionComments?: string;
    completedAt: string;
  }>(`/tasks/${taskId}/decisions`, {
    decision,
    comments,
  } as TaskDecisionRequest);
  
  // Fetch the updated task to get full details
  return getTask(taskId);
}

/**
 * Task Transition
 */
export interface TaskTransition {
  transitionId: string;
  fromState: string;
  toState: string;
  actionLabel: string;
  description?: string;
}

/**
 * Get available transitions for a task
 */
export async function getAvailableTaskTransitions(taskId: string): Promise<TaskTransition[]> {
  const response = await apiClient.get<{
    taskId: string;
    transitions: TaskTransition[];
    count: number;
  }>(`/tasks/${taskId}/transitions`);
  return response.data.transitions;
}

/**
 * Execute a workflow transition for a task
 */
export async function executeTaskTransition(
  taskId: string,
  transitionId: string,
  comments?: string
): Promise<Task> {
  await apiClient.post<{
    taskId: string;
    status: string;
    fromState: string;
    toState: string;
  }>(`/tasks/${taskId}/transitions/${transitionId}`, {
    comments,
  });
  
  // Fetch the updated task to get full details
  return getTask(taskId);
}
