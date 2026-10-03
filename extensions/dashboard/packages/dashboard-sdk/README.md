# @dynamia-tools/dashboard-sdk

TypeScript / JavaScript client for the Dynamia **Dashboard** extension. It is framework-agnostic; use it with
Vue through [`@dynamia-tools/dashboard-vue`](../dashboard-vue) or with any other frontend.

A dashboard is a `view: dashboard` view descriptor whose fields are widgets. The SDK splits the work in two:

1. **Layout** comes from the descriptor: `client.metadata.getView(id)` (core SDK) and `resolveDashboardLayout`.
2. **Widget data** comes from `DashboardApi.widget()`, one request per widget, so a slow widget does not block the rest.

## Installation

```bash
pnpm add @dynamia-tools/dashboard-sdk @dynamia-tools/sdk
```

## Usage

```ts
import { DynamiaClient } from '@dynamia-tools/sdk';
import { DashboardApi, resolveDashboardLayout } from '@dynamia-tools/dashboard-sdk';

const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: '...' });
const dashboard = new DashboardApi(client.http);

const descriptor = await client.metadata.getView('mainDashboard');
const { rows } = resolveDashboardLayout(descriptor);

for (const cell of rows.flat()) {
  const widget = await dashboard.widget('mainDashboard', cell.field, { range: 'lastMonth' });
  console.log(cell.field, widget.type, widget.data);
}
```

## API

| | |
|---|---|
| `DashboardApi.widget(descriptorId, field, params?)` | `GET /api/dashboard/{descriptorId}/widgets/{field}`. Rejects with `DynamiaApiError` (`404` for an unknown dashboard, field or widget). `params` are forwarded to the widget's `update(params)`. |
| `resolveDashboardLayout(descriptor)` | Rows of cells on a 12-column grid, with the same rules as the ZK renderer: `layout.params.columns` (default 4), `span`, `span-sm`, `span-xs`. |

### Widget data

`DashboardWidgetResponse.type` selects the shape of `data`:

| type | `data` |
|---|---|
| `chart` | `ChartWidgetData`: a Chart.js `type`, `data` and `options` |
| `kpi` | `KpiWidgetData`: `value`, `label`, `unit`, `trend` |
| `viewer` | `ViewerWidgetData`: `descriptorId` / `viewType` and the `value` to show |
| `html`, `custom`, any other | application specific |
