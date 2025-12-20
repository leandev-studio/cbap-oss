import { useState, useMemo } from 'react';
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TableSortLabel,
  Checkbox,
  Typography,
  Chip,
  Box,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { EntityDefinition, PropertyDefinition } from '../shared/services/entityMetadataService';
import { EntityRecord, getRecord } from '../shared/services/entityRecordService';

interface EntityListTableProps {
  entityDefinition: EntityDefinition;
  records: EntityRecord[];
  totalRecords: number;
  page: number;
  pageSize: number;
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
  sortField: string | null;
  sortDirection: 'asc' | 'desc';
  onSort: (field: string) => void;
  onRowClick: (recordId: string) => void;
}

/**
 * Metadata-Driven Entity List Table
 * 
 * Generates table columns from property definitions and displays records.
 */
export function EntityListTable({
  entityDefinition,
  records,
  totalRecords,
  page,
  pageSize,
  onPageChange,
  onPageSizeChange,
  sortField,
  sortDirection,
  onSort,
  onRowClick,
}: EntityListTableProps) {
  const [selectedRows, setSelectedRows] = useState<Set<string>>(new Set());

  // Generate columns from entity metadata (listView configuration) or default
  const columns: Array<{
    id: string;
    label: string;
    property?: PropertyDefinition;
    sortable: boolean;
    width?: number;
    isReference?: boolean;
    isLineItems?: boolean;
    align?: 'left' | 'center' | 'right';
  }> = useMemo(() => {
    // Check if entity has listView configuration in metadata
    const listViewConfig = entityDefinition.metadataJson?.listView as any;
    const columnOrder = listViewConfig?.columnOrder as string[] | undefined;
    const columnConfig = listViewConfig?.columnConfig as Record<string, any> | undefined;

    if (columnOrder && Array.isArray(columnOrder)) {
      // Use metadata-driven column order
      const columns: Array<{
        id: string;
        label: string;
        property?: PropertyDefinition;
        sortable: boolean;
        width?: number;
        isReference?: boolean;
        isLineItems?: boolean;
        align?: 'left' | 'center' | 'right';
      }> = [
        { id: '_select', label: '', sortable: false, width: 50 },
      ];

      columnOrder.forEach((propName) => {
        const prop = entityDefinition.properties.find((p) => p.propertyName === propName);
        if (prop) {
          const config = columnConfig?.[propName] || {};
          // Determine alignment based on property type
          let align: 'left' | 'center' | 'right' | undefined;
          if (prop.propertyType === 'number' || prop.propertyType === 'calculated') {
            align = 'right';
          } else if (prop.propertyType === 'date') {
            align = 'center';
          }
          columns.push({
            id: propName,
            label: prop.label || propName,
            property: prop,
            sortable: prop.propertyType !== 'reference' && prop.propertyType !== 'calculated',
            isReference: prop.propertyType === 'reference',
            isLineItems: prop.metadataJson?.isDetailEntityArray === true || config.displayType === 'count',
            align,
          });
        }
      });

      columns.push({ id: 'createdAt', label: 'Created', sortable: true, width: 150, align: 'center' });
      return columns;
    }

    // Default: Show first 10 properties, prioritize non-calculated, non-reference fields
    const displayProperties = entityDefinition.properties
      .filter((prop) => {
        if (prop.propertyType === 'calculated') return false;
        if (prop.metadataJson?.isDetailEntityArray === true) return false; // Skip line items in default view
        return true;
      })
      .slice(0, 10);

    return [
      { id: '_select', label: '', sortable: false, width: 50 },
      ...displayProperties.map((prop) => {
        // Determine alignment based on property type
        let align: 'left' | 'center' | 'right' | undefined;
        if (prop.propertyType === 'number' || prop.propertyType === 'calculated') {
          align = 'right';
        } else if (prop.propertyType === 'date') {
          align = 'center';
        }
        return {
          id: prop.propertyName,
          label: prop.label || prop.propertyName,
          property: prop,
          sortable: prop.propertyType !== 'reference',
          isReference: prop.propertyType === 'reference',
          align,
        };
      }),
      { id: 'createdAt', label: 'Created', sortable: true, width: 150, align: 'center' as const },
    ];
  }, [entityDefinition]);

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      setSelectedRows(new Set(records.map((r) => r.recordId)));
    } else {
      setSelectedRows(new Set());
    }
  };

  const handleSelectRow = (recordId: string) => {
    const newSelected = new Set(selectedRows);
    if (newSelected.has(recordId)) {
      newSelected.delete(recordId);
    } else {
      newSelected.add(recordId);
    }
    setSelectedRows(newSelected);
  };

  const handleRowClick = (recordId: string, event: React.MouseEvent) => {
    // Don't navigate if clicking checkbox
    if ((event.target as HTMLElement).closest('input[type="checkbox"]')) {
      return;
    }
    onRowClick(recordId);
  };

  const renderCellValue = (
    property: PropertyDefinition | undefined,
    value: any,
    isReference?: boolean,
    isLineItems?: boolean
  ) => {
    if (value === null || value === undefined) {
      return <Typography variant="body2" color="text.secondary">—</Typography>;
    }

    if (!property) {
      // For createdAt or other non-property fields
      if (typeof value === 'string' && value.match(/^\d{4}-\d{2}-\d{2}/)) {
        try {
          const date = new Date(value);
          return date.toLocaleString('en-US', {
            month: 'short',
            day: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
          });
        } catch {
          return String(value);
        }
      }
      return String(value);
    }

    // Handle line items - show count
    if (isLineItems || property.metadataJson?.isDetailEntityArray === true) {
      const count = Array.isArray(value) ? value.length : 0;
      return (
        <Typography variant="body2">
          {count} item{count !== 1 ? 's' : ''}
        </Typography>
      );
    }

    // Handle reference fields (metadata-driven displayField)
    if (isReference || property.propertyType === 'reference') {
      const displayField = property.metadataJson?.displayField as string | undefined;
      if (property.referenceEntityId && displayField) {
        return (
          <ReferenceNameCell
            referenceEntityId={property.referenceEntityId}
            referenceId={String(value)}
            displayField={displayField}
          />
        );
      }
      // Fallback: show truncated ID
      return (
        <Typography variant="body2" sx={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>
          {String(value).substring(0, 8)}...
        </Typography>
      );
    }

    switch (property.propertyType) {
      case 'boolean':
        return (
          <Chip
            label={value ? 'Yes' : 'No'}
            size="small"
            color={value ? 'success' : 'default'}
            variant="outlined"
          />
        );
      case 'date':
        try {
          const date = new Date(value);
          return date.toLocaleDateString('en-US', {
            month: 'short',
            day: '2-digit',
            year: 'numeric',
          });
        } catch {
          return String(value);
        }
      case 'number':
        return typeof value === 'number' ? value.toLocaleString() : String(value);
      case 'singleSelect':
        return <Chip label={String(value)} size="small" variant="outlined" color="primary" />;
      case 'multiSelect':
        return Array.isArray(value) ? (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
            {value.slice(0, 2).map((item, idx) => (
              <Chip key={idx} label={String(item)} size="small" variant="outlined" />
            ))}
            {value.length > 2 && (
              <Typography variant="caption" color="text.secondary">
                +{value.length - 2}
              </Typography>
            )}
          </Box>
        ) : (
          String(value)
        );
      default:
        return String(value);
    }
  };

  // Reference Name Cell Component (metadata-driven)
  function ReferenceNameCell({ 
    referenceEntityId, 
    referenceId, 
    displayField 
  }: { 
    referenceEntityId: string; 
    referenceId: string; 
    displayField?: string;
  }) {
    const { data: referencedRecord } = useQuery({
      queryKey: ['entity-record', referenceEntityId, referenceId],
      queryFn: () => getRecord(referenceEntityId, referenceId),
      enabled: !!referenceId,
      staleTime: 5 * 60 * 1000, // Cache for 5 minutes
    });

    let displayValue: string;
    if (displayField && referencedRecord?.data?.[displayField]) {
      displayValue = String(referencedRecord.data[displayField]);
    } else {
      // Fallback to first string field or ID
      const firstStringField = Object.keys(referencedRecord?.data || {}).find(
        (key) => typeof referencedRecord?.data[key] === 'string' && referencedRecord.data[key]
      );
      displayValue = firstStringField 
        ? String(referencedRecord!.data[firstStringField])
        : referenceId.substring(0, 8) + '...';
    }
    
    return <Typography variant="body2">{displayValue}</Typography>;
  }

  return (
    <Paper
      elevation={0}
      sx={{
        backgroundColor: 'background.paper',
        borderRadius: 2,
        border: 1,
        borderColor: 'divider',
        overflow: 'hidden',
      }}
    >
      <TableContainer>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              {columns.map((column) => (
                <TableCell
                  key={column.id}
                  align={column.align || 'left'}
                  sx={{
                    backgroundColor: 'background.paper',
                    fontWeight: 600,
                    width: column.width,
                  }}
                >
                  {column.id === '_select' ? (
                    <Checkbox
                      indeterminate={selectedRows.size > 0 && selectedRows.size < records.length}
                      checked={records.length > 0 && selectedRows.size === records.length}
                      onChange={handleSelectAll}
                      size="small"
                    />
                  ) : column.sortable ? (
                    <TableSortLabel
                      active={sortField === column.id}
                      direction={sortField === column.id ? sortDirection : 'asc'}
                      onClick={() => onSort(column.id)}
                    >
                      {column.label}
                    </TableSortLabel>
                  ) : (
                    column.label
                  )}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {records.length === 0 ? (
              <TableRow>
                <TableCell colSpan={columns.length} align="center" sx={{ py: 4 }}>
                  <Typography variant="body2" color="text.secondary">
                    No records found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              records.map((record) => (
                <TableRow
                  key={record.recordId}
                  hover
                  onClick={(e) => handleRowClick(record.recordId, e)}
                  sx={{
                    cursor: 'pointer',
                    '&:hover': {
                      backgroundColor: 'action.hover',
                    },
                  }}
                >
                  {columns.map((column) => {
                    if (column.id === '_select') {
                      return (
                        <TableCell key={column.id} onClick={(e) => e.stopPropagation()}>
                          <Checkbox
                            checked={selectedRows.has(record.recordId)}
                            onChange={() => handleSelectRow(record.recordId)}
                            size="small"
                          />
                        </TableCell>
                      );
                    }

                    const property = column.property;
                    const value =
                      column.id === 'createdAt'
                        ? record.createdAt
                        : record.data[column.id];

                    return (
                      <TableCell key={column.id} align={column.align || 'left'}>
                        {renderCellValue(property, value, column.isReference, column.isLineItems)}
                      </TableCell>
                    );
                  })}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <TablePagination
        component="div"
        count={totalRecords}
        page={page}
        onPageChange={(_, newPage) => onPageChange(newPage)}
        rowsPerPage={pageSize}
        onRowsPerPageChange={(e) => onPageSizeChange(parseInt(e.target.value, 10))}
        rowsPerPageOptions={[10, 20, 50, 100]}
      />
    </Paper>
  );
}
