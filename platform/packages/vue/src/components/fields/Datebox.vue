<!-- Datebox.vue: Date input field component -->
<template>
  <input
    :id="field.name"
    :type="inputType"
    :value="formattedValue"
    :readonly="readOnly"
    :required="field.resolvedRequired"
    class="dynamia-datebox"
    @change="handleChange"
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

const emit = defineEmits<{ 'update:modelValue': [value: string] }>();

const inputType = computed(() => props.params?.['time'] ? 'datetime-local' : 'date');

const formattedValue = computed(() => {
  if (!props.modelValue) return '';
  try {
    let d: Date;
    if (props.modelValue instanceof Date) {
      d = props.modelValue;
    } else if (typeof props.modelValue === 'string' || typeof props.modelValue === 'number') {
      d = new Date(props.modelValue);
    } else {
      return String(props.modelValue);
    }
    if (isNaN(d.getTime())) return String(props.modelValue);
    return inputType.value === 'datetime-local'
      ? d.toISOString().slice(0, 16)
      : d.toISOString().slice(0, 10);
  } catch { return String(props.modelValue); }
});

function handleChange(event: Event) {
  emit('update:modelValue', (event.target as HTMLInputElement).value);
}
</script>
