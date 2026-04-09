<!-- Form.vue: Thin wrapper for rendering a FormView using the layout engine -->
<template>
  <form class="dynamia-form" @submit.prevent="handleSubmit">
    <div v-if="view.isLoading.value" class="dynamia-form-loading">
      <slot name="loading"><span>Loading...</span></slot>
    </div>
    <template v-else-if="view.layout.value">
      <div
          v-for="(group, groupIdx) in view.layout.value.groups"
          :key="groupIdx"
          class="dynamia-form-group"
      >
        <div v-if="group.label" class="dynamia-form-group-header">
          <span v-if="group.icon" :class="group.icon"/>
          <span>{{ group.label }}</span>
        </div>
        <div
            class="dynamia-form-grid"
            :style="{ '--columns': view.layout.value.columns }"
        >
          <div
              v-for="row in group.rows"
              :key="row.fields.map(f => f.name).join('-')"
              class="dynamia-form-row"
          >
            <div
                v-for="field in row.fields"
                :key="field.name"
                class="dynamia-form-cell"
                :style="{ 'grid-column': `span ${field.gridSpan}` }"
                :data-field="field.name"
                :data-field-icon="field.icon"
                :data-field-required="field.required"
                :data-field-type="resolveViewFieldType(field)"
            >
              <label :for="field.name" class="dynamia-form-label">
                {{ field.resolvedLabel }}
                <span v-if="field.resolvedRequired" class="dynamia-required">*</span>
              </label>
              <Field
                  :field="field"
                  :view="view"
                  :model-value="view.values.value[field.name]"
                  @update:model-value="val => view.setFieldValue(field.name, val)"
              />
              <span v-if="view.errors.value[field.name]" class="dynamia-field-error">
                {{ view.errors.value[field.name] }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>
    <div class="dynamia-form-actions">
      <slot name="actions">
        <button type="submit">Save</button>
        <button type="button" @click="$emit('cancel')">Cancel</button>
      </slot>
    </div>
  </form>
</template>

<script setup lang="ts">
import type {VueFormView} from '../views/VueFormView.js';
import Field from './Field.vue';
import {resolveViewFieldType} from "@dynamia-tools/sdk";

const props = defineProps<{
  /** The VueFormView instance to render */
  view: VueFormView;
  /** Whether the form is in read-only mode */
  readOnly?: boolean;
}>();

const emit = defineEmits<{
  submit: [values: Record<string, unknown>];
  cancel: [];
}>();

async function handleSubmit() {
  if (props.view.validate()) {
    emit('submit', props.view.values.value);
  }
}
</script>

<!--
  Form.vue is intentionally headless — no built-in styles are injected.
  Style the following BEM-like class names in your own stylesheet:

  .dynamia-form               — <form> root element
  .dynamia-form-loading       — shown while view is initializing
  .dynamia-form-group         — wrapper for each field group
  .dynamia-form-group-header  — group title bar  (contains icon + label)
  .dynamia-form-grid          — outer grid container (sets --columns CSS var)
  .dynamia-form-row           — one logical row inside a group
  .dynamia-form-cell          — single field cell  (has inline grid-column:span N)
  .dynamia-form-label         — <label> for a field
  .dynamia-required           — asterisk appended to required field labels
  .dynamia-field-error        — validation error message beneath a field
  .dynamia-form-actions       — actions / button bar at the bottom
-->

