<!-- Combobox.vue: Dropdown select field component -->
<template>
  <select
    :id="field.name"
    v-model="proxyValue"
    :disabled="readOnly"
    :required="field.resolvedRequired"
    class="dynamia-combobox"
  >
    <option v-if="!field.resolvedRequired" value="">-- Select --</option>
    <option
      v-for="opt in options"
      :key="String(opt.value)"
      :value="opt.value"
    >
      {{ opt.label }}
    </option>
  </select>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ResolvedField } from '@dynamia-tools/ui-core';

interface SelectOption { value: unknown; label: string; }

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean;
  params?: Record<string, unknown>;
}>();

const emit = defineEmits<{ 'update:modelValue': [value: unknown] }>();

const proxyValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
});

const options = computed<SelectOption[]>(() => {
  const rawOptions = props.params?.['options'] ?? props.params?.['values'];
  if (Array.isArray(rawOptions)) {
    return rawOptions.map(o => typeof o === 'object' && o !== null
      ? o as SelectOption
      : { value: o, label: String(o) }
    );
  }
  return [];
});
</script>
