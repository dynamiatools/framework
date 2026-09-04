// PromptManager.ts — framework-agnostic single-value-input dialog queue

import type { PromptOptions, PromptRequest } from './types.js';

/** Handler invoked whenever the current prompt request changes. */
export type PromptChangeHandler = (current: PromptRequest | null) => void;

interface PendingPrompt {
  request: PromptRequest;
  resolve: (value: string | null) => void;
}

/**
 * Framework-agnostic, single-flight queue of prompt (single-value input) requests — the `INPUT`
 * counterpart to {@link ConfirmManager}.
 *
 * Only one request is ever "current" at a time; calling {@link prompt} while one is already pending
 * queues the new request behind it. A Vue (or any other) adapter subscribes via {@link on}, renders
 * `current()`, and calls {@link answer} with the user's input — `null` means cancelled — this class has
 * no rendering concerns of its own.
 *
 * Example:
 * <pre>{@code
 * const value = await PromptManager.prompt({ message: 'New quantity?', inputType: 'number' });
 * if (value !== null) { ... }
 * }</pre>
 */
export class PromptManager {
  private readonly _queue: PendingPrompt[] = [];
  private readonly _handlers = new Set<PromptChangeHandler>();

  /**
   * Queue a prompt request and return a promise that resolves to the user's input, or `null` if
   * cancelled (or once code resolves it programmatically via {@link answer}).
   */
  prompt(options: PromptOptions): Promise<string | null> {
    return new Promise<string | null>(resolve => {
      const request: PromptRequest = {
        id: crypto.randomUUID(),
        message: options.message,
        confirmLabel: options.confirmLabel ?? 'OK',
        cancelLabel: options.cancelLabel ?? 'Cancel',
        inputType: options.inputType ?? 'text',
        ...(options.title !== undefined ? { title: options.title } : {}),
        ...(options.defaultValue !== undefined ? { defaultValue: options.defaultValue } : {}),
        ...(options.placeholder !== undefined ? { placeholder: options.placeholder } : {}),
      };
      const wasEmpty = this._queue.length === 0;
      this._queue.push({ request, resolve });
      if (wasEmpty) this._emit();
    });
  }

  /**
   * Answer the current request (by id, defensively) and advance to the next queued one, if any.
   * No-op if `id` doesn't match the current request.
   */
  answer(id: string, value: string | null): void {
    const pending = this._queue[0];
    if (!pending || pending.request.id !== id) return;
    this._queue.shift();
    pending.resolve(value);
    this._emit();
  }

  /** The request currently awaiting an answer, or `null` if none is pending. */
  current(): PromptRequest | null {
    return this._queue[0]?.request ?? null;
  }

  /** Subscribe to changes. Returns an unsubscribe function. */
  on(handler: PromptChangeHandler): () => void {
    this._handlers.add(handler);
    return () => this._handlers.delete(handler);
  }

  /** Unsubscribe a previously registered handler. */
  off(handler: PromptChangeHandler): void {
    this._handlers.delete(handler);
  }

  private _emit(): void {
    const current = this.current();
    this._handlers.forEach(h => h(current));
  }
}

/**
 * Global singleton {@link PromptManager}.
 *
 * Use directly, or via the `useInput()` composable on the Vue side.
 */
export const promptManager = new PromptManager();
