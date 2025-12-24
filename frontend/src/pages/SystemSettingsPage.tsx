import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  CircularProgress,
  Alert,
  TextField,
  Button,
  Snackbar,
  Grid,
} from '@mui/material';
import { Save, Settings } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getSystemSettings, updateSystemSettings, SystemSettings } from '../shared/services/adminService';

/**
 * System Settings Page
 * 
 * Displays and manages system settings.
 * Route: /admin/system/settings
 */
export function SystemSettingsPage() {
  const queryClient = useQueryClient();
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });
  const [formData, setFormData] = useState<SystemSettings>({});

  // Fetch system settings
  const {
    data: settings,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['system-settings'],
    queryFn: getSystemSettings,
    staleTime: 5 * 60 * 1000,
  });

  // Update form data when settings are loaded
  useEffect(() => {
    if (settings) {
      setFormData(settings);
    }
  }, [settings]);

  // Update settings mutation
  const updateMutation = useMutation({
    mutationFn: updateSystemSettings,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['system-settings'] });
      setSnackbar({ open: true, message: 'System settings updated successfully', severity: 'success' });
    },
    onError: (error: Error) => {
      setSnackbar({ open: true, message: `Failed to update settings: ${error.message}`, severity: 'error' });
    },
  });

  const handleSave = () => {
    updateMutation.mutate(formData);
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
          Failed to load system settings: {error instanceof Error ? error.message : 'Unknown error'}
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
            <Settings sx={{ fontSize: 32 }} />
            <Typography variant="h4" component="h1" color="text.primary">
              System Settings
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary">
            Configure system-wide settings
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Save />}
          onClick={handleSave}
          disabled={updateMutation.isPending}
        >
          Save Settings
        </Button>
      </Box>

      {/* Settings Form */}
      <Paper elevation={0} sx={{ p: 3 }}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <TextField
              label="Application Name"
              value={formData.applicationName || ''}
              onChange={(e) => setFormData({ ...formData, applicationName: e.target.value })}
              fullWidth
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              label="Version"
              value={formData.version || ''}
              onChange={(e) => setFormData({ ...formData, version: e.target.value })}
              fullWidth
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              label="Max File Upload Size"
              value={formData.maxFileUploadSize || ''}
              onChange={(e) => setFormData({ ...formData, maxFileUploadSize: e.target.value })}
              fullWidth
              helperText="e.g., 10MB"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              label="Session Timeout (seconds)"
              type="number"
              value={formData.sessionTimeout || ''}
              onChange={(e) => setFormData({ ...formData, sessionTimeout: parseInt(e.target.value) || 0 })}
              fullWidth
            />
          </Grid>
        </Grid>
      </Paper>

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
