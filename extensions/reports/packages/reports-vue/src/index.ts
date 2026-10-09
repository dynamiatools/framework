// @dynamia-tools/reports-vue — Vue 3 components and composables for the Dynamia Reports extension

export { DynamiaReportsVue, registerReportWidget } from './plugin.js';
export type { WidgetRegistryLike } from './plugin.js';

export { useReports } from './composables/useReports.js';
export type { UseReportsOptions } from './composables/useReports.js';
export { useReport } from './composables/useReport.js';
export type { UseReportOptions } from './composables/useReport.js';
export { useReportDesigner } from './composables/useReportDesigner.js';
export type { UseReportDesignerOptions } from './composables/useReportDesigner.js';

export { default as DynamiaReportList } from './components/ReportList.vue';
export { default as DynamiaReportViewer } from './components/ReportViewer.vue';
export { default as DynamiaReportDesigner } from './components/ReportDesigner.vue';
export { default as ReportFilters } from './components/ReportFilters.vue';
export { default as ReportTable } from './components/ReportTable.vue';
export { default as ReportChart } from './components/ReportChart.vue';
export { default as ReportWidget } from './components/ReportWidget.vue';
export type { ReportWidgetData } from './components/ReportWidget.vue';
export { default as QueryPreview } from './components/QueryPreview.vue';
export { default as DefinitionTransfer } from './components/DefinitionTransfer.vue';
export { default as DataSourceTest } from './components/DataSourceTest.vue';

export { englishLabels, spanishLabels, resolveLabels } from './labels.js';
export type { ReportLabels } from './labels.js';
export { formatCell, toApiValue, toInputValue, exportFileName, downloadBlob } from './format.js';
