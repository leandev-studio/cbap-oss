import apiClient from '../api/client';

/**
 * Entity Record DTO
 */
export interface EntityRecord {
  recordId: string;
  entityId: string;
  data: Record<string, any>;
  schemaVersion: number;
  state?: string;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

/**
 * Paginated Records Response
 */
export interface PaginatedRecords {
  records: EntityRecord[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

/**
 * Entity Record Service
 * 
 * Handles fetching entity records.
 */
export async function getRecords(
  entityId: string,
  page: number = 0,
  size: number = 20
): Promise<PaginatedRecords> {
  const response = await apiClient.get<PaginatedRecords>(`/entities/${entityId}/records`, {
    params: { page, size },
  });
  return response.data;
}

export async function getRecord(entityId: string, recordId: string): Promise<EntityRecord> {
  const response = await apiClient.get<EntityRecord>(`/entities/${entityId}/records/${recordId}`);
  return response.data;
}

/**
 * Create Record Request
 */
export interface CreateRecordRequest {
  data: Record<string, any>;
  state?: string;
}

/**
 * Update Record Request
 */
export interface UpdateRecordRequest {
  data: Record<string, any>;
  state?: string;
}

/**
 * Create a new entity record
 */
export async function createRecord(
  entityId: string,
  request: CreateRecordRequest
): Promise<EntityRecord> {
  const response = await apiClient.post<EntityRecord>(`/entities/${entityId}/records`, request);
  return response.data;
}

/**
 * Update an existing entity record
 */
export async function updateRecord(
  entityId: string,
  recordId: string,
  request: UpdateRecordRequest
): Promise<EntityRecord> {
  const response = await apiClient.put<EntityRecord>(
    `/entities/${entityId}/records/${recordId}`,
    request
  );
  return response.data;
}

/**
 * Soft delete an entity record
 */
export async function deleteRecord(entityId: string, recordId: string): Promise<void> {
  await apiClient.delete(`/entities/${entityId}/records/${recordId}`);
}
