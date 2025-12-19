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
