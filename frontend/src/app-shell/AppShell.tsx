import { Box, Container } from '@mui/material';
import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { Footer } from './Footer';

/**
 * Application Shell Component
 * 
 * Provides the main layout structure for the CBAP application.
 * This is metadata-driven and will be extended to support
 * auto-generated navigation based on entity definitions.
 */
export function AppShell() {
  return (
    <Box 
      sx={{ 
        display: 'flex', 
        flexDirection: 'column', 
        minHeight: '100vh',
        backgroundColor: 'background.default',
      }}
    >
      <Header />
      
      <Box 
        component="main" 
        sx={{ 
          flexGrow: 1,
          py: { xs: 2, sm: 3 },
          px: { xs: 1, sm: '20px' }, // 20px margin on sides
          width: '100%',
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
      
      <Footer />
    </Box>
  );
}
