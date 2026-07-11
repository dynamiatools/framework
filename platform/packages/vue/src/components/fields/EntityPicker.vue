<!-- EntityPicker.vue: Autocomplete entity selector that searches the backend.
     Supported field.params:
       entitySearchEndpoint  — relative path used for GET requests (e.g. /api/books/search)
       entityAlias           — alias used to build /api/entities/{alias}/search when no explicit endpoint
       virtualPath           — CrudPage path used to build /api/{virtualPath} search
       displayField          — object field shown as label (default: "name")
       valueField            — object field used as emitted id value (default: "id")
       returnValue           — "id" (default) | "object" — what to emit on selection
       minChars              — minimum chars before searching (default: 2)
       debounce              — debounce delay in ms (default: 300)
       limit                 — max results per request (default: 20)
       placeholder           — input placeholder text
       searcher              — (q: string) => Promise<unknown[]> — custom search function (overrides HTTP)

     Backend endpoint contract (when using entitySearchEndpoint / entityAlias):
       GET {endpoint}?q={query}&page=1&limit={limit}
       Response (any of):
         - CrudListResult<T>: { content: T[], total, page, pageSize, totalPages }  ← preferred
         - { items: T[], total }
         - { data: T[] }
         - T[]  (plain array)

     NOTE: endpoint /api/entities/{alias}/search must be implemented in the backend.
-->
<template>
  <div
      class="dynamia-entity-picker"
      :class="{ 'is-loading': loading, 'has-value': !!modelValue, 'is-open': showDropdown }"
  >
    <!-- ── Selected value display (when a value is set and not actively searching) ── -->
    <div v-if="selectedDisplay && !isSearching" class="dynamia-entity-picker-value">
      <span class="dynamia-entity-picker-label">{{ selectedDisplay }}</span>
      <button
          v-if="!readOnly"
          type="button"
          class="dynamia-entity-picker-btn dynamia-entity-picker-edit"
          :title="'Change selection'"
          aria-label="Change selection"
          @click="startSearch"
      >✎
      </button>
      <button
          v-if="!readOnly"
          type="button"
          class="dynamia-entity-picker-btn dynamia-entity-picker-clear"
          :title="'Clear selection'"
          aria-label="Clear selection"
          @click="handleClear"
      >✕
      </button>
    </div>

    <!-- ── Search input (shown when no value yet OR actively searching) ── -->
    <div v-if="!modelValue || isSearching" class="dynamia-entity-picker-search">
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
          :aria-controls="`${field.name}-listbox`"
          :aria-activedescendant="highlightIndex >= 0 ? `${field.name}-opt-${highlightIndex}` : undefined"
          :aria-busy="loading"
          class="dynamia-entity-picker-input"
          autocomplete="off"
          role="combobox"
          @input="handleInput"
          @blur="handleBlur"
          @keydown="handleKeydown"
      />
      <span v-if="loading" class="dynamia-entity-picker-spinner" aria-hidden="true" title="Searching…">⟳</span>
      <button
          v-if="isSearching && !readOnly"
          type="button"
          class="dynamia-entity-picker-btn dynamia-entity-picker-cancel"
          aria-label="Cancel search"
          @click="cancelSearch"
      >✕
      </button>
    </div>

    <!-- ── Results dropdown ── -->
    <ul
        v-if="showDropdown"
        :id="`${field.name}-listbox`"
        class="dynamia-entity-picker-dropdown"
        role="listbox"
        :aria-label="`Results for ${field.resolvedLabel}`"
    >
      <li
          v-if="results.content.length === 0 && !loading"
          class="dynamia-entity-picker-empty"
          role="option"
          aria-disabled="true"
      >
        No results found
      </li>
      <li
          v-for="(item, index) in results.content"
          :key="getItemId(item) != null ? String(getItemId(item)) : index"
          :id="`${field.name}-opt-${index}`"
          class="dynamia-entity-picker-option"
          :class="{ 'is-highlighted': highlightIndex === index }"
          role="option"
          :aria-selected="highlightIndex === index"
          @mousedown.prevent="handleSelect(item)"
          @mouseover="highlightIndex = index"
      >
        <slot name="item" :item="item">{{ getItemDisplay(item) }}</slot>
      </li>
    </ul>

    <!-- ── Error message ── -->
    <span v-if="error" class="dynamia-entity-picker-error" role="alert">{{ error }}</span>
  </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, ref, watch} from 'vue';
import type {ResolvedField} from '@dynamia-tools/ui-core';
import {useDynamiaClient} from '../../composables/useDynamiaClient.js';
import type {CrudListResult} from "@dynamia-tools/sdk";

function createEmptyCrudListResult(): CrudListResult {
  return {
    content: [],
    total: 0,
    page: 0,
    pageSize: 0,
    totalPages: 0,
  };
}

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean;
  params?: Record<string, unknown>;
}>();

