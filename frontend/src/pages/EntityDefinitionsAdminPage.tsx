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
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
} from '@mui/material';
import { Refresh, Schema, Delete, Add, Edit, Code } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getAllEntities,
  createEntity,
  updateEntity,
  deleteEntity,
  EntityDefinition,
  CreateEntityRequest,
  UpdateEntityRequest,
} from '../shared/services/entityMetadataService';
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
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingEntity, setEditingEntity] = useState<EntityDefinition | null>(null);
  const [formData, setFormData] = useState<CreateEntityRequest>({
    entityId: '',
    name: '',
    description: '',
    schemaVersion: 1,
    screenVersion: 1,
    workflowId: '',
    authorizationModel: '',
    scope: 'LOCAL',
    metadataJson: {},
    properties: [],
  });
  const [jsonDialogOpen, setJsonDialogOpen] = useState(false);
  const [jsonTitle, setJsonTitle] = useState<string>('');
  const [jsonContent, setJsonContent] = useState<string>('');

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

  // Delete entity mutation
  const deleteMutation = useMutation({
    mutationFn: deleteEntity,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['entities'] });
      setSnackbar({
        open: true,
        message: 'Entity definition deleted successfully',
        severity: 'success',
      });
    },
    onError: (error: Error) => {
      setSnackbar({
        open: true,
        message: `Failed to delete entity: ${error.message}`,
        severity: 'error',
      });
    },
  });

  // Create entity mutation
  const createMutation = useMutation({
    mutationFn: createEntity,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['entities'] });
      setSnackbar({
        open: true,
        message: 'Entity definition created successfully',
        severity: 'success',
      });
      handleCloseDialog();
    },
    onError: (error: Error) => {
      setSnackbar({
        open: true,
        message: `Failed to create entity: ${error.message}`,
        severity: 'error',
      });
    },
  });

  // Update entity mutation
  const updateMutation = useMutation({
    mutationFn: ({ entityId, request }: { entityId: string; request: UpdateEntityRequest }) =>
      updateEntity(entityId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['entities'] });
      setSnackbar({
        open: true,
        message: 'Entity definition updated successfully',
        severity: 'success',
      });
      handleCloseDialog();
    },
    onError: (error: Error) => {
      setSnackbar({
        open: true,
        message: `Failed to update entity: ${error.message}`,
        severity: 'error',
      });
    },
  });

  const handleReindex = (entityId: string) => {
    if (window.confirm(`Reindex all records for entity "${entityId}"? This will rebuild the search index.`)) {
      reindexMutation.mutate(entityId);
    }
  };

  const handleDelete = (entityId: string) => {
    if (window.confirm(`Are you sure you want to delete entity definition "${entityId}"? This action cannot be undone.`)) {
      deleteMutation.mutate(entityId);
    }
  };

  const handleOpenCreateDialog = () => {
    setEditingEntity(null);
    setFormData({
      entityId: '',
      name: '',
      description: '',
      schemaVersion: 1,
      screenVersion: 1,
      workflowId: '',
      authorizationModel: '',
      scope: 'LOCAL',
      metadataJson: {},
      properties: [],
    });
    setDialogOpen(true);
  };

  const handleOpenEditDialog = (entity: EntityDefinition) => {
    setEditingEntity(entity);
    setFormData({
      entityId: entity.entityId,
      name: entity.name,
      description: entity.description,
      schemaVersion: entity.schemaVersion,
      screenVersion: entity.screenVersion,
      workflowId: entity.workflowId,
      authorizationModel: entity.authorizationModel,
      scope: entity.scope || 'LOCAL',
      metadataJson: entity.metadataJson || {},
      properties: [], // Property editing is out of scope for this basic editor
    });
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditingEntity(null);
  };

  const handleSave = () => {
    if (!formData.entityId || !formData.name) {
      setSnackbar({
        open: true,
        message: 'Entity ID and Name are required',
        severity: 'error',
      });
      return;
    }

    if (editingEntity) {
      const request: UpdateEntityRequest = {
        name: formData.name,
        description: formData.description,
        schemaVersion: formData.schemaVersion,
        screenVersion: formData.screenVersion,
        workflowId: formData.workflowId || undefined,
        authorizationModel: formData.authorizationModel || undefined,
        scope: formData.scope,
        metadataJson: formData.metadataJson,
      };
      updateMutation.mutate({ entityId: editingEntity.entityId, request });
    } else {
      createMutation.mutate({
        ...formData,
        workflowId: formData.workflowId || undefined,
        authorizationModel: formData.authorizationModel || undefined,
      });
    }
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const handleOpenJsonDialog = (entity: EntityDefinition) => {
    setJsonTitle(`Entity JSON - ${entity.entityId}`);
    setJsonContent(JSON.stringify(entity, null, 2));
    setJsonDialogOpen(true);
  };

  const handleCloseJsonDialog = () => {
    setJsonDialogOpen(false);
    setJsonContent('');
  };

  const handleSaveJson = () => {
    try {
      const parsed = JSON.parse(jsonContent);

      if (!parsed.entityId || !parsed.name) {
        setSnackbar({
          open: true,
          message: 'JSON must include at least "entityId" and "name".',
          severity: 'error',
        });
        return;
      }

      // Build create/update payloads from raw JSON
      const createReq: CreateEntityRequest = {
        entityId: parsed.entityId,
        name: parsed.name,
        description: parsed.description,
        schemaVersion: parsed.schemaVersion,
        screenVersion: parsed.screenVersion,
        workflowId: parsed.workflowId,
        authorizationModel: parsed.authorizationModel,
        scope: parsed.scope,
        metadataJson: parsed.metadataJson,
        properties: parsed.properties,
      };

      const isExisting = entities?.some((e) => e.entityId === parsed.entityId);

      if (isExisting) {
        const updateReq: UpdateEntityRequest = {
          name: parsed.name,
          description: parsed.description,
          schemaVersion: parsed.schemaVersion,
          screenVersion: parsed.screenVersion,
          workflowId: parsed.workflowId,
          authorizationModel: parsed.authorizationModel,
          scope: parsed.scope,
          metadataJson: parsed.metadataJson,
        };
        updateMutation.mutate({ entityId: parsed.entityId, request: updateReq });
      } else {
        createMutation.mutate(createReq);
      }

      // Close JSON dialog; main list will refresh via query invalidation
      handleCloseJsonDialog();
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
          Failed to load entity definitions: {error instanceof Error ? error.message : 'Unknown error'}
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
            <Schema sx={{ fontSize: 32 }} />
            <Typography variant="h4" component="h1" color="text.primary">
              Entity Definitions
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary">
            Manage entity definitions and reindex search data
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={handleOpenCreateDialog}
        >
          Create Entity
        </Button>
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
              <TableCell>Properties</TableCell>
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
                  <TableCell>{entity.properties?.length || 0}</TableCell>
                  <TableCell align="right">
                    <Tooltip title="View raw entity & properties JSON">
                      <IconButton
                        size="small"
                        onClick={() => handleOpenJsonDialog(entity)}
                        sx={{ mr: 0.5 }}
                      >
                        <Code />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Edit entity definition (basic metadata)">
                      <IconButton
                        size="small"
                        onClick={() => handleOpenEditDialog(entity)}
                        color="primary"
                        sx={{ mr: 0.5 }}
                      >
                        <Edit />
                      </IconButton>
                    </Tooltip>
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
                    <Tooltip title="Delete entity definition">
                      <IconButton
                        size="small"
                        onClick={() => handleDelete(entity.entityId)}
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
                <TableCell colSpan={6} align="center">
                  <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                    No entity definitions found
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Raw JSON Viewer Dialog (read-only) */}
      <Dialog open={jsonDialogOpen} onClose={handleCloseJsonDialog} maxWidth="md" fullWidth>
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
          <Button onClick={handleSaveJson} variant="contained" color="primary">
            Save JSON
          </Button>
          <Button onClick={handleCloseJsonDialog}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Create/Edit Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingEntity ? 'Edit Entity Definition' : 'Create Entity Definition'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="Entity ID"
              value={formData.entityId}
              onChange={(e) => setFormData({ ...formData, entityId: e.target.value })}
              disabled={!!editingEntity}
              required
              fullWidth
              helperText="Unique technical identifier (e.g., Order, Invoice)"
            />
            <TextField
              label="Name"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
              fullWidth
            />
            <TextField
              label="Description"
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              multiline
              rows={3}
              fullWidth
            />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField
                label="Schema Version"
                type="number"
                value={formData.schemaVersion ?? 1}
                onChange={(e) =>
                  setFormData({ ...formData, schemaVersion: parseInt(e.target.value, 10) || 1 })
                }
                sx={{ flex: 1 }}
              />
              <TextField
                label="Screen Version"
                type="number"
                value={formData.screenVersion ?? 1}
                onChange={(e) =>
                  setFormData({ ...formData, screenVersion: parseInt(e.target.value, 10) || 1 })
                }
                sx={{ flex: 1 }}
              />
            </Box>
            <TextField
              label="Workflow ID"
              value={formData.workflowId || ''}
              onChange={(e) => setFormData({ ...formData, workflowId: e.target.value })}
              fullWidth
              helperText="Optional workflow identifier for this entity"
            />
            <TextField
              label="Authorization Model"
              value={formData.authorizationModel || ''}
              onChange={(e) => setFormData({ ...formData, authorizationModel: e.target.value })}
              fullWidth
              helperText="Optional authorization model name"
            />
            <TextField
              label="Scope"
              select
              value={formData.scope || 'LOCAL'}
              onChange={(e) =>
                setFormData({ ...formData, scope: e.target.value as CreateEntityRequest['scope'] })
              }
              fullWidth
            >
              <MenuItem value="LOCAL">Local</MenuItem>
              <MenuItem value="GLOBAL">Global</MenuItem>
              <MenuItem value="SHARED">Shared</MenuItem>
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button
            onClick={handleSave}
            variant="contained"
            disabled={createMutation.isPending || updateMutation.isPending}
          >
            {editingEntity ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

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
