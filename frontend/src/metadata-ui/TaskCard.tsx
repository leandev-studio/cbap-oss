import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card,
  CardContent,
  CardActions,
  Typography,
  Chip,
  Button,
  Box,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Edit,
  ArrowForward,
  AccessTime,
  Flag,
} from '@mui/icons-material';
import { Task } from '../shared/services/taskService';
import { TaskDetailDialog } from './TaskDetailDialog';

interface TaskCardProps {
  task: Task;
  onUpdate?: () => void;
}

/**
 * Task Card Component
 * 
 * Displays a task in a card format with key information and actions.
 */
export function TaskCard({ task, onUpdate }: TaskCardProps) {
  const navigate = useNavigate();
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);

  const getStatusColor = (status: string): 'default' | 'primary' | 'success' | 'error' | 'warning' => {
    switch (status) {
      case 'OPEN':
        return 'primary';
      case 'IN_PROGRESS':
        return 'warning';
      case 'DONE':
        return 'success';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getPriorityColor = (priority?: string): 'default' | 'error' | 'warning' | 'info' => {
    switch (priority) {
      case 'URGENT':
        return 'error';
      case 'HIGH':
        return 'warning';
      case 'MEDIUM':
        return 'info';
      case 'LOW':
        return 'default';
      default:
        return 'default';
    }
  };

  const handleViewRecord = () => {
    navigate(`/entities/${task.entityId}/records/${task.recordId}`);
  };

  const handleOpenDetail = () => {
    setDetailDialogOpen(true);
  };

  const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'DONE' && task.status !== 'CANCELLED';

  return (
    <>
      <Card
        elevation={0}
        sx={{
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          border: 1,
          borderColor: 'divider',
          borderRadius: 2,
          transition: 'all 0.2s',
          '&:hover': {
            boxShadow: 2,
            borderColor: 'primary.main',
          },
        }}
      >
        <CardContent sx={{ flexGrow: 1 }}>
          {/* Header */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
            <Typography variant="h6" component="h3" sx={{ fontWeight: 600, flexGrow: 1 }}>
              {task.title}
            </Typography>
            <Chip
              label={task.status}
              size="small"
              color={getStatusColor(task.status)}
              sx={{ ml: 1 }}
            />
          </Box>

          {/* Description */}
          {task.description && (
            <Typography
              variant="body2"
              color="text.secondary"
              sx={{
                mb: 2,
                display: '-webkit-box',
                WebkitLineClamp: 2,
                WebkitBoxOrient: 'vertical',
                overflow: 'hidden',
              }}
            >
              {task.description}
            </Typography>
          )}

          {/* Metadata */}
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 2 }}>
            {task.priority && (
              <Chip
                icon={<Flag />}
                label={task.priority}
                size="small"
                color={getPriorityColor(task.priority)}
                variant="outlined"
              />
            )}
            {task.dueDate && (
              <Chip
                icon={<AccessTime />}
                label={new Date(task.dueDate).toLocaleDateString()}
                size="small"
                variant="outlined"
                color={isOverdue ? 'error' : 'default'}
              />
            )}
            {task.workflowState && (
              <Chip
                label={task.workflowState}
                size="small"
                variant="outlined"
              />
            )}
          </Box>

          {/* Entity Info */}
          <Typography variant="caption" color="text.secondary">
            {task.entityDisplayValue ? (
              <>
                {task.entityId}: {task.entityDisplayValue}
              </>
            ) : (
              <>Entity: {task.entityId}</>
            )}
          </Typography>
        </CardContent>

        <CardActions sx={{ justifyContent: 'space-between', px: 2, pb: 2 }}>
          <Box>
            <Button
              size="small"
              onClick={handleViewRecord}
              endIcon={<ArrowForward />}
            >
              View Record
            </Button>
          </Box>
          <Box>
            <Tooltip title="View Details">
              <IconButton size="small" onClick={handleOpenDetail}>
                <Edit />
              </IconButton>
            </Tooltip>
          </Box>
        </CardActions>
      </Card>

      {/* Task Detail Dialog */}
      <TaskDetailDialog
        open={detailDialogOpen}
        onClose={() => setDetailDialogOpen(false)}
        taskId={task.taskId}
        onUpdate={onUpdate}
      />
    </>
  );
}
