import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';

/**
 * API Client Configuration
 * 
 * Centralized axios instance for API calls with:
 * - Base URL configuration from environment variables
 * - Request interceptors for auth tokens
 * - Response interceptors for error handling
 * - Correlation ID propagation
 */

// API base URL:
// - DEV: always '/api/v1' (same-origin) + Vite proxy to avoid CORS
// - PROD: use VITE_API_BASE_URL if provided, else '/api/v1'
const getApiBaseUrl = (): string => {
  const envUrl = import.meta.env.VITE_API_BASE_URL;

  /**
   * DEV: Always use same-origin + Vite proxy to avoid CORS entirely.
   * Configure the backend host/port via Vite proxy target (vite.config.ts),
   * not by using an absolute baseURL in the browser.
   */
  if (import.meta.env.DEV) {
    return '/api/v1';
  }

  /**
   * PROD: Use the configured URL (can be absolute for different domain/port),
   * otherwise fall back to relative path.
   */
  return envUrl || '/api/v1';
};

const apiClient: AxiosInstance = axios.create({
  baseURL: getApiBaseUrl(),
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for adding auth tokens and correlation IDs
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Add auth token if available (check both localStorage and sessionStorage)
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    if (token && config.headers) {
      // Ensure token is trimmed and not empty
      const trimmedToken = token.trim();
      if (trimmedToken) {
        config.headers.Authorization = `Bearer ${trimmedToken}`;
      }
    } else if (config.url && !config.url.includes('/auth/login') && !config.url.includes('/auth/refresh')) {
      // Log warning for missing token on protected endpoints (for debugging)
      console.warn('API call without auth token:', config.url);
    }

    // Add correlation ID for request tracing
    const correlationId = crypto.randomUUID();
    if (config.headers) {
      config.headers['X-Correlation-ID'] = correlationId;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Handle common error cases
    if (error.response?.status === 401) {
      // Handle unauthorized - clear tokens and redirect to login
      localStorage.removeItem('authToken');
      localStorage.removeItem('refreshToken');
      sessionStorage.removeItem('authToken');
      sessionStorage.removeItem('refreshToken');
      // Only redirect if not already on login page
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
