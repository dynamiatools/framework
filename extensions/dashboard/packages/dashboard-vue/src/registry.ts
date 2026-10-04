import type { Component } from 'vue';

const renderers = new Map<string, Component>();

/**
 * Registry of the Vue components that render a dashboard widget, keyed by the widget `type`
 * (`chart`, `kpi`, `viewer`, or any custom type a backend widget declares in `getType()`).
 *
 * The component receives the loaded widget as props: `data` (the shape depends on the type) and `response`
 * (the whole `DashboardWidgetResponse`). Registering a type again replaces the previous component, so an
 * application can override a built-in widget.
 *
 * @example
 * ```ts
 * import SalesMap from './SalesMap.vue';
 * WidgetRendererRegistry.register('sales-map', SalesMap);
 * ```
 */
export const WidgetRendererRegistry = {
  /** Registers (or replaces) the component that renders widgets of `type`. */
  register(type: string, component: Component): void {
    renderers.set(type, component);
  },

  /** Returns the component registered for `type`, or `undefined`. */
  get(type: string): Component | undefined {
    return renderers.get(type);
  },

  /** Tells whether a component is registered for `type`. */
  has(type: string): boolean {
    return renderers.has(type);
  },

  /** Removes the component registered for `type`. Returns whether there was one. */
  unregister(type: string): boolean {
    return renderers.delete(type);
  },

  /** Removes every registered component, built-ins included. */
  clear(): void {
    renderers.clear();
  },
};
