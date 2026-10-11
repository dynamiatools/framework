<!-- Field.vue: Dispatcher that renders the correct field component by FieldComponent type -->
<template>
  <component
    :is="resolvedFieldComponent"
    v-if="resolvedFieldComponent"
    :field="field"
    :model-value="modelValue"
    :read-only="readOnly"
    :params="field.params"
    @update:model-value="$emit('update:modelValue', $event)"
  />
  <input
    v-else
    :id="field.name"
    type="text"
    :value="String(modelValue ?? '')"
    :readonly="readOnly"
    class="dynamia-field-default"
    @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Component } from 'vue';
import type { ResolvedField, FormView } from '@dynamia-tools/ui-core';
import { resolveFieldComponent } from './fields/fieldComponents.js';

const props = defineProps<{
  /** The resolved field descriptor */
  field: ResolvedField;
  /** The parent FormView (absent when the field renders a table cell) */
  view?: FormView;
  /** Current field value */
  modelValue?: unknown;
  /** Whether the field is in read-only mode */
  readOnly?: boolean;
}>();

defineEmits<{
  'update:modelValue': [value: unknown];
}>();

// Registered (theme/app) -> built in -> basic one for the field type; see fields/fieldComponents.ts
const resolvedFieldComponent = computed<Component | null>(() => resolveFieldComponent(props.field));
</script>
