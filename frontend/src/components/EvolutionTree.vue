<script setup>
import { displayName } from '../utils/format'

// Recursive: chains can branch (Eevee has eight evolutions).
defineOptions({ name: 'EvolutionTree' })
defineProps({
  node: { type: Object, required: true },
  current: { type: Number, default: null },
})
</script>

<template>
  <div class="stage">
    <RouterLink
      :to="{ name: 'detail', params: { idOrName: node.name } }"
      class="species"
      :aria-current="node.speciesId === current ? 'page' : undefined"
    >
      <img :src="node.imageUrl" :alt="displayName(node.name)" width="96" height="96" loading="lazy" />
      <span>{{ displayName(node.name) }}</span>
    </RouterLink>

    <div v-if="node.evolvesTo.length" class="next">
      <div v-for="child in node.evolvesTo" :key="child.speciesId" class="branch">
        <span class="arrow" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="20" height="20"><path d="M5 12h12m-5-5 5 5-5 5" fill="none" stroke="currentColor" stroke-width="2" /></svg>
        </span>
        <EvolutionTree :node="child" :current="current" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.stage { display: flex; align-items: center; gap: 8px; }
.next { display: flex; flex-direction: column; gap: 8px; }
.branch { display: flex; align-items: center; gap: 8px; }
.arrow { color: var(--slate); display: grid; }
.species {
  display: grid;
  justify-items: center;
  gap: 4px;
  padding: 8px 10px;
  border-radius: var(--radius-s);
  text-decoration: none;
  font-weight: 600;
  font-size: var(--step--1);
}
.species:hover { background: var(--paper); }
.species[aria-current='page'] { background: var(--paper); box-shadow: inset 0 0 0 2px var(--ink); }
.species img { width: 80px; height: 80px; object-fit: contain; }

@media (max-width: 600px) {
  .stage { flex-direction: column; align-items: flex-start; }
  .branch { flex-direction: column; align-items: flex-start; padding-left: 12px; }
  .arrow { transform: rotate(90deg); margin-left: 34px; }
}
</style>
