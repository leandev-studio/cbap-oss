import { createTheme, ThemeOptions, Theme } from '@mui/material/styles';
import { getInitialTheme } from './utils/theme';

// Light theme colors (soft/pastel from COLOR_GUIDE.md)
const lightThemeOptions: ThemeOptions = {
  palette: {
    mode: 'light',
    primary: {
      main: '#7B9BC8',
      light: '#A8C0DC',
      dark: '#5F7BA3',
      contrastText: '#FAF9F7',
    },
    secondary: {
      main: '#8BB5D1',
      light: '#A8C8DF',
      dark: '#6F9BB8',
      contrastText: '#2C2A28',
    },
    success: {
      main: '#7FC4A3',
      light: '#A8D9C1',
      dark: '#66A88A',
      contrastText: '#2C2A28',
    },
    warning: {
      main: '#E8B87A',
      light: '#F0CEA0',
      dark: '#D19E5F',
      contrastText: '#2C2A28',
    },
    error: {
      main: '#D89A9A',
      light: '#E5B5B5',
      dark: '#C47A7A',
      contrastText: '#2C2A28',
    },
    info: {
      main: '#8BB5D1',
      light: '#A8C8DF',
      dark: '#6F9BB8',
      contrastText: '#2C2A28',
    },
    background: {
      default: '#FAF9F7',
      paper: '#FFFFFF',
    },
    text: {
      primary: '#2C2A28',
      secondary: '#5A5754',
      disabled: '#B8B6B4',
    },
    divider: '#E5E3E0',
  },
  typography: {
    fontFamily: [
      '-apple-system',
      'BlinkMacSystemFont',
      '"Segoe UI"',
      'Roboto',
      '"Helvetica Neue"',
      'Arial',
      'sans-serif',
    ].join(','),
  },
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: 8,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 12,
        },
      },
    },
  },
};

// Dark theme colors (soft/pastel from COLOR_GUIDE.md)
const darkThemeOptions: ThemeOptions = {
  palette: {
    mode: 'dark',
    primary: {
      main: '#8BB5D1',
      light: '#A8C8DF',
      dark: '#6F9BB8',
      contrastText: '#1E1D1B',
    },
    secondary: {
      main: '#7B9BC8',
      light: '#A8C0DC',
      dark: '#5F7BA3',
      contrastText: '#F5F3F0',
    },
    success: {
      main: '#7FC4A3',
      light: '#95D1B5',
      dark: '#66A88A',
      contrastText: '#1E1D1B',
    },
    warning: {
      main: '#E8B87A',
      light: '#F0CEA0',
      dark: '#D19E5F',
      contrastText: '#1E1D1B',
    },
    error: {
      main: '#D89A9A',
      light: '#E5B5B5',
      dark: '#C47A7A',
      contrastText: '#1E1D1B',
    },
    info: {
      main: '#8BB5D1',
      light: '#A8C8DF',
      dark: '#6F9BB8',
      contrastText: '#1E1D1B',
    },
    background: {
      default: '#1E1D1B',
      paper: '#2C2A28',
    },
    text: {
      primary: '#F5F3F0',
      secondary: '#D4D2D0',
      disabled: '#6B6967',
    },
    divider: '#353330',
  },
  typography: {
    fontFamily: [
      '-apple-system',
      'BlinkMacSystemFont',
      '"Segoe UI"',
      'Roboto',
      '"Helvetica Neue"',
      'Arial',
      'sans-serif',
    ].join(','),
  },
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: 8,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 12,
        },
      },
    },
  },
};

/**
 * Create a theme based on the current theme preference
 */
export function createAppTheme(mode: 'light' | 'dark' = 'light'): Theme {
  return createTheme(mode === 'light' ? lightThemeOptions : darkThemeOptions);
}

// Get initial theme from storage or system preference
const initialTheme = getInitialTheme();

// Default theme (will be replaced by ThemeProvider with dynamic switching)
export const theme = createAppTheme(initialTheme);
