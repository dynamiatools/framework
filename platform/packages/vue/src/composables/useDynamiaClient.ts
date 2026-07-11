// useDynamiaClient: composable for injecting the DynamiaClient instance provided by the DynamiaVue plugin.
import { inject, type InjectionKey } from 'vue';
import type { DynamiaClient } from '@dynamia-tools/sdk';

/**
 * Injection key used by the DynamiaVue plugin to provide the DynamiaClient
 * to the entire component tree.
 *
 * Usage in plugin:
 * ```ts
 * app.use(DynamiaVue, { client: myDynamiaClient });
 * ```
 *
 * Usage in component:
 * ```ts
 * const client = useDynamiaClient();
 * ```
 */
export const DYNAMIA_CLIENT_KEY: InjectionKey<DynamiaClient> = Symbol('DynamiaClient');

/**
 * Composable to access the DynamiaClient instance injected by the DynamiaVue plugin.
 *
 * Returns `null` when no client has been provided (e.g., in standalone tests or
 * when the plugin is used without passing a client option).
 *
 * @example
 * ```ts
 * const client = useDynamiaClient();
 * if (client) {
 *   const results = await client.crud('books').findAll({ q: 'vue' });
 * }
 * ```
 */
export function useDynamiaClient(): DynamiaClient | null {
  return inject<DynamiaClient | null>(DYNAMIA_CLIENT_KEY, null);
}

