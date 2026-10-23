import { defineStore } from 'pinia'
import { ref } from 'vue'
import { pokedexApi } from '../api'

const PAGE_SIZE = 20

/** The local, editable Pokédex (user stories 03 and 04). */
export const usePokedexStore = defineStore('pokedex', () => {
  const items = ref([])
  const page = ref(0)
  const totalPages = ref(0)
  const totalItems = ref(0)
  const search = ref('')
  const loading = ref(false)
  const error = ref(null)

  async function loadList(nextPage = page.value) {
    loading.value = true
    error.value = null
    try {
      const data = await pokedexApi.list(nextPage, PAGE_SIZE, search.value.trim())
      items.value = data.items
      page.value = data.page
      totalPages.value = data.totalPages
      totalItems.value = data.totalItems
    } catch (e) {
      error.value = e
    } finally {
      loading.value = false
    }
  }

  function setSearch(value) {
    search.value = value
    return loadList(0)
  }

  /** Returns the stored entry, or null when this Pokémon has not been added yet. */
  async function find(id) {
    try {
      return await pokedexApi.get(id)
    } catch (e) {
      if (e.status === 404) return null
      throw e
    }
  }

  async function update(id, fields) {
    const updated = await pokedexApi.update(id, fields)
    replaceInList(updated)
    return updated
  }

  async function remove(id) {
    await pokedexApi.remove(id)
    await loadList(items.value.length === 1 && page.value > 0 ? page.value - 1 : page.value)
  }

  async function add(idOrName) {
    const entry = await pokedexApi.syncOne(idOrName)
    await loadList()
    return entry
  }

  async function addRange(fromId, toId) {
    const outcome = await pokedexApi.syncRange(fromId, toId)
    await loadList()
    return outcome
  }

  function replaceInList(entry) {
    const index = items.value.findIndex((item) => item.id === entry.id)
    if (index >= 0) items.value[index] = entry
  }

  return {
    items, page, totalPages, totalItems, search, loading, error,
    loadList, setSearch, find, update, remove, add, addRange,
  }
})
