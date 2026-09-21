// ToastManager.ts — framework-agnostic toast/notification queue

import type { ToastOptions, ToastItem } from './types.js';

/** Handler invoked whenever the visible toast list changes. */
export type ToastChangeHandler = (toasts: ToastItem[]) => void;

const DEFAULT_DURATION = 4000;

/**
 * Framework-agnostic queue of toast notifications.
 *
 * Holds zero or more visible {@link ToastItem}s and notifies subscribers on
 * every change (show/dismiss/auto-dismiss). A Vue (or any other) adapter
 * subscribes via {@link on} and renders `list()` reactively — this class has
 * no rendering concerns of its own.
 *
 * Example:
 * <pre>{@code
 * const id = ToastManager.show({ message: 'Saved', variant: 'success' });
 * ToastManager.dismiss(id);
 * }</pre>
 */
export class ToastManager {
  private readonly _items: ToastItem[] = [];
  private readonly _timers = new Map<string, ReturnType<typeof setTimeout>>();
  private readonly _handlers = new Set<ToastChangeHandler>();

  /**
   * Show a toast. Returns its generated id (usable with {@link dismiss}).
   *
   * When `duration` is `0` the toast stays until dismissed explicitly;
   * otherwise it auto-dismisses after `duration` ms (default `4000`).
   */
  show(options: ToastOptions): string {
    const id = crypto.randomUUID();
    const item: ToastItem = {
      id,
      message: options.message,
      variant: options.variant ?? 'info',
      duration: options.duration ?? DEFAULT_DURATION,
      ...(options.title !== undefined ? { title: options.title } : {}),
    };
    this._items.push(item);
    if (item.duration > 0) {
      this._timers.set(id, setTimeout(() => this.dismiss(id), item.duration));
    }
    this._emit();
    return id;
  }

  /** Dismiss a toast by id. No-op if it's not (or no longer) visible. */
  dismiss(id: string): void {
    const index = this._items.findIndex(t => t.id === id);
    if (index === -1) return;
    this._items.splice(index, 1);
    const timer = this._timers.get(id);
    if (timer) {
      clearTimeout(timer);
      this._timers.delete(id);
    }
    this._emit();
  }

  /** Dismiss every visible toast. */
  clear(): void {
    for (const id of [...this._timers.keys()]) clearTimeout(this._timers.get(id)!);
    this._timers.clear();
    this._items.length = 0;
    this._emit();
  }

  /** Current snapshot of visible toasts, oldest first. */
  list(): ToastItem[] {
    return [...this._items];
  }

  /** Subscribe to changes. Returns an unsubscribe function. */
  on(handler: ToastChangeHandler): () => void {
    this._handlers.add(handler);
    return () => this._handlers.delete(handler);
  }

  /** Unsubscribe a previously registered handler. */
  off(handler: ToastChangeHandler): void {
    this._handlers.delete(handler);
  }

  private _emit(): void {
    const snapshot = this.list();
    this._handlers.forEach(h => h(snapshot));
  }
}

/**
 * Global singleton {@link ToastManager}.
 *
 * Use directly, or via the `useToast()` composable on the Vue side.
 */
export const toastManager = new ToastManager();
