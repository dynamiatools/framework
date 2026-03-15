<!-- Actions.vue: Toolbar rendering resolved actions as buttons or custom registered renderers -->
<template>
  <div class="dynamia-actions">
    <component
      :is="resolveRenderer(action)"
      v-for="action in actions"
      :key="action.id"
      :action="action"
      :view="view"
      :executing="Boolean(executing[action.id])"
      @trigger="handleTrigger(action, $event)"
    />
    <slot />
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import type { Component } from 'vue';
import type { ActionExecutionRequest, ActionMetadata, DynamiaClient } from '@dynamia-tools/sdk';
import {
  ActionRendererRegistry,
  CrudView,
  type ActionExecutionErrorEvent,
  type ActionExecutionEvent,
  type ActionTriggerPayload,
  type View,
} from '@dynamia-tools/ui-core';
import { VueButtonActionRenderer } from '../action-renderers/VueButtonActionRenderer.js';
import {
  isCancelCrudAction,
  isCreateCrudAction,
  isDeleteCrudAction,
  isEditCrudAction,
  isSaveCrudAction,
} from '../actions/crudActionUtils.js';

const props = withDefaults(defineProps<{
  /** List of resolved actions to display */
  actions: ActionMetadata[];
  /** The view this actions toolbar belongs to */
  view?: View | undefined;
  /** DynamiaClient used for optional automatic action execution */
  client?: DynamiaClient | undefined;
  /** Automatically execute non-local actions using the client */
  autoExecute?: boolean | undefined;
  /** Explicit entity class name used for entity-scoped execution */
  entityClassName?: string | undefined;
  /** Request values to merge into the generated action request */
  request?: Partial<ActionExecutionRequest>;
}>(), {
  autoExecute: false,
  request: () => ({}),
});

const emit = defineEmits<{
  /** Emitted when an action button is clicked */
  action: [action: ActionMetadata];
  /** Emitted after a local or remote action finishes successfully */
  'action-executed': [action: ActionMetadata];
  /** Emitted with the full execution payload */
  'action-response': [event: ActionExecutionEvent];
  /** Emitted when automatic execution fails */
  'action-error': [event: ActionExecutionErrorEvent];
}>();

const executing = reactive<Record<string, boolean>>({});

function resolveRenderer(action: ActionMetadata): Component {
  return ActionRendererRegistry.get<Component>(action.renderer) ?? VueButtonActionRenderer;
}

async function handleTrigger(action: ActionMetadata, payload?: ActionTriggerPayload): Promise<void> {
  emit('action', action);

  const request = buildRequest(action, payload?.request);

  try {
    const handledLocally = await tryHandleCrudActionLocally(action, request);
    if (handledLocally) {
      emit('action-executed', action);
      emit('action-response', { action, request, local: true });
      return;
    }

    if (!props.autoExecute || !props.client) {
      return;
    }

    executing[action.id] = true;
    const response = await props.client.actions.execute(action, request, {
      className: resolveEntityClassName(request),
    });
    emit('action-executed', action);
    emit('action-response', { action, request, response, local: false });
  } catch (error) {
    emit('action-error', { action, request, error });
    throw error;
  } finally {
    executing[action.id] = false;
  }
}

function buildRequest(
  action: ActionMetadata,
  overrides: Partial<ActionExecutionRequest> = {},
): ActionExecutionRequest {
  const crudView = props.view instanceof CrudView ? props.view : null;
  const crudRequest = crudView?.buildActionExecutionRequest(
    {
      ...props.request,
      ...overrides,
      params: {
        ...(props.request?.params ?? {}),
        ...(overrides.params ?? {}),
      },
    },
    isDeleteCrudAction(action) ? 'DELETE' : crudView?.getCrudActionState(),
  );

  return {
    ...(crudRequest ?? {}),
    ...props.request,
    ...overrides,
    params: {
      ...(crudRequest?.params ?? {}),
      ...(props.request?.params ?? {}),
      ...(overrides.params ?? {}),
    },
  };
}

function resolveEntityClassName(request: ActionExecutionRequest): string | null {
  if (props.entityClassName) {
    return props.entityClassName;
  }

  if (props.view instanceof CrudView) {
    return props.view.getEntityClassName();
  }

  return request.dataType ?? null;
}

async function tryHandleCrudActionLocally(
  action: ActionMetadata,
  request: ActionExecutionRequest,
): Promise<boolean> {
  if (!(props.view instanceof CrudView)) {
    return false;
  }

  if (isCreateCrudAction(action)) {
    props.view.startCreate();
    return true;
  }

  if (isEditCrudAction(action)) {
    const entity = request.data ?? props.view.getActionData('READ');
    if (entity == null) return false;
    props.view.startEdit(entity);
    return true;
  }

  if (isDeleteCrudAction(action)) {
    const entity = request.data ?? props.view.getActionData('DELETE');
    if (entity == null) return false;
    await props.view.delete(entity);
    return true;
  }

  if (isSaveCrudAction(action)) {
    await props.view.save();
    return true;
  }

  if (isCancelCrudAction(action)) {
    props.view.cancelEdit();
    return true;
  }

  return false;
}
</script>
