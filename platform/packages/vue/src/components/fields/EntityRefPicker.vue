<!-- EntityRefPicker.vue: Entity reference picker for related entities -->
<template>
  <div class="dynamia-entity-ref-picker">
    <input
      v-model="searchText"
      type="text"
      :placeholder="params?.['placeholder'] as string ?? 'Search...'"
      :readonly="readOnly"
      class="dynamia-entity-ref-picker-input"
      @input="handleSearch"
    />
    <div v-if="showResults" class="dynamia-entity-ref-results">
      <div
        v-for="(item, index) in results"
        :key="index"
        class="dynamia-entity-ref-item"
        @click="handleSelect(item)"
      >
        {{ getLabel(item) }}
      </div>
    </div>
    <span v-if="modelValue" class="dynamia-entity-ref-selected">
      {{ getLabel(modelValue) }}
      <button v-if="!readOnly" type="button" @click="$emit('update:modelValue', null)">✕</button>
    </span>
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

function getLabel(item: unknown): string {
  if (!item || typeof item !== 'object') return String(item ?? '');
  const obj = item as Record<string, unknown>;
  return String(obj['name'] ?? obj['label'] ?? obj['id'] ?? '');
}

async function handleSearch() {
  const searcher = props.params?.['searcher'];
  if (typeof searcher === 'function') {
    results.value = await (searcher as (q: string) => Promise<unknown[]>)(searchText.value);
  }
}

function handleSelect(item: unknown) {
  emit('update:modelValue', item);
  results.value = [];
  searchText.value = getLabel(item);
}
</script>
