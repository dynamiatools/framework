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
      name: 'DynamiaReportsVue',
      formats: ['es', 'cjs'],
      fileName: (format) => (format === 'es' ? 'index.js' : 'index.cjs'),
    },
    rollupOptions: {
      external: [
        'vue',
        'chart.js',
        'chart.js/auto',
        '@dynamia-tools/sdk',
        '@dynamia-tools/vue',
        '@dynamia-tools/reports-sdk',
      ],
      output: {
        globals: {
          vue: 'Vue',
          'chart.js/auto': 'Chart',
          '@dynamia-tools/sdk': 'DynamiaSdk',
          '@dynamia-tools/vue': 'DynamiaVue',
          '@dynamia-tools/reports-sdk': 'DynamiaReportsSdk',
        },
      },
    },
    sourcemap: true,
    minify: false,
  },
});
