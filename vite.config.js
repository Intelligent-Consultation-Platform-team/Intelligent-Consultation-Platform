import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

const proxyPrefixes = [
  '/auth',
  '/departments',
  '/doctors',
  '/schedules',
  '/appointments',
  '/consultations',
  '/hospitalizations',
  '/notices',
  '/recharge',
  '/payment',
]

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const target = env.VITE_API_BASE_URL || 'http://localhost:8123'

  return {
    plugins: [vue()],
    server: {
      port: Number(env.VITE_DEV_PORT) || 3000,
      open: true,
      proxy: Object.fromEntries(
        proxyPrefixes.map((prefix) => [prefix, { target, changeOrigin: true }]),
      ),
    },
  }
})

