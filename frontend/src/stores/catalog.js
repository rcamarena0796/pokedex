import { defineStore } from 'pinia'
import { ref } from 'vue'
import { catalogApi } from '../api'

export const PAGE_SIZE = 24

// Pages and details already fetched are kept in memory, so going back and forth
// between the grid and a detail view does not hit the API again.
export const useCatalogStore = defineStore('catalog', () => {
  const pages = new Map()
  const details = new Map()

  const currentPage = ref(0)
  const result = ref(null)
  const loading = ref(false)
  const error = ref(null)

  async function loadPage(page) {
    currentPage.value = page
    error.value = null
    if (pages.has(page)) {
      result.value = pages.get(page)
      return
    }
    loading.value = true
    try {
      const data = await catalogApi.page(page, PAGE_SIZE)
      pages.set(page, data)
      if (currentPage.value === page) result.value = data
    } catch (e) {
      error.value = e
    } finally {
      loading.value = false
    }
  }

  async function loadDetail(idOrName) {
    const key = String(idOrName).toLowerCase()
    if (!details.has(key)) {
      const detail = await catalogApi.detail(key)
      details.set(key, detail)
      details.set(String(detail.id), detail)
      details.set(detail.name, detail)
    }
    return details.get(key)
  }

  return { currentPage, result, loading, error, loadPage, loadDetail }
})
