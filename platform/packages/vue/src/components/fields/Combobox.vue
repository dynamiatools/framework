<!-- Combobox.vue: Dropdown select field component.
     - When field.enum === true, options are built from ENUM_CONSTANTS via resolveFieldEnumConstants.
     - Otherwise options come from field.params['options'] / params['options'].
     - The component always renders a native <select> (non-editable by design). -->
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
import { resolveFieldEnumConstants } from '@dynamia-tools/sdk';

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
  // Priority 1: enum constants from field metadata (field.enum === true)
  if (props.field.enum) {
    const enumConstants = resolveFieldEnumConstants(props.field);
    if (enumConstants.length > 0) {
      return enumConstants.map(c => ({ value: c, label: c }));
    }
  }

  // Priority 2: explicit options list from field.params or component params
  const rawOptions =
    props.field.params?.['options'] ??
    props.field.params?.['values'] ??
    props.params?.['options'] ??
    props.params?.['values'];

  if (Array.isArray(rawOptions)) {
    return rawOptions.map(o =>
      typeof o === 'object' && o !== null
        ? (o as SelectOption)
        : { value: o, label: String(o) }
    );
  }

  return [];
});
</script>
