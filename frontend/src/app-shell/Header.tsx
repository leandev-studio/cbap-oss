import { AppBar, Toolbar, Typography, Box, IconButton, Menu, MenuItem, ListItemIcon } from '@mui/material';
import { Brightness4, Brightness7, AccountCircle, Menu as MenuIcon, Settings } from '@mui/icons-material';
import { toggleTheme, getInitialTheme } from '../shared/utils/theme';
import { useState, useEffect } from 'react';
import { useAuth } from '../shared/context/AuthContext';
import { useMediaQuery, useTheme } from '@mui/material';
import { GlobalSearchBar } from './GlobalSearchBar';

/**
 * Application Header Component
 * 
 * Provides the top navigation bar with theme toggle and menu button.
 */
interface HeaderProps {
  onMenuClick?: () => void;
}

export function Header({ onMenuClick }: HeaderProps) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [currentTheme, setCurrentTheme] = useState<'light' | 'dark'>(getInitialTheme());
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const { user, logout } = useAuth();

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

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    handleMenuClose();
    logout();
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
          {isMobile && onMenuClick && (
            <IconButton
              onClick={onMenuClick}
              color="inherit"
              aria-label="open navigation menu"
              sx={{
                color: 'text.primary',
                mr: 1,
                '&:hover': {
                  backgroundColor: 'action.hover',
                },
              }}
            >
              <MenuIcon />
            </IconButton>
          )}
          
          <Typography 
            variant="h6" 
            component="div" 
            sx={{ 
              fontWeight: 600,
              color: 'text.primary',
              mr: 2,
            }}
          >
            CBAP
          </Typography>

          <Box sx={{ flexGrow: 1 }} />
          
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          {/* Global Search Bar - moved to right side */}
          <GlobalSearchBar />

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

          {user && (
            <>
              <IconButton
                onClick={handleMenuOpen}
                color="inherit"
                aria-label="user menu"
                sx={{
                  color: 'text.primary',
                  '&:hover': {
                    backgroundColor: 'action.hover',
                  },
                }}
              >
                <AccountCircle />
              </IconButton>
              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleMenuClose}
                anchorOrigin={{
                  vertical: 'bottom',
                  horizontal: 'right',
                }}
                transformOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
              >
                <MenuItem disabled>
                  <Typography variant="body2" color="text.secondary">
                    {user.username}
                  </Typography>
                </MenuItem>
                <MenuItem onClick={handleMenuClose}>
                  <ListItemIcon>
                    <Settings fontSize="small" />
                  </ListItemIcon>
                  Settings
                </MenuItem>
                <MenuItem onClick={handleLogout}>Logout</MenuItem>
              </Menu>
            </>
          )}
        </Box>
        </Box>
      </Toolbar>
    </AppBar>
  );
}
