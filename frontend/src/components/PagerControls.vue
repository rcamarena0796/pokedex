<script setup>
defineProps({
  page: { type: Number, required: true }, // zero-based
  totalPages: { type: Number, required: true },
  disabled: { type: Boolean, default: false },
})
defineEmits(['change'])
</script>

<template>
  <nav v-if="totalPages > 1" class="pager" aria-label="Pagination">
    <button class="button small" :disabled="disabled || page === 0" @click="$emit('change', page - 1)">
      Previous
    </button>
    <span class="position">Page {{ page + 1 }} of {{ totalPages }}</span>
    <button
      class="button small"
      :disabled="disabled || page >= totalPages - 1"
      @click="$emit('change', page + 1)"
    >
      Next
    </button>
  </nav>
</template>

<style scoped>
.pager { display: flex; align-items: center; justify-content: center; gap: 16px; margin-top: 32px; }
.position { color: var(--slate); font-variant-numeric: tabular-nums; }
</style>
