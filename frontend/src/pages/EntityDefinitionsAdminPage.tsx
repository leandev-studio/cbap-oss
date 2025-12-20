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
  IconButton,
  Tooltip,
  Snackbar,
} from '@mui/material';
import { Refresh, Schema } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAllEntities } from '../shared/services/entityMetadataService';
import { reindexEntity } from '../shared/services/adminService';

/**
 * Entity Definitions Admin Page
 * 
 * Displays all entity definitions with options to reindex search data.
 * Route: /admin/entity-definitions
 */
export function EntityDefinitionsAdminPage() {
  const queryClient = useQueryClient();
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  // Fetch all entities
  const {
    data: entities,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['entities'],
    queryFn: getAllEntities,
    staleTime: 5 * 60 * 1000,
  });

  // Reindex mutation
  const reindexMutation = useMutation({
    mutationFn: reindexEntity,
    onSuccess: (data) => {
      setSnackbar({
        open: true,
        message: `Reindexing completed: ${data.indexedRecords} of ${data.totalRecords} records indexed for ${data.entityId}`,
        severity: 'success',
      });
      // Invalidate search queries to refresh results
      queryClient.invalidateQueries({ queryKey: ['search'] });
    },
    onError: (error: Error) => {
      setSnackbar({
        open: true,
        message: `Reindexing failed: ${error.message}`,
        severity: 'error',
      });
    },
  });

  const handleReindex = (entityId: string) => {
    if (window.confirm(`Reindex all records for entity "${entityId}"? This will rebuild the search index.`)) {
      reindexMutation.mutate(entityId);
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
          Failed to load entity definitions: {error instanceof Error ? error.message : 'Unknown error'}
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ width: '100%', height: '100%' }}>
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
          <Schema sx={{ fontSize: 32 }} />
          <Typography variant="h4" component="h1" color="text.primary">
            Entity Definitions
          </Typography>
        </Box>
        <Typography variant="body2" color="text.secondary">
          Manage entity definitions and reindex search data
        </Typography>
      </Box>

      {/* Entity Definitions Table */}
      <TableContainer component={Paper} elevation={0}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Entity ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Schema Version</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {entities && entities.length > 0 ? (
              entities.map((entity) => (
                <TableRow key={entity.entityId} hover>
                  <TableCell>
                    <Typography variant="body2" fontFamily="monospace">
                      {entity.entityId}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body1" fontWeight="medium">
                      {entity.name}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {entity.description || '-'}
                    </Typography>
                  </TableCell>
                  <TableCell>{entity.schemaVersion}</TableCell>
                  <TableCell align="right">
                    <Tooltip title="Reindex all records for this entity">
                      <IconButton
                        size="small"
                        onClick={() => handleReindex(entity.entityId)}
                        disabled={reindexMutation.isPending}
                        color="primary"
                      >
                        <Refresh />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={5} align="center">
                  <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                    No entity definitions found
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Snackbar for notifications */}
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
