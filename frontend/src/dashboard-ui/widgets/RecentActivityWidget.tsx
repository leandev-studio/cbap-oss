import { Paper, Typography, Box, List, ListItem, ListItemText, Divider } from '@mui/material';
import { History } from '@mui/icons-material';
import { DashboardPin } from '../../shared/services/dashboardService';

interface RecentActivityWidgetProps {
  pin: DashboardPin;
}

/**
 * Recent Activity Widget (Placeholder)
 * 
 * Displays recent user activity. This is a placeholder implementation.
 */
export function RecentActivityWidget({ pin }: RecentActivityWidgetProps) {
  // Placeholder data
  const activities = [
    { id: '1', action: 'Created', entity: 'User', time: '2 hours ago' },
    { id: '2', action: 'Updated', entity: 'Role', time: '5 hours ago' },
    { id: '3', action: 'Viewed', entity: 'Dashboard', time: '1 day ago' },
  ];

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
        <History sx={{ mr: 1, color: 'text.secondary' }} />
        <Typography variant="h6" component="h3" sx={{ flexGrow: 1 }}>
          {pin.title || 'Recent Activity'}
        </Typography>
      </Box>

      {pin.description && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {pin.description}
        </Typography>
      )}

      <Box sx={{ flexGrow: 1, overflow: 'auto' }}>
        <List disablePadding>
          {activities.map((activity, index) => (
            <Box key={activity.id}>
              <ListItem disablePadding sx={{ py: 1 }}>
                <ListItemText
                  primary={
                    <Typography variant="body2">
                      <strong>{activity.action}</strong> {activity.entity}
                    </Typography>
                  }
                  secondary={activity.time}
                  secondaryTypographyProps={{
                    variant: 'caption',
                    color: 'text.secondary',
                  }}
                />
              </ListItem>
              {index < activities.length - 1 && <Divider />}
            </Box>
          ))}
        </List>
      </Box>

      <Box sx={{ mt: 2, pt: 2, borderTop: 1, borderColor: 'divider' }}>
        <Typography variant="caption" color="text.secondary" sx={{ fontStyle: 'italic' }}>
          Placeholder - Activity tracking coming soon
        </Typography>
      </Box>
    </Paper>
  );
}
