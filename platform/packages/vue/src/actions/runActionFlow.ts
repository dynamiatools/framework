// runActionFlow.ts — drives a FlowRemoteAction to completion (shared by Actions.vue and useCrudPage.ts)

import type {
  ActionExecutionRequest,
  ActionExecutionResponse,
  ActionFlowStep,
  ActionMetadata,
  DynamiaClient,
} from '@dynamia-tools/sdk';
import type { FeedbackVariant } from '@dynamia-tools/ui-core';
import { FlowStepRendererRegistry } from '@dynamia-tools/ui-core';
import { VueFormView } from '../views/VueFormView.js';
import type { DialogFormOptions } from './DialogFormManager.js';

/** Minimal shape of the feedback primitives {@link runActionFlow} needs to render a step. */
export interface FlowStepHandlers {
  confirm: (options: { message: string; title?: string }) => Promise<boolean>;
  showToast: (options: { message: string; variant?: FeedbackVariant; title?: string }) => string;
  /**
   * Renders an `INPUT` step. Optional for backward compatibility — callers that don't supply it get the
   * same "no renderer wired" error `INPUT` steps threw before, only for `INPUT` specifically.
   */
  prompt?: (options: { message: string; title?: string }) => Promise<string | null>;
  /**
   * Renders a `DIALOG` step's already-built {@link DialogFormOptions} (descriptor fetched and prefilled
   * by {@link runActionFlow} itself — the handler only needs to show it and return what was submitted).
   */
  showFormDialog?: (options: DialogFormOptions) => Promise<Record<string, unknown> | null>;
}

/**
 * Drives a `FlowRemoteAction` to completion: calls `execute()`, and for as long as the response carries
 * a non-`DONE` `flow` step, renders it (confirm dialog, toast...) and calls `execute()` again with the
 * user's answer plus the step's `flowId`/`resumeToken`, until a terminal `DONE` step comes back.
 *
 * For a plain, non-flow `RemoteAction` (no `flow` in the response at all) this resolves on the very
 * first call, unchanged from before — see `docs/design/SERVER_DRIVEN_ACTION_FLOWS.md`.
 */
export async function runActionFlow(
  client: DynamiaClient,
  action: ActionMetadata,
  request: ActionExecutionRequest,
  handlers: FlowStepHandlers,
  className: string | null = null,
): Promise<ActionExecutionResponse> {
  let response = await client.actions.execute(action, request, { className });

  while (response.flow && response.flow.type !== 'DONE') {
    const answer = await renderFlowStep(response.flow, handlers, client, className ?? request.dataType ?? null);
    response = await client.actions.execute(action, {
      ...request,
      flowId: response.flow.flowId,
      data: answer,
      ...(response.flow.resumeToken !== undefined ? { resumeToken: response.flow.resumeToken } : {}),
    }, { className });
  }

  if (response.flow?.message) {
    handlers.showToast({ message: response.flow.message, variant: mapMessageTypeToVariant(response.flow.messageType) });
  }

  return response;
}

/**
 * Renders one `ActionFlowStep` and resolves with the client's answer (`request.getData()` on the next
 * call).
 *
 * `CONFIRM`/`NOTIFY`/`INPUT`/`DIALOG`/`CUSTOM` are wired; `REDIRECT`/`CALL` are not yet — their semantics
 * (does the client resume the *same* flow after a redirect/nested call, or are they always terminal?)
 * are still an open design question, see `docs/design/SERVER_DRIVEN_ACTION_FLOWS.md` §9.
 */
async function renderFlowStep(
  step: ActionFlowStep,
  handlers: FlowStepHandlers,
  client: DynamiaClient,
  className: string | null,
): Promise<unknown> {
  switch (step.type) {
    case 'CONFIRM':
      return handlers.confirm({
        message: step.message ?? '',
        ...(step.title !== undefined ? { title: step.title } : {}),
      });

    case 'NOTIFY':
      handlers.showToast({
        message: step.message ?? '',
        variant: mapMessageTypeToVariant(step.messageType),
        ...(step.title !== undefined ? { title: step.title } : {}),
      });
      return undefined; // fire-and-forget — the flow auto-continues with no user answer

    case 'INPUT':
      if (!handlers.prompt) {
        throw new Error('runActionFlow: no "prompt" handler provided for flow step type "INPUT"');
      }
      return handlers.prompt({
        message: step.message ?? '',
        ...(step.title !== undefined ? { title: step.title } : {}),
      });

    case 'DIALOG':
      return renderDialogStep(step, handlers, client, className);

    case 'CUSTOM': {
      const renderer = FlowStepRendererRegistry.resolve(step);
      if (!renderer) {
        const component = (step.data as { component?: string } | undefined)?.component;
        throw new Error(`runActionFlow: no renderer registered for CUSTOM flow step component "${component}"`);
      }
      return renderer(step);
    }

    default:
      throw new Error(`runActionFlow: no renderer wired yet for flow step type "${step.type}"`);
  }
}

/**
 * Fetches `step.viewDescriptor` (and best-effort entity metadata) for `className`, builds and
 * initializes a {@link VueFormView} prefilled with `step.data`, and hands it to the
 * `showFormDialog` handler to actually render.
 */
async function renderDialogStep(
  step: ActionFlowStep,
  handlers: FlowStepHandlers,
  client: DynamiaClient,
  className: string | null,
): Promise<Record<string, unknown> | null> {
  if (!handlers.showFormDialog) {
    throw new Error('runActionFlow: no "showFormDialog" handler provided for flow step type "DIALOG"');
  }
  if (!step.viewDescriptor) {
    throw new Error('runActionFlow: DIALOG flow step is missing "viewDescriptor"');
  }
  if (!className) {
    throw new Error('runActionFlow: DIALOG flow step requires a known entity class (dataType/className)');
  }

  const [descriptor, entityMetadata] = await Promise.all([
    client.metadata.getEntityView(className, step.viewDescriptor),
    client.metadata.getEntity(className).catch(() => null),
  ]);

  const view = new VueFormView(descriptor, entityMetadata);
  await view.initialize();
  if (step.data && typeof step.data === 'object') {
    view.setValue(step.data as Record<string, unknown>);
  }

  return handlers.showFormDialog({
    view,
    ...(step.title !== undefined ? { title: step.title } : {}),
  });
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
