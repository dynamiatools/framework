<!-- EntityRefPicker.vue: Autocomplete entity-reference picker.
     Searches the backend by entityAlias and emits a reference object {id, name}.
     Shares the same param contract as EntityPicker but always emits "object"
     so the form value carries {id, name} (useful for entity-reference fields).

     Supported field.params (same as EntityPicker):
       entitySearchEndpoint  — relative path used for GET requests
       entityAlias           — alias used to build /api/entities/{alias}/search
       virtualPath           — CrudPage path used to build /api/{virtualPath}
       displayField          — field shown as label (default: "name")
       minChars              — minimum chars (default: 2)
       debounce              — delay ms (default: 300)
       limit                 — max results (default: 20)
       placeholder           — input placeholder text
       searcher              — (q: string) => Promise<unknown[]> custom function

     NOTE: backend endpoint /api/entities/{alias}/search must be implemented.
-->
<template>
  <div
      class="dynamia-entity-ref-picker"
      :class="{ 'is-loading': loading, 'has-value': !!modelValue, 'is-open': showDropdown }"
  >
    <!-- ── Selected value display ── -->
    <div v-if="selectedDisplay && !isSearching" class="dynamia-entity-ref-picker-value">
      <span class="dynamia-entity-ref-picker-label">{{ selectedDisplay }}</span>
      <button
          v-if="!readOnly"
          type="button"
          class="dynamia-entity-ref-picker-btn dynamia-entity-ref-picker-edit"
          aria-label="Change selection"
          @click="startSearch"
      >✎
      </button>
      <button
          v-if="!readOnly"
          type="button"
          class="dynamia-entity-ref-picker-btn dynamia-entity-ref-picker-clear"
          aria-label="Clear selection"
          @click="handleClear"
      >✕
      </button>
    </div>

    <!-- ── Search input ── -->
    <div v-if="!modelValue || isSearching" class="dynamia-entity-ref-picker-search">
      <input
          ref="inputRef"
          v-model="searchText"
          type="text"
          :id="field.name"
          :placeholder="placeholder"
          :disabled="readOnly"
          :aria-label="field.resolvedLabel"
          :aria-expanded="showDropdown"
          :aria-autocomplete="'list'"
          :aria-controls="`${field.name}-ref-listbox`"
          :aria-activedescendant="highlightIndex >= 0 ? `${field.name}-ref-opt-${highlightIndex}` : undefined"
          :aria-busy="loading"
          class="dynamia-entity-ref-picker-input"
          autocomplete="off"
          role="combobox"
          @input="handleInput"
          @blur="handleBlur"
          @keydown="handleKeydown"
      />
      <span v-if="loading" class="dynamia-entity-ref-picker-spinner" aria-hidden="true">⟳</span>
      <button
          v-if="isSearching && !readOnly"
          type="button"
          class="dynamia-entity-ref-picker-btn dynamia-entity-ref-cancel"
          aria-label="Cancel search"
          @click="cancelSearch"
      >✕
      </button>
    </div>

    <!-- ── Results dropdown ── -->
    <ul
        v-if="showDropdown"
        :id="`${field.name}-ref-listbox`"
        class="dynamia-entity-ref-picker-dropdown"
        role="listbox"
    >
      <li
          v-if="results.length === 0 && !loading"
          class="dynamia-entity-ref-picker-empty"
          role="option"
          aria-disabled="true"
      >No results found
      </li>
      <li
          v-for="(item, index) in results"
          :key="getItemId(item) != null ? String(getItemId(item)) : index"
          :id="`${field.name}-ref-opt-${index}`"
          class="dynamia-entity-ref-picker-option"
          :class="{ 'is-highlighted': highlightIndex === index }"
          role="option"
          :aria-selected="highlightIndex === index"
          @mousedown.prevent="handleSelect(item)"
          @mouseover="highlightIndex = index"
      >
        {{ getItemDisplay(item) }}
      </li>
    </ul>

    <!-- ── Error message ── -->
    <span v-if="error" class="dynamia-entity-ref-picker-error" role="alert">{{ error }}</span>
  </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, ref, watch} from 'vue';
import type {ResolvedField} from '@dynamia-tools/ui-core';
import {useDynamiaClient} from '../../composables/useDynamiaClient.js';
import type {EntityReference} from "@dynamia-tools/sdk";

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean;
  params?: Record<string, unknown>;
}>();

const emit = defineEmits<{ 'update:modelValue': [value: unknown] }>();

// ── Injected client ───────────────────────────────────────────────────────
const client = useDynamiaClient();

