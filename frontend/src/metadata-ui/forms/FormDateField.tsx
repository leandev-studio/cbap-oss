import { TextField } from '@mui/material';
import { PropertyDefinition } from '../../shared/services/entityMetadataService';

interface FormDateFieldProps {
  property: PropertyDefinition;
  value: string | null;
  onChange: (value: string | null) => void;
  onBlur?: () => void;
  error?: boolean;
  helperText?: string;
  [key: string]: any; // Allow additional TextField props
}

/**
 * Date Picker Form Field Component
 */
export function FormDateField({
  property,
  value,
  onChange,
  onBlur,
  error,
  helperText,
  ...textFieldProps
}: FormDateFieldProps) {
  // Convert date value to YYYY-MM-DD format for input
  const formatDateForInput = (dateValue: string | null | undefined): string => {
    if (!dateValue) return '';
    try {
      const date = new Date(dateValue);
      if (isNaN(date.getTime())) return '';
      const isoString = date.toISOString().split('T')[0];
      return isoString || '';
    } catch {
      return '';
    }
  };

  // Convert input value to ISO string
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const inputValue = e.target.value;
    if (!inputValue) {
      onChange(null);
    } else {
      try {
        const date = new Date(inputValue);
        if (!isNaN(date.getTime())) {
          onChange(date.toISOString());
        }
      } catch {
        // Invalid date, keep as is
      }
    }
  };

  return (
    <TextField
      fullWidth
      name={property.propertyName}
      label={property.label || property.propertyName}
      type="date"
      value={formatDateForInput(value)}
      onChange={handleChange}
      onBlur={onBlur}
      required={property.required}
      disabled={property.readOnly}
      error={error}
      helperText={helperText || property.description}
      InputLabelProps={{
        shrink: true,
      }}
      {...textFieldProps}
    />
  );
}
