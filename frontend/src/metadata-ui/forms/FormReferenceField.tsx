import { useState } from 'react';
import {
  TextField,
  Autocomplete,
  CircularProgress,
  FormHelperText,
  FormControl,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { PropertyDefinition } from '../../shared/services/entityMetadataService';
import { getRecords } from '../../shared/services/entityRecordService';

interface FormReferenceFieldProps {
  property: PropertyDefinition;
  value: string | null;
  onChange: (value: string | null) => void;
  error?: boolean;
  helperText?: string;
}

/**
 * Reference Picker Form Field Component
 * 
 * Allows selecting a record from a referenced entity.
 */
export function FormReferenceField({
  property,
  value,
  onChange,
  error,
  helperText,
}: FormReferenceFieldProps) {
  const [inputValue, setInputValue] = useState('');

  // Fetch records from the referenced entity
  const { data: recordsData, isLoading } = useQuery({
    queryKey: ['entity-records', property.referenceEntityId, 0, 100],
    queryFn: () => getRecords(property.referenceEntityId!, 0, 100),
    enabled: !!property.referenceEntityId,
  });

  // Create options from records (use a display field like 'name' or first string field)
  const options = (recordsData?.records || []).map((record) => {
    // Try to find a display field (name, title, etc.)
    const displayField = Object.keys(record.data).find(
      (key) => typeof record.data[key] === 'string' && record.data[key]
    );
    const displayValue = displayField ? record.data[displayField] : record.recordId;
    return {
      id: record.recordId,
      label: String(displayValue),
      record,
    };
  });

  // Find the selected option
  const selectedOption = value ? options.find((opt) => opt.id === value) || null : null;

  return (
    <FormControl fullWidth error={error} disabled={property.readOnly}>
      <Autocomplete
        value={selectedOption}
        onChange={(_, newValue) => {
          onChange(newValue?.id || null);
        }}
        inputValue={inputValue}
        onInputChange={(_, newInputValue) => {
          setInputValue(newInputValue);
        }}
        options={options}
        getOptionLabel={(option) => option.label}
        isOptionEqualToValue={(option, value) => option.id === value.id}
        loading={isLoading}
        disabled={property.readOnly}
        renderInput={(params) => (
          <TextField
            {...params}
            label={property.label || property.propertyName}
            required={property.required}
            error={error}
            InputProps={{
              ...params.InputProps,
              endAdornment: (
                <>
                  {isLoading ? <CircularProgress color="inherit" size={20} /> : null}
                  {params.InputProps.endAdornment}
                </>
              ),
            }}
          />
        )}
      />
      {(helperText || property.description) && (
        <FormHelperText>{helperText || property.description}</FormHelperText>
      )}
    </FormControl>
  );
}
