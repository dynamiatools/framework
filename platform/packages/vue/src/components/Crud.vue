<!-- Crud.vue: CRUD component orchestrating Table + Form + Actions -->
<template>
  <div class="dynamia-crud">
    <div class="dynamia-crud-toolbar">
      <slot name="toolbar">
        <button
          v-if="!readOnly && view.showTable.value"
          type="button"
          class="dynamia-crud-new-btn"
          @click="view.startCreate()"
        >
          New
        </button>
        <Actions :actions="resolvedActions" :view="view" @action="handleAction" />
      </slot>
    </div>
    <!-- Error banner -->
    <div v-if="view.errorMessage.value" class="dynamia-crud-error">
      {{ view.errorMessage.value }}
    </div>
    <!-- Table mode -->
    <Table v-if="view.showTable.value" :view="view.tableView" :read-only="readOnly ?? false">
      <template #actions="{ row }">
        <button type="button" @click="view.startEdit(row)">Edit</button>
        <button type="button" @click="view.delete(row)">Delete</button>
      </template>
    </Table>
    <!-- Form mode -->
    <Form
      v-if="view.showForm.value"
      :view="view.formView"
      :read-only="readOnly ?? false"
      @submit="handleSave"
      @cancel="view.cancelEdit()"
    />
    <div v-if="view.isLoading.value" class="dynamia-crud-loading">
      <slot name="loading"><span>Saving...</span></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ActionMetadata } from '@dynamia-tools/sdk';
import type { VueCrudView } from '../views/VueCrudView.js';
import Form from './Form.vue';
import Table from './Table.vue';
import Actions from './Actions.vue';

const props = withDefaults(defineProps<{
  /** The VueCrudView instance to render */
  view: VueCrudView;
  /** Whether in read-only mode */
  readOnly?: boolean;
  /** Actions to show in the toolbar */
  actions?: ActionMetadata[];
}>(), { readOnly: false });

const emit = defineEmits<{
  save: [data: Record<string, unknown>];
  delete: [entity: unknown];
  'action-executed': [action: ActionMetadata];
}>();

// Reactive so it updates if the prop changes after setup
const resolvedActions = computed(() => props.actions ?? []);

async function handleSave(values: Record<string, unknown>) {
  await props.view.save();
  emit('save', values);
}

function handleAction(action: ActionMetadata) {
  emit('action-executed', action);
}
</script>
