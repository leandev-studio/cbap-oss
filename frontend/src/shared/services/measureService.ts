import apiClient from '../api/client';

/**
 * Measure DTO
 */
export interface Measure {
  measureId: string;
  measureIdentifier: string;
  name: string;
  description?: string;
  version: number;
  parametersJson?: Array<{
    name: string;
    type: string;
    default?: any;
  }>;
  returnType: 'number' | 'string' | 'bool' | 'date' | 'reference';
  dependsOnJson?: Array<{
    entity: string;
    fields?: string[];
    dimensions?: string[];
  }>;
  definitionType: string;
  expression: string;
  metadataJson?: Record<string, any>;
}

/**
 * Measure Evaluation Request
 */
export interface MeasureEvaluationRequest {
  parameters?: Record<string, any>;
}

/**
 * Measure Evaluation Response
 */
export interface MeasureEvaluationResponse {
  measureIdentifier: string;
  version?: number;
  result: any;
}

/**
 * Measure Service
 * 
 * Handles measure-related API calls.
 */
export async function getAllMeasures(): Promise<Measure[]> {
  const response = await apiClient.get<{ measures: Measure[]; count: number }>('/metadata/measures');
  return response.data.measures || [];
}

export async function getMeasure(measureIdentifier: string, version?: number): Promise<Measure> {
  const url = version 
    ? `/metadata/measures/${measureIdentifier}?version=${version}`
    : `/metadata/measures/${measureIdentifier}`;
  const response = await apiClient.get<{ measure: Measure }>(url);
  return response.data.measure;
}

export async function evaluateMeasure(
  measureIdentifier: string,
  parameters?: Record<string, any>,
  version?: number
): Promise<any> {
  const url = version
    ? `/measures/${measureIdentifier}/evaluate?version=${version}`
    : `/measures/${measureIdentifier}/evaluate`;
  const response = await apiClient.post<MeasureEvaluationResponse>(url, { parameters: parameters || {} });
  return response.data.result;
}
