import { PropertyDefinition } from '../../shared/services/entityMetadataService';
import { FormTextField } from './FormTextField';
import { FormNumberField } from './FormNumberField';
import { FormDateField } from './FormDateField';
import { FormBooleanField } from './FormBooleanField';
import { FormSelectField } from './FormSelectField';
import { FormMultiSelectField } from './FormMultiSelectField';
import { FormReferenceField } from './FormReferenceField';
import { MasterDetailForm } from './MasterDetailForm';
import { Typography, Box } from '@mui/material';

interface FormFieldProps {
  property: PropertyDefinition;
  value: any;
  onChange: (value: any) => void;
  onBlur?: () => void;
  error?: boolean;
  helperText?: string;
}

/**
 * Form Field Component
 * 
 * Renders the appropriate form field component based on property type.
 */
export function FormField({ property, value, onChange, onBlur, error, helperText }: FormFieldProps) {
  // Calculated fields are read-only display
  // These may use measures in their calculationExpression
  if (property.propertyType === 'calculated') {
    return (
      <Box>
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
          {property.label || property.propertyName}
          <Typography component="span" variant="caption" color="text.secondary" sx={{ ml: 0.5, fontStyle: 'italic' }}>
            (calculated)
          </Typography>
        </Typography>
        <Typography variant="body2" sx={{ fontFamily: 'monospace', p: 1, bgcolor: 'action.hover', borderRadius: 1 }}>
          {value !== null && value !== undefined ? String(value) : '—'}
        </Typography>
        {property.calculationExpression && (
          <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block', fontStyle: 'italic' }}>
            {property.calculationExpression}
          </Typography>
        )}
        {property.description && (
          <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
            {property.description}
          </Typography>
        )}
      </Box>
    );
  }

  // Check if this is a master-detail field (nested entity array)
  if (property.metadataJson?.isDetailEntityArray === true) {
    return (
      <MasterDetailForm
        property={property}
        value={Array.isArray(value) ? value : null}
        onChange={onChange}
        onBlur={onBlur}
        error={error}
        helperText={helperText}
      />
    );
  }

  // Render appropriate field based on type
  switch (property.propertyType) {
    case 'string':
      return (
        <FormTextField
          property={property}
          value={value || ''}
          onChange={onChange}
          onBlur={onBlur}
          error={error}
          helperText={helperText}
        />
      );

    case 'number':
      return (
        <FormNumberField
          property={property}
          value={value ?? null}
          onChange={onChange}
          onBlur={onBlur}
          error={error}
          helperText={helperText}
        />
      );

    case 'date':
      return (
        <FormDateField
          property={property}
          value={value || null}
          onChange={onChange}
          onBlur={onBlur}
          error={error}
          helperText={helperText}
        />
      );

    case 'boolean':
      return (
        <FormBooleanField
          property={property}
          value={value ?? null}
          onChange={onChange}
          onBlur={onBlur}
          error={error}
          helperText={helperText}
        />
      );

    case 'singleSelect':
      return (
        <FormSelectField
          property={property}
          value={value ?? null}
          onChange={onChange}
          onBlur={onBlur}
          error={error}
          helperText={helperText}
        />
      );

    case 'multiSelect':
      return (
        <FormMultiSelectField
          property={property}
          value={value ?? null}
          onChange={onChange}
          onBlur={onBlur}
          error={error}
          helperText={helperText}
        />
      );

    case 'reference':
      return (
        <FormReferenceField
          property={property}
          value={value || null}
          onChange={onChange}
          onBlur={onBlur}
          error={error}
          helperText={helperText}
        />
      );

    default:
      // Fallback to text field
      return (
        <FormTextField
          property={property}
          value={value || ''}
          onChange={onChange}
          onBlur={onBlur}
          error={error}
          helperText={helperText}
        />
      );
  }
}
