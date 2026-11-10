import vue from '@vitejs/plugin-vue';
import { dirname, resolve } from 'path';
import { fileURLToPath } from 'url';
import { defineConfig } from 'vite';

const here = dirname(fileURLToPath(import.meta.url));

/** Builds the standalone editor page into dist-page/, with relative URLs so any path can serve it. */
export default defineConfig({
  root: resolve(here, 'page'),
  base: './',
  plugins: [vue()],
  // the page bundles the engine from its sources, so it does not need the engine package to be built first
  resolve: { alias: { '@dynamia-tools/template-engine': resolve(here, '../../../../platform/packages/template-engine/src/index.ts') } },
  build: { outDir: resolve(here, 'dist-page'), emptyOutDir: true, sourcemap: false, chunkSizeWarningLimit: 2500 },
});
