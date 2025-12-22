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

interface MasterDetailFormProps {
  property: PropertyDefinition;
  value: any[] | null;
  onChange: (value: any[]) => void;
  onBlur?: () => void;
  error?: boolean;
  helperText?: string;
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
    return value && Array.isArray(value) ? [...value] : [];
  });

  // Track previous value to detect external changes
  const prevValueRef = useRef(value);

  // Update local state when value prop changes externally (not from our own onChange)
  useEffect(() => {
    // Only update if value changed externally (not from our own updates)
    const valueChanged = prevValueRef.current !== value;
    if (valueChanged) {
      if (value && Array.isArray(value)) {
        setLineItems([...value]);
      } else {
        setLineItems([]);
      }
      prevValueRef.current = value;
    }
  }, [value]);

  // Notify parent of changes - use a ref to avoid including onChange in deps
  const onChangeRef = useRef(onChange);
  useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

  // Only call onChange when lineItems actually changes (not on initial mount or when syncing from prop)
  const prevLineItemsRef = useRef(lineItems);
  useEffect(() => {
    // Skip if this is the initial render or if lineItems hasn't actually changed
    const lineItemsChanged = JSON.stringify(prevLineItemsRef.current) !== JSON.stringify(lineItems);
    if (lineItemsChanged && prevValueRef.current === value) {
      // Only notify if the change came from user interaction, not from prop sync
      onChangeRef.current(lineItems);
      prevLineItemsRef.current = lineItems;
    }
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

  const handleFieldChange = (index: number, fieldName: string, fieldValue: any) => {
    const newItems = [...lineItems];
    newItems[index] = {
      ...newItems[index],
      [fieldName]: fieldValue,
    };

    // Calculate total if unitPrice and quantity are present
    if (fieldName === 'unitPrice' || fieldName === 'quantity') {
      const unitPrice = fieldName === 'unitPrice' ? fieldValue : newItems[index].unitPrice;
      const quantity = fieldName === 'quantity' ? fieldValue : newItems[index].quantity;
      if (unitPrice !== null && quantity !== null && !isNaN(unitPrice) && !isNaN(quantity)) {
        newItems[index].total = unitPrice * quantity;
      }
    }

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

  // Order properties: product, quantity, unitPrice, then total (calculated), then others
  const propertyOrder = ['product', 'quantity', 'unitPrice', 'total'];
  const editableProperties = [
    ...propertyOrder
      .map(key => allEditableProperties.find(p => p.propertyName === key))
      .filter(Boolean) as PropertyDefinition[],
    ...allEditableProperties.filter(p => !propertyOrder.includes(p.propertyName))
  ];

  // Get calculated fields for display (total should be last)
  const calculatedProperties = detailEntityDefinition.properties.filter(
    (p) => p.propertyType === 'calculated'
  );
  // Ensure total is last in calculated fields
  const totalCalc = calculatedProperties.find(p => p.propertyName === 'total');
  const otherCalcs = calculatedProperties.filter(p => p.propertyName !== 'total');
  const orderedCalculatedProperties = [...otherCalcs, ...(totalCalc ? [totalCalc] : [])];

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
                  {editableProperties.map((prop) => (
                    <TableCell key={prop.propertyId}>
                      <Box sx={{ minWidth: 120 }}>
                        <FormField
                          property={prop}
                          value={item[prop.propertyName]}
                          onChange={(val) => handleFieldChange(index, prop.propertyName, val)}
                        />
                      </Box>
                    </TableCell>
                  ))}
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
