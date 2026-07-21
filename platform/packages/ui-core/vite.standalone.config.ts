import { defineConfig } from 'vite';
import { resolve } from 'path';

// Builds dist/dynamia-embed.global.js — a self-contained IIFE for plain
// `<script src="...">` consumption (no bundler, no `type="module"`, no build step).
// Runs *after* the main `vite build` (see package.json "build"); emptyOutDir stays
// false so it doesn't wipe the index.js/embed.js output from that pass.
export default defineConfig({
  build: {
    lib: {
      entry: resolve(__dirname, 'src/embed/auto.ts'),
      name: 'DynamiaEmbedStandalone',
      formats: ['iife'],
      fileName: () => 'dynamia-embed.global.js',
    },
    outDir: 'dist',
    emptyOutDir: false,
    sourcemap: true,
    minify: true,
  },
});
