import { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Chip,
  Divider,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from '@mui/material';
import { PlayArrow, History } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAvailableTransitions, executeTransition, AvailableTransition } from '../shared/services/workflowService';
import { WorkflowHistoryDialog } from './WorkflowHistoryDialog';

interface WorkflowActionBarProps {
  entityId: string;
  recordId: string;
  currentState?: string;
}

/**
 * Workflow Action Bar Component
 * 
 * Displays current workflow state and available transitions.
 * Shows on entity detail view when a workflow is assigned.
 */
export function WorkflowActionBar({ entityId, recordId, currentState }: WorkflowActionBarProps) {
  const [transitionDialogOpen, setTransitionDialogOpen] = useState(false);
  const [selectedTransition, setSelectedTransition] = useState<AvailableTransition | null>(null);
  const [comments, setComments] = useState('');
  const [historyDialogOpen, setHistoryDialogOpen] = useState(false);
  const queryClient = useQueryClient();

  // Fetch available transitions
  const {
    data: transitions,
    isLoading: isLoadingTransitions,
    error: transitionsError,
  } = useQuery({
    queryKey: ['workflow-transitions', entityId, recordId],
    queryFn: () => getAvailableTransitions(entityId, recordId),
    enabled: !!entityId && !!recordId,
  });

  // Execute transition mutation
  const executeTransitionMutation = useMutation({
    mutationFn: ({ transitionId, comments }: { transitionId: string; comments?: string }) =>
      executeTransition(entityId, recordId, transitionId, comments),
    onSuccess: () => {
      // Invalidate queries to refresh data
      queryClient.invalidateQueries({ queryKey: ['entity-record', entityId, recordId] });
      queryClient.invalidateQueries({ queryKey: ['workflow-transitions', entityId, recordId] });
      queryClient.invalidateQueries({ queryKey: ['workflow-audit', entityId, recordId] });
      
      // Close dialog and reset
      setTransitionDialogOpen(false);
      setSelectedTransition(null);
      setComments('');
    },
  });

  const handleTransitionClick = (transition: AvailableTransition) => {
    setSelectedTransition(transition);
    setTransitionDialogOpen(true);
  };

  const handleConfirmTransition = () => {
    if (selectedTransition) {
      executeTransitionMutation.mutate({
        transitionId: selectedTransition.transitionId,
        comments: comments.trim() || undefined,
      });
    }
  };

  const handleCancelTransition = () => {
    setTransitionDialogOpen(false);
    setSelectedTransition(null);
    setComments('');
  };

  // Don't show if no workflow (no transitions available and no state)
  if (!isLoadingTransitions && (!transitions || transitions.length === 0) && !currentState) {
    return null;
  }

  return (
    <>
      <Paper
        elevation={0}
        sx={{
          p: 2,
          mb: 3,
          backgroundColor: 'background.paper',
          borderRadius: 2,
          border: 1,
          borderColor: 'divider',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Typography variant="h6" color="text.primary">
              Workflow
            </Typography>
            {currentState && (
              <Chip
                label={currentState}
                color="primary"
                variant="outlined"
                size="small"
              />
            )}
          </Box>
          <Button
            variant="outlined"
            size="small"
            startIcon={<History />}
            onClick={() => setHistoryDialogOpen(true)}
          >
            History
          </Button>
        </Box>

        {isLoadingTransitions ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
            <CircularProgress size={24} />
          </Box>
        ) : transitionsError ? (
          <Alert severity="error" sx={{ mb: 2 }}>
            Failed to load transitions: {transitionsError instanceof Error ? transitionsError.message : 'Unknown error'}
          </Alert>
        ) : transitions && transitions.length > 0 ? (
          <>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
              Available Actions:
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {transitions.map((transition) => (
                <Button
                  key={transition.transitionId}
                  variant="contained"
                  size="small"
                  startIcon={<PlayArrow />}
                  onClick={() => handleTransitionClick(transition)}
                  sx={{ textTransform: 'none' }}
                >
                  {transition.actionLabel}
                </Button>
              ))}
            </Box>
            {transitions.some((t) => t.description) && (
              <>
                <Divider sx={{ my: 2 }} />
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                  {transitions
                    .filter((t) => t.description)
                    .map((transition) => (
                      <Box key={transition.transitionId}>
                        <Typography variant="caption" color="text.secondary" fontWeight="medium">
                          {transition.actionLabel}:
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {transition.description}
                        </Typography>
                      </Box>
                    ))}
                </Box>
              </>
            )}
          </>
        ) : (
          <Typography variant="body2" color="text.secondary">
            No transitions available from current state
          </Typography>
        )}
      </Paper>

      {/* Transition Confirmation Dialog */}
      <Dialog
        open={transitionDialogOpen}
        onClose={handleCancelTransition}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          Confirm Transition: {selectedTransition?.actionLabel}
        </DialogTitle>
        <DialogContent>
          {selectedTransition && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
              <Box>
                <Typography variant="caption" color="text.secondary">
                  From State:
                </Typography>
                <Typography variant="body2">{selectedTransition.fromState}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">
                  To State:
                </Typography>
                <Typography variant="body2">{selectedTransition.toState}</Typography>
              </Box>
              {selectedTransition.description && (
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Description:
                  </Typography>
                  <Typography variant="body2">{selectedTransition.description}</Typography>
                </Box>
              )}
              <TextField
                label="Comments (Optional)"
                multiline
                rows={3}
                value={comments}
                onChange={(e) => setComments(e.target.value)}
                placeholder="Add any comments or notes about this transition..."
                fullWidth
              />
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelTransition} disabled={executeTransitionMutation.isPending}>
            Cancel
          </Button>
          <Button
            onClick={handleConfirmTransition}
            variant="contained"
            disabled={executeTransitionMutation.isPending}
            startIcon={executeTransitionMutation.isPending ? <CircularProgress size={16} /> : <PlayArrow />}
          >
            {executeTransitionMutation.isPending ? 'Executing...' : 'Confirm'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Workflow History Dialog */}
      <WorkflowHistoryDialog
        open={historyDialogOpen}
        onClose={() => setHistoryDialogOpen(false)}
        entityId={entityId}
        recordId={recordId}
      />
    </>
  );
}
