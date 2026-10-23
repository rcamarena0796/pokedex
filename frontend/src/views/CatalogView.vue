<script setup>
import { onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PokemonTile from '../components/PokemonTile.vue'
import PagerControls from '../components/PagerControls.vue'
import { useCatalogStore } from '../stores/catalog'

// User story 01: paginated catalog. The page number lives in the URL so it survives refresh and back.
const catalog = useCatalogStore()
const route = useRoute()
const router = useRouter()

const pageFromRoute = () => Math.max(0, (Number(route.query.page) || 1) - 1)

onMounted(() => catalog.loadPage(pageFromRoute()))
watch(() => route.query.page, () => catalog.loadPage(pageFromRoute()))

const goTo = (page) => router.push({ query: { page: page + 1 } })
</script>

<template>
  <div class="page">
    <header class="page-head">
      <div>
        <h1>Catalog</h1>
        <p>Every Pokémon in PokeAPI, with its category, weight and abilities. Open one to see stats and its evolution line.</p>
      </div>
      <p v-if="catalog.result" class="muted count">{{ catalog.result.totalItems.toLocaleString() }} entries</p>
    </header>

    <div v-if="catalog.loading" class="loading-bar" role="progressbar" aria-label="Loading Pokémon" />

    <div v-if="catalog.error" class="alert" role="alert">
      {{ catalog.error.message }}
      <button class="button small" @click="catalog.loadPage(catalog.currentPage)">Try again</button>
    </div>

    <ul v-else-if="catalog.result" class="grid" :aria-busy="catalog.loading">
      <li v-for="pokemon in catalog.result.items" :key="pokemon.id">
        <PokemonTile :pokemon="pokemon" />
      </li>
    </ul>

    <PagerControls
      v-if="catalog.result"
      :page="catalog.currentPage"
      :total-pages="catalog.result.totalPages"
      :disabled="catalog.loading"
      @change="goTo"
    />
  </div>
</template>

<style scoped>
.grid {
  list-style: none;
  margin: 16px 0 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.grid > li { display: flex; }
.grid > li > * { flex: 1; }
.count { font-variant-numeric: tabular-nums; }
.alert { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
</style>
