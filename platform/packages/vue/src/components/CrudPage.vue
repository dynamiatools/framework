<!-- CrudPage.vue: auto-renders a full CRUD interface for a CrudPage NavigationNode -->
<template>
  <div class="dynamia-crud-page">
    <!-- Loading state -->
    <div v-if="loading" class="dynamia-crud-page-loading">
      <slot name="loading">
        <span>Loading...</span>
      </slot>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="dynamia-crud-page-error">
      <slot name="error" :error="error">
        <span>{{ error }}</span>
      </slot>
    </div>

    <!-- CRUD view -->
    <Crud
      v-else-if="view"
      :view="view"
      :client="client"
      :auto-execute-actions="true"
      :read-only="readOnly ?? false"
      :actions="actions ?? []"
      @save="emit('save', $event)"
      @delete="emit('delete', $event)"
      @action="emit('action', $event)"
      @action-executed="emit('action-executed', $event)"
      @action-response="emit('action-response', $event)"
      @action-error="emit('action-error', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { watch } from 'vue';
import type { NavigationNode, DynamiaClient, ActionMetadata } from '@dynamia-tools/sdk';
import type { ActionExecutionErrorEvent, ActionExecutionEvent } from '@dynamia-tools/ui-core';
import { useCrudPage } from '../composables/useCrudPage.js';
import Crud from './Crud.vue';

/**
 * Fully-automatic CRUD page component driven by a `CrudPage` NavigationNode.
 *
 * Pass the node and a DynamiaClient — the component resolves entity metadata,
 * builds the view descriptor and wires up all CRUD operations automatically.
 *
 * Example:
 * ```vue
 * <CrudPage :node="navigationNode" :client="dynamiaClient" />
 * ```
 */
const props = withDefaults(
  defineProps<{
    /** NavigationNode with type "CrudPage" */
    node: NavigationNode;
    /** DynamiaClient instance for all API calls */
    client: DynamiaClient;
    /** Whether to render in read-only mode (no create / edit / delete) */
    readOnly?: boolean;
    /** Optional extra toolbar actions */
    actions?: ActionMetadata[];
  }>(),
  { readOnly: false, actions: () => [] },
);

const emit = defineEmits<{
  /** Emitted after a successful save (create or update) */
  save: [data: Record<string, unknown>];
  /** Emitted after a successful delete */
  delete: [entity: unknown];
  /** Emitted when an action is triggered */
  action: [action: ActionMetadata];
  /** Emitted when a toolbar action is executed */
  'action-executed': [action: ActionMetadata];
  /** Emitted with the detailed action execution result */
  'action-response': [event: ActionExecutionEvent];
  /** Emitted when automatic action execution fails */
  'action-error': [event: ActionExecutionErrorEvent];
}>();

const { view, loading, error, reload } = useCrudPage({
  node: props.node,
  client: props.client,
});

// Re-initialize whenever the navigation node changes (user navigates to a
// different CrudPage while this component stays mounted).
watch(
  () => props.node,
  (newNode) => { void reload(newNode); },
);
</script>


