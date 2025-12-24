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
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Chip,
  Snackbar,
} from '@mui/material';
import { Add, Edit, Delete, Security } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getAllRoles,
  getAllPermissions,
  createRole,
  updateRole,
  deleteRole,
  Role,
  CreateRoleRequest,
  UpdateRoleRequest,
} from '../shared/services/adminService';

/**
 * Role Management Page
 * 
 * Displays all roles with CRUD operations.
 * Route: /admin/roles
 */
export function RoleManagementPage() {
  const queryClient = useQueryClient();
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingRole, setEditingRole] = useState<Role | null>(null);
  const [formData, setFormData] = useState<CreateRoleRequest>({
    roleName: '',
    description: '',
    permissions: [],
  });

  // Fetch all roles
  const {
    data: roles,
    isLoading: isLoadingRoles,
    error: rolesError,
  } = useQuery({
    queryKey: ['roles'],
    queryFn: getAllRoles,
    staleTime: 5 * 60 * 1000,
  });

  // Fetch all permissions
  const {
    data: permissions,
    isLoading: isLoadingPermissions,
  } = useQuery({
    queryKey: ['permissions'],
    queryFn: getAllPermissions,
    staleTime: 5 * 60 * 1000,
  });

  // Create role mutation
  const createMutation = useMutation({
    mutationFn: createRole,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      setSnackbar({ open: true, message: 'Role created successfully', severity: 'success' });
      handleCloseDialog();
    },
    onError: (error: Error) => {
      setSnackbar({ open: true, message: `Failed to create role: ${error.message}`, severity: 'error' });
    },
  });

  // Update role mutation
  const updateMutation = useMutation({
    mutationFn: ({ roleId, request }: { roleId: string; request: UpdateRoleRequest }) =>
      updateRole(roleId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      setSnackbar({ open: true, message: 'Role updated successfully', severity: 'success' });
      handleCloseDialog();
    },
    onError: (error: Error) => {
      setSnackbar({ open: true, message: `Failed to update role: ${error.message}`, severity: 'error' });
    },
  });

  // Delete role mutation
  const deleteMutation = useMutation({
    mutationFn: deleteRole,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      setSnackbar({ open: true, message: 'Role deleted successfully', severity: 'success' });
    },
    onError: (error: Error) => {
      setSnackbar({ open: true, message: `Failed to delete role: ${error.message}`, severity: 'error' });
    },
  });

  const handleOpenCreateDialog = () => {
    setEditingRole(null);
    setFormData({ roleName: '', description: '', permissions: [] });
    setDialogOpen(true);
  };

  const handleOpenEditDialog = (role: Role) => {
    setEditingRole(role);
    setFormData({
      roleName: role.roleName,
      description: role.description || '',
      permissions: role.permissions.map((p) => p.permissionName),
    });
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditingRole(null);
    setFormData({ roleName: '', description: '', permissions: [] });
  };

  const handleSave = () => {
    if (editingRole) {
      updateMutation.mutate({
        roleId: editingRole.roleId,
        request: {
          description: formData.description,
          permissions: formData.permissions,
        },
      });
    } else {
      if (!formData.roleName) {
        setSnackbar({ open: true, message: 'Role name is required', severity: 'error' });
        return;
      }
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (roleId: string, roleName: string) => {
    if (window.confirm(`Are you sure you want to delete role "${roleName}"?`)) {
      deleteMutation.mutate(roleId);
    }
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  if (isLoadingRoles) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (rolesError) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">
          Failed to load roles: {rolesError instanceof Error ? rolesError.message : 'Unknown error'}
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
            <Security sx={{ fontSize: 32 }} />
            <Typography variant="h4" component="h1" color="text.primary">
              Role Management
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary">
            Manage roles and their permissions
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={handleOpenCreateDialog}
        >
          Create Role
        </Button>
      </Box>

      {/* Roles Table */}
      <TableContainer component={Paper} elevation={0}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Role Name</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Permissions</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {roles && roles.length > 0 ? (
              roles.map((role) => (
                <TableRow key={role.roleId} hover>
                  <TableCell>
                    <Typography variant="body1" fontWeight="medium">
                      {role.roleName}
                    </Typography>
                  </TableCell>
                  <TableCell>{role.description || '-'}</TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                      {role.permissions.map((permission) => (
                        <Chip key={permission.permissionId} label={permission.permissionName} size="small" variant="outlined" />
                      ))}
                    </Box>
                  </TableCell>
                  <TableCell align="right">
                    <Tooltip title="Edit role">
                      <IconButton
                        size="small"
                        onClick={() => handleOpenEditDialog(role)}
                        color="primary"
                      >
                        <Edit />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete role">
                      <IconButton
                        size="small"
                        onClick={() => handleDelete(role.roleId, role.roleName)}
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
                <TableCell colSpan={4} align="center">
                  <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                    No roles found
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create/Edit Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingRole ? 'Edit Role' : 'Create Role'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="Role Name"
              value={formData.roleName}
              onChange={(e) => setFormData({ ...formData, roleName: e.target.value })}
              disabled={!!editingRole}
              required
              fullWidth
            />
            <TextField
              label="Description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              multiline
              rows={3}
              fullWidth
            />
            <TextField
              label="Permissions (comma-separated)"
              value={Array.isArray(formData.permissions) ? formData.permissions.join(', ') : ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  permissions: e.target.value.split(',').map((p) => p.trim()).filter(Boolean),
                })
              }
              placeholder="SYSTEM_USER_MANAGEMENT, SYSTEM_ROLE_MANAGEMENT"
              fullWidth
              helperText={permissions && !isLoadingPermissions ? `Available: ${permissions.map((p) => p.permissionName).join(', ')}` : ''}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button
            onClick={handleSave}
            variant="contained"
            disabled={createMutation.isPending || updateMutation.isPending}
          >
            {editingRole ? 'Update' : 'Create'}
          </Button>
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
