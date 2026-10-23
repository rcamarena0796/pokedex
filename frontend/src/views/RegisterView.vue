<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const form = reactive({ username: '', email: '', password: '' })
const error = ref(null)
const serverErrors = ref({})
const submitting = ref(false)
const touched = ref(false)

// Same rules as the auth-service domain.
const clientErrors = computed(() => {
  const errors = {}
  if (!/^[a-z0-9_]{3,30}$/.test(form.username.trim().toLowerCase()))
    errors.username = 'Use 3–30 letters, digits or underscores.'
  if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(form.email.trim())) errors.email = 'Enter a valid email address.'
  if (form.password.length < 8 || form.password.length > 72 || !/[a-zA-Z]/.test(form.password) || !/\d/.test(form.password))
    errors.password = 'Use 8–72 characters with at least one letter and one digit.'
  return errors
})
const errorFor = (field) => (touched.value ? clientErrors.value[field] : null) ?? serverErrors.value[field]

async function submit() {
  touched.value = true
  if (Object.keys(clientErrors.value).length) return
  submitting.value = true
  error.value = null
  serverErrors.value = {}
  try {
    await auth.register({ ...form })
    router.replace(typeof route.query.redirect === 'string' ? route.query.redirect : '/pokedex')
  } catch (e) {
    serverErrors.value = e.fieldErrors ?? {}
    error.value = e.message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page auth-page">
    <form class="panel auth-form" novalidate @submit.prevent="submit">
      <h1>Create account</h1>
      <p v-if="error" class="alert" role="alert">{{ error }}</p>

      <div class="field">
        <label for="reg-username">Username</label>
        <input id="reg-username" v-model="form.username" class="input" autocomplete="username" :aria-invalid="!!errorFor('username')" />
        <span v-if="errorFor('username')" class="error">{{ errorFor('username') }}</span>
      </div>
      <div class="field">
        <label for="reg-email">Email</label>
        <input id="reg-email" v-model="form.email" class="input" type="email" autocomplete="email" :aria-invalid="!!errorFor('email')" />
        <span v-if="errorFor('email')" class="error">{{ errorFor('email') }}</span>
      </div>
      <div class="field">
        <label for="reg-password">Password</label>
        <input id="reg-password" v-model="form.password" class="input" type="password" autocomplete="new-password" :aria-invalid="!!errorFor('password')" />
        <span v-if="errorFor('password')" class="error">{{ errorFor('password') }}</span>
      </div>

      <button class="button primary" :disabled="submitting">{{ submitting ? 'Creating account…' : 'Create account' }}</button>
      <p class="muted">Already registered? <RouterLink :to="{ name: 'login', query: route.query }">Sign in</RouterLink></p>
    </form>
  </div>
</template>
