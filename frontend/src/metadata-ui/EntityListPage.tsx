import { useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  CircularProgress,
  Alert,
  TextField,
  InputAdornment,
  IconButton,
  Button,
} from '@mui/material';
import { Search, Clear, Add } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { getEntityById } from '../shared/services/entityMetadataService';
import { getRecords, getRecordsWithFilters } from '../shared/services/entityRecordService';
import { EntityListTable } from './EntityListTable';
import { FilterPanel } from './FilterPanel';

/**
 * Entity List Page Component
 * 
 * Displays a metadata-driven list of entity records.
 * Route: /entities/:entityId
 */
export function EntityListPage() {
  const { entityId } = useParams<{ entityId: string }>();
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [searchText, setSearchText] = useState('');
  const [filters, setFilters] = useState<Record<string, any>>({});
  const [appliedFilters, setAppliedFilters] = useState<Record<string, any>>({});
  const [sortField, setSortField] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');

  if (!entityId) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Entity ID is required</Alert>
      </Box>
    );
  }

  // Fetch entity definition
  const {
    data: entityDefinition,
    isLoading: isLoadingEntity,
    error: entityError,
  } = useQuery({
    queryKey: ['entity-definition', entityId],
    queryFn: () => getEntityById(entityId),
    enabled: !!entityId,
  });

  // Fetch records using database filter API when filters or grid search are applied
  // This uses PostgreSQL JSONB queries, not OpenSearch
  const {
    data: filteredRecordsData,
    isLoading: isLoadingFiltered,
    error: filteredError,
  } = useQuery({
    queryKey: ['entity-records-filtered', entityId, searchText, appliedFilters, page, pageSize],
    queryFn: () => getRecordsWithFilters(entityId!, {
      filters: appliedFilters,
      searchText: searchText || undefined,
    }, page, pageSize),
    enabled: !!entityId && (Object.keys(appliedFilters).length > 0 || searchText.length > 0),
  });

  // Fetch records using regular API when no filters or search
  const {
    data: recordsData,
    isLoading: isLoadingRecords,
    error: recordsError,
  } = useQuery({
    queryKey: ['entity-records', entityId, page, pageSize],
    queryFn: () => getRecords(entityId, page, pageSize),
    enabled: !!entityId && Object.keys(appliedFilters).length === 0 && searchText.length === 0,
  });

  // Determine which records to use
  const records = useMemo(() => {
    if (Object.keys(appliedFilters).length > 0 || searchText.length > 0) {
      // Use filtered records from database
      return filteredRecordsData?.records || [];
    } else {
      // Use regular records
      return recordsData?.records || [];
    }
  }, [appliedFilters, searchText, filteredRecordsData, recordsData]);

  const totalRecords = Object.keys(appliedFilters).length > 0 || searchText.length > 0
    ? filteredRecordsData?.totalElements || 0
    : recordsData?.totalElements || 0;

  const isLoadingRecordsData = Object.keys(appliedFilters).length > 0 || searchText.length > 0
    ? isLoadingFiltered
    : isLoadingRecords;

  const recordsErrorData = Object.keys(appliedFilters).length > 0 || searchText.length > 0
    ? filteredError
    : recordsError;

  // Sort records (client-side for now)
  const sortedRecords = useMemo(() => {
    if (!sortField || records.length === 0) {
      return records;
    }

    return [...records].sort((a, b) => {
      const aValue = a.data[sortField];
      const bValue = b.data[sortField];

      if (aValue === null || aValue === undefined) return 1;
      if (bValue === null || bValue === undefined) return -1;

      let comparison = 0;
      if (typeof aValue === 'number' && typeof bValue === 'number') {
        comparison = aValue - bValue;
      } else {
        comparison = String(aValue).localeCompare(String(bValue));
      }

      return sortDirection === 'asc' ? comparison : -comparison;
    });
  }, [records, sortField, sortDirection]);

  const handleApplyFilters = () => {
    setAppliedFilters(filters);
    setPage(0); // Reset to first page
  };

  const handleClearFilters = () => {
    setFilters({});
    setAppliedFilters({});
    setSearchText('');
    setPage(0);
  };

  const handleSearchChange = (value: string) => {
    setSearchText(value);
    setPage(0);
  };

  const handleSort = (field: string) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const handleRowClick = (recordId: string) => {
    navigate(`/entities/${entityId}/records/${recordId}`);
  };

  if (isLoadingEntity) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (entityError) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">
          Failed to load entity definition: {entityError instanceof Error ? entityError.message : 'Unknown error'}
        </Alert>
      </Box>
    );
  }

  if (!entityDefinition) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="warning">Entity not found</Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ width: '100%', height: '100%' }}>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom color="text.primary">
            {entityDefinition.name}
          </Typography>
          {entityDefinition.description && (
            <Typography variant="body2" color="text.secondary">
              {entityDefinition.description}
            </Typography>
          )}
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => navigate(`/entities/${entityId}/create`)}
          sx={{ ml: 2 }}
        >
          Create New
        </Button>
      </Box>

      {/* Filter Panel */}
      {entityDefinition && (
        <FilterPanel
          entityDefinition={entityDefinition}
          filters={filters}
          onFiltersChange={setFilters}
          onApplyFilters={handleApplyFilters}
          onClearFilters={handleClearFilters}
        />
      )}

      {/* Search */}
      <Paper
        elevation={0}
        sx={{
          p: 2,
          mb: 2,
          backgroundColor: 'background.paper',
          borderRadius: 2,
          border: 1,
          borderColor: 'divider',
        }}
      >
        <TextField
          fullWidth
          placeholder="Search records..."
          value={searchText}
          onChange={(e) => handleSearchChange(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Search />
              </InputAdornment>
            ),
            endAdornment: searchText && (
              <InputAdornment position="end">
                <IconButton size="small" onClick={() => handleSearchChange('')}>
                  <Clear />
                </IconButton>
              </InputAdornment>
            ),
          }}
          size="small"
        />
      </Paper>

      {/* Records Table */}
      {isLoadingRecordsData ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
          <CircularProgress />
        </Box>
      ) : recordsErrorData ? (
        <Alert severity="error">
          Failed to load records: {recordsErrorData instanceof Error ? recordsErrorData.message : 'Unknown error'}
        </Alert>
      ) : (
        <EntityListTable
          entityDefinition={entityDefinition}
          records={sortedRecords}
          totalRecords={totalRecords}
          page={page}
          pageSize={pageSize}
          onPageChange={setPage}
          onPageSizeChange={setPageSize}
          sortField={sortField}
          sortDirection={sortDirection}
          onSort={handleSort}
          onRowClick={handleRowClick}
        />
      )}
    </Box>
  );
}
