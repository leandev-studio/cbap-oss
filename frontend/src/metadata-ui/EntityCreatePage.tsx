import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Breadcrumbs,
  Link,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { getEntityById } from '../shared/services/entityMetadataService';
import { EntityForm } from './EntityForm';

/**
 * Entity Create Page Component
 * 
 * Displays a form for creating a new entity record.
 * Route: /entities/:entityId/create
 */
export function EntityCreatePage() {
  const { entityId } = useParams<{ entityId: string }>();
  const navigate = useNavigate();

  if (!entityId) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Entity ID is required</Alert>
      </Box>
    );
  }

  // Fetch entity definition
  const {
    data: entityDefinition,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['entity-definition', entityId],
    queryFn: () => getEntityById(entityId),
    enabled: !!entityId,
  });

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
          Failed to load entity definition: {error instanceof Error ? error.message : 'Unknown error'}
        </Alert>
      </Box>
    );
  }

  if (!entityDefinition) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="warning">Entity not found</Alert>
      </Box>
    );
  }

  const handleCancel = () => {
    navigate(`/entities/${entityId}`);
  };

  return (
    <Box sx={{ width: '100%', height: '100%' }}>
      {/* Breadcrumbs */}
      <Breadcrumbs sx={{ mb: 2 }}>
        <Link
          component="button"
          variant="body2"
          onClick={() => navigate('/entities')}
          sx={{ cursor: 'pointer', textDecoration: 'none' }}
        >
          Entities
        </Link>
        <Link
          component="button"
          variant="body2"
          onClick={() => navigate(`/entities/${entityId}`)}
          sx={{ cursor: 'pointer', textDecoration: 'none' }}
        >
          {entityDefinition.name}
        </Link>
        <Typography variant="body2" color="text.primary">
          Create New
        </Typography>
      </Breadcrumbs>

      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom color="text.primary">
          Create {entityDefinition.name}
        </Typography>
        {entityDefinition.description && (
          <Typography variant="body2" color="text.secondary">
            {entityDefinition.description}
          </Typography>
        )}
      </Box>

      {/* Form */}
      <EntityForm entityDefinition={entityDefinition} onCancel={handleCancel} />
    </Box>
  );
}
