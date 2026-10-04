// @dynamia-tools/dashboard-vue — Vue 3 components and composables for the Dynamia Dashboard extension

export { DynamiaDashboardVue, registerBuiltinWidgets } from './plugin.js';
export { WidgetRendererRegistry } from './registry.js';
export { useDashboard } from './composables/useDashboard.js';
export type { UseDashboardOptions, WidgetState } from './composables/useDashboard.js';

export { default as DynamiaDashboard } from './components/Dashboard.vue';
export { default as ChartWidget } from './components/ChartWidget.vue';
export { default as KpiWidget } from './components/KpiWidget.vue';
export { default as ViewerWidget } from './components/ViewerWidget.vue';
