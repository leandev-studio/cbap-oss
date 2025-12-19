import { useState } from 'react';
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
} from '@mui/material';
import { EntityDefinition, PropertyDefinition } from '../shared/services/entityMetadataService';
import { EntityRecord } from '../shared/services/entityRecordService';

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

  // Generate columns from property definitions
  // Show first 10 properties by default, prioritize non-calculated, non-reference fields
  const displayProperties = entityDefinition.properties
    .filter((prop) => prop.propertyType !== 'calculated' && prop.propertyType !== 'reference')
    .slice(0, 10);

  // Always include recordId and createdAt
  const columns: Array<{
    id: string;
    label: string;
    property?: PropertyDefinition;
    sortable: boolean;
    width?: number;
  }> = [
    { id: '_select', label: '', sortable: false, width: 50 },
    ...displayProperties.map((prop) => ({
      id: prop.propertyName,
      label: prop.label || prop.propertyName,
      property: prop,
      sortable: true,
    })),
    { id: 'createdAt', label: 'Created', sortable: true, width: 150 },
  ];

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

  const renderCellValue = (property: PropertyDefinition | undefined, value: any) => {
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
      case 'multiSelect':
        return Array.isArray(value) ? value.join(', ') : String(value);
      default:
        return String(value);
    }
  };

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

                    const property = 'property' in column ? column.property : undefined;
                    const value =
                      column.id === 'createdAt'
                        ? record.createdAt
                        : record.data[column.id];

                    return (
                      <TableCell key={column.id}>
                        {renderCellValue(property, value)}
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
