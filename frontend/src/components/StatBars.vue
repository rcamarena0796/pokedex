<script setup>
import { computed } from 'vue'
import { STAT_LABELS, humanize } from '../utils/format'

const props = defineProps({
  stats: { type: Array, required: true },
  tint: { type: String, default: 'var(--ink)' },
})

const MAX = 255 // highest possible base stat
const total = computed(() => props.stats.reduce((sum, s) => sum + s.baseValue, 0))
</script>

<template>
  <table class="stats">
    <caption class="visually-hidden">Base stats</caption>
    <tbody>
      <tr v-for="stat in stats" :key="stat.name">
        <th scope="row">{{ STAT_LABELS[stat.name] ?? humanize(stat.name) }}</th>
        <td class="value">{{ stat.baseValue }}</td>
        <td class="bar-cell" aria-hidden="true">
          <span class="bar" :style="{ width: `${(stat.baseValue / MAX) * 100}%`, background: tint }" />
        </td>
      </tr>
      <tr class="total">
        <th scope="row">Total</th>
        <td class="value">{{ total }}</td>
        <td />
      </tr>
    </tbody>
  </table>
</template>

<style scoped>
.stats { width: 100%; border-collapse: collapse; }
th { text-align: left; font-weight: 500; color: var(--slate); white-space: nowrap; padding: 6px 12px 6px 0; }
.value {
  width: 3.5ch;
  text-align: right;
  font-family: var(--font-display);
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  padding-right: 14px;
}
.bar-cell { width: 100%; }
.bar { display: block; height: 10px; border-radius: 5px; min-width: 4px; }
.total th, .total td { border-top: 1px solid var(--rule); padding-top: 10px; color: var(--ink); }
</style>
