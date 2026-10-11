// builtinCrudActions.ts — registers New/Edit/Cancel as real ClientActions instead of leaving
// them hardcoded inside a component's action-dispatch logic (see @dynamia-tools/vue's
// Actions.vue, which used to special-case these three by id before this existed).
//
// Save/Delete are deliberately NOT here: they have real server-side CrudRemoteAction/
// FlowRemoteAction counterparts (see docs/design/SERVER_DRIVEN_ACTION_FLOWS.md §7) and must keep
// going through that resolution path — registering them as ClientActions would short-circuit
// them locally and they'd never reach runActionFlow.

import type { ClientAction } from './ClientAction.js';
import { ClientActionRegistry } from './ClientAction.js';
import { CrudView } from '../view/CrudView.js';
import type { View } from '../view/View.js';

/** @internal Narrows a ClientActionContext's `view` to a CrudView, or null when not one. */
function asCrudView(view?: View): CrudView | null {
  return view instanceof CrudView ? view : null;
}

const newAction: ClientAction = {
  id: 'NewAction',
  name: 'New',
  execute({ view }) {
    asCrudView(view)?.startCreate();
  },
};

const editAction: ClientAction = {
  id: 'EditAction',
  name: 'Edit',
  execute({ request, view }) {
    const crudView = asCrudView(view);
    if (!crudView) return;
    const entity = request.data ?? crudView.getActionData('READ');
    if (entity == null) return;
    crudView.startEdit(entity);
  },
};

const cancelAction: ClientAction = {
  id: 'CancelAction',
  name: 'Cancel',
  execute({ view }) {
    asCrudView(view)?.cancelEdit();
  },
};

/**
 * The search box of the CRUD, `FindAction` in Java (which is `FRONTEND`: only the front end draws it). The text arrives as
 * the `data` of the request, from the `find` renderer, and is sent to the server as the `_q` parameter.
 */
const findAction: ClientAction = {
  id: 'FindAction',
  name: 'Find',
  applicableState: 'READ',
  renderer: 'find',
  async execute({ request, view }) {
    const table = asCrudView(view)?.tableView;
    await table?.search(typeof request.data === 'string' ? request.data.trim() : '');
  },
};

/**
 * The advanced search of the CRUD, `FiltersAction` in Java: the `filters` renderer draws the panel and sends the values as
 * the `data` of the request (field name to value). An empty object clears the filters.
 */
const filtersAction: ClientAction = {
  id: 'FiltersAction',
  name: 'Filters',
  applicableState: 'READ',
  renderer: 'filters',
  async execute({ request, view }) {
    const table = asCrudView(view)?.tableView;
    const values = request.data && typeof request.data === 'object' ? (request.data as Record<string, unknown>) : {};
    await table?.setFilters(values);
  },
};

/**
 * Registers the built-in New/Edit/Cancel/Find/Filters {@link ClientAction}s in the global
 * {@link ClientActionRegistry}. Idempotent — safe to call more than once.
 *
 * Aliases match the identity strings the backend may send for these actions (either the Java
 * class's simple name, the default `id`, or a short conventional id — see
 * `tools.dynamia.crud.actions.EditAction` et al.). Registering under `id` alone already covers
 * the lowercased class-name form (`ClientActionRegistry`'s key normalization lowercases too);
 * the extra aliases below cover the short conventional ids.
 *
 * An app can override any of these by registering its own {@link ClientAction} under the same
 * id/alias — the last registration wins, so call this before any app-specific overrides (the
 * `DynamiaVue` plugin's `install()` does this automatically).
 *
 * Example — override the default Edit behavior:
 * <pre>{@code
 * registerClientAction({
 *   id: 'EditAction',
 *   execute({ view }) { ... custom edit flow ... },
 * });
 * }</pre>
 */
export function registerBuiltinCrudActions(): void {
  ClientActionRegistry.register(newAction.id, newAction, ['new', 'create', 'createaction']);
  ClientActionRegistry.register(editAction.id, editAction, ['edit']);
  ClientActionRegistry.register(cancelAction.id, cancelAction, ['cancel']);
  ClientActionRegistry.register(findAction.id, findAction, ['find']);
  ClientActionRegistry.register(filtersAction.id, filtersAction, ['filters']);
}
