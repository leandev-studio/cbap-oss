# CBAP Frontend

React/TypeScript frontend for the Composable Business Application Platform.

## Tech Stack

- **React 18+** with **TypeScript**
- **Vite** for build tooling
- **Material UI (MUI)** for components
- **React Query** for server state
- **React Router** for routing
- **React Hook Form** for forms
- **i18next** for internationalization

## Getting Started

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

The app will be available at `http://localhost:3000`.

## Building

```bash
# Production build
npm run build

# Preview production build
npm run preview
```

## Project Structure

```
src/
├── app-shell/        # Application shell and navigation
├── metadata-ui/      # Generated forms and lists
├── workflow-ui/      # Tasks and approvals
├── document-ui/      # Controlled documents
├── dashboard-ui/     # Dashboards and widgets
└── shared/          # Common components and utilities
```

## Theming

The application uses soft/pastel color schemes defined in `COLOR_GUIDE.md`. Theme configuration is in `src/shared/theme.ts`.

## API Integration

API client is configured in `src/shared/api/client.ts`. It automatically:
- Adds authentication tokens
- Propagates correlation IDs
- Handles common error cases

Base URL: `/api/v1` (proxied to backend in development)
