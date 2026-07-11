import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import dts from 'vite-plugin-dts';
import { resolve } from 'path';

export default defineConfig({
  plugins: [
    vue(),
    dts({
      include: ['src'],
      outDir: 'dist',
      insertTypesEntry: true,
      tsconfigPath: './tsconfig.build.json',
    }),
  ],
  build: {
    lib: {
      entry: resolve(__dirname, 'src/index.ts'),
      name: 'DynamiaVue',
      formats: ['es', 'cjs'],
      fileName: (format) => (format === 'es' ? 'index.js' : 'index.cjs'),
    },
    rollupOptions: {
      external: ['vue', '@dynamia-tools/sdk', '@dynamia-tools/ui-core'],
      output: {
        globals: {
          vue: 'Vue',
          '@dynamia-tools/sdk': 'DynamiaSdk',
          '@dynamia-tools/ui-core': 'DynamiaUiCore',
        },
      },
    },
    sourcemap: true,
    minify: false,
  },
});