const emit = defineEmits<{ 'update:modelValue': [value: unknown] }>();

// ── Injected client (optional — provided by DynamiaVue plugin) ────────────
const client = useDynamiaClient();

// ── Reactive state ────────────────────────────────────────────────────────
const searchText = ref('');
const results = ref<CrudListResult>(createEmptyCrudListResult());
const loading = ref(false);
const error = ref<string | null>(null);
const isSearching = ref(false);
const highlightIndex = ref(-1);
const inputRef = ref<HTMLInputElement | null>(null);

// Sequence counter to discard stale responses
let searchSeq = 0;
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

// ── Merged params (field.params takes precedence over component params) ───
const fieldParams = computed<Record<string, unknown>>(() => ({
  ...props.params,
  ...props.field.params,
}));

const placeholder = computed(() => String(fieldParams.value['placeholder'] ?? 'Search…'));
const displayField = computed(() => String(fieldParams.value['displayField'] ?? 'name'));
const valueField = computed(() => String(fieldParams.value['valueField'] ?? 'id'));
const returnValue = computed(() => String(fieldParams.value['returnValue'] ?? 'id'));
const minChars = computed(() => Number(fieldParams.value['minChars'] ?? 2));
const debounceMs = computed(() => Number(fieldParams.value['debounce'] ?? 300));
const limitRows = computed(() => Number(fieldParams.value['limit'] ?? 20));
const virtualPath = computed(() => fieldParams.value['virtualPath'] as string | undefined);

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

/** Resolves the display label for the currently selected value */
const selectedDisplay = computed<string>(() => {
  const val = props.modelValue;
  if (val == null || val === '') return '';
  if (typeof val === 'object') return getItemDisplay(val);
  // Scalar id — just show it; the parent can enrich via slot if needed
  return String(val);
});


// ── Show dropdown when there are results (or when loading in-progress) ────
const showDropdown = computed(() =>
    isSearching.value &&
    searchText.value.length >= minChars.value &&
    ((results.value?.content?.length ?? 0) > 0 || loading.value)
);

// ── Core search logic ─────────────────────────────────────────────────────
async function performSearch(query: string) {
  const seq = ++searchSeq;
  loading.value = true;
  error.value = null;
  results.value = createEmptyCrudListResult();

  try {

    let data: CrudListResult = createEmptyCrudListResult();
    if (client) {

      if (!virtualPath.value) {
        error.value = 'EntityPicker: missing "virtualPath" parameter';
        return;
      }
      // Auth headers handled by HttpClient
      data = await client.crud(virtualPath?.value).findAll({q: query, page: 1, limit: limitRows.value});
    } else {
      return;
    }

    if (seq !== searchSeq) return; // discard stale response
    results.value = data;
  } catch (e: unknown) {
    if (seq !== searchSeq) return;
    error.value = e instanceof Error ? e.message : 'Search failed';
    results.value = createEmptyCrudListResult();
  } finally {
    if (seq === searchSeq) loading.value = false;
  }
}

// ── Event handlers ────────────────────────────────────────────────────────
function handleInput() {
  highlightIndex.value = -1;
  results.value = createEmptyCrudListResult();
  error.value = null;

  if (debounceTimer) {
    clearTimeout(debounceTimer);
    debounceTimer = null;
  }

  if (searchText.value.length < minChars.value) return;

  debounceTimer = setTimeout(() => {
    performSearch(searchText.value);
  }, debounceMs.value);
}

function handleSelect(item: unknown) {
  isSearching.value = false;
  results.value = createEmptyCrudListResult();
  searchText.value = '';
  highlightIndex.value = -1;
  emit('update:modelValue', returnValue.value === 'object' ? item : getItemId(item) ?? item);
}

function handleClear() {
  emit('update:modelValue', null);
  searchText.value = '';
  results.value = createEmptyCrudListResult();
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
  results.value = createEmptyCrudListResult();
  error.value = null;
}

function handleBlur() {
  // Delay to let mousedown on an option register before hiding the dropdown
  setTimeout(() => {
    if (isSearching.value && !searchText.value) {
      isSearching.value = false;
    }
    results.value = createEmptyCrudListResult();
  }, 200);
}

function handleKeydown(e: KeyboardEvent) {
  const items = results.value.content;
  switch (e.key) {
    case 'ArrowDown':
      e.preventDefault();
      highlightIndex.value = Math.min(highlightIndex.value + 1, items.length - 1);
      break;
    case 'ArrowUp':
      e.preventDefault();
      highlightIndex.value = Math.max(highlightIndex.value - 1, 0);
      break;
    case 'Enter':
      if (highlightIndex.value >= 0 && items[highlightIndex.value] != null) {
        e.preventDefault();
        handleSelect(items[highlightIndex.value]!);
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
  ++searchSeq; // invalidate any pending async search
});
</script>
