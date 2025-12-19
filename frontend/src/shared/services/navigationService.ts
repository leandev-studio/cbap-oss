import apiClient from '../api/client';

/**
 * Navigation Item DTO from backend
 */
export interface NavigationItem {
  id: string;
  label: string;
  labelKey?: string;
  icon?: string;
  routePath?: string;
  displayOrder: number;
  section?: string;
  children: NavigationItem[];
}

/**
 * Navigation API Response
 */
export interface NavigationResponse {
  items: NavigationItem[];
  count: number;
}

/**
 * Navigation Service
 * 
 * Handles fetching navigation metadata from the backend API.
 */
export async function getNavigation(): Promise<NavigationItem[]> {
  try {
    const response = await apiClient.get<NavigationResponse>('/navigation');
    return response.data.items || [];
  } catch (error) {
    console.error('Failed to fetch navigation:', error);
    return [];
  }
}
