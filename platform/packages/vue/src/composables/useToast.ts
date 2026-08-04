// useToast: Vue-reactive adapter over the framework-agnostic ToastManager singleton

import { onUnmounted, ref } from 'vue';
import type { Ref } from 'vue';
import { toastManager } from '@dynamia-tools/ui-core';
import type { ToastItem, ToastOptions, FeedbackVariant } from '@dynamia-tools/ui-core';

/** Return type of {@link useToast}. */
export interface UseToastReturn {
  /** Reactive, oldest-first list of currently visible toasts. */
  toasts: Ref<ToastItem[]>;
  /** Show a toast with full control over variant/duration. Returns its id. */
  show: (options: ToastOptions) => string;
  /** Dismiss a toast by id before its duration elapses. */
  dismiss: (id: string) => void;
  /** Shorthand for `show({ message, variant: 'info' })`. */
  info: (message: string, title?: string) => string;
  /** Shorthand for `show({ message, variant: 'success' })`. */
  success: (message: string, title?: string) => string;
  /** Shorthand for `show({ message, variant: 'warning' })`. */
  warning: (message: string, title?: string) => string;
  /** Shorthand for `show({ message, variant: 'error' })`. */
  error: (message: string, title?: string) => string;
}

function shorthand(variant: FeedbackVariant) {
  return (message: string, title?: string): string =>
    toastManager.show(title !== undefined ? { message, title, variant } : { message, variant });
}

/**
 * Vue composable exposing the global toast queue as reactive state.
 *
 * `theme-dynamical-vue` (or any consuming app) should mount `<DynamiaToastHost />`
 * once near the app root — every call to `useToast()` anywhere else shares the
 * same underlying queue.
 *
 * Example:
 * <pre>{@code
 * const toast = useToast();
 * toast.success('Saved successfully');
 * toast.error('Something went wrong', 'Save failed');
 * }</pre>
 *
 * @returns Reactive toast list plus `show`/`dismiss` and variant shorthands
 */
export function useToast(): UseToastReturn {
  const toasts = ref<ToastItem[]>(toastManager.list());
  const unsubscribe = toastManager.on(list => {
    toasts.value = list;
  });
  onUnmounted(unsubscribe);

  return {
    toasts,
    show: options => toastManager.show(options),
    dismiss: id => toastManager.dismiss(id),
    info: shorthand('info'),
    success: shorthand('success'),
    warning: shorthand('warning'),
    error: shorthand('error'),
  };
}
