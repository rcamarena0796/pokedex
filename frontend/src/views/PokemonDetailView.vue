<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import TypeChip from '../components/TypeChip.vue'
import StatBars from '../components/StatBars.vue'
import EvolutionTree from '../components/EvolutionTree.vue'
import { useCatalogStore } from '../stores/catalog'
import { usePokedexStore } from '../stores/pokedex'
import { useAuthStore } from '../stores/auth'
import { dexNumber, displayName, formatKg, formatM, humanize } from '../utils/format'

// User story 02: detail view. Signed-in users also see (or create) the local Pokédex entry.
const props = defineProps({ idOrName: { type: String, required: true } })

const catalog = useCatalogStore()
const pokedex = usePokedexStore()
const auth = useAuthStore()
const router = useRouter()

const pokemon = ref(null)
const local = ref(undefined) // undefined = not checked, null = not in the local Pokédex
const error = ref(null)
const loading = ref(false)
const adding = ref(false)

const tint = computed(() => `var(--type-${pokemon.value?.types[0] ?? 'normal'}, var(--type-normal))`)

async function load() {
  loading.value = true
  error.value = null
  pokemon.value = null
  local.value = undefined
  try {
    pokemon.value = await catalog.loadDetail(props.idOrName)
    if (auth.isAuthenticated) local.value = await pokedex.find(pokemon.value.id)
  } catch (e) {
    error.value = e
  } finally {
    loading.value = false
  }
}

async function addToPokedex() {
  adding.value = true
  try {
    local.value = await pokedex.add(pokemon.value.id)
  } catch (e) {
    error.value = e
  } finally {
    adding.value = false
  }
}

watch(() => props.idOrName, load, { immediate: true })
</script>

<template>
  <div class="page">
    <RouterLink to="/catalog" class="back muted">Back to catalog</RouterLink>

    <div v-if="loading" class="loading-bar" role="progressbar" aria-label="Loading Pokémon" />

    <div v-if="error" class="alert" role="alert">
      <template v-if="error.status === 404">No Pokémon called “{{ idOrName }}” exists in PokeAPI.</template>
      <template v-else>{{ error.message }}</template>
    </div>

    <article v-if="pokemon" class="detail" :style="{ '--tint': tint }">
      <header class="hero">
        <div class="art">
          <img :src="pokemon.imageUrl" :alt="`${displayName(pokemon.name)} artwork`" width="320" height="320" />
        </div>
        <div class="intro">
          <span class="dex-number big">{{ dexNumber(pokemon.id) }}</span>
          <h1>{{ displayName(pokemon.name) }}</h1>
          <p class="category">{{ pokemon.category }}</p>
          <div class="types">
            <TypeChip v-for="type in pokemon.types" :key="type" :type="type" />
          </div>
          <p v-if="pokemon.description" class="description">{{ pokemon.description }}</p>
          <dl class="facts">
            <div><dt>Height</dt><dd>{{ formatM(pokemon.heightM) }}</dd></div>
            <div><dt>Weight</dt><dd>{{ formatKg(pokemon.weightKg) }}</dd></div>
            <div><dt>Abilities</dt><dd>{{ pokemon.abilities.map(humanize).join(', ') }}</dd></div>
          </dl>
        </div>
      </header>

      <div class="columns">
        <section class="panel" aria-labelledby="stats-title">
          <h2 id="stats-title">Base stats</h2>
          <StatBars :stats="pokemon.stats" :tint="tint" />
        </section>

        <section class="panel local" aria-labelledby="local-title">
          <h2 id="local-title">In your Pokédex</h2>
          <template v-if="!auth.isAuthenticated">
            <p class="muted">Sign in to keep this Pokémon in your own Pokédex and add a local name, region and tags.</p>
            <RouterLink :to="{ name: 'login', query: { redirect: $route.fullPath } }" class="button">Sign in</RouterLink>
          </template>
          <template v-else-if="local === null">
            <p class="muted">Not added yet. Adding it copies the PokeAPI data into the local database.</p>
            <button class="button primary" :disabled="adding" @click="addToPokedex">
              {{ adding ? 'Adding…' : 'Add to my Pokédex' }}
            </button>
          </template>
          <template v-else-if="local">
            <dl class="facts">
              <div><dt>Local name</dt><dd>{{ local.localizedName ?? '—' }}</dd></div>
              <div><dt>Region</dt><dd>{{ local.region ?? '—' }}</dd></div>
              <div><dt>Tags</dt><dd>{{ local.tags.length ? local.tags.join(', ') : '—' }}</dd></div>
              <div v-if="local.notes"><dt>Notes</dt><dd>{{ local.notes }}</dd></div>
            </dl>
            <button class="button" @click="router.push({ name: 'edit', params: { id: local.id } })">Edit details</button>
          </template>
        </section>
      </div>

      <section v-if="pokemon.evolutionChain" class="panel" aria-labelledby="evo-title">
        <h2 id="evo-title">Evolution line</h2>
        <div class="evolution">
          <EvolutionTree :node="pokemon.evolutionChain" :current="pokemon.id" />
        </div>
      </section>
    </article>
  </div>
</template>

<style scoped>
.back { display: inline-block; margin-bottom: 20px; text-decoration: none; }
.back:hover { text-decoration: underline; }
.detail { display: grid; gap: 24px; }
.hero {
  display: grid;
  grid-template-columns: minmax(220px, 360px) 1fr;
  gap: 32px;
  align-items: center;
}
.art {
  aspect-ratio: 1;
  border-radius: var(--radius-m);
  background: color-mix(in srgb, var(--tint) 16%, var(--surface));
  display: grid;
  place-items: center;
}
.art img { width: 86%; height: auto; }
.intro { display: grid; gap: 10px; }
.dex-number.big { font-size: var(--step-5); line-height: 0.9; color: color-mix(in srgb, var(--tint) 65%, var(--ink)); }
.category { color: var(--slate); font-size: var(--step-1); }
.types { display: flex; gap: 8px; }
.description { max-width: 60ch; font-size: var(--step-1); line-height: 1.5; }
.facts { margin: 0; display: grid; gap: 6px; }
.facts div { display: grid; grid-template-columns: 96px 1fr; gap: 12px; }
.facts dt { color: var(--slate); }
.facts dd { margin: 0; }
.columns { display: grid; grid-template-columns: 3fr 2fr; gap: 24px; align-items: start; }
.panel h2 { margin-bottom: 16px; }
.local { display: grid; gap: 16px; align-content: start; }
.local h2 { margin-bottom: 0; }
.evolution { overflow-x: auto; padding-bottom: 4px; }

@media (max-width: 820px) {
  .hero, .columns { grid-template-columns: 1fr; }
  .art { max-width: 320px; }
}
</style>
