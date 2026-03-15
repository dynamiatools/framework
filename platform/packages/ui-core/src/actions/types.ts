import type { ActionExecutionRequest, ActionExecutionResponse, ActionMetadata } from '@dynamia-tools/sdk';

export interface ActionTriggerPayload {
  request?: Partial<ActionExecutionRequest>;
}

export interface ActionExecutionEvent {
  action: ActionMetadata;
  request: ActionExecutionRequest;
  response?: ActionExecutionResponse;
  local?: boolean;
}

export interface ActionExecutionErrorEvent {
  action: ActionMetadata;
  request: ActionExecutionRequest;
  error: unknown;
}

