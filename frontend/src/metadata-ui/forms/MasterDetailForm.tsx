import { useState, useEffect, useRef } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Alert,
} from '@mui/material';
import { Add, Delete } from '@mui/icons-material';
import { PropertyDefinition } from '../../shared/services/entityMetadataService';
import { FormField } from './FormField';
import { useQuery } from '@tanstack/react-query';
import { getEntityById } from '../../shared/services/entityMetadataService';
import { getRecord } from '../../shared/services/entityRecordService';

interface MasterDetailFormProps {
  property: PropertyDefinition;
  value: any[] | null;
  onChange: (value: any[]) => void;
  onBlur?: () => void;
  error?: boolean;
  helperText?: string;
  parentFormData?: Record<string, any>; // Parent form data (e.g., Order data for OrderLineItem)
}

/**
 * Master-Detail Form Component
 * 
 * Renders a table-based form for editing nested entity arrays (line items).
 * Supports adding, editing, and removing detail records.
 */
export function MasterDetailForm({
  property,
  value,
  onChange,
  error,
  helperText,
  parentFormData: _parentFormData, // Reserved for future use (parent context for calculated fields)
}: MasterDetailFormProps) {
  const detailEntityId = property.metadataJson?.detailEntityId as string | undefined;
  const minItems = property.metadataJson?.minItems as number | undefined;

  // Fetch detail entity definition
  const { data: detailEntityDefinition, isLoading } = useQuery({
    queryKey: ['entity-definition', detailEntityId],
    queryFn: () => getEntityById(detailEntityId!),
    enabled: !!detailEntityId,
  });

  const [lineItems, setLineItems] = useState<any[]>(() => {
    const items = value && Array.isArray(value) ? [...value] : [];
    // Initialize calculated fields if not present
    return items.map(item => ({
      ...item,
      taxPercent: item.taxPercent ?? 0,
      taxValue: item.taxValue ?? 0,
    }));
  });

  // Track previous value to detect external changes
  const prevValueRef = useRef(value);

  // Update local state when value prop changes externally (not from our own onChange)
  useEffect(() => {
    // Only update if value changed externally (not from our own updates)
    const valueChanged = prevValueRef.current !== value;
    if (valueChanged) {
      if (value && Array.isArray(value)) {
        // Initialize calculated fields if not present
        const items = value.map(item => ({
          ...item,
          taxPercent: item.taxPercent ?? 0,
          taxValue: item.taxValue ?? 0,
        }));
        setLineItems(items);
      } else {
        setLineItems([]);
      }
      prevValueRef.current = value;
    }
  }, [value]);

  // Note: Calculated fields are evaluated server-side based on metadata expressions
  // No client-side recalculation is performed to maintain metadata-driven approach

  // Notify parent of changes - use a ref to avoid including onChange in deps
  const onChangeRef = useRef(onChange);
  useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

  // Only call onChange when lineItems actually changes (not on initial mount or when syncing from prop)
  const prevLineItemsRef = useRef(lineItems);
  const isRecalculatingRef = useRef(false);
  
  useEffect(() => {
    // Skip if this is the initial render or if lineItems hasn't actually changed
    const lineItemsChanged = JSON.stringify(prevLineItemsRef.current) !== JSON.stringify(lineItems);
    if (lineItemsChanged && prevValueRef.current === value && !isRecalculatingRef.current) {
      // Only notify if the change came from user interaction, not from prop sync
      onChangeRef.current(lineItems);
      prevLineItemsRef.current = lineItems;
    }
    isRecalculatingRef.current = false;
  }, [lineItems, value]);

  const handleAddLineItem = () => {
    const newItem: any = {};
    // Initialize fields based on detail entity definition
    if (detailEntityDefinition) {
      detailEntityDefinition.properties.forEach((prop) => {
        if (prop.propertyType !== 'calculated' && !prop.readOnly) {
          if (prop.propertyType === 'number') {
            newItem[prop.propertyName] = 0;
          } else if (prop.propertyType === 'boolean') {
            newItem[prop.propertyName] = false;
          } else {
            newItem[prop.propertyName] = null;
          }
        }
      });
    }
    setLineItems([...lineItems, newItem]);
  };

  const handleRemoveLineItem = (index: number) => {
    const newItems = lineItems.filter((_, i) => i !== index);
    setLineItems(newItems);
  };

  const handleFieldChange = async (index: number, fieldName: string, fieldValue: any) => {
    const newItems = [...lineItems];
    newItems[index] = {
      ...newItems[index],
      [fieldName]: fieldValue,
    };

    // Auto-populate fields based on metadata (autoPopulateFrom, sourceField)
    // This is metadata-driven, not hardcoded
    if (detailEntityDefinition) {
      const changedProperty = detailEntityDefinition.properties.find(p => p.propertyName === fieldName);
      if (changedProperty?.metadataJson?.autoPopulateFrom && changedProperty.metadataJson.sourceField) {
        const sourceField = changedProperty.metadataJson.sourceField as string;
        const sourceEntity = changedProperty.referenceEntityId || changedProperty.metadataJson.autoPopulateFrom as string;
        
        // If this is a reference field that triggers auto-population
        if (fieldValue && changedProperty.propertyType === 'reference') {
          try {
            const sourceRecord = await getRecord(sourceEntity, fieldValue);
            const sourceValue = sourceRecord.data?.[sourceField];
            if (sourceValue !== null && sourceValue !== undefined) {
              // Find the target property that should be auto-populated based on metadata
              const targetProperty = detailEntityDefinition.properties.find(
                p => p.metadataJson?.autoPopulateFrom === fieldName && 
                     p.metadataJson?.sourceField === sourceField
              );
              if (targetProperty) {
                newItems[index][targetProperty.propertyName] = sourceValue;
              }
            }
          } catch (error) {
            console.warn(`Failed to auto-populate from ${sourceEntity}:`, error);
          }
        }
      }
    }

    // Note: Calculated fields are computed server-side based on their metadata expressions
    // Client-side calculation is not performed to avoid hardcoded business logic
    // The backend will evaluate expressions when the record is saved

    setLineItems(newItems);
  };

  if (isLoading) {
    return (
      <Box>
        <Typography variant="body2" color="text.secondary">
          Loading line item definition...
        </Typography>
      </Box>
    );
  }

  if (!detailEntityDefinition) {
    return (
      <Alert severity="error">
        Detail entity definition not found: {detailEntityId}
      </Alert>
    );
  }

  // Filter out calculated and read-only fields for editing
  const allEditableProperties = detailEntityDefinition.properties.filter(
    (p) => p.propertyType !== 'calculated' && !p.readOnly
  );

  // Get column order from metadata (detailView.columnOrder) - metadata-driven, not hardcoded
  const detailViewConfig = property.metadataJson?.detailView as any;
  const columnOrder = detailViewConfig?.columnOrder as string[] | undefined;
  
  // Order properties based on metadata columnOrder, or use natural order
  const editableProperties = columnOrder && Array.isArray(columnOrder)
    ? [
        ...columnOrder
          .map(key => allEditableProperties.find(p => p.propertyName === key))
          .filter(Boolean) as PropertyDefinition[],
        ...allEditableProperties.filter(p => !columnOrder.includes(p.propertyName))
      ]
    : allEditableProperties;

  // Get calculated fields for display - order based on metadata columnOrder
  const calculatedProperties = detailEntityDefinition.properties.filter(
    (p) => p.propertyType === 'calculated'
  );
  
  // Order calculated properties based on metadata columnOrder
  const orderedCalculatedProperties = columnOrder && Array.isArray(columnOrder)
    ? [
        ...columnOrder
          .map(key => calculatedProperties.find(p => p.propertyName === key))
          .filter(Boolean) as PropertyDefinition[],
        ...calculatedProperties.filter(p => !columnOrder.includes(p.propertyName))
      ]
    : calculatedProperties;

  const hasError = error || (minItems !== undefined && lineItems.length < minItems);

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="subtitle2" color={hasError ? 'error' : 'text.primary'}>
          {property.label || property.propertyName}
          {property.required && (
            <Typography component="span" variant="subtitle2" color="error" sx={{ ml: 0.5 }}>
              *
            </Typography>
          )}
        </Typography>
        <Button
          variant="outlined"
          size="small"
          startIcon={<Add />}
          onClick={handleAddLineItem}
        >
          Add Line Item
        </Button>
      </Box>

      {lineItems.length === 0 ? (
        <Paper
          elevation={0}
          sx={{
            p: 3,
            textAlign: 'center',
            border: 1,
            borderColor: hasError ? 'error.main' : 'divider',
            borderRadius: 1,
          }}
        >
          <Typography variant="body2" color="text.secondary">
            No line items. Click "Add Line Item" to add one.
          </Typography>
        </Paper>
      ) : (
        <TableContainer
          component={Paper}
          elevation={0}
          sx={{
            border: 1,
            borderColor: hasError ? 'error.main' : 'divider',
            borderRadius: 1,
          }}
        >
          <Table size="small">
            <TableHead>
              <TableRow>
                {editableProperties.map((prop) => (
                  <TableCell key={prop.propertyId} sx={{ fontWeight: 600 }}>
                    {prop.label || prop.propertyName}
                    {prop.required && (
                      <Typography component="span" variant="caption" color="error" sx={{ ml: 0.5 }}>
                        *
                      </Typography>
                    )}
                  </TableCell>
                ))}
                {calculatedProperties.map((prop) => (
                  <TableCell key={prop.propertyId} sx={{ fontWeight: 600 }}>
                    {prop.label || prop.propertyName}
                  </TableCell>
                ))}
                <TableCell align="right" sx={{ fontWeight: 600 }}>
                  Actions
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {lineItems.map((item, index) => (
                <TableRow key={index}>
                  {editableProperties.map((prop) => {
                    // Check if this field should be read-only based on metadata
                    const isReadOnly = prop.readOnly || 
                      (prop.metadataJson?.autoPopulateFrom !== undefined && prop.metadataJson?.autoPopulateFrom !== null);
                    const readOnlyProp = isReadOnly ? { ...prop, readOnly: true } : prop;
                    
                    return (
                      <TableCell key={prop.propertyId}>
                        <Box sx={{ minWidth: 120 }}>
                          <FormField
                            property={readOnlyProp}
                            value={item[prop.propertyName]}
                            onChange={(val) => handleFieldChange(index, prop.propertyName, val)}
                          />
                        </Box>
                      </TableCell>
                    );
                  })}
                  {orderedCalculatedProperties.map((prop) => (
                    <TableCell key={prop.propertyId}>
                      <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                        {item[prop.propertyName] !== null && item[prop.propertyName] !== undefined
                          ? typeof item[prop.propertyName] === 'number'
                            ? item[prop.propertyName].toLocaleString(undefined, {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2,
                              })
                            : String(item[prop.propertyName])
                          : '—'}
                      </Typography>
                    </TableCell>
                  ))}
                  <TableCell align="right">
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => handleRemoveLineItem(index)}
                    >
                      <Delete />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {hasError && (
        <Typography variant="caption" color="error" sx={{ mt: 0.5, display: 'block' }}>
          {helperText || `At least ${minItems} line item(s) required`}
        </Typography>
      )}
      {!hasError && (helperText || property.description) && (
        <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
          {helperText || property.description}
        </Typography>
      )}
    </Box>
  );
}
