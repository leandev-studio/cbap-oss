import { Routes, Route, Navigate } from 'react-router-dom';
import { AppShell } from './app-shell/AppShell';
import { DashboardPage } from './dashboard-ui/DashboardPage';
import { EntitiesOverviewPage } from './metadata-ui/EntitiesOverviewPage';
import { EntityListPage } from './metadata-ui/EntityListPage';
import { EntityDetailPage } from './metadata-ui/EntityDetailPage';
import { EntityCreatePage } from './metadata-ui/EntityCreatePage';
import { LoginPage } from './pages/LoginPage';
import { SearchResultsPage } from './pages/SearchResultsPage';
import { TasksPage } from './pages/TasksPage';
import { EntityDefinitionsAdminPage } from './pages/EntityDefinitionsAdminPage';
import { UserManagementPage } from './pages/UserManagementPage';
import { RoleManagementPage } from './pages/RoleManagementPage';
import { WorkflowDefinitionEditorPage } from './pages/WorkflowDefinitionEditorPage';
import { MeasureDefinitionEditorPage } from './pages/MeasureDefinitionEditorPage';
import { SystemSettingsPage } from './pages/SystemSettingsPage';
import { LicensingStatusPage } from './pages/LicensingStatusPage';
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
        <Route
          path="search"
          element={
            <ProtectedRoute>
              <SearchResultsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="tasks"
          element={
            <ProtectedRoute>
              <TasksPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="admin/entity-definitions"
          element={
            <ProtectedRoute>
              <EntityDefinitionsAdminPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="admin/users"
          element={
            <ProtectedRoute>
              <UserManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="admin/roles"
          element={
            <ProtectedRoute>
              <RoleManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="admin/workflows"
          element={
            <ProtectedRoute>
              <WorkflowDefinitionEditorPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="admin/measures"
          element={
            <ProtectedRoute>
              <MeasureDefinitionEditorPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="admin/system/settings"
          element={
            <ProtectedRoute>
              <SystemSettingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="admin/system/licensing"
          element={
            <ProtectedRoute>
              <LicensingStatusPage />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
