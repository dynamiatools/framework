// useFormDialog: Vue-reactive adapter over the DialogFormManager singleton

import { onUnmounted, shallowRef } from 'vue';
import type { Ref } from 'vue';
import { dialogFormManager } from '../actions/DialogFormManager.js';
import type { DialogFormRequest, DialogFormOptions } from '../actions/DialogFormManager.js';

/** Return type of {@link useFormDialog}. */
export interface UseFormDialogReturn {
  /** Reactive current form-dialog request, or `null` when none is pending. */
  current: Ref<DialogFormRequest | null>;
  /**
   * Queue a form-dialog request. Resolves to the submitted values, or `null` if cancelled (or once
   * another `answer()` call resolves it programmatically).
   */
  showForm: (options: DialogFormOptions) => Promise<Record<string, unknown> | null>;
  /** Answer a request by id — normally called by `<DynamiaFormDialogHost>`, not app code. */
  answer: (id: string, value: Record<string, unknown> | null) => void;
}

/**
 * Vue composable exposing the global form-dialog queue as reactive state — backs the `DIALOG` flow step
 * (see `runActionFlow` in `docs/design/SERVER_DRIVEN_ACTION_FLOWS.md` §6).
 *
 * `theme-dynamical-vue` (or any consuming app) should mount `<DynamiaFormDialogHost />` once near the
 * app root, alongside `<DynamiaConfirmHost />`/`<DynamiaToastHost />`/`<DynamiaPromptHost />`.
 *
 * @returns Reactive current request plus `showForm`/`answer`
 */
export function useFormDialog(): UseFormDialogReturn {
  // shallowRef, not ref: the request wraps a VueFormView instance — deep reactivity would proxy it and
  // unwrap its nested Refs (values, errors, ...), breaking its class identity (see useCrudPage.ts's same
  // note for VueCrudView).
  const current = shallowRef<DialogFormRequest | null>(dialogFormManager.current());
  const unsubscribe = dialogFormManager.on(request => {
    current.value = request;
  });
  onUnmounted(unsubscribe);

  return {
    current,
    showForm: options => dialogFormManager.showForm(options),
    answer: (id, value) => dialogFormManager.answer(id, value),
  };
}
