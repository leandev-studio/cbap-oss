import apiClient from '../api/client';

/**
 * Dashboard Pin DTO
 */
export interface DashboardPin {
  pinId: string;
  pinType: 'SEARCH' | 'ENTITY_LIST' | 'WIDGET';
  title: string;
  description?: string;
  config: Record<string, any>;
  displayOrder: number;
  widgetType?: string;
}

/**
 * Dashboard DTO
 */
export interface Dashboard {
  dashboardId: string;
  name: string;
  layoutConfig?: Record<string, any>;
  pins: DashboardPin[];
}

/**
 * Create Pin Request
 */
export interface CreatePinRequest {
  pinType: 'SEARCH' | 'ENTITY_LIST' | 'WIDGET';
  title: string;
  description?: string;
  config: Record<string, any>;
  widgetType?: string;
}

/**
 * Dashboard Service
 * 
 * Handles fetching and managing dashboard data.
 */
export async function getDashboard(): Promise<Dashboard> {
  const response = await apiClient.get<Dashboard>('/dashboard');
  return response.data;
}

export async function addPin(request: CreatePinRequest): Promise<DashboardPin> {
  const response = await apiClient.post<DashboardPin>('/dashboard/pins', request);
  return response.data;
}
