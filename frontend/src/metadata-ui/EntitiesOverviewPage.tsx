import { Box, Typography, Paper, CircularProgress, Alert, Grid, Card, CardContent, CardActionArea } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getAllEntities } from '../shared/services/entityMetadataService';
import { TableChart } from '@mui/icons-material';

/**
 * Entities Overview Page
 * 
 * Displays all available entity types that users can browse.
 * Route: /entities
 */
export function EntitiesOverviewPage() {
  const navigate = useNavigate();

  const { data: entities, isLoading, error } = useQuery({
    queryKey: ['entities'],
    queryFn: getAllEntities,
    staleTime: 5 * 60 * 1000, // Cache for 5 minutes
  });

  const handleEntityClick = (entityId: string) => {
    navigate(`/entities/${entityId}`);
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
          Failed to load entities: {error instanceof Error ? error.message : 'Unknown error'}
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ width: '100%', height: '100%' }}>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom color="text.primary">
          Entities
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Browse and manage entity types
        </Typography>
      </Box>

      {!entities || entities.length === 0 ? (
        <Paper
          elevation={0}
          sx={{
            p: 4,
            textAlign: 'center',
            backgroundColor: 'background.paper',
            borderRadius: 2,
            border: 1,
            borderColor: 'divider',
          }}
        >
          <Typography variant="h6" color="text.secondary" gutterBottom>
            No entities found
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Entity definitions will appear here once they are created.
          </Typography>
        </Paper>
      ) : (
        <Grid container spacing={3}>
          {entities.map((entity) => (
            <Grid item xs={12} sm={6} md={4} key={entity.entityId}>
              <Card
                elevation={0}
                sx={{
                  height: '100%',
                  border: 1,
                  borderColor: 'divider',
                  '&:hover': {
                    borderColor: 'primary.main',
                    boxShadow: 2,
                  },
                  transition: 'all 0.2s ease',
                }}
              >
                <CardActionArea onClick={() => handleEntityClick(entity.entityId)} sx={{ height: '100%' }}>
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                      <TableChart sx={{ mr: 1, color: 'text.secondary' }} />
                      <Typography variant="h6" component="h3" sx={{ flexGrow: 1 }}>
                        {entity.name}
                      </Typography>
                    </Box>
                    {entity.description && (
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        {entity.description}
                      </Typography>
                    )}
                    <Typography variant="caption" color="text.secondary">
                      {entity.properties?.length || 0} properties
                    </Typography>
                  </CardContent>
                </CardActionArea>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
}
