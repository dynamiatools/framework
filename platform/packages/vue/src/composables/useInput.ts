// useInput: Vue-reactive adapter over the framework-agnostic PromptManager singleton

import { onUnmounted, ref } from 'vue';
import type { Ref } from 'vue';
import { promptManager } from '@dynamia-tools/ui-core';
import type { PromptRequest, PromptOptions } from '@dynamia-tools/ui-core';

/** Return type of {@link useInput}. */
export interface UseInputReturn {
  /** Reactive current prompt request, or `null` when none is pending. */
  current: Ref<PromptRequest | null>;
  /**
   * Queue a prompt request. Resolves to the user's input, or `null` if cancelled (or once another
   * `answer()` call resolves it programmatically).
   */
  prompt: (options: PromptOptions) => Promise<string | null>;
  /** Answer a request by id — normally called by `<DynamiaPromptHost>`, not app code. */
  answer: (id: string, value: string | null) => void;
}

/**
 * Vue composable exposing the global prompt (single-value input) queue as reactive state — the `INPUT`
 * counterpart to {@link useConfirm}.
 *
 * `theme-dynamical-vue` (or any consuming app) should mount `<DynamiaPromptHost />` once near the app
 * root, alongside `<DynamiaConfirmHost />`/`<DynamiaToastHost />` — every call to `useInput()` anywhere
 * else shares the same underlying queue and awaits the same promise.
 *
 * Example:
 * <pre>{@code
 * const { prompt } = useInput();
 * const qty = await prompt({ message: 'New quantity?', inputType: 'number' });
 * if (qty !== null) { ... }
 * }</pre>
 *
 * @returns Reactive current request plus `prompt`/`answer`
 */
export function useInput(): UseInputReturn {
  const current = ref<PromptRequest | null>(promptManager.current());
  const unsubscribe = promptManager.on(request => {
    current.value = request;
  });
  onUnmounted(unsubscribe);

  return {
    current,
    prompt: options => promptManager.prompt(options),
    answer: (id, value) => promptManager.answer(id, value),
  };
}
