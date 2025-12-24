import apiClient from '../api/client';

/**
 * Property Definition DTO
 */
export interface PropertyDefinition {
  propertyId: string;
  propertyName: string;
  propertyType: 'string' | 'number' | 'date' | 'boolean' | 'singleSelect' | 'multiSelect' | 'reference' | 'calculated';
  label?: string;
  labelKey?: string;
  required: boolean;
  readOnly: boolean;
  denormalize: boolean;
  referenceEntityId?: string;
  calculationExpression?: string;
  metadataJson?: Record<string, any>;
  description?: string;
}

/**
 * Entity Definition DTO
 */
export interface EntityDefinition {
  entityId: string;
  name: string;
  description?: string;
  schemaVersion: number;
  screenVersion: number;
  workflowId?: string;
  authorizationModel?: string;
  scope?: 'LOCAL' | 'GLOBAL' | 'SHARED';
  metadataJson?: Record<string, any>;
  properties: PropertyDefinition[];
}

/**
 * Entity Metadata Service
 * 
 * Handles fetching entity metadata definitions.
 */
export async function getAllEntities(): Promise<EntityDefinition[]> {
  const response = await apiClient.get<{ entities: EntityDefinition[]; count: number }>('/metadata/entities');
  return response.data.entities || [];
}

export async function getEntityById(entityId: string): Promise<EntityDefinition> {
  const response = await apiClient.get<EntityDefinition>(`/metadata/entities/${entityId}`);
  return response.data;
}

/**
 * Requests and mutations for managing entity definitions
 * from the admin UI.
 */
export interface CreateEntityRequest {
  entityId: string;
  name: string;
  description?: string;
  schemaVersion?: number;
  screenVersion?: number;
  workflowId?: string;
  authorizationModel?: string;
  scope?: 'LOCAL' | 'GLOBAL' | 'SHARED';
  metadataJson?: Record<string, any>;
  properties?: Array<{
    propertyName: string;
    propertyType: string;
    label?: string;
    labelKey?: string;
    required?: boolean;
    readOnly?: boolean;
    denormalize?: boolean;
    referenceEntityId?: string;
    calculationExpression?: string;
    metadataJson?: Record<string, any>;
  }>;
}

export interface UpdateEntityRequest {
  name?: string;
  description?: string;
  schemaVersion?: number;
  screenVersion?: number;
  workflowId?: string;
  authorizationModel?: string;
  scope?: 'LOCAL' | 'GLOBAL' | 'SHARED';
  metadataJson?: Record<string, any>;
}

export async function createEntity(request: CreateEntityRequest): Promise<EntityDefinition> {
  const response = await apiClient.post<EntityDefinition>('/metadata/entities', request);
  return response.data;
}

export async function updateEntity(entityId: string, request: UpdateEntityRequest): Promise<EntityDefinition> {
  const response = await apiClient.put<EntityDefinition>(`/metadata/entities/${entityId}`, request);
  return response.data;
}

export async function deleteEntity(entityId: string): Promise<void> {
  await apiClient.delete(`/metadata/entities/${entityId}`);
}
