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

## Environment Variables

The application uses environment variables for configuration. Create environment files based on your needs:

### Development

Create `.env.development`:
```bash
# Backend target for Vite dev proxy (avoids CORS)
VITE_DEV_PROXY_TARGET=http://localhost:8080
```

### Production

Create `.env.production`:
```bash
# For same-domain deployment (recommended)
VITE_API_BASE_URL=/api/v1

# OR for cross-domain deployment
VITE_API_BASE_URL=https://api.example.com/api/v1
```

### Environment Variable Reference

- **VITE_DEV_PROXY_TARGET**: Backend origin for **dev proxy** (frontend calls stay same-origin)
  - Example: `http://localhost:8080`
- **VITE_API_BASE_URL**: Backend API base URL (**production**)
  - Same domain: `/api/v1`
  - Cross-domain: `https://api.example.com/api/v1`

**Note**: All environment variables must be prefixed with `VITE_` to be accessible in the frontend code.

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
- Adds authentication tokens from localStorage/sessionStorage
- Propagates correlation IDs
- Handles common error cases (401 redirects to login)

Base URL is configured via `VITE_API_BASE_URL` environment variable.

## Development Proxy

In development, the frontend always calls `/api/v1/*` on the Vite dev server and Vite proxies `/api/*` to the backend server (default: `http://localhost:8080`).

Configure the backend host/port via:
- `VITE_DEV_PROXY_TARGET` (recommended)

## Production Deployment

For production deployment:

1. **Same Domain**: If frontend and backend are on the same domain:
   - Set `VITE_API_BASE_URL=/api/v1` (relative path)
   - Configure your web server to proxy `/api/*` to the backend

2. **Different Domains**: If frontend and backend are on different domains:
   - Set `VITE_API_BASE_URL=https://api.example.com/api/v1` (absolute URL)
   - Ensure CORS is properly configured on the backend
   - Ensure the backend allows requests from your frontend domain
