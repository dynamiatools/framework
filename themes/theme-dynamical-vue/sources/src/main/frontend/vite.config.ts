import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import tailwindcss from '@tailwindcss/vite';
import { resolve } from 'path';

// Two independent entry points, mirroring the two static shells the Java side expects
// (see ../../java/.../DynamicalVueTemplate.java and scripts/copy-shell-views.mjs):
// - index.html → the authenticated app shell (sidebar/topbar/content/footer)
// - login.html → a small, separately-bundled login page (kept out of the authenticated
//   app's JS/CSS so unauthenticated visitors never download it)
export default defineConfig({
  base: '/',
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
  server: {
    proxy: {
      // Same-origin in production; only needed for `pnpm dev` against a locally running
      // Spring Boot backend (see README.md "Frontend development" section).
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/login': { target: 'http://localhost:8080', changeOrigin: true },
      '/logout': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});
