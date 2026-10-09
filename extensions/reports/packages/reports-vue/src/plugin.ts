import type { App, Component } from 'vue';
import ReportDesigner from './components/ReportDesigner.vue';
import ReportList from './components/ReportList.vue';
import ReportViewer from './components/ReportViewer.vue';
import ReportWidget from './components/ReportWidget.vue';

/** The part of the dashboard `WidgetRendererRegistry` that {@link registerReportWidget} needs. */
export interface WidgetRegistryLike {
  register(type: string, component: Component): void;
}

/**
 * Registers the renderer of the `report` dashboard widget (a report shown as a table). The `chart` and `kpi` displays
 * of the widget use the standard renderers of `@dynamia-tools/dashboard-vue`.
 *
 * Example:
 * <pre>{@code
 * import { WidgetRendererRegistry } from '@dynamia-tools/dashboard-vue';
 * registerReportWidget(WidgetRendererRegistry);
 * }</pre>
 *
 * @param registry - the dashboard widget renderer registry
 */
export function registerReportWidget(registry: WidgetRegistryLike): void {
  registry.register('report', ReportWidget);
}

/**
 * Vue plugin of the reports extension: registers the global components `DynamiaReportList`, `DynamiaReportViewer`
 * and `DynamiaReportDesigner`. Install it after `DynamiaVue`.
 *
 * @example
 * ```ts
 * app.use(DynamiaVue, { client });
 * app.use(DynamiaReportsVue);
 * ```
 */
export const DynamiaReportsVue = {
  install(app: App): void {
    app.component('DynamiaReportList', ReportList);
    app.component('DynamiaReportViewer', ReportViewer);
    app.component('DynamiaReportDesigner', ReportDesigner);
  },
};
