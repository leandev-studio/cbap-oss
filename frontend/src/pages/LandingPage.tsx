import { Box, Typography, Paper } from '@mui/material';

/**
 * Landing Page Component
 * 
 * Initial landing page with welcome message and placeholder content.
 */
export function LandingPage() {
  return (
    <Box sx={{ width: '100%', height: '100%' }}>
      <Paper 
        elevation={0}
        sx={{ 
          p: 4,
          width: '100%',
          backgroundColor: 'background.paper',
          borderRadius: 2,
          border: 1,
          borderColor: 'divider',
        }}
      >
        <Typography variant="h4" component="h1" gutterBottom color="text.primary">
          Welcome to CBAP
        </Typography>
        <Typography variant="body1" color="text.secondary" paragraph>
          Composable Business Application Platform - Open Source
        </Typography>
        <Typography variant="body2" color="text.secondary">
          This is a placeholder for the dashboard content.
        </Typography>
      </Paper>
    </Box>
  );
}
