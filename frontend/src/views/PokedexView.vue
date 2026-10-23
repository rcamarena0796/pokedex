<script setup>
import { onMounted, ref } from 'vue'
import PagerControls from '../components/PagerControls.vue'
import TypeChip from '../components/TypeChip.vue'
import { usePokedexStore } from '../stores/pokedex'
import { useAuthStore } from '../stores/auth'
import { dexNumber, displayName } from '../utils/format'

// User stories 03 and 04: the local copy. Add from PokeAPI, edit our own fields, remove (admin).
const pokedex = usePokedexStore()
const auth = useAuthStore()

const searchText = ref(pokedex.search)
const addValue = ref('')
const rangeFrom = ref(1)
const rangeTo = ref(20)
const busy = ref(false)
const notice = ref(null) // { kind: 'success' | 'error', text }

let searchTimer
function onSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => pokedex.setSearch(searchText.value), 300)
}

async function run(action) {
  busy.value = true
  notice.value = null
  try {
    notice.value = { kind: 'success', text: await action() }
  } catch (e) {
    notice.value = { kind: 'error', text: e.message }
  } finally {
    busy.value = false
  }
}

const addOne = () =>
  run(async () => {
    const entry = await pokedex.add(addValue.value.trim())
    addValue.value = ''
    return `${displayName(entry.name)} is in your Pokédex.`
  })

const addRange = () =>
  run(async () => {
    const result = await pokedex.addRange(rangeFrom.value, rangeTo.value)
    const failed = Object.keys(result.failed).length
    return `Added ${result.created.length}, refreshed ${result.updated.length}` + (failed ? `, ${failed} not found.` : '.')
  })

async function removeEntry(entry) {
  if (!window.confirm(`Remove ${displayName(entry.name)} from the local Pokédex? Its local name, region and tags will be lost.`)) return
  await run(async () => {
    await pokedex.remove(entry.id)
    return `${displayName(entry.name)} was removed.`
  })
}

onMounted(() => pokedex.loadList())
</script>

<template>
  <div class="page">
    <header class="page-head">
      <div>
        <h1>My Pokédex</h1>
        <p>Pokémon copied into the local database. Give each one a local name, a region and tags for internal use.</p>
      </div>
    </header>

    <section class="panel add" aria-labelledby="add-title">
      <h2 id="add-title">Add from PokeAPI</h2>
      <form class="add-row" @submit.prevent="addOne">
        <div class="field grow">
          <label for="add-one">Name or Pokédex number</label>
          <input id="add-one" v-model="addValue" class="input" placeholder="charizard or 6" required maxlength="50" />
        </div>
        <button class="button primary" :disabled="busy || !addValue.trim()">Add Pokémon</button>
      </form>
      <form class="add-row" @submit.prevent="addRange">
        <div class="field">
          <label for="range-from">From number</label>
          <input id="range-from" v-model.number="rangeFrom" class="input" type="number" min="1" required />
        </div>
        <div class="field">
          <label for="range-to">To number</label>
          <input id="range-to" v-model.number="rangeTo" class="input" type="number" :min="rangeFrom" :max="rangeFrom + 49" required />
        </div>
        <button class="button" :disabled="busy">Add range</button>
        <p class="hint muted">Up to 50 at a time. Entries already here are refreshed and keep their local fields.</p>
      </form>
      <p v-if="notice" class="alert" :class="{ success: notice.kind === 'success' }" role="status">{{ notice.text }}</p>
    </section>

    <div class="toolbar">
      <div class="field search">
        <label for="search" class="visually-hidden">Search the Pokédex</label>
        <input
          id="search"
          v-model="searchText"
          class="input"
          type="search"
          placeholder="Search by name or local name"
          maxlength="50"
          @input="onSearch"
        />
      </div>
      <p class="muted">{{ pokedex.totalItems }} {{ pokedex.totalItems === 1 ? 'entry' : 'entries' }}</p>
    </div>

    <div v-if="pokedex.loading" class="loading-bar" role="progressbar" aria-label="Loading your Pokédex" />
    <div v-if="pokedex.error" class="alert" role="alert">{{ pokedex.error.message }}</div>

    <div v-else-if="!pokedex.loading && pokedex.items.length === 0" class="empty">
      <h2>{{ pokedex.search ? 'No matches' : 'Your Pokédex is empty' }}</h2>
      <p>{{ pokedex.search ? 'Try another name, or clear the search.' : 'Add a Pokémon by name or number above.' }}</p>
    </div>

    <ul v-else class="entries">
      <li v-for="entry in pokedex.items" :key="entry.id" class="entry">
        <img :src="entry.spriteUrl" alt="" class="sprite" width="72" height="72" loading="lazy" />
        <div class="identity">
          <span class="dex-number">{{ dexNumber(entry.id) }}</span>
          <RouterLink :to="{ name: 'detail', params: { idOrName: entry.name } }" class="name">
            {{ displayName(entry.name) }}
          </RouterLink>
          <span v-if="entry.localizedName" class="muted">{{ entry.localizedName }}</span>
        </div>
        <div class="meta">
          <div class="types"><TypeChip v-for="type in entry.types" :key="type" :type="type" /></div>
          <span v-if="entry.region" class="muted">{{ entry.region }}</span>
          <ul v-if="entry.tags.length" class="tags" aria-label="Tags">
            <li v-for="tag in entry.tags" :key="tag">{{ tag }}</li>
          </ul>
        </div>
        <div class="actions">
          <RouterLink :to="{ name: 'edit', params: { id: entry.id } }" class="button small">Edit</RouterLink>
          <button v-if="auth.isAdmin" class="button small danger" :disabled="busy" @click="removeEntry(entry)">
            Remove
          </button>
        </div>
      </li>
    </ul>

    <PagerControls
      :page="pokedex.page"
      :total-pages="pokedex.totalPages"
      :disabled="pokedex.loading"
      @change="pokedex.loadList"
    />
  </div>
</template>

<style scoped>
.add { display: grid; gap: 16px; margin-bottom: 32px; }
.add-row { display: flex; flex-wrap: wrap; align-items: flex-end; gap: 12px; }
.add-row .field { min-width: 120px; }
.add-row .grow { flex: 1; min-width: 220px; }
.add-row .hint { flex-basis: 100%; font-size: var(--step--1); }
.toolbar { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
.search { flex: 1; max-width: 420px; }
.entries { list-style: none; margin: 0; padding: 0; border-top: 1px solid var(--rule); }
.entry {
  display: grid;
  grid-template-columns: 72px minmax(160px, 1.2fr) 2fr auto;
  align-items: center;
  gap: 16px;
  padding: 12px 4px;
  border-bottom: 1px solid var(--rule);
}
.identity { display: grid; }
.identity .dex-number { font-size: var(--step-1); line-height: 1; }
.name { font-family: var(--font-display); font-weight: 700; font-size: var(--step-1); text-decoration: none; }
.name:hover { text-decoration: underline; }
.meta { display: flex; flex-wrap: wrap; align-items: center; gap: 8px 14px; }
.types { display: flex; gap: 6px; }
.tags { list-style: none; margin: 0; padding: 0; display: flex; flex-wrap: wrap; gap: 6px; }
.tags li {
  padding: 1px 8px;
  border: 1px solid var(--rule);
  border-radius: 999px;
  font-size: var(--step--1);
  background: var(--surface);
}
.actions { display: flex; gap: 8px; }

@media (max-width: 760px) {
  .entry { grid-template-columns: 56px 1fr; }
  .entry img { width: 56px; height: 56px; }
  .meta, .actions { grid-column: 2; }
}
</style>
