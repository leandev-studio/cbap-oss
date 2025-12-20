import apiClient from '../api/client';

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
 * Admin Service
 * 
 * Handles admin operations like reindexing.
 */
export async function reindexEntity(entityId: string): Promise<ReindexResult> {
  const response = await apiClient.post<ReindexResult>(`/admin/entities/${entityId}/reindex`);
  return response.data;
}
