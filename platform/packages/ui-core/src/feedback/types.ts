// types.ts — shared shapes for the framework-agnostic feedback primitives (toast/confirm)

/** Visual/semantic variant shared by toasts and confirm dialogs. */
export type FeedbackVariant = 'info' | 'success' | 'warning' | 'error';

/** Options accepted by {@link ToastManager.show}. */
export interface ToastOptions {
  /** Main message shown to the user. */
  message: string;
  /** Optional short title/heading. */
  title?: string;
  /** Visual variant, defaults to `'info'`. */
  variant?: FeedbackVariant;
  /**
   * Auto-dismiss delay in milliseconds. Defaults to `4000`.
   * Pass `0` to keep the toast until the user (or code) dismisses it explicitly.
   */
  duration?: number;
}

/** A queued/visible toast, as tracked internally by {@link ToastManager}. */
export interface ToastItem extends Required<Pick<ToastOptions, 'message' | 'variant' | 'duration'>> {
  /** Unique id, generated when the toast is shown. */
  id: string;
  /** Optional short title/heading. */
  title?: string;
}

/** Options accepted by {@link ConfirmManager.confirm}. */
export interface ConfirmOptions {
  /** Question/message shown to the user. */
  message: string;
  /** Optional short title/heading. */
  title?: string;
  /** Label for the confirming button. Defaults to `'Yes'`. */
  confirmLabel?: string;
  /** Label for the cancelling button. Defaults to `'No'`. */
  cancelLabel?: string;
  /** Visual variant, defaults to `'warning'` (most confirms guard a destructive action). */
  variant?: FeedbackVariant;
}

/** A queued/current confirmation request, as tracked internally by {@link ConfirmManager}. */
export interface ConfirmRequest extends Required<Pick<ConfirmOptions, 'message' | 'confirmLabel' | 'cancelLabel' | 'variant'>> {
  /** Unique id, generated when the request is queued. */
  id: string;
  /** Optional short title/heading. */
  title?: string;
}

/** Options accepted by {@link PromptManager.prompt}. */
export interface PromptOptions {
  /** Question/message shown to the user. */
  message: string;
  /** Optional short title/heading. */
  title?: string;
  /** Pre-filled value. */
  defaultValue?: string;
  /** Placeholder shown in the empty input. */
  placeholder?: string;
  /** `'text'` (default) or `'number'` — mirrors the flow's `INPUT` step answer being a String/Number. */
  inputType?: 'text' | 'number';
  /** Label for the confirming button. Defaults to `'OK'`. */
  confirmLabel?: string;
  /** Label for the cancelling button. Defaults to `'Cancel'`. */
  cancelLabel?: string;
}

/** A queued/current prompt request, as tracked internally by {@link PromptManager}. */
export interface PromptRequest extends Required<Pick<PromptOptions, 'message' | 'confirmLabel' | 'cancelLabel' | 'inputType'>> {
  /** Unique id, generated when the request is queued. */
  id: string;
  /** Optional short title/heading. */
  title?: string;
  /** Pre-filled value. */
  defaultValue?: string;
  /** Placeholder shown in the empty input. */
  placeholder?: string;
}
