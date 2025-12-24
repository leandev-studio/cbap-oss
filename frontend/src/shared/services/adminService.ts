import apiClient from '../api/client';

// Re-export apiClient for consistency
export { default as apiClient } from '../api/client';

/**
 * Reindex result
 */
export interface ReindexResult {
  entityId: string;
  totalRecords: number;
  indexedRecords: number;
  message: string;
}

/**
 * User Management Types
 */
export interface User {
  userId: string;
  username: string;
  email: string | null;
  status: string;
  tenantId: string | null;
  facilityId: string | null;
  roles: string[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  username: string;
  password: string;
  email?: string;
  roles?: string[];
}

export interface UpdateUserRequest {
  email?: string;
  status?: string;
  roles?: string[];
}

/**
 * Role Management Types
 */
export interface Role {
  roleId: string;
  roleName: string;
  description: string | null;
  tenantId: string | null;
  permissions: Permission[];
  createdAt: string;
  updatedAt: string;
}

export interface Permission {
  permissionId: string;
  permissionName: string;
  resourceType: string;
  action: string;
  description: string | null;
  createdAt: string;
}

export interface CreateRoleRequest {
  roleName: string;
  description?: string;
  permissions?: string[];
}

export interface UpdateRoleRequest {
  description?: string;
  permissions?: string[];
}

/**
 * Workflow Management Types
 */
export interface WorkflowDefinition {
  workflowId: string;
  name: string;
  description: string | null;
  initialState: string;
  metadataJson: Record<string, any> | null;
  states: WorkflowState[];
  transitions: WorkflowTransition[];
}

export interface WorkflowState {
  stateId: string;
  stateName: string;
  label: string | null;
  labelKey: string | null;
  description: string | null;
  initial: boolean;
  final: boolean;
  metadataJson: Record<string, any> | null;
}

export interface WorkflowTransition {
  transitionId: string;
  fromState: string;
  toState: string;
  actionLabel: string;
  labelKey: string | null;
  description: string | null;
  conditionsJson: Record<string, any> | null;
  allowedRoles: string[] | null;
  preTransitionRules: Record<string, any> | null;
  metadataJson: Record<string, any> | null;
}

/**
 * Measure Management Types
 */
export interface Measure {
  measureId: string;
  measureIdentifier: string;
  version: number;
  name: string;
  description: string | null;
  parametersJson: Record<string, any> | null;
  returnType: string;
  dependsOnJson: Record<string, any> | null;
  definitionType: string;
  expression: string | null;
  metadataJson: Record<string, any> | null;
}

/**
 * Validation Rule Types
 */
export interface ValidationRule {
  validationId: string;
  entityId: string | null;
  propertyName: string | null;
  ruleName: string;
  description: string | null;
  scope: string;
  ruleType: string;
  expression: string | null;
  errorMessage: string | null;
  errorMessageKey: string | null;
  triggerEvents: string[] | null;
  conditionsJson: Record<string, any> | null;
  metadataJson: Record<string, any> | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateValidationRuleRequest {
  entityId: string;
  propertyName?: string;
  ruleName: string;
  description?: string;
  scope: string;
  ruleType: string;
  expression?: string;
  errorMessage?: string;
  errorMessageKey?: string;
  triggerEvents?: string[];
  conditionsJson?: Record<string, any>;
  metadataJson?: Record<string, any>;
}

export interface UpdateValidationRuleRequest {
  ruleName?: string;
  description?: string;
  scope?: string;
  ruleType?: string;
  expression?: string;
  errorMessage?: string;
  errorMessageKey?: string;
  triggerEvents?: string[];
  conditionsJson?: Record<string, any>;
  metadataJson?: Record<string, any>;
}

/**
 * System Configuration Types
 */
export interface SystemSettings {
  applicationName?: string;
  version?: string;
  maxFileUploadSize?: string;
  sessionTimeout?: number;
  enableAuditLogging?: boolean;
  [key: string]: any;
}

export interface LicensingStatus {
  licenseType: string;
  status: string;
  expirationDate: string | null;
  maxUsers: number | null;
  currentUsers: number;
  features: string[];
}

/**
 * Admin Service
 * 
 * Handles admin operations.
 */

// Reindex
export async function reindexEntity(entityId: string): Promise<ReindexResult> {
  const response = await apiClient.post<ReindexResult>(`/admin/entities/${entityId}/reindex`);
  return response.data;
}

// User Management
export async function getAllUsers(): Promise<User[]> {
  const response = await apiClient.get<{ users: User[]; count: number }>('/users');
  return response.data.users;
}

export async function getUser(userId: string): Promise<User> {
  const response = await apiClient.get<User>(`/users/${userId}`);
  return response.data;
}

export async function createUser(request: CreateUserRequest): Promise<User> {
  const response = await apiClient.post<User>('/users', request);
  return response.data;
}

export async function updateUser(userId: string, request: UpdateUserRequest): Promise<User> {
  const response = await apiClient.put<User>(`/users/${userId}`, request);
  return response.data;
}

export async function deleteUser(userId: string): Promise<void> {
  await apiClient.delete(`/users/${userId}`);
}

// Role Management
export async function getAllRoles(): Promise<Role[]> {
  const response = await apiClient.get<{ roles: Role[]; count: number }>('/admin/roles');
  return response.data.roles;
}

export async function getRole(roleId: string): Promise<Role> {
  const response = await apiClient.get<Role>(`/admin/roles/${roleId}`);
  return response.data;
}

export async function createRole(request: CreateRoleRequest): Promise<Role> {
  const response = await apiClient.post<Role>('/admin/roles', request);
  return response.data;
}

export async function updateRole(roleId: string, request: UpdateRoleRequest): Promise<Role> {
  const response = await apiClient.put<Role>(`/admin/roles/${roleId}`, request);
  return response.data;
}

export async function deleteRole(roleId: string): Promise<void> {
  await apiClient.delete(`/admin/roles/${roleId}`);
}

// Permission Management
export async function getAllPermissions(): Promise<Permission[]> {
  const response = await apiClient.get<{ permissions: Permission[]; count: number }>('/admin/permissions');
  return response.data.permissions;
}

export async function getPermission(permissionId: string): Promise<Permission> {
  const response = await apiClient.get<Permission>(`/admin/permissions/${permissionId}`);
  return response.data;
}

export async function assignPermissionsToRole(roleId: string, permissionNames: string[]): Promise<Role> {
  const response = await apiClient.post<Role>(`/admin/roles/${roleId}/permissions`, { permissions: permissionNames });
  return response.data;
}

export async function removePermissionsFromRole(roleId: string, permissionNames: string[]): Promise<Role> {
  const response = await apiClient.delete<Role>(`/admin/roles/${roleId}/permissions`, { data: { permissions: permissionNames } });
  return response.data;
}

// Workflow Management
export async function getAllWorkflows(): Promise<WorkflowDefinition[]> {
  const response = await apiClient.get<{ workflows: WorkflowDefinition[]; count: number }>('/metadata/workflows');
  return response.data.workflows;
}

export async function getWorkflow(workflowId: string): Promise<WorkflowDefinition> {
  const response = await apiClient.get<WorkflowDefinition>(`/metadata/workflows/${workflowId}`);
  return response.data;
}

export async function createWorkflow(request: any): Promise<WorkflowDefinition> {
  const response = await apiClient.post<WorkflowDefinition>('/metadata/workflows', request);
  return response.data;
}

export async function updateWorkflow(workflowId: string, request: any): Promise<WorkflowDefinition> {
  const response = await apiClient.put<WorkflowDefinition>(`/metadata/workflows/${workflowId}`, request);
  return response.data;
}

export async function deleteWorkflow(workflowId: string): Promise<void> {
  await apiClient.delete(`/metadata/workflows/${workflowId}`);
}

// Measure Management
export async function getAllMeasures(): Promise<Measure[]> {
  const response = await apiClient.get<{ measures: Measure[]; count: number }>('/metadata/measures');
  return response.data.measures;
}

export async function getMeasure(measureIdentifier: string, version?: number): Promise<Measure> {
  const params = version ? { version } : {};
  const response = await apiClient.get<{ measure: Measure }>(`/metadata/measures/${measureIdentifier}`, { params });
  return response.data.measure;
}

export async function createOrUpdateMeasure(measure: Measure): Promise<Measure> {
  const response = await apiClient.post<{ measure: Measure }>('/metadata/measures', measure);
  return response.data.measure;
}

export async function updateMeasure(measureIdentifier: string, version: number | undefined, measure: Measure): Promise<Measure> {
  const params = version ? { version } : {};
  const response = await apiClient.put<{ measure: Measure }>(`/metadata/measures/${measureIdentifier}`, measure, { params });
  return response.data.measure;
}

export async function deleteMeasure(measureIdentifier: string, version?: number): Promise<void> {
  const params = version ? { version } : {};
  await apiClient.delete(`/metadata/measures/${measureIdentifier}`, { params });
}

// Validation Rule Management
export async function getValidationRules(entityId?: string): Promise<ValidationRule[]> {
  const params = entityId ? { entityId } : {};
  const response = await apiClient.get<{ validationRules: ValidationRule[]; count: number }>('/admin/validation-rules', { params });
  return response.data.validationRules;
}

export async function getValidationRule(validationId: string): Promise<ValidationRule> {
  const response = await apiClient.get<ValidationRule>(`/admin/validation-rules/${validationId}`);
  return response.data;
}

export async function createValidationRule(request: CreateValidationRuleRequest): Promise<ValidationRule> {
  const response = await apiClient.post<ValidationRule>('/admin/validation-rules', request);
  return response.data;
}

export async function updateValidationRule(validationId: string, request: UpdateValidationRuleRequest): Promise<ValidationRule> {
  const response = await apiClient.put<ValidationRule>(`/admin/validation-rules/${validationId}`, request);
  return response.data;
}

export async function deleteValidationRule(validationId: string): Promise<void> {
  await apiClient.delete(`/admin/validation-rules/${validationId}`);
}

// System Configuration
export async function getSystemSettings(): Promise<SystemSettings> {
  const response = await apiClient.get<SystemSettings>('/admin/system/settings');
  return response.data;
}

export async function updateSystemSettings(settings: SystemSettings): Promise<SystemSettings> {
  const response = await apiClient.put<SystemSettings>('/admin/system/settings', settings);
  return response.data;
}

export async function getLicensingStatus(): Promise<LicensingStatus> {
  const response = await apiClient.get<LicensingStatus>('/admin/system/licensing');
  return response.data;
}
