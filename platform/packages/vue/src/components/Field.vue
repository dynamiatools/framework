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
import { computed, defineAsyncComponent } from 'vue';
import type { Component } from 'vue';
import type { ResolvedField, FormView } from '@dynamia-tools/ui-core';
import { FieldComponents } from '@dynamia-tools/ui-core';

const props = defineProps<{
  /** The resolved field descriptor */
  field: ResolvedField;
  /** The parent FormView */
  view: FormView;
  /** Current field value */
  modelValue?: unknown;
  /** Whether the field is in read-only mode */
  readOnly?: boolean;
}>();

defineEmits<{
  'update:modelValue': [value: unknown];
}>();

// Map of component names to async component loaders
const componentMap: Record<string, () => Promise<Component>> = {
  [FieldComponents.Textbox]: () => import('./fields/Textbox.vue'),
  [FieldComponents.Intbox]: () => import('./fields/Intbox.vue'),
  [FieldComponents.Longbox]: () => import('./fields/Intbox.vue'),
  [FieldComponents.Decimalbox]: () => import('./fields/Spinner.vue'),
  [FieldComponents.Spinner]: () => import('./fields/Spinner.vue'),
  [FieldComponents.Combobox]: () => import('./fields/Combobox.vue'),
  [FieldComponents.Datebox]: () => import('./fields/Datebox.vue'),
  [FieldComponents.Checkbox]: () => import('./fields/Checkbox.vue'),
  [FieldComponents.EntityPicker]: () => import('./fields/EntityPicker.vue'),
  [FieldComponents.EntityRefPicker]: () => import('./fields/EntityRefPicker.vue'),
  [FieldComponents.EntityRefLabel]: () => import('./fields/EntityRefLabel.vue'),
  [FieldComponents.CoolLabel]: () => import('./fields/CoolLabel.vue'),
  [FieldComponents.Link]: () => import('./fields/Link.vue'),
  [FieldComponents.Textareabox]: () => import('./fields/Textbox.vue'),
};

const resolvedFieldComponent = computed<Component | null>(() => {
  const comp = props.field.resolvedComponent;
  if (comp in componentMap) {
    return defineAsyncComponent(componentMap[comp]!);
  }
  return null;
});
</script>
