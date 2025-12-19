import { Paper, Typography, Box, Button, Chip } from '@mui/material';
import { Search, Launch } from '@mui/icons-material';
import { DashboardPin } from '../../shared/services/dashboardService';

interface PinnedSearchWidgetProps {
  pin: DashboardPin;
}

/**
 * Pinned Search Widget
 * 
 * Displays a pinned search with query information and quick access.
 */
export function PinnedSearchWidget({ pin }: PinnedSearchWidgetProps) {
  const searchQuery = pin.config?.query || '';
  const entityId = pin.config?.entityId || '';

  return (
    <Paper
      elevation={0}
      sx={{
        p: 3,
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: 'background.paper',
        borderRadius: 2,
        border: 1,
        borderColor: 'divider',
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
        <Search sx={{ mr: 1, color: 'text.secondary' }} />
        <Typography variant="h6" component="h3" sx={{ flexGrow: 1 }}>
          {pin.title}
        </Typography>
      </Box>

      {pin.description && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {pin.description}
        </Typography>
      )}

      <Box sx={{ mb: 2, flexGrow: 1 }}>
        {searchQuery && (
          <Chip
            label={`Query: ${searchQuery}`}
            size="small"
            sx={{ mb: 1, mr: 1 }}
          />
        )}
        {entityId && (
          <Chip
            label={`Entity: ${entityId}`}
            size="small"
            color="primary"
            variant="outlined"
          />
        )}
      </Box>

      <Button
        variant="outlined"
        startIcon={<Launch />}
        fullWidth
        onClick={() => {
          // TODO: Navigate to search results
          console.log('Navigate to search:', pin.config);
        }}
      >
        Open Search
      </Button>
    </Paper>
  );
}
