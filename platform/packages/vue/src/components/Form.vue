<!-- Form.vue: Thin wrapper for rendering a FormView using the layout engine -->
<template>
  <form class="dynamia-form" @submit.prevent="handleSubmit">
    <div v-if="view.isLoading.value" class="dynamia-form-loading">
      <slot name="loading"><span>Loading...</span></slot>
    </div>
    <template v-else-if="view.layout.value">
      <div
        v-for="group in view.layout.value.groups"
        :key="group.name"
        class="dynamia-form-group"
      >
        <div v-if="group.label" class="dynamia-form-group-header">
          <span v-if="group.icon" :class="group.icon" />
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
import type { VueFormView } from '../views/VueFormView.js';
import Field from './Field.vue';

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
