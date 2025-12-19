import { Box, useTheme, useMediaQuery } from '@mui/material';
import { Outlet } from 'react-router-dom';
import { useState } from 'react';
import { Header } from './Header';
import { Footer } from './Footer';
import { NavigationDrawer } from './NavigationDrawer';

/**
 * Application Shell Component
 * 
 * Provides the main layout structure for the CBAP application.
 * Includes navigation drawer, header, main content area, and footer.
 */
export function AppShell() {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [drawerOpen, setDrawerOpen] = useState(!isMobile); // Open by default on desktop

  const handleDrawerToggle = () => {
    setDrawerOpen(!drawerOpen);
  };

  return (
    <Box 
      sx={{ 
        display: 'flex', 
        flexDirection: 'column', 
        minHeight: '100vh',
        backgroundColor: 'background.default',
      }}
    >
      <Header onMenuClick={handleDrawerToggle} />
      
      <Box 
        sx={{ 
          display: 'flex',
          flexGrow: 1,
          overflow: 'hidden',
        }}
      >
        <NavigationDrawer
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
          onToggle={handleDrawerToggle}
          variant={isMobile ? 'temporary' : 'persistent'}
        />
        
        <Box 
          component="main" 
          sx={{ 
            flexGrow: 1,
            display: 'flex',
            flexDirection: 'column',
            overflow: 'auto',
            py: { xs: 2, sm: 3 },
            px: { xs: 1, sm: '20px' }, // 20px margin on sides
            minHeight: 0, // Allow content to shrink
          }}
        >
          <Box
            sx={{
              width: '100%',
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
            }}
          >
            <Outlet />
          </Box>
        </Box>
      </Box>
      
      <Footer />
    </Box>
  );
}
