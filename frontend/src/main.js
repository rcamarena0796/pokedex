import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { configureHttp } from './api/http'
import { useAuthStore } from './stores/auth'
import './styles.css'

const app = createApp(App)
app.use(createPinia())

const auth = useAuthStore()
configureHttp({
  tokenProvider: () => auth.token,
  // A rejected token means the session is over: clear it and ask the user to sign in again.
  unauthorizedHandler: () => {
    auth.logout()
    router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath, expired: '1' } })
  },
})

app.use(router)
app.mount('#app')
