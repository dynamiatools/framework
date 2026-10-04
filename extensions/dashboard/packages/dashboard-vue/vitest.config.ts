import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  resolve: { conditions: ['development'] },
  test: {
    globals: false,
    environment: 'happy-dom',
  },
});
