# @dynamia-tools/dashboard-vue

Vue 3 components and composables that render a Dynamia **dashboard** without ZK. Built on
[`@dynamia-tools/dashboard-sdk`](../dashboard-sdk) and `@dynamia-tools/vue`.

## Installation

```bash
pnpm add @dynamia-tools/dashboard-vue @dynamia-tools/dashboard-sdk @dynamia-tools/sdk @dynamia-tools/vue
pnpm add chart.js   # only if the dashboard has chart widgets
```

## Setup

```ts
import { createApp } from 'vue';
import { DynamiaClient } from '@dynamia-tools/sdk';
import { DynamiaVue } from '@dynamia-tools/vue';
import { DynamiaDashboardVue } from '@dynamia-tools/dashboard-vue';
import '@dynamia-tools/dashboard-vue/dashboard-vue.css';

const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: '...' });

createApp(App)
  .use(DynamiaVue, { client })
  .use(DynamiaDashboardVue) // registers the chart, kpi and viewer renderers and <DynamiaDashboard>
  .mount('#app');
```

## Usage

```vue
<DynamiaDashboard id="mainDashboard" :params="{ range: 'lastMonth' }" />
```

Props: `id` (the dashboard descriptor id), `client` (defaults to the one given to `DynamiaVue`) and `params`
(query parameters sent to every widget). Slots: `loading`, `error`, `widget-loading`, `widget-error`, `unsupported`.

Each widget loads on its own: a failing or slow widget does not affect the others.

## Custom widgets

A backend widget declares its `getType()`. Register a component for that type; it receives `data` (the widget
data) and `response` (the whole `DashboardWidgetResponse`) as props.

```ts
import { WidgetRendererRegistry } from '@dynamia-tools/dashboard-vue';
import SalesMap from './SalesMap.vue';

WidgetRendererRegistry.register('sales-map', SalesMap);
```

Registering a built-in type (`chart`, `kpi`, `viewer`) replaces its renderer. There is intentionally no built-in
`html` renderer: render server HTML only with a component of your own, after deciding how to sanitize it.

## Composable

```ts
const { layout, widgets, loading, error, reload, reloadWidget } = useDashboard(client, 'mainDashboard', {
  params: () => ({ range: range.value }),
});
```

`widgets` is keyed by descriptor field and holds `{ response, loading, error }` for each widget. `reloadWidget(field,
params?)` refreshes one widget, for example from a widget-level filter.
