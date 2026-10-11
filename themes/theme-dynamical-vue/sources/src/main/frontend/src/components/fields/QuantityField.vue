<!-- QuantityField.vue: `component: quantityinput`, the TailAdmin QuantityInput (- / + stepper).
     params: `min` (default 0), `max`, `step` (default 1), `allowDecimal`. -->
<template>
  <span v-if="readOnly" class="tabular-nums">{{ quantity }}</span>
  <QuantityInput
    v-else
    :model-value="quantity"
    :min="num('min') ?? 0"
    :max="num('max')"
    :step="num('step') ?? 1"
    :allow-decimal="params?.['allowDecimal'] === true"
    @update:model-value="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import QuantityInput from '@dynamia-tools/tailadmin-vue/components/ext/input/QuantityInput.vue';
import type { ResolvedField } from '@dynamia-tools/ui-core';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean | undefined;
  params?: Record<string, unknown> | undefined;
}>();

const emit = defineEmits<{ 'update:modelValue': [value: number] }>();

function num(name: string): number | undefined {
  const value = Number(props.params?.[name]);
  return props.params?.[name] !== undefined && Number.isFinite(value) ? value : undefined;
}

const quantity = computed(() => {
  const value = Number(props.modelValue ?? 0);
  return Number.isFinite(value) ? value : 0;
});
</script>
