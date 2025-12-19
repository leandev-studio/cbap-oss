import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // Load env file based on `mode` in the current working directory.
  const env = loadEnv(mode, process.cwd(), '');
  const devProxyTarget = env.VITE_DEV_PROXY_TARGET || 'http://localhost:8080';
  
  return {
    plugins: [react()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
        '@app-shell': path.resolve(__dirname, './src/app-shell'),
        '@metadata-ui': path.resolve(__dirname, './src/metadata-ui'),
        '@workflow-ui': path.resolve(__dirname, './src/workflow-ui'),
        '@document-ui': path.resolve(__dirname, './src/document-ui'),
        '@dashboard-ui': path.resolve(__dirname, './src/dashboard-ui'),
        '@shared': path.resolve(__dirname, './src/shared'),
      },
    },
    server: {
      port: 3000,
      proxy: {
        '/api': {
          // DEV ONLY: proxy API calls to backend to avoid CORS.
          // Configure backend host/port via VITE_DEV_PROXY_TARGET (e.g. http://localhost:8080).
          target: devProxyTarget,
          changeOrigin: true,
          secure: false, // Allow self-signed certificates in development
        },
      },
    },
    build: {
      outDir: 'dist',
      sourcemap: true,
    },
  };
});
