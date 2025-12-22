import apiClient from '../api/client';

/**
 * Validation Error DTO
 */
export interface ValidationError {
  validationId: string;
  propertyName?: string;
  message: string;
  messageKey?: string;
  level: 'FIELD' | 'ENTITY' | 'CROSS_ENTITY' | 'WORKFLOW_TRANSITION';
}

/**
 * Validation Response
 */
export interface ValidationResponse {
  entityId: string;
  valid: boolean;
  errors: ValidationError[];
  errorCount: number;
}

/**
 * Field Validation Request
 */
export interface FieldValidationRequest {
  value: any;
  fullRecordData?: Record<string, any>;
}

/**
 * Record Validation Request
 */
export interface RecordValidationRequest {
  data: Record<string, any>;
  previousData?: Record<string, any>;
  triggerEvent?: 'CREATE' | 'UPDATE' | 'DELETE' | 'TRANSITION';
}

/**
 * Validation Service
 * 
 * Handles validation-related API calls.
 */
export async function validateRecord(
  entityId: string,
  request: RecordValidationRequest
): Promise<ValidationResponse> {
  const response = await apiClient.post<ValidationResponse>(
    `/validation/entities/${entityId}/validate`,
    request
  );
  return response.data;
}

export async function validateField(
  entityId: string,
  propertyName: string,
  request: FieldValidationRequest
): Promise<ValidationResponse> {
  const response = await apiClient.post<ValidationResponse>(
    `/validation/entities/${entityId}/fields/${propertyName}/validate`,
    request
  );
  return response.data;
}
