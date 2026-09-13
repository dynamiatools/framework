// crudActionDispatch.ts — shared save/delete dispatch logic (extracted out of useCrudPage.ts so
// any other CRUD-invocation surface, e.g. a standalone <Crud>, can reuse the exact same
// action-aware behavior instead of reimplementing it).

import type { ActionExecutionRequest, CrudResourceApi, DynamiaClient, EntityMetadata } from '@dynamia-tools/sdk';
import { runActionFlow, type FlowStepHandlers } from './runActionFlow.js';
import { isDeleteCrudAction, isSaveCrudAction } from './crudActionUtils.js';

/**
 * Context shared by {@link dispatchCrudSave} and {@link dispatchCrudDelete}.
 */
export interface CrudActionDispatchContext {
  /** DynamiaClient instance used to run a resolved action's flow. */
  client: DynamiaClient;
  /** CRUD resource API bound to the entity's virtual path, used for the plain-REST fallback. */
  api: CrudResourceApi;
  /** Entity metadata — its `actions` are searched for a registered "save"/"delete" action. */
  entityMetadata: EntityMetadata | null | undefined;
  /** Entity class name, passed as `dataType`/`className` to the action execution request. */
  entityClass: string | null;
  /** Feedback primitives {@link runActionFlow} needs to render CONFIRM/INPUT/DIALOG/... steps. */
  handlers: FlowStepHandlers;
}

/**
 * Saves an entity (create or update) — action-aware: if the entity has a registered "save"
 * `CrudRemoteAction` (see `docs/design/SERVER_DRIVEN_ACTION_FLOWS.md` §7), drives it through
 * {@link runActionFlow} (Actions framework, `ActionFilter` hooks, optional confirm flow) instead
 * of calling the plain REST verb directly. An entity with no such action behaves exactly as
 * before — this is fully opt-in per entity.
 */
export async function dispatchCrudSave(
  ctx: CrudActionDispatchContext,
  mode: 'create' | 'edit',
  data: Record<string, unknown>,
): Promise<void> {
  const saveAction = ctx.entityMetadata?.actions?.find(isSaveCrudAction);
  if (saveAction) {
    const request: ActionExecutionRequest = { data, ...(ctx.entityClass !== null ? { dataType: ctx.entityClass } : {}) };
    await runActionFlow(ctx.client, saveAction, request, ctx.handlers, ctx.entityClass);
    return;
  }

  if (mode === 'create') {
    await ctx.api.create(data);
  } else {
    const id = data['id'] as string | number | undefined;
    if (id == null) throw new Error('Cannot update entity: "id" field is missing');
    await ctx.api.update(id, data);
  }
}

/**
 * Deletes an entity — action-aware, same pattern as {@link dispatchCrudSave}: typically resolves
 * to a `DeleteFlowRemoteAction`, which confirms before deleting.
 */
export async function dispatchCrudDelete(
  ctx: CrudActionDispatchContext,
  id: string | number,
): Promise<void> {
  const deleteAction = ctx.entityMetadata?.actions?.find(isDeleteCrudAction);
  if (deleteAction) {
    const request: ActionExecutionRequest = { dataId: String(id), ...(ctx.entityClass !== null ? { dataType: ctx.entityClass } : {}) };
    await runActionFlow(ctx.client, deleteAction, request, ctx.handlers, ctx.entityClass);
    return;
  }

  await ctx.api.delete(id);
}
