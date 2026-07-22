<!-- EntityRefLabel.vue: Read-only display of an entity reference -->
<template>
  <span class="dynamia-entity-ref-label">{{ displayLabel }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { entityDisplayLabel } from '@dynamia-tools/ui-core';
import type { ResolvedField } from '@dynamia-tools/ui-core';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean;
  params?: Record<string, unknown>;
}>();

const displayLabel = computed(() => {
  const labelField = props.params?.['field'] as string | undefined;
  return entityDisplayLabel(props.modelValue, labelField);
});
</script>
