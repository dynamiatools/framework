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
      '/api/app/metadata/views/main': descriptor,
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

  it('shows the descriptor loading error', async () => {
    const { client } = makeClient({ '/api/app/metadata/views/main': () => ({ status: 404, body: { message: 'nope' } }) });

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
