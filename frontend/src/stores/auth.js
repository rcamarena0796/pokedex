import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '../api'

const STORAGE_KEY = 'pokedex.session'

function readSession() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) ?? null
  } catch {
    return null
  }
}

function writeSession(session) {
  try {
    if (session) localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
    else localStorage.removeItem(STORAGE_KEY)
  } catch {
    // Storage can be unavailable (private mode); the session then lasts for this tab only.
  }
}

export const useAuthStore = defineStore('auth', () => {
  const session = ref(readSession()) // { token, expiresAt, user }

  const isAuthenticated = computed(
    () => !!session.value && new Date(session.value.expiresAt).getTime() > Date.now(),
  )
  const user = computed(() => (isAuthenticated.value ? session.value.user : null))
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const token = computed(() => (isAuthenticated.value ? session.value.token : null))

  async function login(username, password) {
    const result = await authApi.login(username, password)
    session.value = { token: result.accessToken, expiresAt: result.expiresAt, user: result.user }
    writeSession(session.value)
  }

  async function register(payload) {
    await authApi.register(payload)
    await login(payload.username, payload.password)
  }

  function logout() {
    session.value = null
    writeSession(null)
  }

  return { user, isAuthenticated, isAdmin, token, login, register, logout }
})
