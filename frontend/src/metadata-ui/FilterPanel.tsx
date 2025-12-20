import { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  IconButton,
  Chip,
  Menu,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Autocomplete,
  Collapse,
  Tooltip,
} from '@mui/material';
import {
  FilterList,
  Clear,
  Save,
  Bookmark,
  ExpandMore,
  ExpandLess,
} from '@mui/icons-material';
import { PropertyDefinition, EntityDefinition } from '../shared/services/entityMetadataService';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getRecords } from '../shared/services/entityRecordService';
import { searchService, SavedSearch } from '../shared/services/searchService';

/**
 * Filter Panel Component
 * 
 * Provides filtering capabilities for entity lists with support for:
 * - Property value filters
 * - Date range filters
 * - Reference filters
 * - Save/load saved searches
 */
interface FilterPanelProps {
  entityDefinition: EntityDefinition;
  filters: Record<string, any>;
  onFiltersChange: (filters: Record<string, any>) => void;
  onApplyFilters: () => void;
  onClearFilters: () => void;
}

export function FilterPanel({
  entityDefinition,
  filters,
  onFiltersChange,
  onApplyFilters,
  onClearFilters,
}: FilterPanelProps) {
  const [expanded, setExpanded] = useState(false);
  const [savedSearchesMenuAnchor, setSavedSearchesMenuAnchor] = useState<null | HTMLElement>(null);
  const [saveSearchDialogOpen, setSaveSearchDialogOpen] = useState(false);
  const [saveSearchName, setSaveSearchName] = useState('');
  const [justLoadedSavedSearch, setJustLoadedSavedSearch] = useState(false);

  // Keep panel expanded after loading a saved search
  useEffect(() => {
    if (justLoadedSavedSearch && Object.keys(filters).length > 0) {
      setExpanded(true);
      setJustLoadedSavedSearch(false);
    }
  }, [filters, justLoadedSavedSearch]);

  // Fetch saved searches
  const { data: savedSearches } = useQuery({
    queryKey: ['saved-searches', entityDefinition.entityId],
    queryFn: () => searchService.getSavedSearches(entityDefinition.entityId),
  });

  const handleFilterChange = (propertyName: string, value: any) => {
    const newFilters = { ...filters };
    if (value === null || value === '' || (Array.isArray(value) && value.length === 0)) {
      delete newFilters[propertyName];
    } else {
      newFilters[propertyName] = value;
    }
    onFiltersChange(newFilters);
  };

  const handleLoadSavedSearch = (savedSearch: SavedSearch) => {
    // Close menu first
    setSavedSearchesMenuAnchor(null);
    // Mark that we just loaded a saved search
    setJustLoadedSavedSearch(true);
    // Set expanded to true immediately
    setExpanded(true);
    // Update filters
    onFiltersChange(savedSearch.filters || {});
    // Apply filters
    onApplyFilters();
  };

  const queryClient = useQueryClient();

  const saveSearchMutation = useMutation({
    mutationFn: (search: {
      entityId?: string;
      name: string;
      filters?: Record<string, any>;
    }) => searchService.saveSearch(search),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['saved-searches', entityDefinition.entityId] });
      setSaveSearchDialogOpen(false);
      setSaveSearchName('');
    },
  });

  const handleSaveSearch = () => {
    if (saveSearchName.trim()) {
      saveSearchMutation.mutate({
        entityId: entityDefinition.entityId,
        name: saveSearchName,
        filters: filters,
      });
    }
  };

  const filterableProperties = entityDefinition.properties.filter(
    (prop) => prop.denormalize && prop.propertyType !== 'calculated'
  );

  const hasActiveFilters = Object.keys(filters).length > 0;

  return (
    <Paper 
      elevation={0} 
      sx={{ 
        border: 1, 
        borderColor: 'divider', 
        mb: 2,
        overflow: 'hidden',
      }}
    >
      {/* Collapsible Header */}
      <Box 
        sx={{ 
          p: 1.5, 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'space-between',
          cursor: 'pointer',
          transition: 'background-color 0.2s',
          '&:hover': {
            backgroundColor: 'action.hover',
          },
        }}
        onClick={() => setExpanded(!expanded)}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <IconButton
            size="small"
            onClick={(e) => {
              e.stopPropagation();
              setExpanded(!expanded);
            }}
            sx={{ 
              color: hasActiveFilters ? 'primary.main' : 'text.secondary',
            }}
          >
            <FilterList fontSize="small" />
          </IconButton>
          <Typography 
            variant="subtitle2" 
            sx={{ 
              fontWeight: hasActiveFilters ? 600 : 400,
              color: hasActiveFilters ? 'primary.main' : 'text.primary',
            }}
          >
            Filters
          </Typography>
          {hasActiveFilters && (
            <Chip
              label={Object.keys(filters).length}
              size="small"
              color="primary"
              sx={{ height: 20, fontSize: '0.7rem' }}
            />
          )}
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
          {expanded && (
            <>
              {savedSearches && savedSearches.length > 0 && (
                <>
                  <Tooltip title="Load saved search">
                    <IconButton
                      size="small"
                      onClick={(e) => {
                        e.stopPropagation();
                        setSavedSearchesMenuAnchor(e.currentTarget);
                      }}
                    >
                      <Bookmark fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Menu
                    anchorEl={savedSearchesMenuAnchor}
                    open={Boolean(savedSearchesMenuAnchor)}
                    onClose={() => {
                      setSavedSearchesMenuAnchor(null);
                      // Ensure panel stays expanded when menu closes
                      if (Object.keys(filters).length > 0) {
                        setExpanded(true);
                      }
                    }}
                  >
                    {savedSearches.map((search) => (
                      <MenuItem
                        key={search.searchId}
                        onClick={(e) => {
                          e.stopPropagation(); // Prevent event bubbling
                          handleLoadSavedSearch(search);
                        }}
                      >
                        {search.name}
                      </MenuItem>
                    ))}
                  </Menu>
                </>
              )}
              <Tooltip title="Save current filters">
                <IconButton
                  size="small"
                  onClick={(e) => {
                    e.stopPropagation();
                    setSaveSearchDialogOpen(true);
                  }}
                  disabled={!hasActiveFilters}
                >
                  <Save fontSize="small" />
                </IconButton>
              </Tooltip>
              <Button
                size="small"
                startIcon={<Clear />}
                onClick={(e) => {
                  e.stopPropagation();
                  onClearFilters();
                }}
                disabled={!hasActiveFilters}
                sx={{ mr: 0.5 }}
              >
                Clear
              </Button>
              <Button
                size="small"
                variant="contained"
                onClick={(e) => {
                  e.stopPropagation();
                  onApplyFilters();
                }}
                disabled={!hasActiveFilters}
              >
                Apply
              </Button>
            </>
          )}
          <IconButton
            size="small"
            onClick={(e) => {
              e.stopPropagation();
              setExpanded(!expanded);
            }}
          >
            {expanded ? <ExpandLess /> : <ExpandMore />}
          </IconButton>
        </Box>
      </Box>

      {/* Collapsible Content */}
      <Collapse in={expanded}>
        <Box>
          {filterableProperties.length > 0 && (
            <Box sx={{ px: 2, pb: 2, pt: 1 }}>
              {filterableProperties.map((property) => (
                <FilterField
                  key={property.propertyName}
                  property={property}
                  value={filters[property.propertyName]}
                  onChange={(value) => handleFilterChange(property.propertyName, value)}
                />
              ))}
            </Box>
          )}

          {/* Save Search Dialog */}
          {saveSearchDialogOpen && (
            <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider' }}>
              <TextField
                fullWidth
                size="small"
                label="Search Name"
                value={saveSearchName}
                onChange={(e) => setSaveSearchName(e.target.value)}
                sx={{ mb: 2 }}
              />
              <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                <Button
                  size="small"
                  onClick={() => {
                    setSaveSearchDialogOpen(false);
                    setSaveSearchName('');
                  }}
                >
                  Cancel
                </Button>
                <Button
                  size="small"
                  variant="contained"
                  onClick={handleSaveSearch}
                  disabled={!saveSearchName.trim()}
                >
                  Save
                </Button>
              </Box>
            </Box>
          )}
        </Box>
      </Collapse>
    </Paper>
  );
}

