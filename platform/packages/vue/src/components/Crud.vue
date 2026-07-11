<!-- Crud.vue: CRUD component orchestrating DataSetView (Table or Tree) + Form + Actions -->
<template>
  <div class="dynamia-crud">
    <div class="dynamia-crud-toolbar">
      <slot name="toolbar">
        <button
          v-if="!readOnly && view.showDataSet.value && !hasCreateAction"
          type="button"
          class="dynamia-crud-new-btn"
          @click="view.startCreate()"
        >
          New
        </button>
        <Actions
          :actions="resolvedActions"
          :view="view"
          :client="client"
          :auto-execute="autoExecuteActions"
          :entity-class-name="view.getEntityClassName() ?? undefined"
          @action="handleAction"
          @action-executed="emit('action-executed', $event)"
          @action-response="emit('action-response', $event)"
          @action-error="emit('action-error', $event)"
        />
      </slot>
    </div>

    <!-- Error banner -->
    <div v-if="view.errorMessage.value" class="dynamia-crud-error">
      {{ view.errorMessage.value }}
    </div>

    <!-- Dataset view (list mode) -->
    <template v-if="view.showDataSet.value">
      <!-- Table view (default) -->
      <Table
        v-if="view.dataSetView.isTableView()"
        :view="view.tableView!"
        :read-only="readOnly ?? false"
      >
        <template #actions="{ row }">
          <slot name="row-actions" :row="row">
            <button v-if="!readOnly" type="button" @click="view.startEdit(row)">Edit</button>
            <button v-if="!readOnly" type="button" @click="view.delete(row)">Delete</button>
          </slot>
        </template>
      </Table>

      <!-- Tree view -->
      <Tree
        v-else-if="view.dataSetView.isTreeView()"
        :view="view.treeView!"
        :read-only="readOnly ?? false"
        @select="handleTreeSelect"
      >
        <template #actions="{ node }">
          <slot name="node-actions" :node="node">
            <button v-if="!readOnly" type="button" @click="view.startEdit(node.data ?? node)">Edit</button>
            <button v-if="!readOnly" type="button" @click="view.delete(node.data ?? node)">Delete</button>
          </slot>
        </template>
      </Tree>
    </template>

    <!-- Form view (create / edit mode) -->
    <Form
      v-if="view.showForm.value"
      :view="view.formView"
      :read-only="readOnly ?? false"
      @submit="handleSave"
      @cancel="view.cancelEdit()"
    />

    <div v-if="view.isLoading.value" class="dynamia-crud-loading">
      <slot name="loading"><span>Saving…</span></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ActionMetadata, DynamiaClient } from '@dynamia-tools/sdk';
import type { ActionExecutionErrorEvent, ActionExecutionEvent, TreeNode } from '@dynamia-tools/ui-core';
import type { VueCrudView } from '../views/VueCrudView.js';
import Form from './Form.vue';
import Table from './Table.vue';
import Tree from './Tree.vue';
import Actions from './Actions.vue';
import { isCreateCrudAction } from '../actions/crudActionUtils.js';

const props = withDefaults(defineProps<{
  /** The VueCrudView instance to render */
  view: VueCrudView;
  /** Whether in read-only mode */
  readOnly?: boolean;
  /** Actions to show in the toolbar */
  actions?: ActionMetadata[];
  /** DynamiaClient used for automatic action execution */
  client?: DynamiaClient;
  /** Enables automatic SDK execution for non-local actions */
  autoExecuteActions?: boolean;
}>(), { readOnly: false, autoExecuteActions: false });

const emit = defineEmits<{
  save: [data: Record<string, unknown>];
  delete: [entity: unknown];
  select: [entity: unknown];
  action: [action: ActionMetadata];
  'action-executed': [action: ActionMetadata];
  'action-response': [event: ActionExecutionEvent];
  'action-error': [event: ActionExecutionErrorEvent];
}>();

const resolvedActions = computed(() => {
  const mergedActions = new Map<string, ActionMetadata>();

  for (const action of props.view.entityMetadata?.actions ?? []) {
    mergedActions.set(action.id, action);
  }

  for (const action of props.actions ?? []) {
    mergedActions.set(action.id, action);
  }

  return props.view.resolveActions(Array.from(mergedActions.values()));
});

const hasCreateAction = computed(() => resolvedActions.value.some(action => isCreateCrudAction(action)));

async function handleSave(values: Record<string, unknown>) {
  await props.view.save();
  emit('save', values);
}

function handleTreeSelect(node: TreeNode) {
  emit('select', node.data ?? node);
}

function handleAction(action: ActionMetadata) {
  emit('action', action);
}
</script>
