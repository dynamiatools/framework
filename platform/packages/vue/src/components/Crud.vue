<!-- Crud.vue: CRUD component orchestrating DataSetView (Table or Tree) + Form + Actions -->
<template>
  <div class="dynamia-crud">
    <div class="dynamia-crud-toolbar">
      <slot name="toolbar">
        <button
          v-if="!readOnly && view.showDataSet.value"
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
import type { ActionMetadata } from '@dynamia-tools/sdk';
import type { TreeNode } from '@dynamia-tools/ui-core';
import type { VueCrudView } from '../views/VueCrudView.js';
import Form from './Form.vue';
import Table from './Table.vue';
import Tree from './Tree.vue';
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
  select: [entity: unknown];
  'action-executed': [action: ActionMetadata];
}>();

const resolvedActions = computed(() => props.actions ?? []);

async function handleSave(values: Record<string, unknown>) {
  await props.view.save();
  emit('save', values);
}

function handleTreeSelect(node: TreeNode) {
  emit('select', node.data ?? node);
}

function handleAction(action: ActionMetadata) {
  emit('action-executed', action);
}
</script>
