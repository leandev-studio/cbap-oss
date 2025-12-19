import { Box, Container, Typography, useTheme } from '@mui/material';

/**
 * Application Footer Component
 * 
 * Provides the footer with copyright and logo.
 */
export function Footer() {
  const currentYear = new Date().getFullYear();
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';

  return (
    <Box
      component="footer"
      sx={{
        py: 3,
        mt: 'auto',
        backgroundColor: 'background.paper',
        borderTop: 1,
        borderColor: 'divider',
      }}
    >
      <Box
        sx={{
          width: '100%',
          display: 'flex',
          px: { xs: 2, sm: '20px' }, // 20px margin on sides, matching header and main
        }}
      >
        <Box
          sx={{
            display: 'flex',
            flexDirection: { xs: 'column', sm: 'row' },
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: 2,
            width: '100%',
          }}
        >
          <Typography 
            variant="body2" 
            color="text.secondary"
            sx={{
              flexShrink: 0,
            }}
          >
            © {currentYear} CBAP OSS. All rights reserved.
          </Typography>
          
          <Box 
            sx={{ 
              display: 'flex', 
              alignItems: 'center',
              flexShrink: 0,
              ml: 'auto', // Push to the right
              backgroundColor: 'background.paper', // Match footer background
            }}
          >
            <Box
              component="img"
              src="/logo.png"
              alt="leandev.studio"
              sx={{
                height: '32px',
                width: 'auto',
                filter: isDark ? 'invert(1) brightness(1.2)' : 'none',
                transition: 'filter 0.3s ease',
                display: 'block',
                backgroundColor: 'transparent', // Ensure no background
                mixBlendMode: isDark ? 'normal' : 'normal', // Normal blending
              }}
            />
          </Box>
        </Box>
      </Box>
    </Box>
  );
}
