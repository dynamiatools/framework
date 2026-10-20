import { DynamiaClient } from '@dynamia-tools/sdk';

// Served same-origin by the Spring Boot app (see DynamicalVueTemplate) — empty baseUrl means
// every SDK call resolves against the current origin. A standalone app (Vite dev server, another
// host) sets VITE_DYNAMIA_BASE_URL to the backend origin. `withCredentials` sends the session /
// DYNAMIA_JWT cookies set by POST /login/json (see src/login/Login.vue).
export const client = new DynamiaClient({
  baseUrl: import.meta.env.VITE_DYNAMIA_BASE_URL ?? '',
  withCredentials: true,
});
