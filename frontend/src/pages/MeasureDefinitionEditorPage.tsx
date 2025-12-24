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
  Button,
  Snackbar,
  Chip,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from '@mui/material';
import { Add, Functions, Code } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAllMeasures, createOrUpdateMeasure, Measure } from '../shared/services/adminService';

/**
 * Measure Definition Editor Page
 *
 * Displays all measure definitions and lets admins create/edit them via raw JSON.
 * Route: /admin/measures
 */
export function MeasureDefinitionEditorPage() {
  const queryClient = useQueryClient();
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });
  const [jsonDialogOpen, setJsonDialogOpen] = useState(false);
  const [jsonTitle, setJsonTitle] = useState<string>('');
  const [jsonContent, setJsonContent] = useState<string>('');

  // Load all measures
  const {
    data: measures,
    isLoading,
    error,
  } = useQuery<Measure[]>({
    queryKey: ['measures'],
    queryFn: getAllMeasures,
    staleTime: 5 * 60 * 1000,
  });

  const saveMutation = useMutation({
    mutationFn: createOrUpdateMeasure,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['measures'] });
      setSnackbar({ open: true, message: 'Measure saved successfully', severity: 'success' });
    },
    onError: (err: Error) => {
      setSnackbar({ open: true, message: `Failed to save measure: ${err.message}`, severity: 'error' });
    },
  });

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const handleOpenCreate = () => {
    const template: Partial<Measure> = {
      measureIdentifier: '',
      name: '',
      version: 1,
      returnType: 'number',
      definitionType: 'EXPRESSION',
      description: '',
      parametersJson: [],
      dependsOnJson: [],
      metadataJson: {},
      expression: '',
    };

    setJsonTitle('Create Measure (JSON)');
    setJsonContent(JSON.stringify(template, null, 2));
    setJsonDialogOpen(true);
  };

  const handleOpenEdit = (measure: Measure) => {
    setJsonTitle(`Edit Measure: ${measure.measureIdentifier} (v${measure.version})`);
    setJsonContent(JSON.stringify(measure, null, 2));
    setJsonDialogOpen(true);
  };

  const handleSaveJson = () => {
    try {
      const parsed = JSON.parse(jsonContent) as Measure;

      if (!parsed.measureIdentifier || parsed.version === undefined || !parsed.name || !parsed.returnType) {
        setSnackbar({
          open: true,
          message: 'JSON must include at least "measureIdentifier", "version", "name", and "returnType".',
          severity: 'error',
        });
        return;
      }

      saveMutation.mutate(parsed);
      setJsonDialogOpen(false);
    } catch (err) {
      setSnackbar({
        open: true,
        message: `Invalid JSON: ${err instanceof Error ? err.message : String(err)}`,
        severity: 'error',
      });
    }
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
          Failed to load measures: {error instanceof Error ? error.message : 'Unknown error'}
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
            <Functions sx={{ fontSize: 32 }} />
            <Typography variant="h4" component="h1" color="text.primary">
              Measure Definitions
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary">
            View and manage measure definitions (edit raw JSON to define complex calculations)
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<Add />} onClick={handleOpenCreate}>
          Create Measure
        </Button>
      </Box>

      {/* Measures Table */}
      <TableContainer component={Paper} elevation={0}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Identifier</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Version</TableCell>
              <TableCell>Return Type</TableCell>
              <TableCell>Definition Type</TableCell>
              <TableCell>Description</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {measures && measures.length > 0 ? (
              measures.map((measure) => (
                <TableRow key={`${measure.measureIdentifier}-${measure.version}`} hover>
                  <TableCell>
                    <Typography variant="body2" fontFamily="monospace">
                      {measure.measureIdentifier}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body1" fontWeight="medium">
                      {measure.name}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip label={`v${measure.version}`} size="small" />
                  </TableCell>
                  <TableCell>{measure.returnType}</TableCell>
                  <TableCell>{measure.definitionType}</TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {measure.description || '-'}
                    </Typography>
                  </TableCell>
                  <TableCell align="right">
                    <Tooltip title="Edit measure JSON">
                      <IconButton size="small" onClick={() => handleOpenEdit(measure)} color="primary">
                        <Code />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                    No measure definitions found
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
            InputProps={{ sx: { fontFamily: 'monospace', fontSize: 13 } }}
          />
        </DialogContent>
        <DialogActions>
          <Button
            onClick={handleSaveJson}
            variant="contained"
            disabled={saveMutation.isPending}
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
