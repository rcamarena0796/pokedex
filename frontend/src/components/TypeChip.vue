<script setup>
import { displayName } from '../utils/format'

defineProps({ type: { type: String, required: true } })
</script>

<template>
  <span class="chip" :style="{ '--chip': `var(--type-${type}, var(--type-normal))` }">{{ displayName(type) }}</span>
</template>

<style scoped>
.chip {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--chip);
  color: #fff;
  font-size: var(--step--1);
  font-weight: 600;
  line-height: 1.6;
}
</style>
