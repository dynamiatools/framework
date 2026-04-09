<!-- Spinner.vue: Decimal/float number input field component -->
<template>
  <input
    :id="field.name"
    v-model.number="proxyValue"
    type="number"
    :placeholder="params?.['placeholder'] as string ?? ''"
    :readonly="readOnly"
    :required="field.resolvedRequired"
    :min="params?.['min'] as number ?? undefined"
    :max="params?.['max'] as number ?? undefined"
    :step="params?.['step'] as number ?? 0.01"
    class="dynamia-spinner"
  />
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

const emit = defineEmits<{ 'update:modelValue': [value: number] }>();

const proxyValue = computed({
  get: () => Number(props.modelValue ?? 0),
  set: (val) => emit('update:modelValue', val),
});
</script>
