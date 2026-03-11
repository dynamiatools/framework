import type { HttpClient } from '../http.js';
import type { ActionExecutionRequest, ActionExecutionResponse } from './types.js';

/**
 * Execute platform actions (global or entity-scoped).
 */
export class ActionsApi {
  private readonly http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  /**
   * POST /api/app/metadata/actions/{action}
   * Execute a global action.
   */
  executeGlobal(action: string, request?: ActionExecutionRequest): Promise<ActionExecutionResponse> {
    return this.http.post<ActionExecutionResponse>(
      `/api/app/metadata/actions/${encodeURIComponent(action)}`,
      request ?? {},
    );
  }

  /**
   * POST /api/app/metadata/entities/{className}/action/{action}
   * Execute an entity-scoped action.
   */
  executeEntity(
    className: string,
    action: string,
    request?: ActionExecutionRequest,
  ): Promise<ActionExecutionResponse> {
    return this.http.post<ActionExecutionResponse>(
      `/api/app/metadata/entities/${encodeURIComponent(className)}/action/${encodeURIComponent(action)}`,
      request ?? {},
    );
  }
}
