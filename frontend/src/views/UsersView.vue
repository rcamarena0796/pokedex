<script setup>
import { onMounted, ref } from 'vue'
import { useUsersStore } from '../stores/users'
import { useAuthStore } from '../stores/auth'

const usersStore = useUsersStore()
const auth = useAuthStore()
const notice = ref(null)

async function run(action, success) {
  notice.value = null
  try {
    await action()
    notice.value = { kind: 'success', text: success }
  } catch (e) {
    notice.value = { kind: 'error', text: e.message }
    await usersStore.load() // restore the real state (e.g. a role select that was reverted)
  }
}

const changeRole = (user, role) =>
  run(() => usersStore.changeRole(user.id, role), `${user.username} is now ${role === 'ADMIN' ? 'an admin' : 'a user'}.`)

function remove(user) {
  if (!window.confirm(`Delete the account “${user.username}”? This cannot be undone.`)) return
  run(() => usersStore.remove(user.id), `${user.username} was deleted.`)
}

const joined = (iso) => new Date(iso).toLocaleDateString()

onMounted(usersStore.load)
</script>

<template>
  <div class="page">
    <header class="page-head">
      <div>
        <h1>Users</h1>
        <p>Everyone who can sign in. Admins can remove Pokémon from the local Pokédex and manage accounts.</p>
      </div>
    </header>

    <p v-if="notice" class="alert" :class="{ success: notice.kind === 'success' }" role="status">{{ notice.text }}</p>
    <div v-if="usersStore.loading" class="loading-bar" role="progressbar" aria-label="Loading users" />
    <div v-if="usersStore.error" class="alert" role="alert">{{ usersStore.error.message }}</div>

    <div v-else class="table-wrap panel">
      <table>
        <thead>
          <tr><th scope="col">Username</th><th scope="col">Email</th><th scope="col">Role</th><th scope="col">Joined</th><th /></tr>
        </thead>
        <tbody>
          <tr v-for="user in usersStore.users" :key="user.id">
            <td class="strong">{{ user.username }}<span v-if="user.username === auth.user?.username" class="muted"> (you)</span></td>
            <td>{{ user.email }}</td>
            <td>
              <label :for="`role-${user.id}`" class="visually-hidden">Role for {{ user.username }}</label>
              <select
                :id="`role-${user.id}`"
                class="input"
                :value="user.role"
                :disabled="user.username === auth.user?.username"
                @change="changeRole(user, $event.target.value)"
              >
                <option value="USER">User</option>
                <option value="ADMIN">Admin</option>
              </select>
            </td>
            <td class="muted">{{ joined(user.createdAt) }}</td>
            <td class="end">
              <button
                class="button small danger"
                :disabled="user.username === auth.user?.username"
                @click="remove(user)"
              >
                Delete
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.alert { margin-bottom: 16px; }
.table-wrap { padding: 0; overflow-x: auto; }
table { width: 100%; border-collapse: collapse; min-width: 640px; }
th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid var(--rule); }
thead th { color: var(--slate); font-weight: 500; }
tbody tr:last-child td { border-bottom: 0; }
.strong { font-weight: 600; }
select.input { min-height: 36px; width: auto; }
.end { text-align: right; }
</style>
