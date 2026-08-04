// runActionFlow.ts — drives a FlowRemoteAction to completion (shared by Actions.vue and useCrudPage.ts)

import type {
  ActionExecutionRequest,
  ActionExecutionResponse,
  ActionFlowStep,
  ActionMetadata,
  DynamiaClient,
} from '@dynamia-tools/sdk';
import type { FeedbackVariant } from '@dynamia-tools/ui-core';

/** Minimal shape of the Phase-0 feedback primitives {@link runActionFlow} needs to render a step. */
export interface FlowStepHandlers {
  confirm: (options: { message: string; title?: string }) => Promise<boolean>;
  showToast: (options: { message: string; variant?: FeedbackVariant; title?: string }) => string;
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
    const answer = await renderFlowStep(response.flow, handlers);
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
 * call). Only `CONFIRM` and `NOTIFY` are wired to a renderer today (the Phase-0 primitives —
 * `useConfirm`/`useToast`); `INPUT`/`DIALOG`/`REDIRECT`/`CALL`/`CUSTOM` are a documented future step.
 */
async function renderFlowStep(step: ActionFlowStep, handlers: FlowStepHandlers): Promise<unknown> {
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

    default:
      throw new Error(`runActionFlow: no renderer wired yet for flow step type "${step.type}"`);
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
