import { AppBar, Toolbar, Typography, Box, IconButton } from '@mui/material';
import { Brightness4, Brightness7 } from '@mui/icons-material';
import { toggleTheme, getInitialTheme } from '../shared/utils/theme';
import { useState, useEffect } from 'react';

/**
 * Application Header Component
 * 
 * Provides the top navigation bar with theme toggle.
 */
export function Header() {
  const [currentTheme, setCurrentTheme] = useState<'light' | 'dark'>(getInitialTheme());

  // Update theme when it changes
  useEffect(() => {
    const handleThemeChange = () => {
      setCurrentTheme(getInitialTheme());
    };
    
    // Listen for storage changes (when theme is changed in another tab)
    window.addEventListener('storage', handleThemeChange);
    
    // Custom event for theme changes in same tab
    window.addEventListener('themechange', handleThemeChange);
    
    return () => {
      window.removeEventListener('storage', handleThemeChange);
      window.removeEventListener('themechange', handleThemeChange);
    };
  }, []);

  const handleThemeToggle = () => {
    const newTheme = toggleTheme();
    setCurrentTheme(newTheme);
    // Dispatch custom event to notify other components
    window.dispatchEvent(new Event('themechange'));
  };

  return (
    <AppBar 
      position="static" 
      elevation={0}
      sx={{
        backgroundColor: 'background.paper',
        borderBottom: 1,
        borderColor: 'divider',
        color: 'text.primary',
      }}
    >
      <Toolbar
        sx={{
          px: 0, // Remove default padding
          minHeight: '64px !important',
        }}
      >
        <Box
          sx={{
            width: '100%',
            display: 'flex',
            alignItems: 'center',
            px: { xs: 2, sm: '20px' }, // 20px margin on sides, matching main content
          }}
        >
          <Typography 
            variant="h6" 
            component="div" 
            sx={{ 
              flexGrow: 1,
              fontWeight: 600,
              color: 'text.primary',
            }}
          >
            CBAP
          </Typography>
          
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <IconButton
              onClick={handleThemeToggle}
              color="inherit"
              aria-label="toggle theme"
              sx={{
                color: 'text.primary',
                '&:hover': {
                  backgroundColor: 'action.hover',
                },
              }}
            >
              {currentTheme === 'dark' ? <Brightness7 /> : <Brightness4 />}
            </IconButton>
          </Box>
        </Box>
      </Toolbar>
    </AppBar>
  );
}
