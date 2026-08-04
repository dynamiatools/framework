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
import type {
  ActionExecutionRequest,
  ActionExecutionResponse,
  ActionFlowStep,
  ActionMetadata,
  DynamiaClient,
} from '@dynamia-tools/sdk';
import {
  ActionRendererRegistry,
  ClientActionRegistry,
  CrudView,
  type ActionExecutionErrorEvent,
  type ActionExecutionEvent,
  type ActionTriggerPayload,
  type FeedbackVariant,
  type View,
} from '@dynamia-tools/ui-core';
import { VueButtonActionRenderer } from '../action-renderers/VueButtonActionRenderer.js';
import { useConfirm } from '../composables/useConfirm.js';
import { useToast } from '../composables/useToast.js';
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
const { confirm } = useConfirm();
const { show: showToast } = useToast();

function resolveRenderer(action: ActionMetadata): Component {
  return ActionRendererRegistry.get<Component>(action.renderer) ?? VueButtonActionRenderer;
}

async function handleTrigger(action: ActionMetadata, payload?: ActionTriggerPayload): Promise<void> {
  emit('action', action);

  const request = buildRequest(action, payload?.request);

  try {
    // 1. Registered client actions (highest priority — can override any action)
    const clientHandled = await tryHandleClientAction(action, request);
    if (clientHandled) {
      emit('action-executed', action);
      emit('action-response', { action, request, local: true });
      return;
    }

    // 2. Built-in CRUD local actions (new / edit / save / cancel / delete)
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
    const response = await runFlow(props.client, action, request);
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

async function tryHandleClientAction(
  action: ActionMetadata,
  request: ActionExecutionRequest,
): Promise<boolean> {
  const clientAction = ClientActionRegistry.resolve(action);
  if (!clientAction) return false;
  await clientAction.execute({
    action,
    request,
    ...(props.view !== undefined ? { view: props.view } : {}),
  });
  return true;
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

/**
 * Drives a `FlowRemoteAction` to completion: calls `execute()`, and for as long as the response carries
 * a non-`DONE` `flow` step, renders it (confirm dialog, toast...) and calls `execute()` again with the
 * user's answer plus the step's `flowId`/`resumeToken`, until a terminal `DONE` step comes back.
 *
 * For a plain, non-flow `RemoteAction` (no `flow` in the response at all) this resolves on the very
 * first call, unchanged from before — see `docs/design/SERVER_DRIVEN_ACTION_FLOWS.md`.
 */
async function runFlow(
  client: DynamiaClient,
  action: ActionMetadata,
  request: ActionExecutionRequest,
): Promise<ActionExecutionResponse> {
  const className = resolveEntityClassName(request);
  let response = await client.actions.execute(action, request, { className });

  while (response.flow && response.flow.type !== 'DONE') {
    const answer = await renderFlowStep(response.flow);
    response = await client.actions.execute(action, {
      ...request,
      flowId: response.flow.flowId,
      data: answer,
      ...(response.flow.resumeToken !== undefined ? { resumeToken: response.flow.resumeToken } : {}),
    }, { className });
  }

  if (response.flow?.message) {
    showToast({ message: response.flow.message, variant: mapMessageTypeToVariant(response.flow.messageType) });
  }

  return response;
}

/**
 * Renders one `ActionFlowStep` and resolves with the client's answer (`request.getData()` on the next
 * call). Only `CONFIRM` and `NOTIFY` are wired to a renderer today (the Phase-0 primitives —
 * `useConfirm`/`useToast`); `INPUT`/`DIALOG`/`REDIRECT`/`CALL`/`CUSTOM` are a documented future step.
 */
async function renderFlowStep(step: ActionFlowStep): Promise<unknown> {
  switch (step.type) {
    case 'CONFIRM':
      return confirm({
        message: step.message ?? '',
        ...(step.title !== undefined ? { title: step.title } : {}),
      });

    case 'NOTIFY':
      showToast({
        message: step.message ?? '',
        variant: mapMessageTypeToVariant(step.messageType),
        ...(step.title !== undefined ? { title: step.title } : {}),
      });
      return undefined; // fire-and-forget — the flow auto-continues with no user answer

    default:
      throw new Error(`Actions.vue: no renderer wired yet for flow step type "${step.type}"`);
  }
}

function mapMessageTypeToVariant(messageType?: string): FeedbackVariant {
  switch (messageType) {
    case 'ERROR':
    case 'CRITICAL':
      return 'error';
    case 'WARNING':
      return 'warning';
    default:
      return 'info';
  }
}
</script>