// ── Reactive state ────────────────────────────────────────────────────────
const searchText = ref('');
const results = ref<EntityReference[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const isSearching = ref(false);
const highlightIndex = ref(-1);
const inputRef = ref<HTMLInputElement | null>(null);

let searchSeq = 0;
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

// ── Merged params ─────────────────────────────────────────────────────────
const fieldParams = computed<Record<string, unknown>>(() => ({
  ...props.params,
  ...props.field.params,
}));

const placeholder = computed(() => String(fieldParams.value['placeholder'] ?? 'Search…'));
const displayField = computed(() => String(fieldParams.value['displayField'] ?? 'name'));
const valueField = computed(() => String(fieldParams.value['valueField'] ?? 'id'));
const minChars = computed(() => Number(fieldParams.value['minChars'] ?? 2));
const debounceMs = computed(() => Number(fieldParams.value['debounce'] ?? 300));
const entityAlias = computed(() => fieldParams.value['entityAlias'] as string | undefined);


// ── Helpers ───────────────────────────────────────────────────────────────
function getItemId(item: unknown): unknown {
  if (!item || typeof item !== 'object') return item;
  return (item as Record<string, unknown>)[valueField.value] ?? (item as Record<string, unknown>)['id'] ?? null;
}

function getItemDisplay(item: unknown): string {
  if (!item || typeof item !== 'object') return String(item ?? '');
  const obj = item as Record<string, unknown>;
  return String(obj[displayField.value] ?? obj['display'] ?? obj['name'] ?? obj['id'] ?? '');
}

const selectedDisplay = computed<string>(() => {
  const val = props.modelValue;
  if (val == null || val === '') return '';
  if (typeof val === 'object') return getItemDisplay(val);
  return String(val);
});


const showDropdown = computed(() =>
    isSearching.value &&
    searchText.value.length >= minChars.value &&
    (results.value.length > 0 || loading.value)
);

// ── Search logic ──────────────────────────────────────────────────────────
async function performSearch(query: string) {
  const seq = ++searchSeq;
  loading.value = true;
  error.value = null;
  results.value = [];

  try {
    const searcher = fieldParams.value['searcher'];
    if (typeof searcher === 'function') {
      const raw = await (searcher as (q: string) => Promise<unknown[]>)(query);
      if (seq !== searchSeq) return;
      results.value = Array.isArray(raw) ? raw : [];
      return;
    }


    let data: EntityReference[] = [];
    if (client) {
      data = await client.metadata.findEntityReferences(entityAlias, query);
    }

    if (seq !== searchSeq) return;
    results.value = data;
  } catch (e: unknown) {
    if (seq !== searchSeq) return;
    error.value = e instanceof Error ? e.message : 'Search failed';
    results.value = [];
  } finally {
    if (seq === searchSeq) loading.value = false;
  }
}

// ── Event handlers ────────────────────────────────────────────────────────
function handleInput() {
  highlightIndex.value = -1;
  results.value = [];
  error.value = null;
  if (debounceTimer) {
    clearTimeout(debounceTimer);
    debounceTimer = null;
  }
  if (searchText.value.length < minChars.value) return;
  debounceTimer = setTimeout(() => performSearch(searchText.value), debounceMs.value);
}

function handleSelect(item: unknown) {
  // EntityRefPicker always emits the full object {id, name} as a reference
  isSearching.value = false;
  results.value = [];
  searchText.value = '';
  highlightIndex.value = -1;
  emit('update:modelValue', item);
}

function handleClear() {
  emit('update:modelValue', null);
  searchText.value = '';
  results.value = [];
  isSearching.value = false;
  error.value = null;
}

function startSearch() {
  isSearching.value = true;
  nextTick(() => inputRef.value?.focus());
}

function cancelSearch() {
  isSearching.value = false;
  searchText.value = '';
  results.value = [];
  error.value = null;
}

function handleBlur() {
  setTimeout(() => {
    if (isSearching.value && !searchText.value) isSearching.value = false;
    results.value = [];
  }, 200);
}

function handleKeydown(e: KeyboardEvent) {
  switch (e.key) {
    case 'ArrowDown':
      e.preventDefault();
      highlightIndex.value = Math.min(highlightIndex.value + 1, results.value.length - 1);
      break;
    case 'ArrowUp':
      e.preventDefault();
      highlightIndex.value = Math.max(highlightIndex.value - 1, 0);
      break;
    case 'Enter':
      if (highlightIndex.value >= 0 && results.value[highlightIndex.value] != null) {
        e.preventDefault();
        handleSelect(results.value[highlightIndex.value]!);
      }
      break;
    case 'Escape':
      cancelSearch();
      break;
  }
}

// ── Watchers ──────────────────────────────────────────────────────────────
watch(() => props.modelValue, (val) => {
  if (val == null) {
    searchText.value = '';
    isSearching.value = false;
  }
});

// ── Cleanup ───────────────────────────────────────────────────────────────
onBeforeUnmount(() => {
  if (debounceTimer) clearTimeout(debounceTimer);
  ++searchSeq;
});
</script>
