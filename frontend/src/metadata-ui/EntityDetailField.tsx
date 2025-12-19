import { Box, Typography, Chip, Link } from '@mui/material';
import { PropertyDefinition, EntityDefinition } from '../shared/services/entityMetadataService';
import { EntityRecord } from '../shared/services/entityRecordService';
import { useNavigate } from 'react-router-dom';

interface EntityDetailFieldProps {
  property: PropertyDefinition;
  value: any;
  record: EntityRecord;
  entityDefinition: EntityDefinition;
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
  record,
  entityDefinition,
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
      const referenceValue = typeof value === 'object' ? value : { id: value, name: String(value) };
      
      return (
        <Box>
          {referenceValue.id ? (
            <Link
              component="button"
              variant="body2"
              onClick={() => navigate(`/entities/${property.referenceEntityId}/records/${referenceValue.id}`)}
              sx={{ cursor: 'pointer', textDecoration: 'none' }}
            >
              {referenceValue.name || referenceValue.id || String(value)}
            </Link>
          ) : (
            <Typography variant="body2">{referenceValue.name || String(value)}</Typography>
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
