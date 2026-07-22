<!-- Label.vue: Read-only display of a scalar field value, with an optional named converter
     (field.params.converter, e.g. "converters.Currency" — see resolveConverter in ui-core). -->
<template>
  <span class="dynamia-label">{{ displayValue }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { entityDisplayLabel, resolveConverter } from '@dynamia-tools/ui-core';
import type { ResolvedField } from '@dynamia-tools/ui-core';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean;
  params?: Record<string, unknown>;
}>();

const displayValue = computed(() => {
  const converter = resolveConverter(props.params?.['converter'] as string | undefined);
  if (converter) return converter(props.modelValue, props.params);
  return entityDisplayLabel(props.modelValue);
});
</script>
