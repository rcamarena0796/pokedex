<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import TagInput from '../components/TagInput.vue'
import { usePokedexStore } from '../stores/pokedex'
import { dexNumber, displayName } from '../utils/format'

// User story 04: edit the proprietary fields. Client checks mirror the server rules;
// the server stays the source of truth and its field errors are shown inline.
const props = defineProps({ id: { type: Number, required: true } })

const pokedex = usePokedexStore()
const router = useRouter()

const entry = ref(null)
const loadError = ref(null)
const saving = ref(false)
const saveError = ref(null)
const serverErrors = ref({})
const form = reactive({ localizedName: '', region: '', tags: [], notes: '' })

const TAG = /^[a-z0-9][a-z0-9-]{0,29}$/
const REGION = /^[\p{L} .'-]{1,40}$/u

const clientErrors = computed(() => {
  const errors = {}
  if (form.localizedName.trim().length > 60) errors.localizedName = 'Use 60 characters or fewer.'
  if (form.region.trim() && !REGION.test(form.region.trim()))
    errors.region = 'Use letters, spaces, dots, apostrophes or hyphens (40 characters max).'
  const badTag = form.tags.find((t) => !TAG.test(t))
  if (badTag) errors.tags = `“${badTag}” is not a valid tag. Use lowercase letters, digits and hyphens.`
  if (form.notes.length > 500) errors.notes = 'Use 500 characters or fewer.'
  return errors
})

const errorFor = (field) => clientErrors.value[field] ?? serverErrors.value[field]
const isValid = computed(() => Object.keys(clientErrors.value).length === 0)

onMounted(async () => {
  try {
    entry.value = await pokedex.find(props.id)
    if (!entry.value) {
      loadError.value = 'This Pokémon is not in the local Pokédex. Add it from the catalog first.'
      return
    }
    form.localizedName = entry.value.localizedName ?? ''
    form.region = entry.value.region ?? ''
    form.tags = [...entry.value.tags]
    form.notes = entry.value.notes ?? ''
  } catch (e) {
    loadError.value = e.message
  }
})

async function save() {
  if (!isValid.value) return
  saving.value = true
  saveError.value = null
  serverErrors.value = {}
  try {
    await pokedex.update(props.id, {
      localizedName: form.localizedName,
      region: form.region,
      tags: form.tags,
      notes: form.notes,
    })
    router.push({ name: 'pokedex' })
  } catch (e) {
    serverErrors.value = e.fieldErrors ?? {}
    saveError.value = e.status === 400 ? 'Some fields need attention.' : e.message
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="page narrow">
    <RouterLink to="/pokedex" class="back muted">Back to My Pokédex</RouterLink>

    <div v-if="loadError" class="alert" role="alert">{{ loadError }}</div>

    <template v-else-if="entry">
      <header class="head">
        <img :src="entry.spriteUrl" alt="" class="sprite" width="96" height="96" />
        <div>
          <span class="dex-number">{{ dexNumber(entry.id) }}</span>
          <h1>Edit {{ displayName(entry.name) }}</h1>
          <p class="muted">These fields exist only in the local database and are kept when the Pokémon is refreshed from PokeAPI.</p>
        </div>
      </header>

      <form class="panel form" novalidate @submit.prevent="save">
        <div class="field">
          <label for="localizedName">Local name</label>
          <input
            id="localizedName"
            v-model="form.localizedName"
            class="input"
            maxlength="60"
            :aria-invalid="!!errorFor('localizedName')"
            aria-describedby="localizedName-hint"
          />
          <span id="localizedName-hint" class="hint">For example the Japanese or French name.</span>
          <span v-if="errorFor('localizedName')" class="error">{{ errorFor('localizedName') }}</span>
        </div>

        <div class="field">
          <label for="region">Region</label>
          <input id="region" v-model="form.region" class="input" maxlength="40" :aria-invalid="!!errorFor('region')" />
          <span v-if="errorFor('region')" class="error">{{ errorFor('region') }}</span>
        </div>

        <div class="field">
          <label for="tags">Tags</label>
          <TagInput id="tags" v-model="form.tags" :invalid="!!errorFor('tags')" />
          <span class="hint">Internal classification. Press Enter after each tag, up to 10.</span>
          <span v-if="errorFor('tags')" class="error">{{ errorFor('tags') }}</span>
        </div>

        <div class="field">
          <label for="notes">Notes</label>
          <textarea id="notes" v-model="form.notes" class="input" maxlength="500" :aria-invalid="!!errorFor('notes')" />
          <span class="hint">{{ form.notes.length }} / 500</span>
          <span v-if="errorFor('notes')" class="error">{{ errorFor('notes') }}</span>
        </div>

        <p v-if="saveError" class="alert" role="alert">{{ saveError }}</p>

        <div class="buttons">
          <button class="button primary" :disabled="saving || !isValid">{{ saving ? 'Saving…' : 'Save changes' }}</button>
          <RouterLink to="/pokedex" class="button quiet">Cancel</RouterLink>
        </div>
      </form>
    </template>
  </div>
</template>

<style scoped>
.narrow { max-width: 760px; }
.back { display: inline-block; margin-bottom: 20px; text-decoration: none; }
.head { display: flex; gap: 16px; align-items: center; margin-bottom: 24px; }
.head .dex-number { font-size: var(--step-2); line-height: 1; }
.head p { margin-top: 6px; max-width: 60ch; }
.form { display: grid; gap: 20px; }
.buttons { display: flex; gap: 12px; }
@media (max-width: 600px) { .head { flex-direction: column; align-items: flex-start; } }
</style>
