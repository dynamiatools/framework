import { DynamiaClient } from '@dynamia-tools/sdk';
import { handleUnauthorized, loginUrl, withAuthGuard } from './auth.js';

// Served same-origin by the Spring Boot app (see DynamicalVueTemplate) — empty baseUrl means
// every SDK call resolves against the current origin. A standalone app (Vite dev server, another
// host) sets VITE_DYNAMIA_BASE_URL to the backend origin. `withCredentials` sends the session /
// DYNAMIA_JWT cookies set by POST /login/json (see src/login/Login.vue). A 401 sends the user to the
// login page, see lib/auth.ts.
export const client = new DynamiaClient({
  baseUrl: import.meta.env.VITE_DYNAMIA_BASE_URL ?? '',
  withCredentials: true,
  fetch: withAuthGuard(
    (input, init) => globalThis.fetch(input, init),
    pathname => pathname === loginUrl(),
    handleUnauthorized,
  ),
});
