import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Chip,
  Divider,
  TextField,
  Alert,
  CircularProgress,
  Grid,
} from '@mui/material';
import {
  CheckCircle,
  Cancel,
  Build,
  ArrowForward,
  AccessTime,
  Flag,
  Person,
  CalendarToday,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getTask, submitDecision, getAvailableTaskTransitions, executeTaskTransition } from '../shared/services/taskService';

interface TaskDetailDialogProps {
  open: boolean;
  onClose: () => void;
  taskId: string;
  onUpdate?: () => void;
}

/**
 * Task Detail Dialog Component
 * 
 * Displays full task details and allows actions (approve/reject/request-changes/complete).
 */
export function TaskDetailDialog({
  open,
  onClose,
  taskId,
  onUpdate,
}: TaskDetailDialogProps) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [decisionComments, setDecisionComments] = useState('');
  const [selectedDecision, setSelectedDecision] = useState<'APPROVED' | 'REJECTED' | 'REQUEST_CHANGES' | null>(null);
  const [transitionComments, setTransitionComments] = useState('');
  const [selectedTransition, setSelectedTransition] = useState<string | null>(null);

  // Fetch task details
  const {
    data: task,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['task', taskId],
    queryFn: () => getTask(taskId),
    enabled: open && !!taskId,
  });

  // Fetch available transitions
  const {
    data: availableTransitions,
  } = useQuery({
    queryKey: ['task-transitions', taskId],
    queryFn: () => getAvailableTaskTransitions(taskId),
    enabled: open && !!taskId && task && (task.status === 'OPEN' || task.status === 'IN_PROGRESS'),
  });

  // Execute transition mutation
  const executeTransitionMutation = useMutation({
    mutationFn: ({ transitionId, comments }: { transitionId: string; comments?: string }) =>
      executeTaskTransition(taskId, transitionId, comments),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['task', taskId] });
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      queryClient.invalidateQueries({ queryKey: ['task-transitions', taskId] });
      onUpdate?.();
      setSelectedTransition(null);
      setTransitionComments('');
    },
  });

  // Submit decision mutation
  const submitDecisionMutation = useMutation({
    mutationFn: ({ decision, comments }: { decision: 'APPROVED' | 'REJECTED' | 'REQUEST_CHANGES'; comments?: string }) =>
      submitDecision(taskId, decision, comments),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['task', taskId] });
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      onUpdate?.();
      handleClose();
    },
  });


  const handleClose = () => {
    setDecisionComments('');
    setSelectedDecision(null);
    setTransitionComments('');
    setSelectedTransition(null);
    onClose();
  };

  const handleDecisionClick = (decision: 'APPROVED' | 'REJECTED' | 'REQUEST_CHANGES') => {
    setSelectedDecision(decision);
  };

  const handleSubmitDecision = () => {
    if (selectedDecision) {
      submitDecisionMutation.mutate({
        decision: selectedDecision,
        comments: decisionComments.trim() || undefined,
      });
    }
  };

  const handleTransitionClick = (transitionId: string) => {
    setSelectedTransition(transitionId);
  };

  const handleExecuteTransition = () => {
    if (selectedTransition) {
      executeTransitionMutation.mutate({
        transitionId: selectedTransition,
        comments: transitionComments.trim() || undefined,
      });
    }
  };

  const handleViewRecord = () => {
    if (task) {
      navigate(`/entities/${task.entityId}/records/${task.recordId}`);
      handleClose();
    }
  };

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

  const canTakeAction = task && (task.status === 'OPEN' || task.status === 'IN_PROGRESS');
  const isOverdue = task?.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'DONE' && task.status !== 'CANCELLED';

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">{task?.title || 'Task Details'}</Typography>
          {task && (
            <Chip
              label={task.status}
              size="small"
              color={getStatusColor(task.status)}
            />
          )}
        </Box>
      </DialogTitle>
      <DialogContent>
        {isLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        ) : error ? (
          <Alert severity="error">
            Failed to load task: {error instanceof Error ? error.message : 'Unknown error'}
          </Alert>
        ) : task ? (
          <Box>
            {/* Description */}
            {task.description && (
              <Box sx={{ mb: 3 }}>
                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                  Description
                </Typography>
                <Typography variant="body1">{task.description}</Typography>
              </Box>
            )}

            <Divider sx={{ my: 3 }} />

            {/* Task Metadata */}
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} sm={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                  <Person fontSize="small" color="action" />
                  <Typography variant="caption" color="text.secondary">
                    Assignee
                  </Typography>
                </Box>
                <Typography variant="body2">{task.assigneeUsername}</Typography>
              </Grid>
              {task.priority && (
                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                    <Flag fontSize="small" color="action" />
                    <Typography variant="caption" color="text.secondary">
                      Priority
                    </Typography>
                  </Box>
                  <Chip
                    label={task.priority}
                    size="small"
                    color={getPriorityColor(task.priority)}
                  />
                </Grid>
              )}
              {task.dueDate && (
                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                    <AccessTime fontSize="small" color={isOverdue ? 'error' : 'action'} />
                    <Typography variant="caption" color="text.secondary">
                      Due Date
                    </Typography>
                  </Box>
                  <Typography variant="body2" color={isOverdue ? 'error.main' : 'text.primary'}>
                    {new Date(task.dueDate).toLocaleString()}
                    {isOverdue && ' (Overdue)'}
                  </Typography>
                </Grid>
              )}
              {task.workflowState && (
                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                    <CalendarToday fontSize="small" color="action" />
                    <Typography variant="caption" color="text.secondary">
                      Workflow State
                    </Typography>
                  </Box>
                  <Chip label={task.workflowState} size="small" variant="outlined" />
                </Grid>
              )}
              <Grid item xs={12} sm={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                  <CalendarToday fontSize="small" color="action" />
                  <Typography variant="caption" color="text.secondary">
                    Created At
                  </Typography>
                </Box>
                <Typography variant="body2">
                  {new Date(task.createdAt).toLocaleString()}
                </Typography>
              </Grid>
            </Grid>

            {/* Decision Section (if task is done) */}
            {task.status === 'DONE' && task.decision && (
              <>
                <Divider sx={{ my: 3 }} />
                <Box>
                  <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                    Decision
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
                    <Chip
                      label={task.decision}
                      color={task.decision === 'APPROVED' ? 'success' : task.decision === 'REJECTED' ? 'error' : 'warning'}
                      size="small"
                    />
                  </Box>
                  {task.decisionComments && (
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1, fontStyle: 'italic' }}>
                      "{task.decisionComments}"
                    </Typography>
                  )}
                  {task.completedAt && (
                    <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                      Completed: {new Date(task.completedAt).toLocaleString()}
                    </Typography>
                  )}
                </Box>
              </>
            )}

            {/* Workflow Transitions Section (if task is open/in progress) */}
            {canTakeAction && availableTransitions && availableTransitions.length > 0 && (
              <>
                <Divider sx={{ my: 3 }} />
                <Box>
                  <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                    Status Transitions
                  </Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 2 }}>
                    {availableTransitions.map((transition) => (
                      <Button
                        key={transition.transitionId}
                        variant={selectedTransition === transition.transitionId ? 'contained' : 'outlined'}
                        color="primary"
                        onClick={() => handleTransitionClick(transition.transitionId)}
                        size="small"
                      >
                        {transition.actionLabel}
                      </Button>
                    ))}
                  </Box>

                  {/* Comments field (shown when a transition is selected) */}
                  {selectedTransition && (
                    <Box sx={{ mt: 2 }}>
                      <TextField
                        label="Comments (Optional)"
                        multiline
                        rows={3}
                        value={transitionComments}
                        onChange={(e) => setTransitionComments(e.target.value)}
                        placeholder="Add any comments about this transition..."
                        fullWidth
                      />
                    </Box>
                  )}
                </Box>
              </>
            )}

            {/* Action Section (if task is open/in progress) */}
            {canTakeAction && (
              <>
                <Divider sx={{ my: 3 }} />
                <Box>
                  <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                    Decisions
                  </Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 2 }}>
                    <Button
                      variant={selectedDecision === 'APPROVED' ? 'contained' : 'outlined'}
                      color="success"
                      startIcon={<CheckCircle />}
                      onClick={() => handleDecisionClick('APPROVED')}
                      size="small"
                    >
                      Approve
                    </Button>
                    <Button
                      variant={selectedDecision === 'REJECTED' ? 'contained' : 'outlined'}
                      color="error"
                      startIcon={<Cancel />}
                      onClick={() => handleDecisionClick('REJECTED')}
                      size="small"
                    >
                      Reject
                    </Button>
                    <Button
                      variant={selectedDecision === 'REQUEST_CHANGES' ? 'contained' : 'outlined'}
                      color="warning"
                      startIcon={<Build />}
                      onClick={() => handleDecisionClick('REQUEST_CHANGES')}
                      size="small"
                    >
                      Request Changes
                    </Button>
                  </Box>

                  {/* Comments field (shown when a decision is selected) */}
                  {selectedDecision && (
                    <Box sx={{ mt: 2 }}>
                      <TextField
                        label="Comments (Optional)"
                        multiline
                        rows={3}
                        value={decisionComments}
                        onChange={(e) => setDecisionComments(e.target.value)}
                        placeholder="Add any comments about this decision..."
                        fullWidth
                      />
                    </Box>
                  )}
                </Box>
              </>
            )}
          </Box>
        ) : null}
      </DialogContent>
      <DialogActions>
        <Button onClick={handleViewRecord} startIcon={<ArrowForward />}>
          View Record
        </Button>
        <Box sx={{ flexGrow: 1 }} />
        {canTakeAction && selectedTransition && (
          <Button
            onClick={handleExecuteTransition}
            variant="contained"
            disabled={executeTransitionMutation.isPending}
            startIcon={executeTransitionMutation.isPending ? <CircularProgress size={16} /> : <CheckCircle />}
          >
            {executeTransitionMutation.isPending ? 'Executing...' : 'Execute Transition'}
          </Button>
        )}
        {canTakeAction && selectedDecision && (
          <Button
            onClick={handleSubmitDecision}
            variant="contained"
            disabled={submitDecisionMutation.isPending}
            startIcon={submitDecisionMutation.isPending ? <CircularProgress size={16} /> : <CheckCircle />}
          >
            {submitDecisionMutation.isPending ? 'Submitting...' : 'Submit Decision'}
          </Button>
        )}
        <Button onClick={handleClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
}
