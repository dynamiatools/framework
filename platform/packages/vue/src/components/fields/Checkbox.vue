<!-- Checkbox.vue: Boolean checkbox field component -->
<template>
  <label class="dynamia-checkbox-wrapper">
    <input
      :id="field.name"
      v-model="proxyValue"
      type="checkbox"
      :disabled="readOnly"
      class="dynamia-checkbox"
    />
    <span v-if="params?.['label']" class="dynamia-checkbox-label">{{ params['label'] }}</span>
  </label>
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

const emit = defineEmits<{ 'update:modelValue': [value: boolean] }>();

const proxyValue = computed({
  get: () => Boolean(props.modelValue),
  set: (val) => emit('update:modelValue', val),
});
</script>
