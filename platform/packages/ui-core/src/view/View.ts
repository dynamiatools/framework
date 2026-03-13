// Abstract base class for all view types in ui-core

import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import type { ViewType } from './ViewType.js';
import type { ViewState } from '../types/state.js';

/** Handler function type for view events */
export type EventHandler = (payload?: unknown) => void;

/**
 * Abstract base class for all view types.
 * Mirrors the backend tools.dynamia.viewers.View contract.
 *
 * Example:
 * <pre>{@code
 * class MyCustomView extends View {
 *   async initialize() { ... }
 *   validate() { return true; }
 * }
 * }</pre>
 */
export abstract class View {
  /** The view type identity */
  readonly viewType: ViewType;
  /** The view descriptor from the backend */
  readonly descriptor: ViewDescriptor;
  /** Entity metadata for this view's bean class */
  readonly entityMetadata: EntityMetadata | null;
  /** Current view state */
  protected state: ViewState;

  private readonly _eventHandlers = new Map<string, Set<EventHandler>>();

  constructor(viewType: ViewType, descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    this.viewType = viewType;
    this.descriptor = descriptor;
    this.entityMetadata = entityMetadata;
    this.state = { loading: false, error: null, initialized: false };
  }

  /** Initialize the view. Must be called after construction. */
  abstract initialize(): Promise<void>;

  /** Validate the current view state. Returns true if valid. */
  abstract validate(): boolean;

  /** Get the primary value held by this view */
  getValue(): unknown { return undefined; }

  /** Set the primary value on this view */
  setValue(_value: unknown): void {}

  /** Get the data source for this view (e.g. list of entities) */
  getSource(): unknown { return undefined; }

  /** Set the data source for this view */
  setSource(_source: unknown): void {}

  /** Whether this view is in read-only mode */
  isReadOnly(): boolean { return false; }

  /** Set read-only mode */
  setReadOnly(_readOnly: boolean): void {}

  /** Register an event handler */
  on(event: string, handler: EventHandler): void {
    if (!this._eventHandlers.has(event)) {
      this._eventHandlers.set(event, new Set());
    }
    this._eventHandlers.get(event)!.add(handler);
  }

  /** Unregister an event handler */
  off(event: string, handler: EventHandler): void {
    this._eventHandlers.get(event)?.delete(handler);
  }

  /** Emit an event to all registered handlers */
  emit(event: string, payload?: unknown): void {
    this._eventHandlers.get(event)?.forEach(h => h(payload));
  }

  /** Get current view state (read-only snapshot) */
  getState(): Readonly<ViewState> { return { ...this.state }; }
}
