// FlowStepRenderer.ts — client-registered renderer for a FlowRemoteAction's CUSTOM step

import type { ActionFlowStep } from '@dynamia-tools/sdk';
import { Registry } from '../registry/Registry.js';

/**
 * A client-registered handler for a `FlowRemoteAction`'s `CUSTOM` step (see
 * `docs/design/SERVER_DRIVEN_ACTION_FLOWS.md` §3/§6): given the step, shows whatever UI it needs and
 * resolves with the client's answer — the next `ActionExecutionRequest.data` in the flow.
 *
 * Registered under the component name carried in `step.data.component`, same shape as
 * {@link ClientActionRegistry} (keyed by action id/className instead).
 *
 * Example:
 * <pre>{@code
 * registerFlowStepRenderer('bookCoverPicker', async (step) => {
 *   const url = await showCoverPickerDialog(step.data);
 *   return { coverUrl: url };
 * });
 * }</pre>
 */
export type FlowStepRenderer = (step: ActionFlowStep) => Promise<unknown>;

function normalizeComponentKey(key: string): string[] {
  const trimmed = key.trim();
  return [...new Set([trimmed, trimmed.toLowerCase()])];
}

/**
 * Registry for {@link FlowStepRenderer}s, keyed by the `component` name a `CUSTOM` step carries in
 * `step.data.component`. Lookup is case-insensitive.
 */
export class FlowStepRendererRegistryClass extends Registry<FlowStepRenderer> {
  constructor() {
    super(normalizeComponentKey);
  }

  /** Resolve the renderer registered for `step`'s `data.component`, or `null` if none is registered. */
  resolve(step: ActionFlowStep): FlowStepRenderer | null {
    const component = (step.data as { component?: string } | undefined)?.component;
    return this.get(component ?? null);
  }
}

/**
 * Global singleton registry for `CUSTOM` flow step renderers.
 *
 * Use {@link registerFlowStepRenderer} as the primary convenience API.
 */
export const FlowStepRendererRegistry = new FlowStepRendererRegistryClass();

/**
 * Register a {@link FlowStepRenderer} under `component` in the global {@link FlowStepRendererRegistry}.
 *
 * Example:
 * <pre>{@code
 * registerFlowStepRenderer('bookCoverPicker', async (step) => ({ coverUrl: await pickCover(step.data) }));
 * }</pre>
 */
export function registerFlowStepRenderer(component: string, renderer: FlowStepRenderer): void {
  FlowStepRendererRegistry.register(component, renderer);
}
