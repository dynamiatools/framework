// useConfirm: Vue-reactive adapter over the framework-agnostic ConfirmManager singleton

import { onUnmounted, ref } from 'vue';
import type { Ref } from 'vue';
import { confirmManager } from '@dynamia-tools/ui-core';
import type { ConfirmRequest, ConfirmOptions } from '@dynamia-tools/ui-core';

/** Return type of {@link useConfirm}. */
export interface UseConfirmReturn {
  /** Reactive current confirm request, or `null` when none is pending. */
  current: Ref<ConfirmRequest | null>;
  /**
   * Queue a confirmation request. Resolves to `true`/`false` once the user
   * answers (or once another `answer()` call resolves it programmatically).
   */
  confirm: (options: ConfirmOptions) => Promise<boolean>;
  /** Answer a request by id — normally called by `<DynamiaConfirmHost>`, not app code. */
  answer: (id: string, result: boolean) => void;
}

/**
 * Vue composable exposing the global confirm-dialog queue as reactive state.
 *
 * `theme-dynamical-vue` (or any consuming app) should mount `<DynamiaConfirmHost />`
 * once near the app root — every call to `useConfirm()` anywhere else (e.g. a
 * delete button) shares the same underlying queue and awaits the same promise.
 *
 * Example — guard a delete action:
 * <pre>{@code
 * const { confirm } = useConfirm();
 * async function onDelete() {
 *   if (await confirm({ message: 'Delete this record?', title: 'Confirm' })) {
 *     await api.delete(id);
 *   }
 * }
 * }</pre>
 *
 * @returns Reactive current request plus `confirm`/`answer`
 */
export function useConfirm(): UseConfirmReturn {
  const current = ref<ConfirmRequest | null>(confirmManager.current());
  const unsubscribe = confirmManager.on(request => {
    current.value = request;
  });
  onUnmounted(unsubscribe);

  return {
    current,
    confirm: options => confirmManager.confirm(options),
    answer: (id, result) => confirmManager.answer(id, result),
  };
}
