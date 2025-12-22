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
import { Save, Cancel, ErrorOutline } from '@mui/icons-material';
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
import { validateRecord, ValidationError } from '../shared/services/validationService';
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
  const [entityErrors, setEntityErrors] = useState<string[]>([]);
  const [serverValidationErrors, setServerValidationErrors] = useState<ValidationError[]>([]);

  // Update form data when record changes
  useEffect(() => {
    if (record) {
      setFormData({ ...record.data });
    } else {
      setFormData({});
    }
    setErrors({});
    setEntityErrors([]);
    setServerValidationErrors([]);
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
    onError: (error: any) => {
      // Parse server validation errors from response
      parseServerValidationErrors(error);
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
    onError: (error: any) => {
      // Parse server validation errors from response
      parseServerValidationErrors(error);
    },
  });

  /**
   * Parse server validation errors from API error response.
   */
  const parseServerValidationErrors = (error: any) => {
    const fieldErrors: Record<string, string> = {};
    const entityLevelErrors: string[] = [];

    // Check if error response contains validation errors
    if (error?.response?.data?.message) {
      const errorMessage = error.response.data.message;
      
      // Try to parse validation errors from message
      // Format: "Validation failed: propertyName: message; propertyName2: message2; ..."
      if (errorMessage.includes('Validation failed:')) {
        const validationPart = errorMessage.split('Validation failed:')[1];
        if (validationPart) {
          const errorParts = validationPart.split(';').filter((part: string) => part.trim());
          
          errorParts.forEach((part: string) => {
            const trimmed = part.trim();
            if (trimmed.includes(':')) {
              const [propertyName, ...messageParts] = trimmed.split(':');
              const message = messageParts.join(':').trim();
              if (propertyName && message) {
                fieldErrors[propertyName.trim()] = message;
              } else {
                entityLevelErrors.push(trimmed);
              }
            } else {
              entityLevelErrors.push(trimmed);
            }
          });
        }
      } else {
        // General error message
        entityLevelErrors.push(errorMessage);
      }
    } else if (error?.message) {
      entityLevelErrors.push(error.message);
    } else {
      entityLevelErrors.push('An error occurred while saving');
    }

    setErrors(fieldErrors);
    setEntityErrors(entityLevelErrors);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Clear previous errors
    setErrors({});
    setEntityErrors([]);
    setServerValidationErrors([]);

    // Client-side validation
    if (!validate()) {
      return;
    }

    // Pre-submit server validation
    try {
      const previousData = record ? { ...record.data } : undefined;
      const validationResponse = await validateRecord(entityDefinition.entityId, {
        data: formData,
        previousData,
        triggerEvent: isEditMode ? 'UPDATE' : 'CREATE',
      });

      if (!validationResponse.valid && validationResponse.errors.length > 0) {
        // Process validation errors
        const fieldErrors: Record<string, string> = {};
        const entityLevelErrors: string[] = [];

        validationResponse.errors.forEach((error) => {
          if (error.level === 'FIELD' && error.propertyName) {
            fieldErrors[error.propertyName] = error.message;
          } else if (error.level === 'ENTITY' || error.level === 'CROSS_ENTITY') {
            entityLevelErrors.push(error.message);
          }
        });

        setErrors(fieldErrors);
        setEntityErrors(entityLevelErrors);
        setServerValidationErrors(validationResponse.errors);
        return; // Don't submit if validation fails
      }
    } catch (validationError) {
      // If validation API fails, still try to submit (server will validate)
      console.warn('Pre-submit validation failed:', validationError);
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
    // Clear error for this field (both client and server errors)
    if (errors[propertyName]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[propertyName];
        return newErrors;
      });
    }
    // Clear server validation errors for this field
    setServerValidationErrors((prev) => 
      prev.filter(e => !(e.propertyName === propertyName && e.level === 'FIELD'))
    );
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
      {/* Server Error Alert */}
      {error && entityErrors.length === 0 && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error instanceof Error ? error.message : 'An error occurred while saving'}
        </Alert>
      )}

      {/* Validation Error Summary */}
      {(entityErrors.length > 0 || serverValidationErrors.length > 0) && (
        <Alert 
          severity="error" 
          icon={<ErrorOutline />}
          sx={{ mb: 2 }}
        >
          <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
            Validation Errors ({entityErrors.length + serverValidationErrors.filter(e => e.level !== 'FIELD').length} error{entityErrors.length + serverValidationErrors.filter(e => e.level !== 'FIELD').length !== 1 ? 's' : ''})
          </Typography>
          {entityErrors.length > 0 && (
            <Box component="ul" sx={{ m: 0, pl: 2 }}>
              {entityErrors.map((err, index) => (
                <li key={index}>
                  <Typography variant="body2">{err}</Typography>
                </li>
              ))}
            </Box>
          )}
          {serverValidationErrors.filter(e => e.level !== 'FIELD').length > 0 && (
            <Box component="ul" sx={{ m: entityErrors.length > 0 ? 1 : 0, pl: 2 }}>
              {serverValidationErrors
                .filter(e => e.level !== 'FIELD')
                .map((err) => (
                  <li key={err.validationId}>
                    <Typography variant="body2">{err.message}</Typography>
                  </li>
                ))}
            </Box>
          )}
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
                      helperText={errors[property.propertyName] || 
                        serverValidationErrors
                          .find(e => e.propertyName === property.propertyName && e.level === 'FIELD')
                          ?.message}
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
                      helperText={errors[property.propertyName] || 
                        serverValidationErrors
                          .find(e => e.propertyName === property.propertyName && e.level === 'FIELD')
                          ?.message}
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
