import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Same routing as nginx in production: /api/<prefix> -> owning service, with /api stripped.
const strip = (path) => path.replace(/^\/api/, '')
const authService = { target: 'http://localhost:8081', changeOrigin: true, rewrite: strip }
const pokemonService = { target: 'http://localhost:8082', changeOrigin: true, rewrite: strip }

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api/auth': authService,
      '/api/users': authService,
      '/api/catalog': pokemonService,
      '/api/pokemon': pokemonService,
    },
  },
})
