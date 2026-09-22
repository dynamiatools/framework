import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import tailwindcss from '@tailwindcss/vite';
import { resolve } from 'path';

// Backend `pnpm dev` proxies to (override with DYNAMIA_BACKEND=http://localhost:8484 for another port).
const backend = process.env.DYNAMIA_BACKEND ?? 'http://localhost:8080';

// Two independent entry points, mirroring the two static shells the Java side expects
// (see ../../java/.../DynamicalVueTemplate.java and scripts/copy-shell-views.mjs):
// - index.html → the authenticated app shell (sidebar/topbar/content/footer)
// - login.html → a small, separately-bundled login page (kept out of the authenticated
//   app's JS/CSS so unauthenticated visitors never download it)
export default defineConfig({
  base: '/',
  // @dynamia-tools/tailadmin-vue's layout components reference /images/logo/* and /images/user/*, which
  // it ships in its own public/ — serve that as ours instead of copying the assets into this repo.
  publicDir: resolve(__dirname, 'node_modules/@dynamia-tools/tailadmin-vue/public'),
  plugins: [
    vue({
      template: {
        compilerOptions: {
          // <dynamia-embed> is a real custom element (@dynamia-tools/ui-core/embed), not a
          // Vue component — tell the compiler to leave it alone instead of trying to resolve it.
          isCustomElement: tag => tag === 'dynamia-embed',
        },
      },
    }),
    tailwindcss(),
  ],
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        index: resolve(__dirname, 'index.html'),
        login: resolve(__dirname, 'login.html'),
      },
    },
  },
  // Dev only: keep the package (raw .vue/.ts source) out of Vite's dependency pre-bundling. Otherwise
  // a deep import like `.../composables/useSidebar` is pre-bundled into a second module instance,
  // distinct from the one the package's own components import, and provide/inject keys stop matching.
  optimizeDeps: { exclude: ['@dynamia-tools/tailadmin-vue'] },
  server: {
    proxy: {
      // Same-origin in production; only needed for `pnpm dev` against a locally running
      // Spring Boot backend (see README.md "Frontend development" section).
      '/api': { target: backend, changeOrigin: true },
      '/login': { target: backend, changeOrigin: true },
      '/logout': { target: backend, changeOrigin: true },
    },
  },
});
