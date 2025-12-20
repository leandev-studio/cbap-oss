import { Box, Typography, Chip, Link, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, CircularProgress } from '@mui/material';
import { PropertyDefinition, EntityDefinition, getEntityById } from '../shared/services/entityMetadataService';
import { EntityRecord } from '../shared/services/entityRecordService';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getRecord } from '../shared/services/entityRecordService';

interface EntityDetailFieldProps {
  property: PropertyDefinition;
  value: any;
  record?: EntityRecord;
  entityDefinition?: EntityDefinition;
}

/**
 * Reference Display Component
 * Generic component that fetches and displays referenced entity using metadata-driven displayField
 */
function ReferenceDisplay({ 
  referenceEntityId, 
  referenceId, 
  displayField 
}: { 
  referenceEntityId: string; 
  referenceId: string; 
  displayField: string;
}) {
  const navigate = useNavigate();
  const { data: referencedRecord, isLoading } = useQuery({
    queryKey: ['entity-record', referenceEntityId, referenceId],
    queryFn: () => getRecord(referenceEntityId, referenceId),
    enabled: !!referenceId,
    staleTime: 5 * 60 * 1000, // Cache for 5 minutes
  });

  if (isLoading) {
    return <CircularProgress size={16} />;
  }

  const displayValue = referencedRecord?.data?.[displayField] || referenceId.substring(0, 8) + '...';

  return (
    <Link
      component="button"
      variant="body2"
      onClick={() => navigate(`/entities/${referenceEntityId}/records/${referenceId}`)}
      sx={{ cursor: 'pointer', textDecoration: 'none', fontWeight: 500 }}
    >
      {displayValue}
    </Link>
  );
}

/**
 * Master Detail Display Component
 * Displays nested entity arrays (line items) with proper reference resolution
 */
