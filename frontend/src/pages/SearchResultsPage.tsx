import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Chip,
  Pagination,
  CircularProgress,
  Alert,
  Divider,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { searchService, SearchHit } from '../shared/services/searchService';

/**
 * Search Results Page
 * 
 * Displays search results grouped by entity type with pagination.
 */
export function SearchResultsPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const query = searchParams.get('q') || '';
  const entityId = searchParams.get('entity') || undefined;
  const page = parseInt(searchParams.get('page') || '0', 10);
  const size = 20;

  const { data: searchResult, isLoading, error } = useQuery({
    queryKey: ['search-results', query, entityId, page],
    queryFn: () => searchService.search(query, entityId || undefined, page, size),
    enabled: query.length >= 2,
  });

  // Group results by entity type
  const groupedResults = (searchResult?.hits || []).reduce((acc, hit) => {
    const entityId = hit.entityId;
    if (entityId) {
      if (!acc[entityId]) {
        acc[entityId] = [];
      }
      acc[entityId].push(hit);
    }
    return acc;
  }, {} as Record<string, SearchHit[]>);

  const handlePageChange = (_event: React.ChangeEvent<unknown>, value: number) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', (value - 1).toString());
    navigate(`/search?${params.toString()}`, { replace: true });
  };

  if (!query || query.length < 2) {
    return (
      <Box>
        <Typography variant="h5" gutterBottom>
          Search
        </Typography>
        <Alert severity="info">
          Enter at least 2 characters to search.
        </Alert>
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom>
          Search Results
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {searchResult ? (
            <>
              Found {searchResult.totalHits} result{searchResult.totalHits !== 1 ? 's' : ''} for "{query}"
            </>
          ) : (
            `Searching for "${query}"...`
          )}
        </Typography>
      </Box>

      {isLoading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      ) : error ? (
        <Alert severity="error">
          An error occurred while searching. Please try again.
        </Alert>
      ) : searchResult && searchResult.hits.length > 0 ? (
        <>
          {Object.entries(groupedResults).map(([entityId, hits], index) => (
            <Box key={entityId} sx={{ mb: 4 }}>
              {index > 0 && <Divider sx={{ my: 3 }} />}
              <Box sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                <Typography variant="h6">{entityId}</Typography>
                <Chip label={hits.length} size="small" />
              </Box>
              <Paper elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
                <List>
                  {hits.map((hit, hitIndex) => (
                    <ListItem
                      key={`${hit.entityId}-${hit.recordId}`}
                      disablePadding
                      divider={hitIndex < hits.length - 1}
                    >
                      <ListItemButton
                        component={Link}
                        to={`/entities/${hit.entityId}/records/${hit.recordId}`}
                      >
                        <ListItemText
                          primary={hit.displayValue || hit.data.name || hit.data.companyName || hit.data.title || hit.data.orderNumber || hit.recordId}
                          secondary={
                            <Box>
                              {hit.data.description && (
                                <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                                  {hit.data.description}
                                </Typography>
                              )}
                              <Typography variant="caption" color="text.secondary">
                                {hit.entityId} • {hit.recordId.substring(0, 8)}...
                              </Typography>
                            </Box>
                          }
                        />
                      </ListItemButton>
                    </ListItem>
                  ))}
                </List>
              </Paper>
            </Box>
          ))}

          {searchResult.totalHits > size && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
              <Pagination
                count={Math.ceil(searchResult.totalHits / size)}
                page={page + 1}
                onChange={handlePageChange}
                color="primary"
              />
            </Box>
          )}
        </>
      ) : (
        <Alert severity="info">
          No results found for "{query}". Try different keywords.
        </Alert>
      )}
    </Box>
  );
}
