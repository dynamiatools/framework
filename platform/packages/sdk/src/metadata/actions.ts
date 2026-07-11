import type { HttpClient } from '../http.js';
import type { ActionExecutionRequest, ActionExecutionResponse, ActionMetadata } from './types.js';

export interface ExecuteActionOptions {
  /** Explicit entity class name for ClassAction / CrudAction execution. */
  className?: string | null;
}

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

  /**
   * Execute an action from metadata, automatically choosing the global or
   * entity-scoped endpoint.
   */
  execute(
    action: string | ActionMetadata,
    request?: ActionExecutionRequest,
    options?: ExecuteActionOptions,
  ): Promise<ActionExecutionResponse> {
    if (typeof action === 'string') {
      return this.executeGlobal(action, request);
    }

    const className = this.resolveEntityClassName(action, request, options);
    if (className) {
      return this.executeEntity(className, action.id, request);
    }

    return this.executeGlobal(action.id, request);
  }

  private resolveEntityClassName(
    action: ActionMetadata,
    request?: ActionExecutionRequest,
    options?: ExecuteActionOptions,
  ): string | null {
    const explicitClassName = options?.className?.trim();
    if (explicitClassName) {
      return explicitClassName;
    }

    const requestDataType = request?.dataType?.trim();
    if (requestDataType) {
      return requestDataType;
    }

    const applicableClasses = (action.applicableClasses ?? []).filter(
      className => className && className.toLowerCase() !== 'all',
    );

    if (applicableClasses.length === 1) {
      return applicableClasses[0] ?? null;
    }

    if (this.isEntityScopedAction(action)) {
      throw new Error(
        `Action "${action.id}" requires an entity class name. `
        + 'Provide ExecuteActionOptions.className or ActionExecutionRequest.dataType.',
      );
    }

    return null;
  }

  private isEntityScopedAction(action: ActionMetadata): boolean {
    return action.type === 'ClassAction'
      || action.type === 'CrudAction'
      || (action.applicableClasses?.length ?? 0) > 0
      || (action.applicableStates?.length ?? 0) > 0;
  }
}
