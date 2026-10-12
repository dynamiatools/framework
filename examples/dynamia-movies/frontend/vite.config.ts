import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// The public site is served by Vite in development; /api (the automatic REST API of DynamiaTools)
// is proxied to the backend so the browser sees a single origin.
export default defineConfig({
  plugins: [vue()],
  // The @dynamia-tools/* packages export a `development` condition that points at their TypeScript sources.
  // Those sources are not shipped to this app (only dist/), so resolve the built files in dev as well.
  resolve: { conditions: ['module', 'browser', 'production'] },
  server: {
    port: 5173,
    proxy: {
      '/api': { target: process.env.DYNAMIA_BACKEND ?? 'http://localhost:8484', changeOrigin: true },
    },
  },
})