function MasterDetailDisplay({ 
  property, 
  value 
}: { 
  property: PropertyDefinition; 
  value: any[];
}) {
  const detailEntityId = property.metadataJson?.detailEntityId as string | undefined;
  
  // Fetch detail entity definition to get property metadata
  const { data: detailEntityDefinition, isLoading } = useQuery({
    queryKey: ['entity-definition', detailEntityId],
    queryFn: () => getEntityById(detailEntityId!),
    enabled: !!detailEntityId,
  });

  if (isLoading) {
    return (
      <Box>
        <CircularProgress size={16} />
      </Box>
    );
  }

  if (value.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
        No items
      </Typography>
    );
  }

  // Get column configuration from metadata (metadata-driven, not hardcoded)
  const detailViewConfig = property.metadataJson?.detailView as any;
  const columnOrder = detailViewConfig?.columnOrder as string[] | undefined;
  const columnConfig = detailViewConfig?.columnConfig as Record<string, any> | undefined;

  // Use metadata column order or fallback to all keys
  const allKeys = Object.keys(value[0] || {});
  const orderedKeys = columnOrder && Array.isArray(columnOrder)
    ? columnOrder.filter(key => allKeys.includes(key)).concat(allKeys.filter(key => !columnOrder.includes(key)))
    : allKeys;

  return (
    <TableContainer component={Paper} elevation={0} sx={{ border: 1, borderColor: 'divider', borderRadius: 1 }}>
      <Table size="small">
        <TableHead>
          <TableRow>
            {orderedKeys.map((key) => {
              // Get label from column config, or use key (capitalized)
              const config = columnConfig?.[key] || {};
              const label = config.label || key.charAt(0).toUpperCase() + key.slice(1);
              
              // Determine alignment based on property type from detail entity
              let align: 'left' | 'center' | 'right' = 'left';
              if (detailEntityDefinition) {
                const detailProp = detailEntityDefinition.properties.find((p: PropertyDefinition) => p.propertyName === key);
                if (detailProp) {
                  if (detailProp.propertyType === 'number' || detailProp.propertyType === 'calculated') {
                    align = 'right';
                  } else if (detailProp.propertyType === 'date') {
                    align = 'center';
                  }
                }
              }
              
              return (
                <TableCell key={key} align={align} sx={{ fontWeight: 600 }}>
                  {label}
                </TableCell>
              );
            })}
          </TableRow>
        </TableHead>
        <TableBody>
          {value.map((item: any, index: number) => (
            <TableRow key={index}>
              {orderedKeys.map((key) => {
                const cellValue = item[key];
                const config = columnConfig?.[key] || {};
                
                // Determine alignment based on property type from detail entity
                let align: 'left' | 'center' | 'right' = 'left';
                if (detailEntityDefinition) {
                  const detailProp = detailEntityDefinition.properties.find((p: PropertyDefinition) => p.propertyName === key);
                  if (detailProp) {
                    if (detailProp.propertyType === 'number' || detailProp.propertyType === 'calculated') {
                      align = 'right';
                    } else if (detailProp.propertyType === 'date') {
                      align = 'center';
                    }
                  }
                }
                
                // Handle reference fields using metadata-driven displayField
                // Get the property definition from detail entity to find referenceEntityId and displayField
                if (config.displayType === 'reference' && typeof cellValue === 'string' && detailEntityDefinition) {
                  const detailProp = detailEntityDefinition.properties.find((p: PropertyDefinition) => p.propertyName === key);
                  if (detailProp && detailProp.propertyType === 'reference' && detailProp.referenceEntityId) {
                    const displayField = detailProp.metadataJson?.displayField as string | undefined || config.displayField as string | undefined;
                    if (displayField) {
                      return (
                        <TableCell key={key} align={align}>
                          <ReferenceDisplay
                            referenceEntityId={detailProp.referenceEntityId}
                            referenceId={cellValue}
                            displayField={displayField}
                          />
                        </TableCell>
                      );
                    }
                  }
                }
                
                // Handle numbers with formatting from config
                if (typeof cellValue === 'number') {
                  const format = config.format || 'number';
                  const decimals = format === 'currency' ? 2 : (key === 'total' || key === 'unitPrice' ? 2 : 0);
                  return (
                    <TableCell key={key} align={align}>
                      <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                        {cellValue.toLocaleString(undefined, {
                          minimumFractionDigits: decimals,
                          maximumFractionDigits: decimals,
                        })}
                      </Typography>
                    </TableCell>
                  );
                }
                
                return (
                  <TableCell key={key} align={align}>
                    <Typography variant="body2">{String(cellValue || '—')}</Typography>
                  </TableCell>
                );
              })}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

/**
 * Entity Detail Field Component
 * 
 * Renders a single property field based on its type.
 * Supports all property types including references and calculated fields.
 */
export function EntityDetailField({
  property,
  value,
}: EntityDetailFieldProps) {
  const navigate = useNavigate();

  const renderValue = () => {
    // Handle null/undefined
    if (value === null || value === undefined) {
      return (
        <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
          Not set
        </Typography>
      );
    }

    // Calculated fields
    if (property.propertyType === 'calculated') {
      return (
        <Box>
          <Typography variant="body2" component="span" sx={{ fontFamily: 'monospace' }}>
            {String(value)}
          </Typography>
          {property.calculationExpression && (
            <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 0.5 }}>
              Calculated: {property.calculationExpression}
            </Typography>
          )}
        </Box>
      );
    }

    // Reference fields
    if (property.propertyType === 'reference' && property.referenceEntityId) {
      const referenceId = typeof value === 'object' ? value?.id || value : value;
      
      // Use metadata-driven displayField configuration
      const displayField = property.metadataJson?.displayField as string | undefined;
      
      // If displayField is configured, fetch and display the referenced record
      if (displayField && referenceId) {
        return <ReferenceDisplay 
          referenceEntityId={property.referenceEntityId} 
          referenceId={String(referenceId)}
          displayField={displayField}
        />;
      }
      
      // Fallback: show ID as link
      return (
        <Box>
          {referenceId ? (
            <Link
              component="button"
              variant="body2"
              onClick={() => navigate(`/entities/${property.referenceEntityId}/records/${referenceId}`)}
              sx={{ cursor: 'pointer', textDecoration: 'none' }}
            >
              {String(referenceId).substring(0, 8)}...
            </Link>
          ) : (
            <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
              Not set
            </Typography>
          )}
          {property.referenceEntityId && (
            <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 0.5 }}>
              → {property.referenceEntityId}
            </Typography>
          )}
        </Box>
      );
    }

    // Boolean fields
    if (property.propertyType === 'boolean') {
      return (
        <Chip
          label={value ? 'Yes' : 'No'}
          size="small"
          color={value ? 'success' : 'default'}
          variant="outlined"
        />
      );
    }

    // Date fields
    if (property.propertyType === 'date') {
      try {
        const date = new Date(value);
        return (
          <Typography variant="body2">
            {date.toLocaleDateString('en-US', {
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}
          </Typography>
        );
      } catch {
        return <Typography variant="body2">{String(value)}</Typography>;
      }
    }

    // Number fields
    if (property.propertyType === 'number') {
      return (
        <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
          {typeof value === 'number' ? value.toLocaleString() : String(value)}
        </Typography>
      );
    }

    // Select fields (single or multi)
    if (property.propertyType === 'singleSelect' || property.propertyType === 'multiSelect') {
      if (Array.isArray(value)) {
        return (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
            {value.map((item, index) => (
              <Chip key={index} label={String(item)} size="small" variant="outlined" />
            ))}
          </Box>
        );
      }
      return (
        <Chip label={String(value)} size="small" variant="outlined" color="primary" />
      );
    }

    // Master-detail fields (nested entity arrays)
    if (property.metadataJson?.isDetailEntityArray === true && Array.isArray(value)) {
      return <MasterDetailDisplay property={property} value={value} />;
    }

    // String fields (default)
    return (
      <Typography variant="body2" sx={{ wordBreak: 'break-word' }}>
        {String(value)}
      </Typography>
    );
  };

  return (
    <Box>
      <Typography
        variant="caption"
        color="text.secondary"
        sx={{
          display: 'block',
          mb: 0.5,
          fontWeight: property.required ? 600 : 400,
        }}
      >
        {property.label || property.propertyName}
        {property.required && (
          <Typography component="span" variant="caption" color="error" sx={{ ml: 0.5 }}>
            *
          </Typography>
        )}
        {property.readOnly && (
          <Typography component="span" variant="caption" color="text.secondary" sx={{ ml: 0.5, fontStyle: 'italic' }}>
            (read-only)
          </Typography>
        )}
      </Typography>
      <Box sx={{ minHeight: '24px' }}>{renderValue()}</Box>
      {property.description && (
        <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
          {property.description}
        </Typography>
      )}
    </Box>
  );
}
