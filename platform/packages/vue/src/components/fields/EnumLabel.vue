<!-- EnumLabel.vue: Read-only display of an enum value (e.g. "OUT_STOCK") as a readable badge
     ("Out Stock"). Styling hook: .dynamia-enum-label. -->
<template>
  <span class="dynamia-enum-label">{{ displayValue }}</span>
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

const displayValue = computed(() => {
  const value = props.modelValue;
  if (value === null || value === undefined || value === '') return '';
  return String(value)
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/(^|\s)\w/g, char => char.toUpperCase());
});
</script>
