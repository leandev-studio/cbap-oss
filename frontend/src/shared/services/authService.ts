import apiClient from '../api/client';

/**
 * Authentication Service
 * 
 * Handles authentication operations: login, logout, token management
 */

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: {
    userId: string;
    username: string;
    email: string | null;
    status: string;
    roles: string[];
  };
}

export interface UserInfo {
  userId: string;
  username: string;
  email: string | null;
  status: string;
  roles: string[];
}

const TOKEN_KEY = 'authToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const REMEMBER_ME_KEY = 'rememberMe';

/**
 * Login with username and password
 */
export async function login(username: string, password: string): Promise<LoginResponse> {
  const response = await apiClient.post<LoginResponse>('/auth/login', {
    username,
    password,
  });
  
  return response.data;
}

/**
 * Logout (clear tokens)
 */
export function logout(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(REMEMBER_ME_KEY);
}

/**
 * Store authentication tokens
 */
export function storeTokens(accessToken: string, refreshToken: string, rememberMe: boolean = false): void {
  if (rememberMe) {
    // Store in localStorage (persists across sessions)
    localStorage.setItem(TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    localStorage.setItem(REMEMBER_ME_KEY, 'true');
  } else {
    // Store in sessionStorage (cleared on browser close)
    sessionStorage.setItem(TOKEN_KEY, accessToken);
    sessionStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }
}

/**
 * Get stored access token
 */
export function getAccessToken(): string | null {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);
}

/**
 * Get stored refresh token
 */
export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || sessionStorage.getItem(REFRESH_TOKEN_KEY);
}

/**
 * Check if user is authenticated
 */
export function isAuthenticated(): boolean {
  return !!getAccessToken();
}

/**
 * Refresh access token using refresh token
 */
export async function refreshAccessToken(): Promise<LoginResponse> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  const response = await apiClient.post<LoginResponse>('/auth/refresh', {
    refreshToken,
  });

  const rememberMe = localStorage.getItem(REMEMBER_ME_KEY) === 'true';
  storeTokens(response.data.accessToken, response.data.refreshToken, rememberMe);

  return response.data;
}

/**
 * Get current user info
 */
export async function getCurrentUser(): Promise<UserInfo> {
  const response = await apiClient.get<UserInfo>('/auth/me');
  return response.data;
}