/**
 * Filter Field Component
 * 
 * Renders a filter input based on property type.
 */
interface FilterFieldProps {
  property: PropertyDefinition;
  value: any;
  onChange: (value: any) => void;
}

function FilterField({ property, value, onChange }: FilterFieldProps) {
  const propertyType = property.propertyType;

  // For reference fields, fetch options
  const { data: referenceRecords } = useQuery({
    queryKey: ['entity-records', property.referenceEntityId, 0, 100],
    queryFn: () => getRecords(property.referenceEntityId!, 0, 100),
    enabled: propertyType === 'reference' && !!property.referenceEntityId,
  });

  if (propertyType === 'string' || propertyType === 'number') {
    return (
      <TextField
        fullWidth
        size="small"
        label={property.label || property.propertyName}
        value={value || ''}
        onChange={(e) => onChange(e.target.value || null)}
        type={propertyType === 'number' ? 'number' : 'text'}
        sx={{ mb: 2 }}
      />
    );
  }

  if (propertyType === 'date') {
    return (
      <Box sx={{ mb: 2 }}>
        <Typography variant="caption" color="text.secondary" sx={{ mb: 0.5, display: 'block' }}>
          {property.label || property.propertyName}
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <TextField
            size="small"
            type="date"
            label="From"
            value={value?.from || ''}
            onChange={(e) => onChange({ ...value, from: e.target.value || undefined })}
            InputLabelProps={{ shrink: true }}
            sx={{ flex: 1 }}
          />
          <TextField
            size="small"
            type="date"
            label="To"
            value={value?.to || ''}
            onChange={(e) => onChange({ ...value, to: e.target.value || undefined })}
            InputLabelProps={{ shrink: true }}
            sx={{ flex: 1 }}
          />
        </Box>
      </Box>
    );
  }

  if (propertyType === 'boolean') {
    return (
      <FormControl fullWidth size="small" sx={{ mb: 2 }}>
        <InputLabel>{property.label || property.propertyName}</InputLabel>
        <Select
          value={value === undefined ? '' : String(value)}
          onChange={(e) => {
            const val = e.target.value;
            onChange(val === '' ? null : val === 'true');
          }}
          label={property.label || property.propertyName}
        >
          <MenuItem value="">All</MenuItem>
          <MenuItem value="true">Yes</MenuItem>
          <MenuItem value="false">No</MenuItem>
        </Select>
      </FormControl>
    );
  }

  if (propertyType === 'reference' && property.referenceEntityId) {
    const options = (referenceRecords?.records || []).map((record) => {
      const displayField = property.metadataJson?.displayField as string | undefined;
      const displayValue = displayField && record.data?.[displayField]
        ? String(record.data[displayField])
        : record.recordId;
      return { id: record.recordId, label: displayValue, record };
    });

    return (
      <Autocomplete
        fullWidth
        size="small"
        options={options}
        getOptionLabel={(option) => option.label}
        value={options.find((opt) => opt.id === value) || null}
        onChange={(_event, newValue) => onChange(newValue?.id || null)}
        renderInput={(params) => (
          <TextField
            {...params}
            label={property.label || property.propertyName}
          />
        )}
        sx={{ mb: 2 }}
      />
    );
  }

  if (propertyType === 'singleSelect' || propertyType === 'multiSelect') {
    const options = property.metadataJson?.options || property.metadataJson?.enum || [];
    
    if (propertyType === 'multiSelect') {
      return (
        <Autocomplete
          fullWidth
          size="small"
          multiple
          options={options}
          value={value || []}
          onChange={(_event, newValue) => onChange(newValue.length > 0 ? newValue : null)}
          renderInput={(params) => (
            <TextField
              {...params}
              label={property.label || property.propertyName}
            />
          )}
          sx={{ mb: 2 }}
        />
      );
    } else {
      return (
        <FormControl fullWidth size="small" sx={{ mb: 2 }}>
          <InputLabel>{property.label || property.propertyName}</InputLabel>
          <Select
            value={value || ''}
            onChange={(e) => onChange(e.target.value || null)}
            label={property.label || property.propertyName}
          >
            <MenuItem value="">All</MenuItem>
            {options.map((option: string) => (
              <MenuItem key={option} value={option}>
                {option}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      );
    }
  }

  return null;
}
