import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Collapse,
  Box,
  Typography,
  Divider,
  useTheme,
  useMediaQuery,
  IconButton,
} from '@mui/material';
import {
  Dashboard,
  TableChart,
  Settings,
  People,
  Security,
  Schema,
  Assignment,
  ExpandLess,
  ExpandMore,
  ChevronLeft,
  ChevronRight,
} from '@mui/icons-material';
import { getNavigation, NavigationItem } from '../shared/services/navigationService';
import { useQuery } from '@tanstack/react-query';

/**
 * Icon mapping for navigation items
 */
const iconMap: Record<string, React.ReactNode> = {
  Dashboard: <Dashboard />,
  TableChart: <TableChart />,
  Settings: <Settings />,
  People: <People />,
  Security: <Security />,
  Schema: <Schema />,
  Assignment: <Assignment />,
};

/**
 * Get icon component for navigation item
 */
function getIcon(iconName?: string): React.ReactNode {
  if (!iconName) return null;
  return iconMap[iconName] || null;
}

/**
 * Navigation Item Component
 */
interface NavigationItemComponentProps {
  item: NavigationItem;
  level?: number;
  onNavigate: (path: string) => void;
  currentPath: string;
}

interface NavigationItemComponentProps {
  item: NavigationItem;
  level?: number;
  onNavigate: (path: string) => void;
  currentPath: string;
  drawerOpen?: boolean;
}

function NavigationItemComponent({
  item,
  level = 0,
  onNavigate,
  currentPath,
  drawerOpen = true,
}: NavigationItemComponentProps) {
  const [open, setOpen] = useState(false);
  const hasChildren = item.children && item.children.length > 0;
  const isActive = item.routePath === currentPath;
  const isParentActive = item.children?.some(
    (child) => child.routePath === currentPath
  );

  useEffect(() => {
    // Auto-expand if current path is a child
    if (isParentActive) {
      setOpen(true);
    }
  }, [isParentActive]);

  const handleClick = () => {
    if (hasChildren) {
      setOpen(!open);
    } else if (item.routePath) {
      console.log('Navigating to:', item.routePath); // Debug log
      onNavigate(item.routePath);
    } else {
      console.warn('Navigation item has no routePath:', item); // Debug log
    }
  };

  return (
    <>
      <ListItem disablePadding sx={{ pl: drawerOpen ? level * 2 : 0 }}>
        <ListItemButton
          onClick={handleClick}
          selected={isActive}
          sx={{
            minHeight: 48,
            borderRadius: 1,
            mx: 1,
            mb: 0.5,
            justifyContent: drawerOpen ? 'flex-start' : 'center',
            px: drawerOpen ? 2 : 1,
            '&.Mui-selected': {
              backgroundColor: 'primary.main',
              color: 'primary.contrastText',
              '&:hover': {
                backgroundColor: 'primary.dark',
              },
              '& .MuiListItemIcon-root': {
                color: 'primary.contrastText',
              },
            },
          }}
          title={!drawerOpen ? item.label : undefined} // Tooltip when collapsed
        >
          <ListItemIcon
            sx={{
              minWidth: drawerOpen ? 40 : 0,
              color: isActive ? 'inherit' : 'text.secondary',
              justifyContent: 'center',
            }}
          >
            {getIcon(item.icon)}
          </ListItemIcon>
          {drawerOpen && (
            <>
              <ListItemText
                primary={item.label}
                primaryTypographyProps={{
                  variant: 'body2',
                  fontWeight: isActive ? 600 : 400,
                }}
              />
              {hasChildren && (open ? <ExpandLess /> : <ExpandMore />)}
            </>
          )}
        </ListItemButton>
      </ListItem>
      {hasChildren && drawerOpen && (
        <Collapse in={open} timeout="auto" unmountOnExit>
          <List component="div" disablePadding>
            {item.children.map((child) => (
              <NavigationItemComponent
                key={child.id}
                item={child}
                level={level + 1}
                onNavigate={onNavigate}
                currentPath={currentPath}
                drawerOpen={drawerOpen}
              />
            ))}
          </List>
        </Collapse>
      )}
    </>
  );
}

/**
 * Navigation Drawer Component
 * 
 * Displays a sidebar navigation drawer with metadata-driven menu items.
 * Supports collapsible sections and active route highlighting.
 */
interface NavigationDrawerProps {
  open: boolean;
  onClose: () => void;
  onToggle?: () => void;
  variant?: 'permanent' | 'persistent' | 'temporary';
}

