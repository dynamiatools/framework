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

## Matching the host design

The package does not need a design system. To make a dashboard look like the rest of your application:

- **Frame each widget** with the `frame` prop: a component that receives `field`, `response`, `loading` and `error` as
  props and renders the widget (or its loading and error state) in its default slot. Use it to wrap the widget in your
  card component and to show the title your way. `WidgetFrame` is the default frame and documents the contract.
- **Replace the renderers** you want to look native: `WidgetRendererRegistry.register('kpi', MyInfobox)` after
  installing the plugin replaces the built-in `kpi` renderer (see [Custom widgets](#custom-widgets)).
- **Loading, error and unsupported states** are slots (`loading`, `error`, `widget-loading`, `widget-error`,
  `unsupported`).
- The built-in components read CSS custom properties with fallbacks, so a host can theme them without extra CSS:
  `--dynamia-dashboard-gap`, `--dynamia-success` and `--dynamia-danger` (trend colors).

```vue
<DynamiaDashboard id="mainDashboard" :frame="MyCard" />
```
