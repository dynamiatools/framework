// runActionFlow.ts — drives a FlowRemoteAction to completion. Framework free: a front end (Vue, POS, shop) provides the handlers
// that render each step; this loop only speaks the protocol (see docs/next/dynamia-ui.md, I7).

import type {
  ActionExecutionRequest,
  ActionExecutionResponse,
  ActionFlowStep,
  ActionMetadata,
  FlowDownload,
  FlowFileRef,
  EntityMetadata,
  JobStatus,
  ViewDescriptor,
  DynamiaClient,
} from '@dynamia-tools/sdk';
import type { FeedbackVariant } from '../feedback/types.js';
import { FlowStepRendererRegistry } from '../actions/FlowStepRenderer.js';

/**
 * What a front end needs to render a `DIALOG` or `VIEW` step as a form: the runner already fetched the descriptor and the
 * entity metadata and has the values, the message of a form shown again and the size hints. The handler builds and shows its
 * own form and resolves with what the user submitted, or `null` if they cancelled.
 */
export interface FlowFormRequest {
  descriptor: ViewDescriptor;
  entity: EntityMetadata | null;
  /** Values to prefill (the entity, or what the user sent before a validation error). */
  values?: Record<string, unknown>;
  title?: string;
  /** Show it read only with a close button (the `VIEW` step). */
  readonly?: boolean;
  /** Why the form is shown again: validation error, changed question. */
  message?: string;
  messageType?: string;
  /** Message of each field that failed, by field name. */
  fieldErrors?: Record<string, string>;
  width?: string;
  height?: string;
}

/** The primitives {@link runActionFlow} needs to render the steps of a flow. A front end implements them. */
export interface FlowHandlers {
  confirm: (options: { message: string; title?: string }) => Promise<boolean>;
  showToast: (options: { message: string; variant?: FeedbackVariant; title?: string }) => string;
  /**
   * Renders an `INPUT` step. Optional for backward compatibility — callers that don't supply it get the
   * same "no renderer wired" error `INPUT` steps threw before, only for `INPUT` specifically.
   */
  prompt?: (options: { message: string; title?: string }) => Promise<string | null>;
  /**
   * Renders a `DIALOG` or `VIEW` step as a form ({@link FlowFormRequest}: descriptor fetched and values prefilled by
   * {@link runActionFlow} itself) and returns what was submitted, or `null` if the user cancelled.
   */
  showForm?: (request: FlowFormRequest) => Promise<Record<string, unknown> | null>;
  /**
   * **Experimental.** Performs the navigation for a `REDIRECT` step. Optional: defaults to
   * `window.location.assign(url)`. Apps with a client-side router can supply a router-aware version.
   */
  navigate?: (url: string, options?: { newWindow?: boolean }) => void | Promise<void>;
  /**
   * Renders a `CHOICE` step: shows the labels and returns the **keys** chosen (empty or `null` if the user cancels).
   * Required for `CHOICE` steps; there is no sensible default look.
   */
  choose?: (options: { title?: string; options: FlowChoiceOption[]; multiple: boolean; message?: string }) => Promise<string[] | null>;
  /**
   * Renders an `UPLOAD` step: asks the user for files and returns them, or `null`/`[]` if they cancel. The runner checks
   * them against the limits of the step, sends each one to `/api/app/transfers` and answers with the references; the
   * content never travels inside the flow. Optional: defaults to the browser's file picker ({@link browserPickFiles}).
   */
  pickFiles?: (options: { title?: string; accept?: string; multiple?: boolean }) => Promise<File[] | null>;
  /**
   * Shows the progress of the background task of a `PROGRESS` step, called every time the job is polled. Optional: without it
   * the task is followed silently.
   */
  showProgress?: (status: JobStatus) => void;
  /** Milliseconds between two polls of a job. Defaults to 500. */
  progressIntervalMs?: number;
  /**
   * Gives a file the action produced (`params.downloads` of the final response) to the user. Optional: defaults to a
   * browser download ({@link browserSaveFile}): a plain link when requests are authenticated with cookies, otherwise the
   * file is fetched with the client's credentials.
   */
  saveFile?: (file: FlowDownload, client: DynamiaClient) => void | Promise<void>;
}

