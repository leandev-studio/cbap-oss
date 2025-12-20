import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  TextField,
  InputAdornment,
  IconButton,
  Paper,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Typography,
  CircularProgress,
} from '@mui/material';
import { Search as SearchIcon, Clear as ClearIcon } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { searchService, SearchHit } from '../shared/services/searchService';

/**
 * Global Search Bar Component
 * 
 * Provides a search input in the header with autocomplete and quick navigation.
 */
export function GlobalSearchBar() {
  const [query, setQuery] = useState('');
  const [open, setOpen] = useState(false);
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const searchRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  // Debounce search query
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedQuery(query);
    }, 300);

    return () => clearTimeout(timer);
  }, [query]);

  // Fetch search results
  const { data: searchResult, isLoading } = useQuery({
    queryKey: ['global-search', debouncedQuery],
    queryFn: () => searchService.search(debouncedQuery, undefined, 0, 5),
    enabled: debouncedQuery.length >= 2,
    staleTime: 30000,
  });

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setQuery(event.target.value);
    setOpen(true);
  };

  const handleClear = () => {
    setQuery('');
    setOpen(false);
  };

  const handleSearchSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (query.trim()) {
      navigate(`/search?q=${encodeURIComponent(query.trim())}`);
      setOpen(false);
    }
  };

  const handleResultClick = (hit: SearchHit) => {
    navigate(`/entities/${hit.entityId}/records/${hit.recordId}`);
    setOpen(false);
    setQuery('');
  };

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

  return (
    <Box
      ref={searchRef}
      sx={{
        position: 'relative',
        maxWidth: { xs: '200px', sm: '300px', md: '350px' },
        minWidth: { xs: '150px', sm: '200px' },
      }}
    >
      <form onSubmit={handleSearchSubmit}>
        <TextField
          fullWidth
          size="small"
          placeholder="Search..."
          value={query}
          onChange={handleSearchChange}
          onFocus={() => setOpen(true)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon fontSize="small" color="action" />
              </InputAdornment>
            ),
            endAdornment: query && (
              <InputAdornment position="end">
                <IconButton
                  size="small"
                  onClick={handleClear}
                  edge="end"
                  sx={{ p: 0.5 }}
                >
                  <ClearIcon fontSize="small" />
                </IconButton>
              </InputAdornment>
            ),
          }}
          sx={{
            backgroundColor: 'background.paper',
            borderRadius: 1,
            '& .MuiOutlinedInput-root': {
              '& fieldset': {
                borderColor: 'divider',
              },
              '&:hover fieldset': {
                borderColor: 'primary.main',
              },
            },
          }}
        />
      </form>

      {/* Search Results Dropdown */}
      {open && (query.length >= 2 || searchResult) && (
        <Paper
          elevation={4}
          sx={{
            position: 'absolute',
            top: '100%',
            left: 0,
            right: 0,
            mt: 1,
            maxHeight: '400px',
            overflow: 'auto',
            zIndex: 1300,
          }}
        >
          {isLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
              <CircularProgress size={24} />
            </Box>
          ) : searchResult && searchResult.hits.length > 0 ? (
            <List dense>
              {Object.entries(groupedResults).map(([entityId, hits]) => (
                <Box key={entityId}>
                  <ListItem>
                    <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
                      {entityId} ({hits.length})
                    </Typography>
                  </ListItem>
                  {hits.map((hit) => (
                    <ListItemButton
                      key={`${hit.entityId}-${hit.recordId}`}
                      onClick={() => handleResultClick(hit)}
                    >
                      <ListItemText
                        primary={hit.displayValue || hit.data.name || hit.data.companyName || hit.data.title || hit.data.orderNumber || hit.recordId}
                        secondary={hit.data.description || hit.data.notes || ''}
                        primaryTypographyProps={{
                          noWrap: true,
                        }}
                        secondaryTypographyProps={{
                          noWrap: true,
                        }}
                      />
                    </ListItemButton>
                  ))}
                </Box>
              ))}
              {searchResult.totalHits > searchResult.hits.length && (
                <ListItemButton
                  onClick={() => {
                    navigate(`/search?q=${encodeURIComponent(query.trim())}`);
                    setOpen(false);
                  }}
                >
                  <ListItemText
                    primary={`View all ${searchResult.totalHits} results`}
                    primaryTypographyProps={{
                      color: 'primary',
                      fontWeight: 500,
                    }}
                  />
                </ListItemButton>
              )}
            </List>
          ) : query.length >= 2 ? (
            <Box sx={{ p: 2, textAlign: 'center' }}>
              <Typography variant="body2" color="text.secondary">
                No results found
              </Typography>
            </Box>
          ) : null}
        </Paper>
      )}
    </Box>
  );
}
