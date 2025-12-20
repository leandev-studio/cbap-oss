import { Routes, Route, Navigate } from 'react-router-dom';
import { AppShell } from './app-shell/AppShell';
import { DashboardPage } from './dashboard-ui/DashboardPage';
import { EntitiesOverviewPage } from './metadata-ui/EntitiesOverviewPage';
import { EntityListPage } from './metadata-ui/EntityListPage';
import { EntityDetailPage } from './metadata-ui/EntityDetailPage';
import { EntityCreatePage } from './metadata-ui/EntityCreatePage';
import { LoginPage } from './pages/LoginPage';
import { ProtectedRoute } from './components/ProtectedRoute';

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<AppShell />}>
        <Route
          index
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="entities"
          element={
            <ProtectedRoute>
              <EntitiesOverviewPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="entities/:entityId"
          element={
            <ProtectedRoute>
              <EntityListPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="entities/:entityId/create"
          element={
            <ProtectedRoute>
              <EntityCreatePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="entities/:entityId/records/:recordId"
          element={
            <ProtectedRoute>
              <EntityDetailPage />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
