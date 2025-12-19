import { Routes, Route, Navigate } from 'react-router-dom';
import { AppShell } from './app-shell/AppShell';
import { LandingPage } from './pages/LandingPage';

function App() {
  return (
    <Routes>
      <Route path="/" element={<AppShell />}>
        <Route index element={<LandingPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
