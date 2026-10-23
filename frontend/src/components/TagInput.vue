<script setup>
import { ref } from 'vue'

const tags = defineModel({ type: Array, required: true })
const props = defineProps({
  id: { type: String, required: true },
  max: { type: Number, default: 10 },
  invalid: { type: Boolean, default: false },
})

const draft = ref('')

function commit() {
  const value = draft.value.trim().toLowerCase().replace(/\s+/g, '-')
  if (value && !tags.value.includes(value) && tags.value.length < props.max) {
    tags.value = [...tags.value, value]
  }
  draft.value = ''
}

function onKeydown(event) {
  if (event.key === 'Enter' || event.key === ',') {
    event.preventDefault()
    commit()
  } else if (event.key === 'Backspace' && !draft.value && tags.value.length) {
    tags.value = tags.value.slice(0, -1)
  }
}

const remove = (tag) => (tags.value = tags.value.filter((t) => t !== tag))
</script>

<template>
  <div class="tag-input input" :aria-invalid="invalid">
    <span v-for="tag in tags" :key="tag" class="tag">
      {{ tag }}
      <button type="button" :aria-label="`Remove tag ${tag}`" @click="remove(tag)">×</button>
    </span>
    <input
      :id="id"
      v-model="draft"
      :disabled="tags.length >= max"
      :placeholder="tags.length >= max ? `Maximum of ${max} tags` : 'Type a tag and press Enter'"
      @keydown="onKeydown"
      @blur="commit"
    />
  </div>
</template>

<style scoped>
.tag-input { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; height: auto; }
.tag-input:focus-within { outline: 3px solid var(--ink); outline-offset: 0; }
.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 4px 2px 10px;
  border-radius: 999px;
  background: var(--paper);
  border: 1px solid var(--rule);
  font-size: var(--step--1);
}
.tag button {
  border: 0;
  background: transparent;
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
  padding: 0 4px;
  color: var(--slate);
}
input { flex: 1; min-width: 160px; border: 0; outline: 0; font: inherit; background: transparent; padding: 4px 0; }
</style>
