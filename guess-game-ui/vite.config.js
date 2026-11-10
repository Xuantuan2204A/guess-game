import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // Mọi request đến /api sẽ được proxy sang Spring Boot
      // → Trình duyệt chỉ thấy localhost:5173, không còn cross-origin
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
