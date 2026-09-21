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
  /**
   * **Experimental.** Performs the navigation for a `REDIRECT` step. Optional: defaults to
   * `window.location.assign(url)`. Apps with a client-side router can supply a router-aware version.
   */
  navigate?: (url: string) => void | Promise<void>;
}

/** Max nesting of `CALL` steps (a flow calling an action that itself calls...) before failing fast. */
const MAX_CALL_DEPTH = 5;

type ExecuteFn = (request: ActionExecutionRequest) => Promise<ActionExecutionResponse>;

/**
 * Drives a `FlowRemoteAction` to completion: calls `execute()`, and for as long as the response carries
 * a non-`DONE` `flow` step, renders it (confirm dialog, toast...) and calls `execute()` again with the
 * user's answer plus the step's `flowId`/`resumeToken`, until a terminal `DONE` step comes back.
 *
 * For a plain, non-flow `RemoteAction` (no `flow` in the response at all) this resolves on the very
 * first call, unchanged from before — see `docs/design/SERVER_DRIVEN_ACTION_FLOWS.md`.
 *
 * **Experimental** — `REDIRECT` and `CALL` steps:
 * - `REDIRECT` is terminal: the client navigates and the loop ends, resolving with the response that
 *   carried the `REDIRECT` step (`response.flow.type === 'REDIRECT'`). `awaitReturn: true` is rejected.
 * - `CALL` runs `step.data.action` (through this same loop, so it may itself be a flow) and resumes the
 *   calling flow with the nested response (minus its `flow`) as the answer.
 *
 * @experimental
 */
export async function runActionFlow(
  client: DynamiaClient,
  action: ActionMetadata,
  request: ActionExecutionRequest,
  handlers: FlowStepHandlers,
  className: string | null = null,
): Promise<ActionExecutionResponse> {
  return driveFlow(
    req => client.actions.execute(action, req, { className }),
    request,
    handlers,
    client,
    className ?? request.dataType ?? null,
    0,
  );
}

async function driveFlow(
  execute: ExecuteFn,
  request: ActionExecutionRequest,
  handlers: FlowStepHandlers,
  client: DynamiaClient,
  className: string | null,
  depth: number,
): Promise<ActionExecutionResponse> {
  let response = await execute(request);

  while (response.flow && response.flow.type !== 'DONE') {
    const step = response.flow;
    let answer: unknown;

    if (step.type === 'REDIRECT') {
      await redirect(step, handlers);
      return response; // terminal — see runActionFlow
    }

    if (step.type === 'CALL') {
      const nested = await runCallStep(step, handlers, client, className, depth);
      if (nested.flow?.type === 'REDIRECT') {
        return nested; // the nested flow already navigated away — nothing left to resume
      }
      const { flow: _flow, ...rest } = nested;
      answer = rest;
    } else {
      answer = await renderFlowStep(step, handlers, client, className);
    }

    response = await execute({
      ...request,
      flowId: step.flowId,
      data: answer,
      ...(step.resumeToken !== undefined ? { resumeToken: step.resumeToken } : {}),
    });
  }

  if (response.flow?.message) {
    handlers.showToast({ message: response.flow.message, variant: mapMessageTypeToVariant(response.flow.messageType) });
  }

  return response;
}

/** Only relative and http(s) URLs may be navigated to — a server-built `javascript:`/`data:` URL is never OK. */
function isSafeRedirectUrl(url: string): boolean {
  try {
    const { protocol } = new URL(url, 'http://placeholder.invalid');
    return protocol === 'http:' || protocol === 'https:';
  } catch {
    return false;
  }
}

async function redirect(step: ActionFlowStep, handlers: FlowStepHandlers): Promise<void> {
  const { url, awaitReturn } = (step.data ?? {}) as { url?: unknown; awaitReturn?: unknown };
  if (typeof url !== 'string' || !url) {
    throw new Error('runActionFlow: REDIRECT flow step is missing "data.url"');
  }
  if (awaitReturn) {
    throw new Error('runActionFlow: REDIRECT with "awaitReturn" is not supported yet (experimental)');
  }
  if (!isSafeRedirectUrl(url)) {
    throw new Error(`runActionFlow: REDIRECT to "${url}" refused — only relative and http(s) URLs are allowed`);
  }
  if (handlers.navigate) {
    await handlers.navigate(url);
  } else if (typeof window !== 'undefined') {
    window.location.assign(url);
  } else {
    throw new Error('runActionFlow: no "navigate" handler provided for flow step type "REDIRECT"');
  }
}

/**
 * Runs the action a `CALL` step names (`step.data.action`, plus optional `step.data.className` for an
 * entity-scoped action) through {@link driveFlow}. Any other `step.data` entries become the nested
 * request's `data`.
 */
async function runCallStep(
  step: ActionFlowStep,
  handlers: FlowStepHandlers,
  client: DynamiaClient,
  className: string | null,
  depth: number,
): Promise<ActionExecutionResponse> {
  const { action: actionId, className: callClassName, ...payload } = (step.data ?? {}) as Record<string, unknown>;
  if (typeof actionId !== 'string' || !actionId) {
    throw new Error('runActionFlow: CALL flow step is missing "data.action"');
  }
  if (depth >= MAX_CALL_DEPTH) {
    throw new Error(`runActionFlow: CALL nesting deeper than ${MAX_CALL_DEPTH} levels, aborting "${actionId}"`);
  }

  const targetClass = typeof callClassName === 'string' && callClassName ? callClassName : null;
  const request: ActionExecutionRequest = {
    ...(Object.keys(payload).length > 0 ? { data: payload } : {}),
    ...(targetClass ? { dataType: targetClass } : {}),
  };
  const execute: ExecuteFn = req => targetClass
    ? client.actions.executeEntity(targetClass, actionId, req)
    : client.actions.executeGlobal(actionId, req);

  return driveFlow(execute, request, handlers, client, targetClass ?? className, depth + 1);
}

/**
 * Renders one `ActionFlowStep` and resolves with the client's answer (`request.getData()` on the next
 * call).
 *
 * `CONFIRM`/`NOTIFY`/`INPUT`/`DIALOG`/`CUSTOM` are rendered here; `REDIRECT`/`CALL` need the loop's
 * control flow (terminal / nested run) and are handled by {@link driveFlow} before getting here.
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
      throw new Error(`runActionFlow: unsupported flow step type "${step.type}"`);
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
