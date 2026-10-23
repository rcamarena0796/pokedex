<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const username = ref('')
const password = ref('')
const error = ref(null)
const submitting = ref(false)

async function submit() {
  submitting.value = true
  error.value = null
  try {
    await auth.login(username.value, password.value)
    router.replace(typeof route.query.redirect === 'string' ? route.query.redirect : '/pokedex')
  } catch (e) {
    error.value = e.status === 401 ? 'That username and password do not match.' : e.message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page auth-page">
    <form class="panel auth-form" @submit.prevent="submit">
      <h1>Sign in</h1>
      <p v-if="route.query.expired" class="alert" role="status">Your session ended. Sign in again to continue.</p>
      <p v-if="error" class="alert" role="alert">{{ error }}</p>

      <div class="field">
        <label for="username">Username</label>
        <input id="username" v-model="username" class="input" autocomplete="username" required />
      </div>
      <div class="field">
        <label for="password">Password</label>
        <input id="password" v-model="password" class="input" type="password" autocomplete="current-password" required />
      </div>

      <button class="button primary" :disabled="submitting">{{ submitting ? 'Signing in…' : 'Sign in' }}</button>

      <p class="muted">
        No account? <RouterLink :to="{ name: 'register', query: route.query }">Create one</RouterLink>
      </p>
      <p class="demo muted">Demo accounts: <strong>admin</strong> / admin123 (admin) and <strong>ash</strong> / pikachu123.</p>
    </form>
  </div>
</template>

