import { Paper, Typography, Box, Button, Chip } from '@mui/material';
import { TableChart, Launch } from '@mui/icons-material';
import { DashboardPin } from '../../shared/services/dashboardService';

interface PinnedEntityListWidgetProps {
  pin: DashboardPin;
}

/**
 * Pinned Entity List Widget
 * 
 * Displays a pinned entity list with filters and quick access.
 */
export function PinnedEntityListWidget({ pin }: PinnedEntityListWidgetProps) {
  const entityId = pin.config?.entityId || '';
  const filters = pin.config?.filters || {};
  const filterCount = Object.keys(filters).length;

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
        <TableChart sx={{ mr: 1, color: 'text.secondary' }} />
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
        {entityId && (
          <Chip
            label={`Entity: ${entityId}`}
            size="small"
            color="primary"
            sx={{ mb: 1, mr: 1 }}
          />
        )}
        {filterCount > 0 && (
          <Chip
            label={`${filterCount} filter${filterCount > 1 ? 's' : ''}`}
            size="small"
            variant="outlined"
          />
        )}
      </Box>

      <Button
        variant="outlined"
        startIcon={<Launch />}
        fullWidth
        onClick={() => {
          // TODO: Navigate to entity list
          console.log('Navigate to entity list:', pin.config);
        }}
      >
        Open List
      </Button>
    </Paper>
  );
}
