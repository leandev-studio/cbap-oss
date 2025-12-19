import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { UserInfo, login as loginApi, logout as logoutApi, getCurrentUser, isAuthenticated, getAccessToken } from '../services/authService';

interface AuthContextType {
  user: UserInfo | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string, rememberMe: boolean) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  // Check authentication status on mount
  useEffect(() => {
    const checkAuth = async () => {
      if (isAuthenticated()) {
        try {
          const userInfo = await getCurrentUser();
          setUser(userInfo);
        } catch (error) {
          // Token might be invalid, clear it
          logoutApi();
          setUser(null);
        }
      }
      setIsLoading(false);
    };

    checkAuth();
  }, []);

  const login = async (username: string, password: string, rememberMe: boolean) => {
    try {
      const response = await loginApi(username, password);
      
      // Store tokens
      const { storeTokens } = await import('../services/authService');
      storeTokens(response.accessToken, response.refreshToken, rememberMe);
      
      // Set user info
      setUser(response.user);
      
      // Redirect to home
      navigate('/');
    } catch (error: any) {
      // Re-throw for component to handle
      throw error;
    }
  };

  const logout = () => {
    logoutApi();
    setUser(null);
    navigate('/login');
  };

  const refreshUser = async () => {
    try {
      const userInfo = await getCurrentUser();
      setUser(userInfo);
    } catch (error) {
      // If refresh fails, logout
      logout();
    }
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    logout,
    refreshUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
