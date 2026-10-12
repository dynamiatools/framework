<!-- EntityRefMultiPicker.vue: Autocomplete picker for collections of entities (e.g. a many-to-many `Set<Genre>`).
     Searches the backend by entityAlias, like EntityRefPicker, and keeps the selection as an array of
     references {id, name} shown as removable chips.
     Supported field.params:
       entityAlias  — alias of the EntityReferenceRepository to search (required)
       displayField — field shown as label (default: "name")
       minChars     — minimum chars before searching (default: 1)
       debounce     — delay ms (default: 300)
       placeholder  — input placeholder text
-->
<template>
  <div class="dynamia-entity-ref-multi" :class="{ 'is-open': showDropdown }">
    <ul v-if="selected.length" class="dynamia-entity-ref-multi-chips">
      <li v-for="item in selected" :key="String(getItemId(item))" class="dynamia-entity-ref-multi-chip">
        <span>{{ getItemDisplay(item) }}</span>
        <button
            v-if="!readOnly"
            type="button"
            class="dynamia-entity-ref-multi-remove"
            :aria-label="`Remove ${getItemDisplay(item)}`"
            @click="remove(item)"
        >✕
        </button>
      </li>
    </ul>

    <div v-if="!readOnly" class="dynamia-entity-ref-picker-search">
      <input
          v-model="searchText"
          type="text"
          :id="field.name"
          :placeholder="placeholder"
          :aria-label="field.resolvedLabel"
          :aria-expanded="showDropdown"
          :aria-controls="`${field.name}-multi-listbox`"
          class="dynamia-entity-ref-picker-input"
          autocomplete="off"
          role="combobox"
          @input="handleInput"
          @blur="handleBlur"
          @keydown.esc="clearSearch"
      />
      <span v-if="loading" class="dynamia-entity-ref-picker-spinner" aria-hidden="true">⟳</span>

      <ul
          v-if="showDropdown"
          :id="`${field.name}-multi-listbox`"
          class="dynamia-entity-ref-picker-dropdown"
          role="listbox"
      >
        <li v-if="available.length === 0 && !loading" class="dynamia-entity-ref-picker-empty" role="option" aria-disabled="true">
          No results found
        </li>
        <li
            v-for="item in available"
            :key="String(getItemId(item))"
            class="dynamia-entity-ref-picker-option"
            role="option"
            @mousedown.prevent="add(item)"
        >
          {{ getItemDisplay(item) }}
        </li>
      </ul>
    </div>


    <span v-if="error" class="dynamia-entity-ref-picker-error" role="alert">{{ error }}</span>
  </div>
</template>

<script setup lang="ts">
import {computed, onBeforeUnmount, ref} from 'vue';
import type {ResolvedField} from '@dynamia-tools/ui-core';
import type {EntityReference} from '@dynamia-tools/sdk';
import {useDynamiaClient} from '../../composables/useDynamiaClient.js';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean;
  params?: Record<string, unknown>;
}>();

const emit = defineEmits<{ 'update:modelValue': [value: unknown] }>();

const client = useDynamiaClient();

const searchText = ref('');
const results = ref<EntityReference[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const focused = ref(false);

let searchSeq = 0;
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

const fieldParams = computed<Record<string, unknown>>(() => ({...props.params, ...props.field.params}));
const placeholder = computed(() => String(fieldParams.value['placeholder'] ?? 'Search…'));
const displayField = computed(() => String(fieldParams.value['displayField'] ?? 'name'));
const minChars = computed(() => Number(fieldParams.value['minChars'] ?? 1));
const debounceMs = computed(() => Number(fieldParams.value['debounce'] ?? 300));
const entityAlias = computed(() => fieldParams.value['entityAlias'] as string | undefined);

const selected = computed<unknown[]>(() => (Array.isArray(props.modelValue) ? props.modelValue : []));

function getItemId(item: unknown): unknown {
  if (!item || typeof item !== 'object') return item;
  return (item as Record<string, unknown>)['id'] ?? null;
}

function getItemDisplay(item: unknown): string {
  if (!item || typeof item !== 'object') return String(item ?? '');
  const obj = item as Record<string, unknown>;
  return String(obj[displayField.value] ?? obj['name'] ?? obj['id'] ?? '');
}

/** Search results that are not already selected. */
const available = computed(() => {
  const ids = new Set(selected.value.map(s => String(getItemId(s))));
  return results.value.filter(r => !ids.has(String(getItemId(r))));
});

const showDropdown = computed(() =>
    focused.value && searchText.value.length >= minChars.value && (results.value.length > 0 || loading.value)
);

async function performSearch(query: string) {
  const seq = ++searchSeq;
  loading.value = true;
  error.value = null;
  try {
    if (!entityAlias.value) throw new Error('EntityRefMultiPicker: missing "entityAlias" parameter');
    const data = client ? await client.metadata.findEntityReferences(entityAlias.value, query) : [];
    if (seq === searchSeq) results.value = data;
  } catch (e: unknown) {
    if (seq !== searchSeq) return;
    error.value = e instanceof Error ? e.message : 'Search failed';
    results.value = [];
  } finally {
    if (seq === searchSeq) loading.value = false;
  }
}

function handleInput() {
  focused.value = true;
  error.value = null;
  if (debounceTimer) clearTimeout(debounceTimer);
  if (searchText.value.length < minChars.value) {
    results.value = [];
    return;
  }
  debounceTimer = setTimeout(() => performSearch(searchText.value), debounceMs.value);
}

function add(item: EntityReference) {
  // Keep just the reference ({id, name}): the search result also carries className and attributes.
  emit('update:modelValue', [...selected.value, {id: getItemId(item), name: getItemDisplay(item)}]);
  clearSearch();
}

function remove(item: unknown) {
  const id = String(getItemId(item));
  emit('update:modelValue', selected.value.filter(s => String(getItemId(s)) !== id));
}

function clearSearch() {
  searchText.value = '';
  results.value = [];
}

function handleBlur() {
  setTimeout(() => {
    focused.value = false;
  }, 200);
}

onBeforeUnmount(() => {
  if (debounceTimer) clearTimeout(debounceTimer);
});
</script>
