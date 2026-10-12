// runActionFlow.ts — the Vue side of the flow runner. The loop itself lives in @dynamia-tools/ui-core (framework free, shared with
// POS and shop); this file only provides what is Vue specific: a DIALOG/VIEW step becomes a VueFormView shown through the
// DialogFormManager.

import type { ActionExecutionRequest, ActionExecutionResponse, ActionMetadata, DynamiaClient } from '@dynamia-tools/sdk';
import { runActionFlow as runFlow } from '@dynamia-tools/ui-core';
import type { FlowFormRequest, FlowHandlers } from '@dynamia-tools/ui-core';
import { VueFormView } from '../views/VueFormView.js';
import type { DialogFormOptions } from './DialogFormManager.js';

export type { FlowChoiceOption } from '@dynamia-tools/ui-core';

/**
 * The handlers of the flow runner for a Vue app: those of {@link FlowHandlers}, except that a form is rendered from an
 * already built {@link VueFormView} ({@link DialogFormOptions}) instead of a neutral request.
 */
export interface FlowStepHandlers extends Omit<FlowHandlers, 'showForm'> {
  /**
   * Renders a `DIALOG` or `VIEW` step: the descriptor was fetched and the {@link VueFormView} built and prefilled by this layer,
   * the handler only shows it and returns what was submitted (or `null` if cancelled).
   */
  showFormDialog?: (options: DialogFormOptions) => Promise<Record<string, unknown> | null>;
}

/**
 * Drives a `FlowRemoteAction` to completion in a Vue app. See `runActionFlow` in `@dynamia-tools/ui-core` for the protocol.
 *
 * @experimental
 */
export function runActionFlow(
  client: DynamiaClient,
  action: ActionMetadata,
  request: ActionExecutionRequest,
  handlers: FlowStepHandlers,
  className: string | null = null,
): Promise<ActionExecutionResponse> {
  return runFlow(client, action, request, toFlowHandlers(handlers), className);
}

function toFlowHandlers(handlers: FlowStepHandlers): FlowHandlers {
  const { showFormDialog, ...rest } = handlers;
  return showFormDialog ? { ...rest, showForm: request => showVueForm(request, showFormDialog) } : rest;
}

async function showVueForm(
  request: FlowFormRequest,
  show: (options: DialogFormOptions) => Promise<Record<string, unknown> | null>,
): Promise<Record<string, unknown> | null> {
  const view = new VueFormView(request.descriptor, request.entity);
  await view.initialize();
  if (request.values) {
    view.setValue(request.values);
  }
  if (request.fieldErrors) {
    view.errors.value = { ...request.fieldErrors };
  }
  return show({
    view,
    ...(request.title !== undefined ? { title: request.title } : {}),
    ...(request.readonly ? { readonly: true } : {}),
    ...(request.message ? { message: request.message } : {}),
    ...(request.messageType ? { messageType: request.messageType } : {}),
    ...(request.width ? { width: request.width } : {}),
    ...(request.height ? { height: request.height } : {}),
  });
}
