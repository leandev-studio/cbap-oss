import {
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  OutlinedInput,
  FormHelperText,
  SelectChangeEvent,
  CircularProgress,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { PropertyDefinition } from '../../shared/services/entityMetadataService';
import { getRecords } from '../../shared/services/entityRecordService';

interface FormMultiSelectFieldProps {
  property: PropertyDefinition;
  value: (string | number)[] | null;
  onChange: (value: (string | number)[]) => void;
  error?: boolean;
  helperText?: string;
}

/**
 * Multi-Select Form Field Component
 * 
 * Supports both static options from metadata and dynamic options from a referenced entity.
 */
export function FormMultiSelectField({
  property,
  value,
  onChange,
  error,
  helperText,
}: FormMultiSelectFieldProps) {
  // Check if we should fetch options from a referenced entity
  const fetchFromEntity = property.metadataJson?.fetchOptionsFromEntity === true;
  const referenceEntityId = property.metadataJson?.referenceEntity || property.referenceEntityId;

  // Fetch records from referenced entity if needed
  const { data: recordsData, isLoading } = useQuery({
    queryKey: ['entity-records', referenceEntityId, 0, 1000], // Fetch up to 1000 records
    queryFn: () => getRecords(referenceEntityId!, 0, 1000),
    enabled: fetchFromEntity && !!referenceEntityId,
  });

  // Build options from metadata or from referenced entity records
  let options: any[] = [];
  
  if (fetchFromEntity && recordsData?.records) {
    // Create options from entity records
    options = recordsData.records.map((record) => {
      // Try to find a display field (name, title, etc.)
      const displayField = Object.keys(record.data).find(
        (key) => typeof record.data[key] === 'string' && record.data[key]
      );
      const displayValue = displayField ? record.data[displayField] : record.recordId;
      return {
        value: record.recordId,
        label: String(displayValue),
      };
    });
  } else {
    // Get options from metadata
    options = property.metadataJson?.options || property.metadataJson?.enum || [];
    // Normalize options to {value, label} format
    options = options.map((opt: any) => {
      if (typeof opt === 'object' && opt.value !== undefined) {
        return opt;
      }
      return { value: opt, label: String(opt) };
    });
  }

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
        disabled={isLoading || property.readOnly}
        renderValue={(selected) => (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
            {selected.map((val) => {
              const option = options.find((opt: any) => opt.value === val);
              const label = option?.label || String(val);
              return <Chip key={val} label={label} size="small" />;
            })}
          </div>
        )}
        endAdornment={isLoading ? <CircularProgress size={20} /> : undefined}
      >
        {options.map((option: any) => {
          const optionValue = option.value;
          const optionLabel = option.label;
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
