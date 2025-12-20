import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  CircularProgress,
  Alert,
  Grid,
  Divider,
  IconButton,
  Breadcrumbs,
  Link,
  Button,
} from '@mui/material';
import { ArrowBack, Edit } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { getEntityById } from '../shared/services/entityMetadataService';
import { getRecord } from '../shared/services/entityRecordService';
import { EntityDetailField } from './EntityDetailField';
import { EntityForm } from './EntityForm';
import { WorkflowActionBar } from './WorkflowActionBar';

/**
 * Entity Detail Page Component
 * 
 * Displays a single entity record in read-only mode.
 * Route: /entities/:entityId/records/:recordId
 */
export function EntityDetailPage() {
  const { entityId, recordId } = useParams<{ entityId: string; recordId: string }>();
  const navigate = useNavigate();
  const [isEditMode, setIsEditMode] = useState(false);

  if (!entityId || !recordId) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Entity ID and Record ID are required</Alert>
      </Box>
    );
  }

  // Fetch entity definition
  const {
    data: entityDefinition,
    isLoading: isLoadingEntity,
    error: entityError,
  } = useQuery({
    queryKey: ['entity-definition', entityId],
    queryFn: () => getEntityById(entityId),
    enabled: !!entityId,
  });

  // Fetch record
  const {
    data: record,
    isLoading: isLoadingRecord,
    error: recordError,
  } = useQuery({
    queryKey: ['entity-record', entityId, recordId],
    queryFn: () => getRecord(entityId, recordId),
    enabled: !!entityId && !!recordId,
  });

  const isLoading = isLoadingEntity || isLoadingRecord;
  const error = entityError || recordError;

  const handleBack = () => {
    navigate(`/entities/${entityId}`);
  };

  const handleEdit = () => {
    setIsEditMode(true);
  };

  const handleCancelEdit = () => {
    setIsEditMode(false);
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
          Failed to load data: {error instanceof Error ? error.message : 'Unknown error'}
        </Alert>
      </Box>
    );
  }

  if (!entityDefinition || !record) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="warning">Entity or record not found</Alert>
      </Box>
    );
  }

  // Separate properties into header fields, line items, and other fields
  const lineItemsProperty = entityDefinition.properties.find(
    (p) => p.metadataJson?.isDetailEntityArray === true
  );
  const headerProperties = entityDefinition.properties.filter(
    (p) => p.propertyId !== lineItemsProperty?.propertyId && p.propertyType !== 'calculated'
  );
  const calculatedProperties = entityDefinition.properties.filter(
    (p) => p.propertyType === 'calculated'
  );

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
          Record Details
        </Typography>
      </Breadcrumbs>

      {/* Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={handleBack} sx={{ mr: 1 }}>
          <ArrowBack />
        </IconButton>
        <Box sx={{ flexGrow: 1 }}>
          <Typography variant="h4" component="h1" gutterBottom color="text.primary">
            {entityDefinition.name}
          </Typography>
        </Box>
        {!isEditMode && (
          <Button
            variant="outlined"
            startIcon={<Edit />}
            onClick={handleEdit}
            sx={{ ml: 2 }}
          >
            Edit
          </Button>
        )}
      </Box>

      {/* Record Data - Show form in edit mode, detail view otherwise */}
      {isEditMode ? (
        <EntityForm
          entityDefinition={entityDefinition}
          record={record}
          onCancel={handleCancelEdit}
        />
      ) : (
        <>
          {/* Workflow Action Bar */}
          <WorkflowActionBar
            entityId={entityId}
            recordId={recordId}
            currentState={record.state}
          />

          <Paper
            elevation={0}
            sx={{
              p: 3,
              backgroundColor: 'background.paper',
              borderRadius: 2,
              border: 1,
              borderColor: 'divider',
            }}
          >
          {/* Header Fields */}
          <Grid container spacing={3}>
            {headerProperties.length === 0 ? (
              <Grid item xs={12}>
                <Typography variant="body2" color="text.secondary">
                  No properties defined for this entity
                </Typography>
              </Grid>
            ) : (
              headerProperties.map((property) => {
                const value = record.data[property.propertyName];

                return (
                  <Grid item xs={12} sm={6} md={4} key={property.propertyId}>
                    <EntityDetailField
                      property={property}
                      value={value}
                      record={record}
                      entityDefinition={entityDefinition}
                    />
                  </Grid>
                );
              })
            )}
          </Grid>

          {/* Line Items Section (if exists) */}
          {lineItemsProperty && (
            <>
              <Divider sx={{ my: 3 }} />
              <Box>
                <EntityDetailField
                  property={lineItemsProperty}
                  value={record.data[lineItemsProperty.propertyName]}
                  record={record}
                  entityDefinition={entityDefinition}
                />
              </Box>
            </>
          )}

          {/* Calculated Fields (if any) */}
          {calculatedProperties.length > 0 && (
            <>
              <Divider sx={{ my: 3 }} />
              <Box>
                <Typography variant="h6" gutterBottom color="text.secondary">
                  Calculated Fields
                </Typography>
                <Grid container spacing={3}>
                  {calculatedProperties.map((property) => {
                    const value = record.data[property.propertyName];
                    return (
                      <Grid item xs={12} sm={6} md={4} key={property.propertyId}>
                        <EntityDetailField
                          property={property}
                          value={value}
                          record={record}
                          entityDefinition={entityDefinition}
                        />
                      </Grid>
                    );
                  })}
                </Grid>
              </Box>
            </>
          )}

          {/* Metadata Section */}
          <Divider sx={{ my: 3 }} />
          <Box>
            <Typography variant="h6" gutterBottom color="text.secondary">
              Metadata
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Typography variant="caption" color="text.secondary">
                  Schema Version
                </Typography>
                <Typography variant="body2">{record.schemaVersion}</Typography>
              </Grid>
              {record.state && (
                <Grid item xs={12} sm={6}>
                  <Typography variant="caption" color="text.secondary">
                    State
                  </Typography>
                  <Typography variant="body2">{record.state}</Typography>
                </Grid>
              )}
              <Grid item xs={12} sm={6}>
                <Typography variant="caption" color="text.secondary">
                  Created At
                </Typography>
                <Typography variant="body2">
                  {new Date(record.createdAt).toLocaleString()}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="caption" color="text.secondary">
                  Updated At
                </Typography>
                <Typography variant="body2">
                  {new Date(record.updatedAt).toLocaleString()}
                </Typography>
              </Grid>
            </Grid>
          </Box>
          </Paper>
        </>
      )}
    </Box>
  );
}
