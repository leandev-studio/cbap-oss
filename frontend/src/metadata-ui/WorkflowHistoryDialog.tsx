import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  CircularProgress,
  Alert,
  Chip,
  Divider,
} from '@mui/material';
import { Circle } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { getWorkflowAuditLog, WorkflowAuditLogEntry } from '../shared/services/workflowService';

interface WorkflowHistoryDialogProps {
  open: boolean;
  onClose: () => void;
  entityId: string;
  recordId: string;
}

/**
 * Workflow History Dialog Component
 * 
 * Displays the workflow audit log (state history) for a record.
 */
export function WorkflowHistoryDialog({
  open,
  onClose,
  entityId,
  recordId,
}: WorkflowHistoryDialogProps) {
  const {
    data: auditLog,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['workflow-audit', entityId, recordId],
    queryFn: () => getWorkflowAuditLog(entityId, recordId),
    enabled: open && !!entityId && !!recordId,
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Workflow History</DialogTitle>
      <DialogContent>
        {isLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        ) : error ? (
          <Alert severity="error">
            Failed to load workflow history: {error instanceof Error ? error.message : 'Unknown error'}
          </Alert>
        ) : auditLog && auditLog.length > 0 ? (
          <Box>
            {auditLog.map((entry, index) => (
              <Box key={entry.auditId}>
                <Box sx={{ display: 'flex', gap: 2 }}>
                  <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: 40 }}>
                    <Circle sx={{ fontSize: 16, color: 'primary.main' }} />
                    {index < auditLog.length - 1 && (
                      <Box
                        sx={{
                          width: 2,
                          flexGrow: 1,
                          backgroundColor: 'divider',
                          my: 0.5,
                        }}
                      />
                    )}
                  </Box>
                  <Box sx={{ flexGrow: 1, pb: index < auditLog.length - 1 ? 2 : 0 }}>
                    <WorkflowHistoryEntry entry={entry} />
                  </Box>
                </Box>
                {index < auditLog.length - 1 && <Divider sx={{ my: 1 }} />}
              </Box>
            ))}
          </Box>
        ) : (
          <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
            No workflow history available
          </Typography>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
}

/**
 * Workflow History Entry Component
 */
function WorkflowHistoryEntry({ entry }: { entry: WorkflowAuditLogEntry }) {
  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
        <Typography variant="subtitle2" color="text.primary">
          {entry.transitionLabel}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          by {entry.performedByUsername || entry.performedBy}
        </Typography>
      </Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
        <Chip
          label={entry.fromState}
          size="small"
          variant="outlined"
          sx={{ fontSize: '0.7rem', height: 20 }}
        />
        <Typography variant="caption" color="text.secondary">
          →
        </Typography>
        <Chip
          label={entry.toState}
          size="small"
          color="primary"
          sx={{ fontSize: '0.7rem', height: 20 }}
        />
      </Box>
      {entry.comments && (
        <Box sx={{ mb: 1 }}>
          <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
            "{entry.comments}"
          </Typography>
        </Box>
      )}
      <Typography variant="caption" color="text.secondary">
        {new Date(entry.performedAt).toLocaleString()}
      </Typography>
    </Box>
  );
}
