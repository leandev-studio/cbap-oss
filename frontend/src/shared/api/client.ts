import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';

/**
 * API Client Configuration
 * 
 * Centralized axios instance for API calls with:
 * - Base URL configuration
 * - Request interceptors for auth tokens
 * - Response interceptors for error handling
 * - Correlation ID propagation
 */
const apiClient: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for adding auth tokens and correlation IDs
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Add auth token if available
    const token = localStorage.getItem('authToken');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
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
      // Handle unauthorized - redirect to login
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
