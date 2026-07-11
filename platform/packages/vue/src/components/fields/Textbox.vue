<!-- Textbox.vue: Single-line text input field component -->
<template>
  <input
    :id="field.name"
    v-model="proxyValue"
    type="text"
    :placeholder="params?.['placeholder'] as string ?? ''"
    :readonly="readOnly"
    :required="field.resolvedRequired"
    :maxlength="params?.['maxlength'] as number ?? undefined"
    class="dynamia-textbox"
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

const proxyValue = computed({
  get: () => String(props.modelValue ?? ''),
  set: (val) => emit('update:modelValue', val),
});
</script>
