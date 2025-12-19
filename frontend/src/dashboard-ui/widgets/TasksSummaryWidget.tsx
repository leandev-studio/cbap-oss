import { Paper, Typography, Box, List, ListItem, ListItemText, Chip, Divider } from '@mui/material';
import { Assignment } from '@mui/icons-material';
import { DashboardPin } from '../../shared/services/dashboardService';

interface TasksSummaryWidgetProps {
  pin: DashboardPin;
}

/**
 * Tasks Summary Widget (Placeholder)
 * 
 * Displays task summary information. This is a placeholder implementation.
 */
export function TasksSummaryWidget({ pin }: TasksSummaryWidgetProps) {
  // Placeholder data
  const tasks = [
    { id: '1', title: 'Review Purchase Order', status: 'Pending', priority: 'High' },
    { id: '2', title: 'Approve Budget Request', status: 'In Progress', priority: 'Medium' },
    { id: '3', title: 'Complete Audit Report', status: 'Pending', priority: 'Low' },
  ];

  const statusColors: Record<string, 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning'> = {
    'Pending': 'warning',
    'In Progress': 'info',
    'Completed': 'success',
  };

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
        <Assignment sx={{ mr: 1, color: 'text.secondary' }} />
        <Typography variant="h6" component="h3" sx={{ flexGrow: 1 }}>
          {pin.title || 'Tasks Summary'}
        </Typography>
      </Box>

      {pin.description && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {pin.description}
        </Typography>
      )}

      <Box sx={{ mb: 2 }}>
        <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
          <Chip label={`${tasks.length} Total`} size="small" color="primary" />
          <Chip label="3 Pending" size="small" color="warning" />
        </Box>
      </Box>

      <Box sx={{ flexGrow: 1, overflow: 'auto' }}>
        <List disablePadding>
          {tasks.map((task, index) => (
            <Box key={task.id}>
              <ListItem disablePadding sx={{ py: 1 }}>
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography variant="body2" sx={{ flexGrow: 1 }}>
                        {task.title}
                      </Typography>
                      <Chip
                        label={task.status}
                        size="small"
                        color={statusColors[task.status] || 'default'}
                      />
                    </Box>
                  }
                  secondary={`Priority: ${task.priority}`}
                  secondaryTypographyProps={{
                    variant: 'caption',
                    color: 'text.secondary',
                  }}
                />
              </ListItem>
              {index < tasks.length - 1 && <Divider />}
            </Box>
          ))}
        </List>
      </Box>

      <Box sx={{ mt: 2, pt: 2, borderTop: 1, borderColor: 'divider' }}>
        <Typography variant="caption" color="text.secondary" sx={{ fontStyle: 'italic' }}>
          Placeholder - Task management coming soon
        </Typography>
      </Box>
    </Paper>
  );
}
