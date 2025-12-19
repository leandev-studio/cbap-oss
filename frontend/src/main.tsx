import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';
import App from './App';
import { AuthProvider } from './shared/context/AuthContext';
import { createAppTheme } from './shared/theme';
import { initializeTheme, getInitialTheme, Theme } from './shared/utils/theme';
import './shared/styles/global.css';
import './shared/i18n';

// Initialize theme before rendering
initializeTheme();

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

/**
 * App with Theme Support
 * 
 * Wraps the app with dynamic theme switching support.
 */
function AppWithTheme() {
  const [themeMode, setThemeMode] = useState<Theme>(() => {
    // Initialize theme immediately
    const initialTheme = getInitialTheme();
    // Ensure data-theme attribute is set
    document.documentElement.setAttribute('data-theme', initialTheme);
    return initialTheme;
  });
  const theme = createAppTheme(themeMode);

  useEffect(() => {
    // Listen for theme changes
    const handleThemeChange = () => {
      const newTheme = getInitialTheme();
      setThemeMode(newTheme);
      document.documentElement.setAttribute('data-theme', newTheme);
    };

    // Listen for storage changes (when theme is changed in another tab)
    window.addEventListener('storage', handleThemeChange);
    
    // Listen for custom theme change events
    window.addEventListener('themechange', handleThemeChange);

    return () => {
      window.removeEventListener('storage', handleThemeChange);
      window.removeEventListener('themechange', handleThemeChange);
    };
  }, []);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <App />
      </AuthProvider>
    </ThemeProvider>
  );
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AppWithTheme />
      </BrowserRouter>
    </QueryClientProvider>
  </React.StrictMode>,
);
