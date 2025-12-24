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
  MenuItem,
  Chip,
  Snackbar,
} from '@mui/material';
import { Add, Edit, Delete, People } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getAllUsers,
  createUser,
  updateUser,
  deleteUser,
  User,
  CreateUserRequest,
  UpdateUserRequest,
} from '../shared/services/adminService';

/**
 * User Management Page
 * 
 * Displays all users with CRUD operations.
 * Route: /admin/users
 */
export function UserManagementPage() {
  const queryClient = useQueryClient();
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [formData, setFormData] = useState<CreateUserRequest & { status?: string }>({
    username: '',
    password: '',
    email: '',
    roles: [],
  });

  // Fetch all users
  const {
    data: users,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['users'],
    queryFn: getAllUsers,
    staleTime: 5 * 60 * 1000,
  });

  // Create user mutation
  const createMutation = useMutation({
    mutationFn: createUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setSnackbar({ open: true, message: 'User created successfully', severity: 'success' });
      handleCloseDialog();
    },
    onError: (error: Error) => {
      setSnackbar({ open: true, message: `Failed to create user: ${error.message}`, severity: 'error' });
    },
  });

  // Update user mutation
  const updateMutation = useMutation({
    mutationFn: ({ userId, request }: { userId: string; request: UpdateUserRequest }) =>
      updateUser(userId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setSnackbar({ open: true, message: 'User updated successfully', severity: 'success' });
      handleCloseDialog();
    },
    onError: (error: Error) => {
      setSnackbar({ open: true, message: `Failed to update user: ${error.message}`, severity: 'error' });
    },
  });

  // Delete user mutation
  const deleteMutation = useMutation({
    mutationFn: deleteUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setSnackbar({ open: true, message: 'User deleted successfully', severity: 'success' });
    },
    onError: (error: Error) => {
      setSnackbar({ open: true, message: `Failed to delete user: ${error.message}`, severity: 'error' });
    },
  });

  const handleOpenCreateDialog = () => {
    setEditingUser(null);
    setFormData({ username: '', password: '', email: '', roles: [] });
    setDialogOpen(true);
  };

  const handleOpenEditDialog = (user: User) => {
    setEditingUser(user);
    setFormData({
      username: user.username,
      password: '', // Not needed for edit, but required by type
      email: user.email || '',
      status: user.status,
      roles: user.roles,
    });
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditingUser(null);
    setFormData({ username: '', password: '', email: '', roles: [] });
  };

  const handleSave = () => {
    if (editingUser) {
      updateMutation.mutate({
        userId: editingUser.userId,
        request: {
          email: formData.email,
          status: formData.status,
          roles: formData.roles,
        },
      });
    } else {
      if (!formData.username || !formData.password) {
        setSnackbar({ open: true, message: 'Username and password are required', severity: 'error' });
        return;
      }
      createMutation.mutate({
        username: formData.username,
        password: formData.password,
        email: formData.email || undefined,
        roles: formData.roles,
      });
    }
  };

  const handleDelete = (userId: string, username: string) => {
    if (window.confirm(`Are you sure you want to delete user "${username}"?`)) {
      deleteMutation.mutate(userId);
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
          Failed to load users: {error instanceof Error ? error.message : 'Unknown error'}
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
            <People sx={{ fontSize: 32 }} />
            <Typography variant="h4" component="h1" color="text.primary">
              User Management
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary">
            Manage system users and their roles
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={handleOpenCreateDialog}
        >
          Create User
        </Button>
      </Box>

      {/* Users Table */}
      <TableContainer component={Paper} elevation={0}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Username</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Roles</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users && users.length > 0 ? (
              users.map((user) => (
                <TableRow key={user.userId} hover>
                  <TableCell>
                    <Typography variant="body1" fontWeight="medium">
                      {user.username}
                    </Typography>
                  </TableCell>
                  <TableCell>{user.email || '-'}</TableCell>
                  <TableCell>
                    <Chip
                      label={user.status}
                      size="small"
                      color={user.status === 'ACTIVE' ? 'success' : 'default'}
                    />
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                      {user.roles.map((role) => (
                        <Chip key={role} label={role} size="small" variant="outlined" />
                      ))}
                    </Box>
                  </TableCell>
                  <TableCell align="right">
                    <Tooltip title="Edit user">
                      <IconButton
                        size="small"
                        onClick={() => handleOpenEditDialog(user)}
                        color="primary"
                      >
                        <Edit />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete user">
                      <IconButton
                        size="small"
                        onClick={() => handleDelete(user.userId, user.username)}
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
                <TableCell colSpan={5} align="center">
                  <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                    No users found
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create/Edit Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingUser ? 'Edit User' : 'Create User'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="Username"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              disabled={!!editingUser}
              required
              fullWidth
            />
            {!editingUser && (
              <TextField
                label="Password"
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                required
                fullWidth
              />
            )}
            <TextField
              label="Email"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              fullWidth
            />
            {editingUser && (
              <TextField
                label="Status"
                select
                value={formData.status || 'ACTIVE'}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                fullWidth
              >
                <MenuItem value="ACTIVE">Active</MenuItem>
                <MenuItem value="INACTIVE">Inactive</MenuItem>
                <MenuItem value="LOCKED">Locked</MenuItem>
              </TextField>
            )}
            <TextField
              label="Roles (comma-separated)"
              value={Array.isArray(formData.roles) ? formData.roles.join(', ') : ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  roles: e.target.value.split(',').map((r) => r.trim()).filter(Boolean),
                })
              }
              placeholder="Admin, User"
              fullWidth
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
            {editingUser ? 'Update' : 'Create'}
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
