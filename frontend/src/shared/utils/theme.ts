/**
 * Theme Utilities
 * 
 * Utilities for managing theme switching and accessing theme values
 */

export type Theme = 'light' | 'dark';

const THEME_STORAGE_KEY = 'cbap-theme';

/**
 * Get the current theme from localStorage or system preference
 */
export function getInitialTheme(): Theme {
  // Check localStorage first
  const stored = localStorage.getItem(THEME_STORAGE_KEY) as Theme | null;
  if (stored === 'light' || stored === 'dark') {
    return stored;
  }

  // Fall back to system preference
  if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return 'dark';
  }

  // Default to light
  return 'light';
}

/**
 * Set the theme and persist to localStorage
 */
export function setTheme(theme: Theme): void {
  localStorage.setItem(THEME_STORAGE_KEY, theme);
  document.documentElement.setAttribute('data-theme', theme);
}

/**
 * Initialize theme on page load
 */
export function initializeTheme(): void {
  const theme = getInitialTheme();
  setTheme(theme);
}

/**
 * Toggle between light and dark themes
 */
export function toggleTheme(): Theme {
  const currentTheme = getInitialTheme();
  const newTheme: Theme = currentTheme === 'light' ? 'dark' : 'light';
  setTheme(newTheme);
  return newTheme;
}

/**
 * Listen for system theme changes and update accordingly
 */
export function watchSystemTheme(callback: (theme: Theme) => void): () => void {
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
  
  const handleChange = (e: MediaQueryListEvent) => {
    // Only update if user hasn't manually set a preference
    if (!localStorage.getItem(THEME_STORAGE_KEY)) {
      const theme: Theme = e.matches ? 'dark' : 'light';
      setTheme(theme);
      callback(theme);
    }
  };

  // Modern browsers
  if (mediaQuery.addEventListener) {
    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }
  
  // Fallback for older browsers
  mediaQuery.addListener(handleChange);
  return () => mediaQuery.removeListener(handleChange);
}

/**
 * Get a CSS variable value
 */
export function getCSSVariable(variableName: string): string {
  return getComputedStyle(document.documentElement)
    .getPropertyValue(variableName)
    .trim();
}
