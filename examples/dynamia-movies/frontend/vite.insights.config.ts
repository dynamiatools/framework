import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'

// Builds the dashboard + reports pages of the backoffice as a single ES module (`insights.js` + `insights.css`)
// straight into the backend's static resources. The Dynamical Vue theme embeds non-CRUD pages with
// <dynamia-embed>, and a JS embed is mounted in the host page (same origin, so the login cookies are sent),
// unlike an HTML embed, which runs in a sandboxed iframe.
export default defineConfig({
  plugins: [vue()],
  publicDir: false,
  define: { 'process.env.NODE_ENV': JSON.stringify('production') },
  build: {
    outDir: fileURLToPath(new URL('../backend/src/main/resources/static/insights', import.meta.url)),
    emptyOutDir: true,
    cssCodeSplit: false,
    lib: {
      entry: fileURLToPath(new URL('./insights/element.ts', import.meta.url)),
      formats: ['es'],
      fileName: () => 'insights.js',
      cssFileName: 'insights',
    },
    rollupOptions: { output: { inlineDynamicImports: true } },
  },
})
