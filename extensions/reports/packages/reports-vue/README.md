# @dynamia-tools/reports-vue

Vue 3 components and composables to **list, run, export and design** Dynamia reports without ZK. Built on
[`@dynamia-tools/reports-sdk`](../reports-sdk) and `@dynamia-tools/vue`.

## Installation

```bash
pnpm add @dynamia-tools/reports-vue @dynamia-tools/reports-sdk @dynamia-tools/sdk @dynamia-tools/vue
pnpm add chart.js   # only if the reports have charts
```

## Setup

```ts
import { createApp } from 'vue';
import { DynamiaClient } from '@dynamia-tools/sdk';
import { DynamiaVue } from '@dynamia-tools/vue';
import { DynamiaReportsVue } from '@dynamia-tools/reports-vue';
import '@dynamia-tools/reports-vue/reports-vue.css';

const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: '...' });

createApp(App)
  .use(DynamiaVue, { client })
  .use(DynamiaReportsVue) // registers <DynamiaReportList>, <DynamiaReportViewer> and <DynamiaReportDesigner>
  .mount('#app');
```

Every component takes an optional `client` prop (defaults to the one given to `DynamiaVue`) and a `labels` prop to
translate the texts (`englishLabels` is the default, `spanishLabels` is included).

## Report list and viewer

```vue
<DynamiaReportList @select="(report) => (selected = report.id)" />
<DynamiaReportViewer v-if="selected" :id="selected" />
```

`DynamiaReportList` shows the reports the user can run, grouped, with search. `DynamiaReportViewer` shows one report:

- the filters form, built from the report definition (text, number, boolean, date, date time, time, enum, entity,
  query and static options; required filters block the run button);
- the result table with sorting and paging, number and date formatting by column type;
- the charts of the report (needs `chart.js`);
- export buttons for the formats the server offers (xlsx, csv, pdf) that download the whole result;
- a warning when the result was truncated by the server row limit.

Props: `id`, `client`, `autoRun` (run when shown and nothing is required, default `true`), `pageSize` (25),
`showCharts` (`true`), `locale`, `labels`. Slots: `loading`, `error`, `cell` (custom cell content). Emits `result`.

### Composables

```ts
const report = useReport(client, 12, { pageSize: 50 });
report.values.year = '2026';
await report.search();     // first page; also: run(page), sortBy(column), setPage(n), setPageSize(n), reset()
await report.exportAs('csv');
```

`useReport` exposes `definition`, `values` (filter form values), `result`, `charts`, `loading`, `running`, `exporting`,
`error`, `runError`, `page`, `size`, `totalPages`, `sort`, `direction`, `missingRequired`, `canRun` and
`filterOptions(filter, q?)`. `useReports(client)` loads the catalog (`groups`, `loading`, `error`, `reload`).

The lower level components `ReportFilters`, `ReportTable` and `ReportChart` are exported too, so you can build your own
layout.

## Dashboards

A dashboard can show a report with the `report` widget (see the
[extension README](../../README.md#dashboard-widget)). `chart` and `kpi` displays use the standard renderers of
`@dynamia-tools/dashboard-vue`; register the renderer of the table display:

```ts
import { WidgetRendererRegistry } from '@dynamia-tools/dashboard-vue';
import { registerReportWidget } from '@dynamia-tools/reports-vue';

registerReportWidget(WidgetRendererRegistry);
```

## Designer

```vue
<DynamiaReportDesigner :node="reportsDesignNode" />
```

Two tabs. **Reports** is the platform CRUD page of the report entity (`node`: the `CrudPage` navigation node of the
design page), which renders the report form with its filters, fields and charts from the view descriptors. **Tools**
has a query preview (first rows of the query you are writing, with datasource and parameters), export and import of
report definitions as JSON files, and a datasource connection test.

The tools need a designer role on the server (`dynamia.reports.designer-roles`); other users see a message. Without
`node` only the tools are shown. The pieces are exported too: `QueryPreview`, `DefinitionTransfer`, `DataSourceTest`
and the `useReportDesigner(client)` composable.

## Navigation

With `dynamia.reports.ui=vue` (the default) the viewer page of the reports module has the type `ReportViewerPage` in
the navigation API. Render it in your shell with `DynamiaReportList` and `DynamiaReportViewer`:

```ts
// in the component that renders a navigation node
if (node.type === 'ReportViewerPage') return h(ReportsPage); // your page using the components above
```

Pages of other types keep using the usual shell logic (`CrudPage` with `DynamiaCrudPage`, others embedded with ZK).
