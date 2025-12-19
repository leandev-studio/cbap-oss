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
} from '@mui/material';
import { Search, Clear } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { getEntityById } from '../shared/services/entityMetadataService';
import { getRecords } from '../shared/services/entityRecordService';
import { EntityListTable } from './EntityListTable';

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

  // Fetch records
  const {
    data: recordsData,
    isLoading: isLoadingRecords,
    error: recordsError,
  } = useQuery({
    queryKey: ['entity-records', entityId, page, pageSize],
    queryFn: () => getRecords(entityId, page, pageSize),
    enabled: !!entityId,
  });

  // Filter records by search text (client-side for now)
  const filteredRecords = useMemo(() => {
    if (!recordsData?.records || !searchText) {
      return recordsData?.records || [];
    }

    const lowerSearch = searchText.toLowerCase();
    return recordsData.records.filter((record) => {
      // Search across all string/number fields in the data
      return Object.values(record.data).some((value) => {
        if (value === null || value === undefined) return false;
        return String(value).toLowerCase().includes(lowerSearch);
      });
    });
  }, [recordsData?.records, searchText]);

  // Sort records (client-side for now)
  const sortedRecords = useMemo(() => {
    if (!sortField || filteredRecords.length === 0) {
      return filteredRecords;
    }

    return [...filteredRecords].sort((a, b) => {
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
  }, [filteredRecords, sortField, sortDirection]);

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
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom color="text.primary">
          {entityDefinition.name}
        </Typography>
        {entityDefinition.description && (
          <Typography variant="body2" color="text.secondary">
            {entityDefinition.description}
          </Typography>
        )}
      </Box>

      {/* Search and Filters */}
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
          onChange={(e) => setSearchText(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Search />
              </InputAdornment>
            ),
            endAdornment: searchText && (
              <InputAdornment position="end">
                <IconButton size="small" onClick={() => setSearchText('')}>
                  <Clear />
                </IconButton>
              </InputAdornment>
            ),
          }}
          size="small"
        />
      </Paper>

      {/* Records Table */}
      {isLoadingRecords ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
          <CircularProgress />
        </Box>
      ) : recordsError ? (
        <Alert severity="error">
          Failed to load records: {recordsError instanceof Error ? recordsError.message : 'Unknown error'}
        </Alert>
      ) : (
        <EntityListTable
          entityDefinition={entityDefinition}
          records={sortedRecords}
          totalRecords={recordsData?.totalElements || 0}
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
