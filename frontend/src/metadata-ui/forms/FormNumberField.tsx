import { TextField } from '@mui/material';
import { PropertyDefinition } from '../../shared/services/entityMetadataService';

interface FormNumberFieldProps {
  property: PropertyDefinition;
  value: number | null;
  onChange: (value: number | null) => void;
  error?: boolean;
  helperText?: string;
  [key: string]: any; // Allow additional TextField props
}

/**
 * Number Input Form Field Component
 */
export function FormNumberField({
  property,
  value,
  onChange,
  onBlur,
  error,
  helperText,
  ...textFieldProps
}: FormNumberFieldProps) {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const inputValue = e.target.value;
    if (inputValue === '' || inputValue === null || inputValue === undefined) {
      onChange(null);
    } else {
      const numValue = parseFloat(inputValue);
      if (!isNaN(numValue)) {
        onChange(numValue);
      }
    }
  };

  return (
    <TextField
      fullWidth
      name={property.propertyName}
      label={property.label || property.propertyName}
      type="number"
      value={value === null ? '' : value}
      onChange={handleChange}
      onBlur={onBlur}
      required={property.required}
      disabled={property.readOnly}
      error={error}
      helperText={helperText || property.description}
      inputProps={{
        min: property.metadataJson?.min,
        max: property.metadataJson?.max,
        step: property.metadataJson?.step || 'any',
      }}
      {...textFieldProps}
    />
  );
}
