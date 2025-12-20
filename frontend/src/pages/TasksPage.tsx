import { useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  CircularProgress,
  Alert,
  Tabs,
  Tab,
  Grid,
  Pagination,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { getUserTasks } from '../shared/services/taskService';
import { TaskCard } from '../metadata-ui/TaskCard';

/**
 * Tasks Page Component
 * 
 * Displays a list of tasks assigned to the current user.
 * Route: /tasks
 */
export function TasksPage() {
  const [statusFilter, setStatusFilter] = useState<string | undefined>('OPEN'); // Default to OPEN
  const [page, setPage] = useState(0);
  const pageSize = 20;

  // Fetch tasks
  const {
    data: tasksData,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ['tasks', statusFilter, page],
    queryFn: () => getUserTasks(statusFilter, page, pageSize),
  });

  const handleStatusChange = (_event: React.SyntheticEvent, newValue: string) => {
    setStatusFilter(newValue === 'all' ? undefined : newValue);
    setPage(0); // Reset to first page when filter changes
  };

  const handlePageChange = (_event: React.ChangeEvent<unknown>, value: number) => {
    setPage(value - 1); // MUI Pagination is 1-based, API is 0-based
  };

  return (
    <Box sx={{ width: '100%', height: '100%' }}>
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom color="text.primary">
          My Tasks
        </Typography>
        <Typography variant="body2" color="text.secondary">
          View and manage tasks assigned to you
        </Typography>
      </Box>

      {/* Status Filter Tabs */}
      <Paper elevation={0} sx={{ mb: 3, border: 1, borderColor: 'divider' }}>
        <Tabs
          value={statusFilter || 'all'}
          onChange={handleStatusChange}
          variant="scrollable"
          scrollButtons="auto"
        >
          <Tab label="Open" value="OPEN" />
          <Tab label="In Progress" value="IN_PROGRESS" />
          <Tab label="Done" value="DONE" />
          <Tab label="Cancelled" value="CANCELLED" />
          <Tab label="All" value="all" />
        </Tabs>
      </Paper>

      {/* Tasks List */}
      {isLoading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      ) : error ? (
        <Alert severity="error">
          Failed to load tasks: {error instanceof Error ? error.message : 'Unknown error'}
        </Alert>
      ) : tasksData && tasksData.tasks.length > 0 ? (
        <>
          <Grid container spacing={2}>
            {tasksData.tasks.map((task) => (
              <Grid item xs={12} sm={6} md={4} key={task.taskId}>
                <TaskCard task={task} onUpdate={refetch} />
              </Grid>
            ))}
          </Grid>
          
          {/* Pagination */}
          {tasksData.totalPages > 1 && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
              <Pagination
                count={tasksData.totalPages}
                page={page + 1}
                onChange={handlePageChange}
                color="primary"
                size="large"
              />
            </Box>
          )}
        </>
      ) : (
        <Paper
          elevation={0}
          sx={{
            p: 4,
            textAlign: 'center',
            border: 1,
            borderColor: 'divider',
            borderRadius: 2,
          }}
        >
          <Typography variant="body1" color="text.secondary">
            No tasks found
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            {statusFilter
              ? `You don't have any ${statusFilter.toLowerCase()} tasks.`
              : "You don't have any tasks assigned to you."}
          </Typography>
        </Paper>
      )}
    </Box>
  );
}
