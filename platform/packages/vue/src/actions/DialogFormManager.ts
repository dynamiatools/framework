// DialogFormManager.ts — single-flight queue of "render this form in a dialog and collect the answer"
// requests, backing the `DIALOG` flow step (see docs/design/SERVER_DRIVEN_ACTION_FLOWS.md §3/§6).
//
// Lives in @dynamia-tools/vue (not @dynamia-tools/ui-core, unlike ConfirmManager/PromptManager) because
// it queues a VueFormView instance, which is Vue-specific.

import type { VueFormView } from '../views/VueFormView.js';

/** A form-dialog request to queue via {@link DialogFormManager.showForm}. */
export interface DialogFormOptions {
  /** An already-initialized {@link VueFormView} (descriptor fetched/prefilled by the caller). */
  view: VueFormView;
  /** Optional heading shown in the dialog header. */
  title?: string;
}

/** A queued/current form-dialog request, as tracked internally by {@link DialogFormManager}. */
export interface DialogFormRequest extends DialogFormOptions {
  /** Unique id, generated when the request is queued. */
  id: string;
}

/** Handler invoked whenever the current form-dialog request changes. */
export type DialogFormChangeHandler = (current: DialogFormRequest | null) => void;

interface PendingDialogForm {
  request: DialogFormRequest;
  resolve: (value: Record<string, unknown> | null) => void;
}

/**
 * Framework-agnostic-in-spirit (but Vue-typed), single-flight queue of form-dialog requests — the
 * `DIALOG` counterpart to {@link ConfirmManager}/{@link PromptManager}.
 *
 * Only one request is ever "current" at a time. A `<DynamiaFormDialogHost>` subscribes via {@link on},
 * renders `current()`'s `view` inside a `<DynamiaDialog>`/`<DynamiaForm>`, and calls {@link answer} with
 * the submitted values (or `null` on cancel).
 */
export class DialogFormManager {
  private readonly _queue: PendingDialogForm[] = [];
  private readonly _handlers = new Set<DialogFormChangeHandler>();

  /**
   * Queue a form-dialog request and return a promise that resolves to the submitted values, or `null`
   * if cancelled.
   */
  showForm(options: DialogFormOptions): Promise<Record<string, unknown> | null> {
    return new Promise<Record<string, unknown> | null>(resolve => {
      const request: DialogFormRequest = {
        id: crypto.randomUUID(),
        view: options.view,
        ...(options.title !== undefined ? { title: options.title } : {}),
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
  answer(id: string, value: Record<string, unknown> | null): void {
    const pending = this._queue[0];
    if (!pending || pending.request.id !== id) return;
    this._queue.shift();
    pending.resolve(value);
    this._emit();
  }

  /** The request currently awaiting an answer, or `null` if none is pending. */
  current(): DialogFormRequest | null {
    return this._queue[0]?.request ?? null;
  }

  /** Subscribe to changes. Returns an unsubscribe function. */
  on(handler: DialogFormChangeHandler): () => void {
    this._handlers.add(handler);
    return () => this._handlers.delete(handler);
  }

  /** Unsubscribe a previously registered handler. */
  off(handler: DialogFormChangeHandler): void {
    this._handlers.delete(handler);
  }

  private _emit(): void {
    const current = this.current();
    this._handlers.forEach(h => h(current));
  }
}

/**
 * Global singleton {@link DialogFormManager}.
 *
 * Use directly, or via the `useFormDialog()` composable.
 */
export const dialogFormManager = new DialogFormManager();
