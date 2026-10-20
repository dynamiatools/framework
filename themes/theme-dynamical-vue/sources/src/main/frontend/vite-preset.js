import { createRequire } from 'node:module';
import { dirname, resolve } from 'node:path';
import vue from '@vitejs/plugin-vue';
import tailwindcss from '@tailwindcss/vite';

const require = createRequire(import.meta.url);

/**
 * Vite config shared by every app built on this theme: Vue + Tailwind, TailAdmin assets, the
 * `dynamia-embed` custom element and the dev proxy to the Dynamia backend.
 *
 * Plain JavaScript on purpose: Vite loads it from node_modules, where Node does not strip TypeScript.
 *
 * @param {import('./vite-preset.js').DynamiaViteOptions} [options]
 * @returns {import('vite').UserConfig}
 */
export function dynamiaViteConfig(options = {}) {
  const backend = options.backend ?? process.env.DYNAMIA_BACKEND ?? 'http://localhost:8080';
  // @dynamia-tools/tailadmin-vue's layout components reference /images/logo/* and /images/user/*, which it
  // ships in its own public/ — serve that as ours instead of copying the assets into every app.
  const tailadminPublic = resolve(dirname(require.resolve('@dynamia-tools/tailadmin-vue/package.json')), 'public');

  return {
    base: '/',
    publicDir: tailadminPublic,
    plugins: [
      vue({
        template: {
          compilerOptions: {
            // <dynamia-embed> is a real custom element (@dynamia-tools/ui-core/embed), not a Vue
            // component — tell the compiler to leave it alone instead of trying to resolve it.
            isCustomElement: tag => tag === 'dynamia-embed',
          },
        },
      }),
      tailwindcss(),
    ],
    build: {
      outDir: 'dist',
      emptyOutDir: true,
      ...(options.input ? { rollupOptions: { input: options.input } } : {}),
    },
    // Dev only: keep the packages (raw .vue/.ts source) out of Vite's dependency pre-bundling. Otherwise a
    // deep import like `.../composables/useSidebar` is pre-bundled into a second module instance, distinct
    // from the one the package's own components import, and provide/inject keys stop matching.
    optimizeDeps: { exclude: ['@dynamia-tools/tailadmin-vue', '@dynamia-tools/theme-dynamical-vue'] },
    server: {
      proxy: {
        // Same-origin in production; only needed against a locally running Spring Boot backend.
        '/api': { target: backend, changeOrigin: true },
        // The SDK's local endpoints (metadata, navigation, dashboards) live under /apps/api.
        '/apps': { target: backend, changeOrigin: true },
        '/login': { target: backend, changeOrigin: true },
        '/logout': { target: backend, changeOrigin: true },
      },
    },
  };
}