export function NavigationDrawer({
  open,
  onClose,
  onToggle,
  variant = 'persistent',
}: NavigationDrawerProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  // Fetch navigation items
  const { data: navigationItems = [], isLoading } = useQuery({
    queryKey: ['navigation'],
    queryFn: getNavigation,
    staleTime: 5 * 60 * 1000, // Cache for 5 minutes
  });

  const handleNavigate = (path: string) => {
    navigate(path);
    // Close drawer on mobile after navigation
    if (isMobile) {
      onClose();
    }
  };

  // Group items by section
  const itemsBySection = navigationItems.reduce(
    (acc, item) => {
      const section = item.section || 'Main';
      if (!acc[section]) {
        acc[section] = [];
      }
      acc[section].push(item);
      return acc;
    },
    {} as Record<string, NavigationItem[]>
  );

  const drawerWidth = 280;
  const collapsedWidth = 64;

  const drawerContent = (
    <Box 
      sx={{ 
        width: open ? drawerWidth : collapsedWidth,
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        transition: 'width 0.3s ease',
      }}
    >
      <Box
        sx={{
          p: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: 1,
          borderColor: 'divider',
          minHeight: 64,
          flexShrink: 0,
        }}
      >
        {open && (
          <Typography variant="h6" component="div" sx={{ fontWeight: 600 }}>
            Navigation
          </Typography>
        )}
        <Box sx={{ display: 'flex', gap: 0.5, ml: 'auto' }}>
          {!isMobile && onToggle && (
            <IconButton 
              onClick={onToggle} 
              size="small"
              sx={{
                color: 'text.secondary',
                '&:hover': {
                  backgroundColor: 'action.hover',
                },
              }}
            >
              {open ? <ChevronLeft /> : <ChevronRight />}
            </IconButton>
          )}
          {isMobile && (
            <IconButton onClick={onClose} size="small">
              <ChevronLeft />
            </IconButton>
          )}
        </Box>
      </Box>

      <Box 
        sx={{ 
          flexGrow: 1,
          overflowY: 'auto',
          overflowX: 'hidden',
          py: 1,
          // Ensure scrollbar is visible and styled
          '&::-webkit-scrollbar': {
            width: '8px',
          },
          '&::-webkit-scrollbar-track': {
            backgroundColor: 'transparent',
          },
          '&::-webkit-scrollbar-thumb': {
            backgroundColor: (theme) => 
              theme.palette.mode === 'dark' 
                ? 'rgba(255, 255, 255, 0.2)' 
                : 'rgba(0, 0, 0, 0.2)',
            borderRadius: '4px',
            '&:hover': {
              backgroundColor: (theme) => 
                theme.palette.mode === 'dark' 
                  ? 'rgba(255, 255, 255, 0.3)' 
                  : 'rgba(0, 0, 0, 0.3)',
            },
          },
          // Firefox scrollbar
          scrollbarWidth: 'thin',
          ...(theme.palette.mode === 'dark' 
            ? { scrollbarColor: 'rgba(255, 255, 255, 0.2) transparent' }
            : { scrollbarColor: 'rgba(0, 0, 0, 0.2) transparent' }),
        }}
      >
        {isLoading ? (
          <Box sx={{ p: 2, textAlign: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Loading navigation...
            </Typography>
          </Box>
        ) : navigationItems.length === 0 ? (
          <Box sx={{ p: 2, textAlign: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              No navigation items available
            </Typography>
          </Box>
        ) : (
          Object.entries(itemsBySection).map(([section, items]) => (
            <Box key={section}>
              {section !== 'Main' && open && (
                <>
                  <Box sx={{ px: 2, py: 1 }}>
                    <Typography
                      variant="caption"
                      sx={{
                        fontWeight: 600,
                        color: 'text.secondary',
                        textTransform: 'uppercase',
                        letterSpacing: 1,
                      }}
                    >
                      {section}
                    </Typography>
                  </Box>
                  <Divider sx={{ mx: 2, mb: 1 }} />
                </>
              )}
              <List component="nav" disablePadding>
                {items.map((item) => (
                  <NavigationItemComponent
                    key={item.id}
                    item={item}
                    onNavigate={handleNavigate}
                    currentPath={location.pathname}
                    drawerOpen={open}
                  />
                ))}
              </List>
            </Box>
          ))
        )}
      </Box>
    </Box>
  );

  if (isMobile) {
    return (
      <Drawer
        variant="temporary"
        open={open}
        onClose={onClose}
        ModalProps={{
          keepMounted: true, // Better open performance on mobile
        }}
        sx={{
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
          },
        }}
      >
        {drawerContent}
      </Drawer>
    );
  }

  return (
    <Drawer
      variant={variant}
      open={true} // Always render, but control width via content
      sx={{
        width: open ? drawerWidth : collapsedWidth,
        flexShrink: 0,
        transition: 'width 0.3s ease',
        '& .MuiDrawer-paper': {
          width: open ? drawerWidth : collapsedWidth,
          boxSizing: 'border-box',
          borderRight: 1,
          borderColor: 'divider',
          position: 'relative',
          height: '100%',
          overflow: 'hidden',
          transition: 'width 0.3s ease',
        },
      }}
    >
      {drawerContent}
    </Drawer>
  );
}
