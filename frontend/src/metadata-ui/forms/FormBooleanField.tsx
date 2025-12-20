import { FormControlLabel, Switch, FormHelperText, FormControl, Typography } from '@mui/material';
import { PropertyDefinition } from '../../shared/services/entityMetadataService';

interface FormBooleanFieldProps {
  property: PropertyDefinition;
  value: boolean | null;
  onChange: (value: boolean) => void;
  error?: boolean;
  helperText?: string;
}

/**
 * Boolean (Switch) Form Field Component
 */
export function FormBooleanField({
  property,
  value,
  onChange,
  error,
  helperText,
}: FormBooleanFieldProps) {
  return (
    <FormControl fullWidth error={error} disabled={property.readOnly}>
      <FormControlLabel
        control={
          <Switch
            checked={value === true}
            onChange={(e) => onChange(e.target.checked)}
            disabled={property.readOnly}
          />
        }
        label={
          <Typography variant="body2">
            {property.label || property.propertyName}
            {property.required && (
              <Typography component="span" variant="body2" color="error" sx={{ ml: 0.5 }}>
                *
              </Typography>
            )}
          </Typography>
        }
      />
      {(helperText || property.description) && (
        <FormHelperText>{helperText || property.description}</FormHelperText>
      )}
    </FormControl>
  );
}
