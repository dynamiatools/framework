import { defineConfig } from 'vite';
import { resolve } from 'path';
import { dynamiaViteConfig } from './vite-preset.js';

// Backend `pnpm dev` proxies to (override with DYNAMIA_BACKEND=http://localhost:8484 for another port).
//
// Two independent entry points, mirroring the two static shells the Java side expects
// (see ../../java/.../DynamicalVueTemplate.java and scripts/copy-shell-views.mjs):
// - index.html → the authenticated app shell (sidebar/topbar/content/footer)
// - login.html → a small, separately-bundled login page (kept out of the authenticated
//   app's JS/CSS so unauthenticated visitors never download it)
export default defineConfig(
  dynamiaViteConfig({
    input: {
      index: resolve(__dirname, 'index.html'),
      login: resolve(__dirname, 'login.html'),
    },
  }),
);
