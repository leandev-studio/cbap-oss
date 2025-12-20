import { TextField } from '@mui/material';
import { PropertyDefinition } from '../../shared/services/entityMetadataService';

interface FormTextFieldProps {
  property: PropertyDefinition;
  value: string;
  onChange: (value: string) => void;
  error?: boolean;
  helperText?: string;
  [key: string]: any; // Allow additional TextField props
}

/**
 * Text Input Form Field Component
 */
export function FormTextField({
  property,
  value,
  onChange,
  error,
  helperText,
  ...textFieldProps
}: FormTextFieldProps) {
  return (
    <TextField
      fullWidth
      name={property.propertyName}
      label={property.label || property.propertyName}
      value={value || ''}
      onChange={(e) => onChange(e.target.value)}
      required={property.required}
      disabled={property.readOnly}
      error={error}
      helperText={helperText || property.description}
      multiline={property.metadataJson?.multiline === true}
      rows={property.metadataJson?.rows || 1}
      {...textFieldProps}
    />
  );
}
