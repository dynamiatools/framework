import { DynamiaClient } from '@dynamia-tools/sdk'

// baseUrl is the backend's app origin (e.g. http://localhost:8080) -- SDK endpoints already
// include /api/... internally, don't append it here. Configure VITE_API_BASE_URL in .env
// (see .env.example) once your backend is running.
export const dynamiaClient = new DynamiaClient({
  baseUrl: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
})
