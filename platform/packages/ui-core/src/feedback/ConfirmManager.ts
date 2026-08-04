// ConfirmManager.ts — framework-agnostic confirm-dialog queue

import type { ConfirmOptions, ConfirmRequest } from './types.js';

/** Handler invoked whenever the current confirm request changes. */
export type ConfirmChangeHandler = (current: ConfirmRequest | null) => void;

interface PendingConfirm {
  request: ConfirmRequest;
  resolve: (value: boolean) => void;
}

/**
 * Framework-agnostic, single-flight queue of confirm requests.
 *
 * Only one request is ever "current" at a time; calling {@link confirm} while
 * one is already pending queues the new request behind it. A Vue (or any
 * other) adapter subscribes via {@link on}, renders `current()`, and calls
 * {@link answer} with the user's choice — this class has no rendering
 * concerns of its own.
 *
 * Example:
 * <pre>{@code
 * const ok = await ConfirmManager.confirm({ message: 'Delete this record?' });
 * if (ok) { ... }
 * }</pre>
 */
export class ConfirmManager {
  private readonly _queue: PendingConfirm[] = [];
  private readonly _handlers = new Set<ConfirmChangeHandler>();

  /**
   * Queue a confirmation request and return a promise that resolves to
   * `true`/`false` once the user (or code, via {@link answer}) responds.
   */
  confirm(options: ConfirmOptions): Promise<boolean> {
    return new Promise<boolean>(resolve => {
      const request: ConfirmRequest = {
        id: crypto.randomUUID(),
        message: options.message,
        confirmLabel: options.confirmLabel ?? 'Yes',
        cancelLabel: options.cancelLabel ?? 'No',
        variant: options.variant ?? 'warning',
        ...(options.title !== undefined ? { title: options.title } : {}),
      };
      const wasEmpty = this._queue.length === 0;
      this._queue.push({ request, resolve });
      if (wasEmpty) this._emit();
    });
  }

  /**
   * Answer the current request (by id, defensively) and advance to the next
   * queued one, if any. No-op if `id` doesn't match the current request.
   */
  answer(id: string, result: boolean): void {
    const pending = this._queue[0];
    if (!pending || pending.request.id !== id) return;
    this._queue.shift();
    pending.resolve(result);
    this._emit();
  }

  /** The request currently awaiting an answer, or `null` if none is pending. */
  current(): ConfirmRequest | null {
    return this._queue[0]?.request ?? null;
  }

  /** Subscribe to changes. Returns an unsubscribe function. */
  on(handler: ConfirmChangeHandler): () => void {
    this._handlers.add(handler);
    return () => this._handlers.delete(handler);
  }

  /** Unsubscribe a previously registered handler. */
  off(handler: ConfirmChangeHandler): void {
    this._handlers.delete(handler);
  }

  private _emit(): void {
    const current = this.current();
    this._handlers.forEach(h => h(current));
  }
}

/**
 * Global singleton {@link ConfirmManager}.
 *
 * Use directly, or via the `useConfirm()` composable on the Vue side.
 */
export const confirmManager = new ConfirmManager();
