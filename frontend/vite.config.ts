import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
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
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
  },
});
