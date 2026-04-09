import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // Todas las peticiones que empiecen con /api se redirigirán
      '/api': {
        target: 'http://localhost:8484', // La URL de tu Spring Boot
        changeOrigin: true,
        secure: false,
        // Opcional: si tu backend NO tiene el prefijo /api,
        // pero tu frontend sí lo usa para organizarse:
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
});

