import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { defineComponent } from 'vue';
import { flushPromises, mount } from '@vue/test-utils';
import { DynamiaDashboard, KpiWidget, WidgetRendererRegistry, registerBuiltinWidgets } from '../src/index.js';
import { dashboardDescriptor, makeClient, widgetResponse } from './helpers.js';

const chartCtor = vi.fn();
const chartDestroy = vi.fn();
vi.mock('chart.js/auto', () => ({
  Chart: class {
    constructor(...args: unknown[]) {
      chartCtor(...args);
    }
    destroy = chartDestroy;
  },
}));

const Probe = defineComponent({
  props: ['data', 'response'],
  template: '<span class="probe">{{ data.value }}</span>',
});

describe('DynamiaDashboard', () => {
  beforeEach(() => {
    WidgetRendererRegistry.clear();
    WidgetRendererRegistry.register('kpi', Probe);
  });
  afterEach(() => WidgetRendererRegistry.clear());

  const descriptor = dashboardDescriptor([
    ['totalSales', { widget: 'sales-kpi', span: 1 }],
    ['broken', { widget: 'x' }],
    ['custom', { widget: 'y' }],
  ]);

  it('renders cells with their spans, titles, renderers, errors and unsupported types', async () => {
    const { client } = makeClient({
      '/apps/api/metadata/views/main': descriptor,
      '/widgets/totalSales': widgetResponse('totalSales', 'kpi', { value: 42 }, { title: 'Sales', titleVisible: true }),
      '/widgets/broken': () => ({ status: 500, body: { message: 'boom' } }),
      '/widgets/custom': widgetResponse('custom', 'sales-map', null),
    });

    const wrapper = mount(DynamiaDashboard, { props: { id: 'main', client } });
    await flushPromises();

    const sales = wrapper.get('[data-field="totalSales"]');
    expect(sales.attributes('style')).toContain('--dynamia-span-md: 6');
    expect(sales.get('.dynamia-dashboard-title').text()).toBe('Sales');
    expect(sales.get('.probe').text()).toBe('42');
    expect(wrapper.get('[data-field="broken"]').text()).toContain('boom');
    expect(wrapper.get('[data-field="custom"]').text()).toContain('No renderer registered for widget type "sales-map"');
  });

  it('lets the host frame every widget with the `frame` prop', async () => {
    const Frame = defineComponent({
      props: ['field', 'response', 'loading', 'error'],
      template:
        '<section class="host-card" :data-state="error ? \'error\' : loading ? \'loading\' : \'ready\'">' +
        '<h3 v-if="response">{{ response.title }}</h3><slot /></section>',
    });
    const { client } = makeClient({
      '/apps/api/metadata/views/main': descriptor,
      '/widgets/totalSales': widgetResponse('totalSales', 'kpi', { value: 42 }, { title: 'Sales', titleVisible: false }),
      '/widgets/broken': () => ({ status: 500, body: { message: 'boom' } }),
      '/widgets/custom': widgetResponse('custom', 'sales-map', null),
    });

    const wrapper = mount(DynamiaDashboard, { props: { id: 'main', client, frame: Frame } });
    await flushPromises();

    const sales = wrapper.get('[data-field="totalSales"] .host-card');
    expect(sales.attributes('data-state')).toBe('ready');
    expect(sales.get('h3').text()).toBe('Sales');
    expect(sales.get('.probe').text()).toBe('42');
    expect(wrapper.find('[data-field="totalSales"] .dynamia-dashboard-title').exists()).toBe(false);

    const broken = wrapper.get('[data-field="broken"] .host-card');
    expect(broken.attributes('data-state')).toBe('error');
    expect(broken.text()).toContain('boom');
    expect(wrapper.get('[data-field="custom"] .host-card').text()).toContain('No renderer registered');
  });

  it('shows the descriptor loading error', async () => {
    const { client } = makeClient({ '/apps/api/metadata/views/main': () => ({ status: 404, body: { message: 'nope' } }) });

    const wrapper = mount(DynamiaDashboard, { props: { id: 'main', client } });
    await flushPromises();

    expect(wrapper.get('.dynamia-dashboard-error').text()).toContain('nope');
  });

  it('requires a client', () => {
    const spy = vi.spyOn(console, 'warn').mockImplementation(() => {});
    expect(() => mount(DynamiaDashboard, { props: { id: 'main' } })).toThrow(/needs a DynamiaClient/);
    spy.mockRestore();
  });
});

describe('built-in widgets', () => {
  it('registerBuiltinWidgets registers chart, kpi and viewer', () => {
    WidgetRendererRegistry.clear();
    registerBuiltinWidgets();
    expect(['chart', 'kpi', 'viewer'].every((t) => WidgetRendererRegistry.has(t))).toBe(true);
    WidgetRendererRegistry.clear();
  });

  it('KpiWidget shows value, label, unit and a signed trend percentage', () => {
    const up = mount(KpiWidget, { props: { data: { value: 1200, label: 'Revenue', unit: 'USD', trend: 0.125 } } });
    expect(up.text()).toContain('Revenue');
    expect(up.text()).toContain('1200');
    expect(up.text()).toContain('USD');
    expect(up.get('.dynamia-kpi-trend-up').text()).toBe('+12.5%');

    const down = mount(KpiWidget, { props: { data: { value: 3, trend: -0.05 } } });
    expect(down.get('.dynamia-kpi-trend-down').text()).toBe('-5%');

    const none = mount(KpiWidget, { props: { data: { value: 3 } } });
    expect(none.find('.dynamia-kpi-trend').exists()).toBe(false);
    expect(none.find('.dynamia-kpi-description').exists()).toBe(false);

    const described = mount(KpiWidget, { props: { data: { value: 3, description: '12 sales made', icon: 'cart', color: 'success' } } });
    expect(described.get('.dynamia-kpi-description').text()).toBe('12 sales made');
  });

  it('ChartWidget builds a Chart.js chart from the data and destroys it on unmount', async () => {
    const { ChartWidget } = await import('../src/index.js');
    const data = { type: 'bar', data: { labels: ['Jan'], datasets: [] }, options: null };

    const wrapper = mount(ChartWidget, { props: { data } });
    await flushPromises();

    expect(chartCtor).toHaveBeenCalledTimes(1);
    const config = chartCtor.mock.calls[0]![1] as { type: string; data: unknown };
    expect(config.type).toBe('bar');
    expect(config.data).toEqual(data.data);

    wrapper.unmount();
    expect(chartDestroy).toHaveBeenCalled();
  });
});
