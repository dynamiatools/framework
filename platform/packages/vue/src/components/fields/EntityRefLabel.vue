<!-- EntityRefLabel.vue: Read-only display of an entity reference -->
<template>
  <span class="dynamia-entity-ref-label">{{ displayLabel }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ResolvedField } from '@dynamia-tools/ui-core';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean;
  params?: Record<string, unknown>;
}>();

const displayLabel = computed(() => {
  const val = props.modelValue;
  if (!val) return '';
  if (typeof val !== 'object') return String(val);
  const obj = val as Record<string, unknown>;
  const labelField = props.params?.['field'] as string ?? 'name';
  return String(obj[labelField] ?? obj['name'] ?? obj['label'] ?? obj['id'] ?? '');
});
</script>
