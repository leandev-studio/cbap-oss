import { TextField, MenuItem } from '@mui/material';
import { PropertyDefinition } from '../../shared/services/entityMetadataService';

interface FormSelectFieldProps {
  property: PropertyDefinition;
  value: string | number | null;
  onChange: (value: string | number | null) => void;
  onBlur?: () => void;
  error?: boolean;
  helperText?: string;
  [key: string]: any; // Allow additional TextField props
}

/**
 * Single Select Dropdown Form Field Component
 */
export function FormSelectField({
  property,
  value,
  onChange,
  onBlur,
  error,
  helperText,
  ...textFieldProps
}: FormSelectFieldProps) {
  // Get options from metadata (e.g., metadataJson.options or metadataJson.enum)
  const options = property.metadataJson?.options || property.metadataJson?.enum || [];

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    if (newValue === '') {
      onChange(null);
    } else {
      // Try to preserve type (number vs string)
      const numValue = parseFloat(newValue);
      onChange(isNaN(numValue) ? newValue : numValue);
    }
  };

  return (
    <TextField
      fullWidth
      name={property.propertyName}
      label={property.label || property.propertyName}
      select
      value={value === null ? '' : value}
      onChange={handleChange}
      onBlur={onBlur}
      required={property.required}
      disabled={property.readOnly}
      error={error}
      helperText={helperText || property.description}
      {...textFieldProps}
    >
      {!property.required && (
        <MenuItem value="">
          <em>None</em>
        </MenuItem>
      )}
      {options.map((option: any) => {
        const optionValue = typeof option === 'object' ? option.value : option;
        const optionLabel = typeof option === 'object' ? option.label : option;
        return (
          <MenuItem key={optionValue} value={optionValue}>
            {optionLabel}
          </MenuItem>
        );
      })}
    </TextField>
  );
}
