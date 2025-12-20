import apiClient from '../api/client';

/**
 * Search Hit DTO
 */
export interface SearchHit {
  entityId: string;
  recordId: string;
  data: Record<string, any>;
  displayValue?: string; // Computed display value from metadata
}

/**
 * Search Result DTO
 */
export interface SearchResult {
  query: string;
  entityId?: string;
  page: number;
  size: number;
  totalHits: number;
  hits: SearchHit[];
}

/**
 * Search Request DTO
 */
export interface SearchRequest {
  query?: string;
  filters?: Record<string, any>;
  page?: number;
  size?: number;
}

/**
 * Saved Search DTO
 */
export interface SavedSearch {
  searchId: string;
  entityId?: string;
  name: string;
  description?: string;
  queryText?: string;
  filters?: Record<string, any>;
  isGlobal?: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Search Service
 * 
 * Provides functions to interact with the search API.
 */
export const searchService = {
  /**
   * Global search across entities or a specific entity.
   */
  async search(
    query: string,
    entityId?: string,
    page: number = 0,
    size: number = 20
  ): Promise<SearchResult> {
    const params = new URLSearchParams({
      q: query,
      page: page.toString(),
      size: size.toString(),
    });
    if (entityId) {
      params.append('entity', entityId);
    }

    const response = await apiClient.get<SearchResult>(`/search?${params}`);
    return response.data;
  },

  /**
   * Advanced search with filters for a specific entity.
   */
  async searchWithFilters(
    entityId: string,
    request: SearchRequest
  ): Promise<SearchResult> {
    const response = await apiClient.post<{
      entityId: string;
      query?: string;
      filters?: Record<string, any>;
      page: number;
      size: number;
      totalHits: number;
      hits: Array<{
        entityId: string;
        recordId: string;
        data: Record<string, any>;
      }>;
    }>(
      `/entities/${entityId}/records/search`,
      request
    );
    // Transform response to SearchResult format
    return {
      query: response.data.query || '',
      entityId: response.data.entityId,
      page: response.data.page,
      size: response.data.size,
      totalHits: response.data.totalHits,
      hits: response.data.hits,
    };
  },

  /**
   * Get user's saved searches.
   */
  async getSavedSearches(entityId?: string): Promise<SavedSearch[]> {
    const params = entityId ? `?entityId=${entityId}` : '';
    const response = await apiClient.get<{ searches: SavedSearch[] }>(
      `/searches${params}`
    );
    return response.data.searches;
  },

  /**
   * Save a search.
   */
  async saveSearch(search: {
    entityId?: string;
    name: string;
    description?: string;
    queryText?: string;
    filters?: Record<string, any>;
    isGlobal?: boolean;
  }): Promise<SavedSearch> {
    const response = await apiClient.post<SavedSearch>(
      '/searches',
      search
    );
    return response.data;
  },
};
