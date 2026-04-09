// ClientAction.ts — framework-agnostic client-side action system

import type { ActionExecutionRequest, ActionMetadata } from '@dynamia-tools/sdk';
import type { View } from '../view/View.js';
import { Registry } from '../registry/Registry.js';

// ── Context ───────────────────────────────────────────────────────────────────

/**
 * Runtime context passed to a {@link ClientAction} when it is executed.
 */
export interface ClientActionContext {
  /** The server-side metadata that triggered this client action. */
  action: ActionMetadata;
  /** Execution request built from the current view context. */
  request: ActionExecutionRequest;
  /** The active view the action belongs to, when available. */
  view?: View;
}

// ── Interface ─────────────────────────────────────────────────────────────────

/**
 * A **ClientAction** lives entirely in the frontend.
 *
 * Implement this interface to intercept an action identified by `id` (or the
 * server-side `className`) and handle it locally — without a server round-trip.
 *
 * ClientActions are resolved **before** built-in CRUD actions and before remote
 * execution, so they can override any server-side action if needed.
 *
 * Use `applicableClass` / `applicableState` to scope an action to a specific
 * entity type or CRUD state, and `renderer` to customise how it is rendered.
 *
 * Example — scoped export action:
 * <pre>{@code
 * registerClientAction({
 *   id: 'ExportCsvAction',
 *   name: 'Export CSV',
 *   applicableClass: 'Book',
 *   applicableState: 'READ',
 *   execute({ request }) {
 *     downloadCsv(request.data);
 *   },
 * });
 * }</pre>
 */
export interface ClientAction {
  /**
   * Must match the server-side action `id` **or** `className`.
   * Resolution is case-insensitive.
   */
  id: string;
  /** Optional display name (informational). */
  name?: string;
  /** Optional description (informational). */
  description?: string;
  /** Optional icon key (informational). */
  icon?: string;
  /**
   * Entity class name(s) this action applies to.
   * Accepts a single string or an array for multiple classes.
   * Absent / empty means the action applies to **all** classes.
   *
   * Example: `'Book'` or `['Book', 'Author']`
   */
  applicableClass?: string | string[];
  /**
   * CRUD state(s) in which this action is available.
   * Accepts a single string or an array.
   * Recognised values: `'READ'`, `'CREATE'`, `'UPDATE'`, `'DELETE'`
   * (case-insensitive; `'list'`/`'edit'`/`'create'` aliases are also supported).
   * Absent / empty means the action is available in **all** states.
   *
   * Example: `'READ'` or `['CREATE', 'UPDATE']`
   */
  applicableState?: string | string[];
  /**
   * Optional renderer key used to customise how this action is displayed.
   * Resolved via {@link ActionRendererRegistry} — falls back to the default
   * button renderer when absent or unregistered.
   */
  renderer?: string;
  /**
   * Execute the action.
   * May return a `Promise` — the caller will await it before continuing.
   */
  execute(ctx: ClientActionContext): void | Promise<void>;
}

// ── Applicability helpers ─────────────────────────────────────────────────────

/** @internal Coerce `string | string[] | undefined` → `string[]` */
function toArray(value?: string | string[]): string[] {
  if (!value) return [];
  return Array.isArray(value) ? value : [value];
}

/**
 * Returns `true` when `action` is applicable for the given `className` and/or
 * `state`.
 *
 * Rules:
 * - If `applicableClass` is empty / absent → matches any class.
 * - If `applicableClass` contains `"all"` (case-insensitive) → matches any class.
 * - Class matching is done on the **simple name** (last segment after `.`).
 * - If `applicableState` is empty / absent → matches any state.
 * - State matching is case-insensitive; `list` is treated as `READ`,
 *   `edit` as `UPDATE`.
 *
 * Example:
 * <pre>{@code
 * isClientActionApplicable(action, 'Book', 'READ');
 * }</pre>
 */
export function isClientActionApplicable(
  action: ClientAction,
  className?: string | null,
  state?: string | null,
): boolean {
  return _classMatches(toArray(action.applicableClass), className)
    && _stateMatches(toArray(action.applicableState), state);
}

function _simpleName(fqn: string): string {
  const dot = fqn.lastIndexOf('.');
  return dot >= 0 ? fqn.substring(dot + 1) : fqn;
}

function _normalizeState(s: string): string {
  switch (s.trim().toUpperCase()) {
    case 'LIST':
    case 'READ': return 'READ';
    case 'CREATE': return 'CREATE';
    case 'EDIT':
    case 'UPDATE': return 'UPDATE';
    case 'DELETE': return 'DELETE';
    default: return s.trim().toUpperCase();
  }
}

function _classMatches(applicableClasses: string[], className?: string | null): boolean {
  if (applicableClasses.length === 0) return true;
  if (applicableClasses.some(c => c.toLowerCase() === 'all')) return true;
  if (!className) return false;
  const targetSimple = _simpleName(className.trim()).toLowerCase();
  return applicableClasses.some(c => {
    const cLower = c.trim().toLowerCase();
    return cLower === targetSimple || cLower === className.trim().toLowerCase();
  });
}

function _stateMatches(applicableStates: string[], state?: string | null): boolean {
  if (applicableStates.length === 0) return true;
  if (!state) return false;
  const normalizedTarget = _normalizeState(state);
  return applicableStates.some(s => _normalizeState(s) === normalizedTarget);
}

// ── Key normalisation ─────────────────────────────────────────────────────────

function normalizeClientActionKey(key: string): string[] {
  const trimmed = key.trim();
  const lower = trimmed.toLowerCase();
  return [...new Set([trimmed, lower])];
}

// ── Registry class ────────────────────────────────────────────────────────────

/**
 * Registry for {@link ClientAction} instances.
 *
 * Lookup is case-insensitive and tries both `action.id` and `action.className`
 * from the server-side {@link ActionMetadata}.
 *
 * Use {@link filter} (inherited from {@link Registry}) for predicate-based
 * searches, or the convenience method {@link findApplicable} to filter by
 * entity class and CRUD state.
 */
export class ClientActionRegistryClass extends Registry<ClientAction> {
  constructor() {
    super(normalizeClientActionKey);
  }

  /**
   * Resolve a {@link ClientAction} for the given {@link ActionMetadata}.
   * Tries `action.id` first, then `action.className`.
   * Returns `null` when no client action is registered for either.
   */
  resolve(action: ActionMetadata): ClientAction | null {
    return this.get(action.id) ?? this.get(action.className ?? null);
  }

  /**
   * Find all registered {@link ClientAction}s applicable to the given entity
   * class and optional CRUD state.
   *
   * Example — get all READ actions for Book:
   * <pre>{@code
   * const actions = ClientActionRegistry.findApplicable('Book', 'READ');
   * }</pre>
   */
  findApplicable(className?: string | null, state?: string | null): ClientAction[] {
    return this.filter(action => isClientActionApplicable(action, className, state));
  }
}

// ── Singleton + convenience API ───────────────────────────────────────────────

/**
 * Global singleton registry for {@link ClientAction} instances.
 *
 * Use {@link registerClientAction} as the primary convenience API.
 */
export const ClientActionRegistry = new ClientActionRegistryClass();

/**
 * Register a {@link ClientAction} in the global {@link ClientActionRegistry}.
 *
 * Example:
 * <pre>{@code
 * registerClientAction({
 *   id: 'PrintAction',
 *   applicableClass: 'Book',
 *   execute() { window.print(); },
 * });
 * }</pre>
 */
export function registerClientAction(action: ClientAction): void {
  ClientActionRegistry.register(action.id, action);
}
