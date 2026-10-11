<!-- Crud.vue: CRUD component orchestrating DataSetView (Table or Tree) + Form + Actions -->
<template>
  <div class="dynamia-crud">
    <div v-if="view.showDataSet.value || toolbarActions.length > 0" class="dynamia-crud-toolbar">
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
          :actions="toolbarActions"
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
            <RowActions
              v-if="!readOnly"
              :row="row"
              :actions="rowActions"
              :show-edit="!hasEditAction"
              :view="view"
              :client="client"
              :auto-execute="autoExecuteActions"
              :entity-class-name="view.getEntityClassName() ?? undefined"
              @edit="view.startEdit($event)"
              @action-executed="emit('action-executed', $event)"
              @action-response="emit('action-response', $event)"
              @action-error="emit('action-error', $event)"
            />
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
import { ClientActionRegistry, type ActionExecutionErrorEvent, type ActionExecutionEvent, type TreeNode } from '@dynamia-tools/ui-core';
import type { VueCrudView } from '../views/VueCrudView.js';
import Form from './Form.vue';
import Table from './Table.vue';
import Tree from './Tree.vue';
import Actions from './Actions.vue';
import RowActions from './RowActions.vue';
import {
  isCancelCrudAction,
  isCreateCrudAction,
  isEditCrudAction,
  isSaveCrudAction,
} from '../actions/crudActionUtils.js';

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

/**
 * A crud action of the server with no renderer of its own (`Delete`, `View`, the ones of the extensions) works on one
 * row, so it goes to the row menu. The rest stay in the toolbar: the ones that need no row (`New`, the search box and
 * the filters, which draw themselves with a renderer) and the ones that are not crud actions.
 */
function isRowAction(action: ActionMetadata): boolean {
  if (action.type !== 'CrudAction' || action.runtime === 'FRONTEND' || action.renderer) return false;
  if (ClientActionRegistry.resolve(action)?.renderer) return false;
  return !isCreateCrudAction(action) && !isSaveCrudAction(action) && !isCancelCrudAction(action);
}

const rowActions = computed(() => resolvedActions.value.filter(isRowAction));
// Save and Cancel are the form's own buttons, so they are not repeated in the toolbar.
const toolbarActions = computed(() => resolvedActions.value.filter(
  action => !isRowAction(action) && !isSaveCrudAction(action) && !isCancelCrudAction(action),
));
const hasCreateAction = computed(() => resolvedActions.value.some(action => isCreateCrudAction(action)));
const hasEditAction = computed(() => rowActions.value.some(action => isEditCrudAction(action)));

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
