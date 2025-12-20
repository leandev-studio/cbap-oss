import {
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  OutlinedInput,
  FormHelperText,
  SelectChangeEvent,
} from '@mui/material';
import { PropertyDefinition } from '../../shared/services/entityMetadataService';

interface FormMultiSelectFieldProps {
  property: PropertyDefinition;
  value: (string | number)[] | null;
  onChange: (value: (string | number)[]) => void;
  error?: boolean;
  helperText?: string;
}

/**
 * Multi-Select Form Field Component
 */
export function FormMultiSelectField({
  property,
  value,
  onChange,
  error,
  helperText,
}: FormMultiSelectFieldProps) {
  // Get options from metadata
  const options = property.metadataJson?.options || property.metadataJson?.enum || [];

  const handleChange = (event: SelectChangeEvent<(string | number)[]>) => {
    const {
      target: { value: newValue },
    } = event;
    onChange(typeof newValue === 'string' ? newValue.split(',') : newValue);
  };

  return (
    <FormControl fullWidth error={error} disabled={property.readOnly}>
      <InputLabel required={property.required}>
        {property.label || property.propertyName}
      </InputLabel>
      <Select
        multiple
        value={value || []}
        onChange={handleChange}
        input={<OutlinedInput label={property.label || property.propertyName} />}
        renderValue={(selected) => (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
            {selected.map((val) => {
              const option = options.find(
                (opt: any) => (typeof opt === 'object' ? opt.value : opt) === val
              );
              const label = typeof option === 'object' ? option.label : option || val;
              return <Chip key={val} label={label} size="small" />;
            })}
          </div>
        )}
      >
        {options.map((option: any) => {
          const optionValue = typeof option === 'object' ? option.value : option;
          const optionLabel = typeof option === 'object' ? option.label : option;
          return (
            <MenuItem key={optionValue} value={optionValue}>
              {optionLabel}
            </MenuItem>
          );
        })}
      </Select>
      {(helperText || property.description) && (
        <FormHelperText>{helperText || property.description}</FormHelperText>
      )}
    </FormControl>
  );
}
