<script setup>
import { computed } from 'vue'
import TypeChip from './TypeChip.vue'
import { dexNumber, displayName, formatKg, humanize } from '../utils/format'

const props = defineProps({ pokemon: { type: Object, required: true } })
const primaryType = computed(() => props.pokemon.types?.[0] ?? 'normal')
</script>

<template>
  <RouterLink
    :to="{ name: 'detail', params: { idOrName: pokemon.name } }"
    class="tile"
    :style="{ '--tint': `var(--type-${primaryType}, var(--type-normal))` }"
  >
    <div class="band">
      <span class="dex-number">{{ dexNumber(pokemon.id) }}</span>
      <img
        v-if="pokemon.spriteUrl"
        class="sprite"
        :src="pokemon.spriteUrl"
        :alt="`${displayName(pokemon.name)} sprite`"
        width="96"
        height="96"
        loading="lazy"
      />
    </div>
    <div class="body">
      <h3>{{ displayName(pokemon.name) }}</h3>
      <p class="muted category">{{ pokemon.category ?? 'Unknown category' }}</p>
      <div class="types">
        <TypeChip v-for="type in pokemon.types" :key="type" :type="type" />
      </div>
      <dl class="facts">
        <div>
          <dt>Weight</dt>
          <dd>{{ formatKg(pokemon.weightKg) }}</dd>
        </div>
        <div>
          <dt>Abilities</dt>
          <dd>{{ pokemon.abilities.map(humanize).join(', ') }}</dd>
        </div>
      </dl>
    </div>
  </RouterLink>
</template>

<style scoped>
.tile {
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border: 1px solid var(--rule);
  border-radius: var(--radius-m);
  overflow: hidden;
  text-decoration: none;
  color: inherit;
}
.tile:hover { border-color: var(--tint); }
.tile:hover .band { background: color-mix(in srgb, var(--tint) 24%, var(--surface)); }
.band {
  position: relative;
  height: 120px;
  display: grid;
  place-items: center;
  background: color-mix(in srgb, var(--tint) 14%, var(--surface));
}
.band .dex-number {
  position: absolute;
  top: 8px;
  left: 14px;
  font-size: var(--step-2);
  color: color-mix(in srgb, var(--tint) 70%, var(--ink));
}
.band img { width: 112px; height: 112px; }
.body { padding: 14px 16px 18px; display: grid; gap: 6px; }
.category { font-size: var(--step--1); }
.types { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 2px; }
.facts { margin: 8px 0 0; display: grid; gap: 4px; font-size: var(--step--1); }
.facts div { display: grid; grid-template-columns: 72px 1fr; gap: 8px; }
.facts dt { color: var(--slate); }
.facts dd { margin: 0; }
</style>
