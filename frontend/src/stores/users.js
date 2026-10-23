import { defineStore } from 'pinia'
import { ref } from 'vue'
import { usersApi } from '../api'

export const useUsersStore = defineStore('users', () => {
  const users = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function load() {
    loading.value = true
    error.value = null
    try {
      users.value = await usersApi.list()
    } catch (e) {
      error.value = e
    } finally {
      loading.value = false
    }
  }

  async function changeRole(id, role) {
    const updated = await usersApi.changeRole(id, role)
    users.value = users.value.map((u) => (u.id === id ? updated : u))
  }

  async function remove(id) {
    await usersApi.remove(id)
    users.value = users.value.filter((u) => u.id !== id)
  }

  return { users, loading, error, load, changeRole, remove }
})
