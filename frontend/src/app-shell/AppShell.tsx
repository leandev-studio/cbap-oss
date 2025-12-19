import { Box, Container } from '@mui/material';
import { Outlet } from 'react-router-dom';

/**
 * Application Shell Component
 * 
 * Provides the main layout structure for the CBAP application.
 * This is metadata-driven and will be extended to support
 * auto-generated navigation based on entity definitions.
 */
export function AppShell() {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      {/* Header/Navigation will go here */}
      <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
        <Container maxWidth="xl">
          <Outlet />
        </Container>
      </Box>
      {/* Footer will go here */}
    </Box>
  );
}
