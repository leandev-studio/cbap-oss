import { Box, Grid, Paper, Typography, CircularProgress } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { getDashboard, DashboardPin } from '../shared/services/dashboardService';
import { PinnedSearchWidget } from './widgets/PinnedSearchWidget';
import { PinnedEntityListWidget } from './widgets/PinnedEntityListWidget';
import { RecentActivityWidget } from './widgets/RecentActivityWidget';
import { TasksSummaryWidget } from './widgets/TasksSummaryWidget';

/**
 * Dashboard Page Component
 * 
 * Main dashboard page with grid layout for widgets.
 */
export function DashboardPage() {
  const { data: dashboard, isLoading, error } = useQuery({
    queryKey: ['dashboard'],
    queryFn: getDashboard,
    staleTime: 1 * 60 * 1000, // Cache for 1 minute
  });

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="error">
          Failed to load dashboard
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          {error instanceof Error ? error.message : 'Unknown error'}
        </Typography>
      </Box>
    );
  }

  if (!dashboard) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6">No dashboard found</Typography>
      </Box>
    );
  }

  // Render widgets based on pin type
  const renderWidget = (pin: DashboardPin) => {
    switch (pin.pinType) {
      case 'SEARCH':
        return <PinnedSearchWidget key={pin.pinId} pin={pin} />;
      case 'ENTITY_LIST':
        return <PinnedEntityListWidget key={pin.pinId} pin={pin} />;
      case 'WIDGET':
        if (pin.widgetType === 'RECENT_ACTIVITY') {
          return <RecentActivityWidget key={pin.pinId} pin={pin} />;
        } else if (pin.widgetType === 'TASKS_SUMMARY') {
          return <TasksSummaryWidget key={pin.pinId} pin={pin} />;
        }
        return null;
      default:
        return null;
    }
  };

  // Auto-layout: Use grid system with responsive columns
  // Default layout: 2 columns on desktop, 1 on mobile
  const hasPins = dashboard.pins && dashboard.pins.length > 0;

  return (
    <Box sx={{ width: '100%', height: '100%' }}>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom color="text.primary">
          {dashboard.name}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Your personalized dashboard
        </Typography>
      </Box>

      {!hasPins ? (
        <Paper
          elevation={0}
          sx={{
            p: 4,
            textAlign: 'center',
            backgroundColor: 'background.paper',
            borderRadius: 2,
            border: 1,
            borderColor: 'divider',
          }}
        >
          <Typography variant="h6" color="text.secondary" gutterBottom>
            No widgets yet
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Pin searches, entity lists, or add widgets to get started.
          </Typography>
        </Paper>
      ) : (
        <Grid container spacing={3}>
          {dashboard.pins.map((pin) => (
            <Grid item xs={12} sm={6} md={4} key={pin.pinId}>
              {renderWidget(pin)}
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
}
