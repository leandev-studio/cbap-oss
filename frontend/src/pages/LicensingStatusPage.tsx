import { useState } from 'react';
import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Grid,
  Card,
  CardContent,
  Chip,
  Snackbar,
} from '@mui/material';
import { VerifiedUser } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { getLicensingStatus } from '../shared/services/adminService';

/**
 * Licensing Status Page
 * 
 * Displays licensing information.
 * Route: /admin/system/licensing
 */
export function LicensingStatusPage() {
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  // Fetch licensing status
  const {
    data: licensing,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['licensing-status'],
    queryFn: getLicensingStatus,
    staleTime: 5 * 60 * 1000,
  });

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
          Failed to load licensing status: {error instanceof Error ? error.message : 'Unknown error'}
        </Alert>
      </Box>
    );
  }

  if (!licensing) {
    return null;
  }

  return (
    <Box sx={{ width: '100%', height: '100%' }}>
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
          <VerifiedUser sx={{ fontSize: 32 }} />
          <Typography variant="h4" component="h1" color="text.primary">
            Licensing Status
          </Typography>
        </Box>
        <Typography variant="body2" color="text.secondary">
          View current licensing information
        </Typography>
      </Box>

      {/* Licensing Info Cards */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card elevation={0}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                License Type
              </Typography>
              <Chip
                label={licensing.licenseType}
                color={licensing.licenseType === 'OSS' ? 'success' : 'primary'}
                sx={{ mt: 1 }}
              />
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card elevation={0}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Status
              </Typography>
              <Chip
                label={licensing.status}
                color={licensing.status === 'ACTIVE' ? 'success' : 'error'}
                sx={{ mt: 1 }}
              />
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card elevation={0}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Expiration Date
              </Typography>
              <Typography variant="body1" sx={{ mt: 1 }}>
                {licensing.expirationDate || 'No expiration (OSS)'}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card elevation={0}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                User Limits
              </Typography>
              <Typography variant="body1" sx={{ mt: 1 }}>
                {licensing.maxUsers
                  ? `${licensing.currentUsers} / ${licensing.maxUsers} users`
                  : `Unlimited (${licensing.currentUsers} current)`}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12}>
          <Card elevation={0}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Features
              </Typography>
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 1 }}>
                {licensing.features.map((feature) => (
                  <Chip key={feature} label={feature} size="small" variant="outlined" />
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

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
