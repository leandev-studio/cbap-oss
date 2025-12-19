import { Routes, Route } from 'react-router-dom';
import { Box } from '@mui/material';
import { AppShell } from './app-shell/AppShell';

function App() {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Routes>
        <Route path="/*" element={<AppShell />} />
      </Routes>
    </Box>
  );
}

export default App;
