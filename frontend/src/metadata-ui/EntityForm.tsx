import { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Grid,
  Button,
  Typography,
  Alert,
  CircularProgress,
  Divider,
} from '@mui/material';
import { Save, Cancel } from '@mui/icons-material';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { EntityDefinition } from '../shared/services/entityMetadataService';
import {
  EntityRecord,
  createRecord,
  updateRecord,
  CreateRecordRequest,
  UpdateRecordRequest,
} from '../shared/services/entityRecordService';
import { FormField } from './forms/FormField';

interface EntityFormProps {
  entityDefinition: EntityDefinition;
  record?: EntityRecord; // If provided, form is in edit mode
  onCancel?: () => void;
}

/**
 * Entity Form Component
 * 
 * Metadata-driven form for creating and editing entity records.
 * Generates form fields automatically from property definitions.
 */
export function EntityForm({ entityDefinition, record, onCancel }: EntityFormProps) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isEditMode = !!record;

  // Form state - initialize with record data if editing
  const [formData, setFormData] = useState<Record<string, any>>(() => {
    if (record) {
      return { ...record.data };
    }
    return {};
  });

  // Validation errors
  const [errors, setErrors] = useState<Record<string, string>>({});

  // Update form data when record changes
  useEffect(() => {
    if (record) {
      setFormData({ ...record.data });
    } else {
      setFormData({});
    }
    setErrors({});
  }, [record]);

  // Validate form
  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    entityDefinition.properties.forEach((property) => {
      // Skip calculated and read-only fields
      if (property.propertyType === 'calculated' || property.readOnly) {
        return;
      }

      const value = formData[property.propertyName];

      // Required field validation
      if (property.required) {
        if (value === null || value === undefined || value === '') {
          newErrors[property.propertyName] = `${property.label || property.propertyName} is required`;
        }
      }

      // Type validation
      if (value !== null && value !== undefined && value !== '') {
        switch (property.propertyType) {
          case 'number':
            if (typeof value !== 'number' && isNaN(parseFloat(String(value)))) {
              newErrors[property.propertyName] = `${property.label || property.propertyName} must be a number`;
            }
            break;
          case 'boolean':
            if (typeof value !== 'boolean') {
              newErrors[property.propertyName] = `${property.label || property.propertyName} must be a boolean`;
            }
            break;
          case 'multiSelect':
            if (!Array.isArray(value)) {
              newErrors[property.propertyName] = `${property.label || property.propertyName} must be an array`;
            }
            break;
        }
      }

      // Master-detail validation (nested entity array)
      if (property.metadataJson?.isDetailEntityArray === true) {
        const minItems = property.metadataJson?.minItems as number | undefined;
        if (minItems !== undefined) {
          const arrayValue = Array.isArray(value) ? value : [];
          if (arrayValue.length < minItems) {
            newErrors[property.propertyName] = `${property.label || property.propertyName} must have at least ${minItems} item(s)`;
          }
        }
      }
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (request: CreateRecordRequest) => createRecord(entityDefinition.entityId, request),
    onSuccess: (data) => {
      // Invalidate queries
      queryClient.invalidateQueries({ queryKey: ['entity-records', entityDefinition.entityId] });
      queryClient.invalidateQueries({ queryKey: ['entity-record', entityDefinition.entityId, data.recordId] });
      
      // Navigate to detail page
      navigate(`/entities/${entityDefinition.entityId}/records/${data.recordId}`);
    },
  });

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: (request: UpdateRecordRequest) =>
      updateRecord(entityDefinition.entityId, record!.recordId, request),
    onSuccess: (data) => {
      // Invalidate queries
      queryClient.invalidateQueries({ queryKey: ['entity-records', entityDefinition.entityId] });
      queryClient.invalidateQueries({ queryKey: ['entity-record', entityDefinition.entityId, data.recordId] });
      
      // Navigate to detail page
      navigate(`/entities/${entityDefinition.entityId}/records/${data.recordId}`);
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) {
      return;
    }

    const request: CreateRecordRequest | UpdateRecordRequest = {
      data: formData,
    };

    if (isEditMode) {
      updateMutation.mutate(request);
    } else {
      createMutation.mutate(request);
    }
  };

  const handleFieldChange = (propertyName: string, value: any) => {
    setFormData((prev) => ({
      ...prev,
      [propertyName]: value,
    }));
    // Clear error for this field
    if (errors[propertyName]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[propertyName];
        return newErrors;
      });
    }
  };

  const handleCancel = () => {
    if (onCancel) {
      onCancel();
    } else if (isEditMode && record) {
      navigate(`/entities/${entityDefinition.entityId}/records/${record.recordId}`);
    } else {
      navigate(`/entities/${entityDefinition.entityId}`);
    }
  };

  const isLoading = createMutation.isPending || updateMutation.isPending;
  const error = createMutation.error || updateMutation.error;

  // Filter properties - exclude calculated fields from form (they're display-only)
  const formProperties = entityDefinition.properties.filter(
    (p) => p.propertyType !== 'calculated'
  );

  return (
    <Box component="form" onSubmit={handleSubmit}>
      {/* Error Alert */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error instanceof Error ? error.message : 'An error occurred while saving'}
        </Alert>
      )}

      {/* Form Fields */}
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
        <Grid container spacing={3}>
          {formProperties.length === 0 ? (
            <Grid item xs={12}>
              <Typography variant="body2" color="text.secondary">
                No editable properties defined for this entity
              </Typography>
            </Grid>
          ) : (
            <>
              {/* Header Fields */}
              {formProperties
                .filter((property) => {
                  // Skip read-only fields in create mode
                  if (!isEditMode && property.readOnly) {
                    return false;
                  }
                  // Skip master-detail fields (they'll be shown separately)
                  return property.metadataJson?.isDetailEntityArray !== true;
                })
                .map((property) => (
                  <Grid item xs={12} sm={6} md={4} key={property.propertyId}>
                    <FormField
                      property={property}
                      value={formData[property.propertyName]}
                      onChange={(value) => handleFieldChange(property.propertyName, value)}
                      error={!!errors[property.propertyName]}
                      helperText={errors[property.propertyName]}
                    />
                  </Grid>
                ))}
              
              {/* Master-Detail Fields (Line Items) - Full Width */}
              {formProperties
                .filter((property) => property.metadataJson?.isDetailEntityArray === true)
                .map((property) => (
                  <Grid item xs={12} key={property.propertyId}>
                    <FormField
                      property={property}
                      value={formData[property.propertyName]}
                      onChange={(value) => handleFieldChange(property.propertyName, value)}
                      error={!!errors[property.propertyName]}
                      helperText={errors[property.propertyName]}
                    />
                  </Grid>
                ))}
            </>
          )}
        </Grid>

        {/* Action Buttons */}
        <Divider sx={{ my: 3 }} />
        <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
          <Button
            variant="outlined"
            onClick={handleCancel}
            disabled={isLoading}
            startIcon={<Cancel />}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={isLoading}
            startIcon={isLoading ? <CircularProgress size={16} /> : <Save />}
          >
            {isLoading ? 'Saving...' : isEditMode ? 'Update' : 'Create'}
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}
