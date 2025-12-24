import { useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  CircularProgress,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Snackbar,
  Button,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from '@mui/material';
import { AccountTree, Add, Edit, Delete, Code } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getAllWorkflows,
  createWorkflow,
  updateWorkflow,
  deleteWorkflow,
  WorkflowDefinition,
} from '../shared/services/adminService';

/**
 * Workflow Definition Editor Page
 * 
 * Displays all workflow definitions with basic JSON-based CRUD.
 * Route: /admin/workflows
 */
export function WorkflowDefinitionEditorPage() {
  const queryClient = useQueryClient();
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });
  const [jsonDialogOpen, setJsonDialogOpen] = useState(false);
  const [jsonTitle, setJsonTitle] = useState<string>('');
  const [jsonContent, setJsonContent] = useState<string>('');

  // Fetch all workflows
  const {
    data: workflows,
    isLoading,
    error,
  } = useQuery<WorkflowDefinition[]>({
    queryKey: ['workflows'],
    queryFn: getAllWorkflows,
    staleTime: 5 * 60 * 1000,
  });

  const createMutation = useMutation({
    mutationFn: createWorkflow,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      setSnackbar({ open: true, message: 'Workflow created successfully', severity: 'success' });
    },
    onError: (err: Error) => {
      setSnackbar({ open: true, message: `Failed to create workflow: ${err.message}`, severity: 'error' });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ workflowId, payload }: { workflowId: string; payload: any }) =>
      updateWorkflow(workflowId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      setSnackbar({ open: true, message: 'Workflow updated successfully', severity: 'success' });
    },
    onError: (err: Error) => {
      setSnackbar({ open: true, message: `Failed to update workflow: ${err.message}`, severity: 'error' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteWorkflow,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      setSnackbar({ open: true, message: 'Workflow deleted successfully', severity: 'success' });
    },
    onError: (err: Error) => {
      setSnackbar({ open: true, message: `Failed to delete workflow: ${err.message}`, severity: 'error' });
    },
  });

  const handleOpenCreateJson = () => {
    const initial: Partial<WorkflowDefinition> = {
      workflowId: '',
      name: '',
      description: '',
      initialState: '',
      metadataJson: {},
      states: [],
      transitions: [],
    };
    setJsonTitle('Create Workflow Definition (JSON)');
    setJsonContent(JSON.stringify(initial, null, 2));
    setJsonDialogOpen(true);
  };

  const handleOpenEditJson = (workflow: WorkflowDefinition) => {
    setJsonTitle(`Edit Workflow: ${workflow.workflowId}`);
    setJsonContent(JSON.stringify(workflow, null, 2));
    setJsonDialogOpen(true);
  };

  const handleSaveJson = () => {
    try {
      const parsed = JSON.parse(jsonContent);

      if (!parsed.workflowId || !parsed.name || !parsed.initialState) {
        setSnackbar({
          open: true,
          message: 'JSON must include at least "workflowId", "name", and "initialState".',
          backtrace: undefined,
          severity: 'error',
        } as any);
        return;
      }

      const exists = (workflows || []).some((w) => w.workflowId === parsed.workflowId);

      if (exists) {
        updateMutation.mutate({ workflowId: parsed.workflowId, payload: parsed });
      } else {
        createMutation.mutate(parsed);
      }

      setJsonDialogOpen(false);
    } catch (err) {
      setSnackbar({
        open: true,
        message: `Invalid JSON: ${err instanceof Error ? err.message : String(err)}`,
        severity: 'error',
      });
    }
  };

  const handleDeleteWorkflow = (workflowId: string) => {
    if (window.confirm(`Are you sure you want to delete workflow "${workflowId}"?`)) {
      deleteMutation.mutate(workflowId);
    }
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

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
        <Alert severity="error">
          Failed to load workflows: {error instanceof Error ? error.message : 'Unknown error'}
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ width: '100%', height: '100%' }}>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
            <AccountTree sx={{ fontSize: 32 }} />
            <Typography variant="h4" component="h1" color="text.primary">
              Workflow Definitions
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary">
            View and manage workflow definitions (edit raw JSON for advanced scenarios)
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={handleOpenCreateJson}
        >
          Create Workflow
        </Button>
      </Box>

      {/* Workflows Table */}
      <TableContainer component={Paper} elevation={0}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Workflow ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Initial State</TableCell>
              <TableCell>States</TableCell>
              <TableCell>Transitions</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {workflows && workflows.length > 0 ? (
              workflows.map((workflow) => (
                <TableRow key={workflow.workflowId} hover>
                  <TableCell>
                    <Typography variant="body2" fontFamily="monospace">
                      {workflow.workflowId}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body1" fontWeight="medium">
                      {workflow.name}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {workflow.description || '-'}
                    </Typography>
                  </TableCell>
                  <TableCell>{workflow.initialState}</TableCell>
                  <TableCell>{workflow.states?.length || 0}</TableCell>
                  <TableCell>{workflow.transitions?.length || 0}</TableCell>
                  <TableCell align="right">
                    <Tooltip title="Edit workflow JSON">
                      <IconButton
                        size="small"
                        onClick={() => handleOpenEditJson(workflow)}
                        color="primary"
                      >
                        <Code />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete workflow">
                      <IconButton
                        size="small"
                        onClick={() => handleDeleteWorkflow(workflow.workflowId)}
                        disabled={deleteMutation.isPending}
                        color="error"
                      >
                        <Delete />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                    No workflow definitions found
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* JSON Editor Dialog */}
      <Dialog open={jsonDialogOpen} onClose={() => setJsonDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>{jsonTitle}</DialogTitle>
        <DialogContent dividers>
          <TextField
            value={jsonContent}
            onChange={(e) => setJsonContent(e.target.value)}
            multiline
            minRows={20}
            fullWidth
            InputProps={{ sx: { fontFamily: 'Roboto Mono, monospace', fontSize: 12 } }}
          />
        </DialogContent>
        <DialogActions>
          <Button
            onClick={handleSaveJson}
            variant="contained"
            disabled={createMutation.isPending || updateMutation.isPending}
          >
            Save JSON
          </Button>
          <Button onClick={() => setJsonDialogOpen(false)}>Cancel</Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}
