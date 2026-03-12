<!-- EntityPicker.vue: Entity selection picker with search -->
<template>
  <div class="dynamia-entity-picker">
    <input
      v-model="searchText"
      type="text"
      :placeholder="params?.['placeholder'] as string ?? 'Search...'"
      :readonly="readOnly"
      class="dynamia-entity-picker-input"
      @input="handleSearch"
    />
    <div v-if="showResults" class="dynamia-entity-picker-results">
      <div
        v-for="(item, index) in results"
        :key="index"
        class="dynamia-entity-picker-item"
        @click="handleSelect(item)"
      >
        <slot name="item" :item="item">{{ getLabel(item) }}</slot>
      </div>
    </div>
    <div v-if="selectedValue" class="dynamia-entity-picker-selected">
      <slot name="selected" :value="selectedValue">
        <span>{{ getLabel(selectedValue) }}</span>
        <button v-if="!readOnly" type="button" @click="handleClear">✕</button>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import type { ResolvedField } from '@dynamia-tools/ui-core';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean;
  params?: Record<string, unknown>;
}>();

const emit = defineEmits<{ 'update:modelValue': [value: unknown] }>();

const searchText = ref('');
const results = ref<unknown[]>([]);
const showResults = computed(() => results.value.length > 0);
const selectedValue = computed(() => props.modelValue);

function getLabel(item: unknown): string {
  if (!item || typeof item !== 'object') return String(item ?? '');
  const obj = item as Record<string, unknown>;
  return String(obj['name'] ?? obj['label'] ?? obj['id'] ?? '');
}

async function handleSearch() {
  const query = searchText.value;
  if (!query) { results.value = []; return; }
  const searcher = props.params?.['searcher'];
  if (typeof searcher === 'function') {
    results.value = await (searcher as (q: string) => Promise<unknown[]>)(query);
  }
}

function handleSelect(item: unknown) {
  emit('update:modelValue', item);
  results.value = [];
  searchText.value = getLabel(item);
}

function handleClear() {
  emit('update:modelValue', null);
  searchText.value = '';
}
</script>
