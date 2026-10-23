<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const menuOpen = ref(false)

watch(() => route.fullPath, () => (menuOpen.value = false))

function signOut() {
  auth.logout()
  router.push({ name: 'catalog' })
}
</script>

<template>
  <header class="header">
    <div class="bar">
      <RouterLink to="/catalog" class="brand">
        <svg viewBox="0 0 32 32" aria-hidden="true" width="28" height="28">
          <circle cx="16" cy="16" r="14" fill="#fff" stroke="currentColor" stroke-width="3" />
          <path d="M2 16a14 14 0 0 1 28 0z" fill="var(--signal)" stroke="currentColor" stroke-width="3" />
          <circle cx="16" cy="16" r="4.5" fill="#fff" stroke="currentColor" stroke-width="3" />
        </svg>
        Pokédex Desk
      </RouterLink>

      <button
        class="menu-toggle button quiet small"
        :aria-expanded="menuOpen"
        aria-controls="primary-nav"
        @click="menuOpen = !menuOpen"
      >
        Menu
      </button>

      <nav id="primary-nav" class="nav" :class="{ open: menuOpen }" aria-label="Main">
        <RouterLink to="/catalog">Catalog</RouterLink>
        <RouterLink v-if="auth.isAuthenticated" to="/pokedex">My Pokédex</RouterLink>
        <RouterLink v-if="auth.isAdmin" to="/users">Users</RouterLink>

        <div class="account">
          <template v-if="auth.isAuthenticated">
            <span class="who">
              {{ auth.user.username }}<span v-if="auth.isAdmin" class="role">admin</span>
            </span>
            <button class="button small" @click="signOut">Sign out</button>
          </template>
          <template v-else>
            <RouterLink to="/login" class="button small">Sign in</RouterLink>
            <RouterLink to="/register" class="button small primary">Create account</RouterLink>
          </template>
        </div>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header {
  background: var(--surface);
  border-bottom: 1px solid var(--rule);
  position: sticky;
  top: 0;
  z-index: 5;
}
.bar {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--gutter);
  min-height: 64px;
  display: flex;
  align-items: center;
  gap: 24px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: var(--font-display);
  font-weight: 800;
  font-size: var(--step-1);
  text-decoration: none;
  white-space: nowrap;
}
.nav {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav > a {
  padding: 8px 12px;
  border-radius: var(--radius-s);
  text-decoration: none;
  font-weight: 500;
  color: var(--slate);
}
.nav > a:hover { color: var(--ink); background: var(--paper); }
.nav > a.router-link-active { color: var(--ink); box-shadow: inset 0 -3px 0 var(--signal); border-radius: 0; }
.account { margin-left: auto; display: flex; align-items: center; gap: 10px; }
.who { font-weight: 600; }
.role {
  margin-left: 8px;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--paper);
  color: var(--slate);
  font-size: var(--step--1);
  font-weight: 500;
}
.menu-toggle { display: none; margin-left: auto; }

@media (max-width: 760px) {
  .bar { flex-wrap: wrap; gap: 0; }
  .menu-toggle { display: inline-flex; }
  .nav {
    display: none;
    flex-basis: 100%;
    flex-direction: column;
    align-items: stretch;
    padding: 8px 0 16px;
  }
  .nav.open { display: flex; }
  .nav > a.router-link-active { box-shadow: inset 3px 0 0 var(--signal); }
  .account { margin: 12px 0 0; flex-wrap: wrap; }
}
</style>
