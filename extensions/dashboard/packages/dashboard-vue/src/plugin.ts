import type { App } from 'vue';
import ChartWidget from './components/ChartWidget.vue';
import DashboardComponent from './components/Dashboard.vue';
import KpiWidget from './components/KpiWidget.vue';
import ViewerWidget from './components/ViewerWidget.vue';
import { WidgetRendererRegistry } from './registry.js';

/**
 * Registers the built-in widget renderers (`chart`, `kpi` and `viewer`) in the {@link WidgetRendererRegistry}.
 * Called by the {@link DynamiaDashboardVue} plugin; call it yourself when you do not use the plugin.
 */
export function registerBuiltinWidgets(): void {
  WidgetRendererRegistry.register('chart', ChartWidget);
  WidgetRendererRegistry.register('kpi', KpiWidget);
  WidgetRendererRegistry.register('viewer', ViewerWidget);
}

/**
 * Vue plugin of the dashboard extension: registers the built-in widget renderers and the global
 * `DynamiaDashboard` component. Install it after `DynamiaVue`.
 *
 * @example
 * ```ts
 * app.use(DynamiaVue, { client });
 * app.use(DynamiaDashboardVue);
 * ```
 */
export const DynamiaDashboardVue = {
  install(app: App): void {
    registerBuiltinWidgets();
    app.component('DynamiaDashboard', DashboardComponent);
  },
};
