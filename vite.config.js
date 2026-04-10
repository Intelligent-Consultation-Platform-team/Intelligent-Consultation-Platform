import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/auth': {
        target: 'http://localhost:8123',
        changeOrigin: true,
      },
      '/departments': {
        target: 'http://localhost:8123',
        changeOrigin: true,
      },
      '/doctors': {
        target: 'http://localhost:8123',
        changeOrigin: true,
      },
      '/schedules': {
        target: 'http://localhost:8123',
        changeOrigin: true,
      },
      '/appointments': {
        target: 'http://localhost:8123',
        changeOrigin: true,
      },
      '/consultations': {
        target: 'http://localhost:8123',
        changeOrigin: true,
      },
      '/hospitalizations': {
        target: 'http://localhost:8123',
        changeOrigin: true,
      },
      '/notices': {
        target: 'http://localhost:8123',
        changeOrigin: true,
      },
      '/recharge': {
        target: 'http://localhost:8123',
        changeOrigin: true,
      },
      '/payment': {
        target: 'http://localhost:8123',
        changeOrigin: true,
      },
    },
  },
});