/** An option of a `CHOICE` step: the client answers with the `key`. */
export interface FlowChoiceOption {
  key: string;
  label: string;
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
  handlers: FlowHandlers,
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
  handlers: FlowHandlers,
  client: DynamiaClient,
  className: string | null,
  depth: number,
): Promise<ActionExecutionResponse> {
  let response = await execute(request);

  while (response.flow && response.flow.type !== 'DONE') {
    const step = response.flow;
    let answer: unknown;

    if (step.type === 'REDIRECT') {
      await saveDownloads(response, handlers, client);
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

  await saveDownloads(response, handlers, client);

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

async function redirect(step: ActionFlowStep, handlers: FlowHandlers): Promise<void> {
  const { url, awaitReturn, newWindow } = (step.data ?? {}) as { url?: unknown; awaitReturn?: unknown; newWindow?: unknown };
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
    await (newWindow === true ? handlers.navigate(url, { newWindow: true }) : handlers.navigate(url));
  } else if (typeof window !== 'undefined') {
    if (newWindow === true) {
      window.open(url, '_blank', 'noopener');
    } else {
      window.location.assign(url);
    }
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
  handlers: FlowHandlers,
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
  handlers: FlowHandlers,
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

    case 'VIEW':
      await renderDialogStep(step, handlers, client, className, true);
      return true; // the user only had to see it

    case 'CHOICE': {
      if (!handlers.choose) {
        throw new Error('runActionFlow: no "choose" handler provided for flow step type "CHOICE"');
      }
      const { options, multiple } = (step.data ?? {}) as { options?: FlowChoiceOption[]; multiple?: boolean };
      return (await handlers.choose({
        ...(step.title ? { title: step.title } : {}),
        ...(step.message ? { message: step.message } : {}),
        options: options ?? [],
        multiple: multiple === true,
      })) ?? [];
    }

    case 'UPLOAD': {
      const limits = (step.data ?? {}) as UploadStepData;
      const pick = handlers.pickFiles ?? browserPickFiles;
      const files = (await pick({
        ...(step.title ? { title: step.title } : {}),
        ...(limits.accept ? { accept: limits.accept } : {}),
        multiple: limits.multiple === true,
      })) ?? [];
      checkUploadLimits(files, limits);
      const refs: FlowFileRef[] = [];
      for (const file of files) {
        refs.push(await client.transfers.upload(file));
      }
      return refs.map(({ ref }) => ({ ref }));
    }

    case 'PROGRESS': {
      // the action runs a long task in the background: follow it until it stops, then say so; the server reads the real state
      const { jobId } = (step.data ?? {}) as { jobId?: string };
      if (!jobId) {
        throw new Error('runActionFlow: PROGRESS flow step is missing "jobId"');
      }
      const interval = handlers.progressIntervalMs ?? 500;
      for (;;) {
        const status = await client.jobs.status(jobId);
        handlers.showProgress?.(status);
        if (status.state !== 'RUNNING') {
          return { jobId, state: status.state };
        }
        await new Promise(resolve => setTimeout(resolve, interval));
      }
    }

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
 * Fetches `step.viewDescriptor` (and best-effort entity metadata) for `className` and hands the front end a
 * {@link FlowFormRequest} to render.
 */
async function renderDialogStep(
  step: ActionFlowStep,
  handlers: FlowHandlers,
  client: DynamiaClient,
  className: string | null,
  readonly = false,
): Promise<Record<string, unknown> | null> {
  if (!handlers.showForm) {
    throw new Error('runActionFlow: no "showForm" handler provided for flow step type "DIALOG"');
  }
  if (!step.viewDescriptor) {
    throw new Error('runActionFlow: DIALOG flow step is missing "viewDescriptor"');
  }
  const viewClass = step.viewClass ?? className;
  if (!viewClass) {
    throw new Error('runActionFlow: DIALOG flow step requires a known entity class (viewClass or dataType/className)');
  }

  const [descriptor, entity] = await Promise.all([
    client.metadata.getEntityView(viewClass, step.viewDescriptor),
    client.metadata.getEntity(viewClass).catch(() => null),
  ]);

  return handlers.showForm({
    descriptor,
    entity,
    ...(step.data && typeof step.data === 'object' ? { values: step.data as Record<string, unknown> } : {}),
    ...(step.title !== undefined ? { title: step.title } : {}),
    ...(readonly ? { readonly: true } : {}),
    ...(step.message ? { message: step.message } : {}),
    ...(step.messageType ? { messageType: step.messageType } : {}),
    ...(step.fieldErrors ? { fieldErrors: { ...step.fieldErrors } } : {}),
    ...(step.hints?.['width'] ? { width: step.hints['width'] } : {}),
    ...(step.hints?.['height'] ? { height: step.hints['height'] } : {}),
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

/** The data of an `UPLOAD` step: what to ask and the limits the server will enforce. */
interface UploadStepData {
  accept?: string | null;
  multiple?: boolean;
  maxFiles?: number;
  maxFileSize?: number;
  maxTotalSize?: number;
}

/** Gives quick feedback before sending anything; the server enforces the same limits again. */
function checkUploadLimits(files: File[], limits: UploadStepData): void {
  if (limits.maxFiles && files.length > limits.maxFiles) {
    throw new Error(`At most ${limits.maxFiles} file(s) are allowed, got ${files.length}`);
  }
  let total = 0;
  for (const file of files) {
    if (limits.maxFileSize && file.size > limits.maxFileSize) {
      throw new Error(`${file.name} is ${file.size} bytes, the limit is ${limits.maxFileSize}`);
    }
    if (!acceptsFile(limits.accept, file)) {
      throw new Error(`${file.name} is not of an accepted type: ${limits.accept}`);
    }
    total += file.size;
  }
  if (limits.maxTotalSize && total > limits.maxTotalSize) {
    throw new Error(`The files add up to ${total} bytes, the limit is ${limits.maxTotalSize}`);
  }
}

function acceptsFile(accept: string | null | undefined, file: File): boolean {
  if (!accept || !accept.trim()) return true;
  const name = file.name.toLowerCase();
  const type = (file.type || '').toLowerCase();
  return accept.split(',').map(rule => rule.trim().toLowerCase()).filter(Boolean).some(rule =>
    (rule.startsWith('.') && name.endsWith(rule))
    || (rule.endsWith('/*') && type.startsWith(rule.slice(0, -1)))
    || rule === type);
}

/** Hands the files the action produced (`params.downloads`) to the user. */
async function saveDownloads(response: ActionExecutionResponse, handlers: FlowHandlers, client: DynamiaClient): Promise<void> {
  const downloads = response.params?.downloads;
  if (!Array.isArray(downloads)) return;
  const save = handlers.saveFile ?? browserSaveFile;
  for (const file of downloads as FlowDownload[]) {
    await save(file, client);
  }
}

/** Default `UPLOAD` renderer: the browser's file picker. Resolves with `null` when the user cancels. */
export function browserPickFiles(options: { accept?: string; multiple?: boolean }): Promise<File[] | null> {
  return new Promise(resolve => {
    const input = document.createElement('input');
    input.type = 'file';
    if (options.accept) input.accept = options.accept;
    input.multiple = options.multiple === true;
    input.addEventListener('cancel', () => resolve(null));
    input.addEventListener('change', () => {
      const files = Array.from(input.files ?? []);
      resolve(files.length ? files : null);
    });
    input.click();
  });
}

/**
 * Default `params.downloads` handler. With cookie authentication the browser streams the file from a plain link; with a
 * token or basic authentication the file is fetched with the client's credentials and saved from a temporary object URL.
 */
export async function browserSaveFile(file: FlowDownload, client: DynamiaClient): Promise<void> {
  const link = document.createElement('a');
  link.download = file.name;
  let objectUrl: string | null = null;
  if (client.transfers.needsAuthorizedFetch()) {
    objectUrl = URL.createObjectURL(await client.transfers.download(file.url));
    link.href = objectUrl;
  } else {
    link.href = client.transfers.absoluteUrl(file.url);
  }
  document.body.appendChild(link);
  link.click();
  link.remove();
  if (objectUrl) URL.revokeObjectURL(objectUrl);
}
