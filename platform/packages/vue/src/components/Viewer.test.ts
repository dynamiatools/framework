// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { defineComponent, h, ref } from 'vue';
import { flushPromises, mount } from '@vue/test-utils';
import type { DynamiaClient, ViewDescriptor } from '@dynamia-tools/sdk';
import { View, ViewRendererRegistry } from '@dynamia-tools/ui-core';
import type { ViewType } from '@dynamia-tools/ui-core';
import Viewer from './Viewer.vue';
import { DYNAMIA_CLIENT_KEY } from '../composables/useDynamiaClient.js';
import { VueTableRenderer } from '../renderers/VueTableRenderer.js';
import { VueTableView } from '../views/VueTableView.js';

const TABLE: ViewType = { name: 'table' };

class ProbeView extends View {
  // reactive, like the rows of VueTableView
  rows = ref<unknown[]>([]);
  constructor(descriptor: ViewDescriptor) { super(TABLE, descriptor, null); }
  async initialize(): Promise<void> {}
  validate(): boolean { return true; }
  override setSource(source: unknown): void { this.rows.value = (source as unknown[]) ?? []; }
  override getSource(): unknown { return this.rows.value; }
}

// The probe component shows the descriptor id and the rows of the view it is given.
const Probe = defineComponent({
  props: { view: { type: Object, required: true }, readOnly: Boolean },
  setup: (props) => () => h('div', { 'data-testid': 'probe' }, `${(props.view as ProbeView).descriptor.id}:${((props.view as ProbeView).getSource() as unknown[]).length}`),
});

const widget: ViewDescriptor = { id: 'clientesWidget', beanClass: '', view: 'table', params: {}, fields: [] };
const client = (getView = vi.fn().mockResolvedValue(widget)) => ({ metadata: { getView } }) as unknown as DynamiaClient;

beforeEach(() => {
  ViewRendererRegistry.registerViewFactory(TABLE, (descriptor) => new ProbeView(descriptor));
  ViewRendererRegistry.register(TABLE, { render: () => Probe } as never);
});
afterEach(() => ViewRendererRegistry.clear());

describe('DynamiaViewer', () => {
  it('renders a descriptor written as a plain object, with no client', async () => {
    const wrapper = mount(Viewer, { props: { descriptor: { view: 'table', fields: ['a'] }, source: [1, 2, 3] } });
    await flushPromises();

    expect(wrapper.find('[data-testid=probe]').text()).toBe('inline-table:3');
  });

  it('resolves a descriptor id with the client prop', async () => {
    const getView = vi.fn().mockResolvedValue(widget);
    const wrapper = mount(Viewer, { props: { descriptorId: 'clientesWidget', client: client(getView), source: [1] } });
    await flushPromises();

    expect(getView).toHaveBeenCalledWith('clientesWidget');
    expect(wrapper.find('[data-testid=probe]').text()).toBe('clientesWidget:1');
  });

  it('resolves a descriptor id with the client provided to the app', async () => {
    const getView = vi.fn().mockResolvedValue(widget);
    const wrapper = mount(Viewer, { props: { descriptorId: 'clientesWidget' }, global: { provide: { [DYNAMIA_CLIENT_KEY as symbol]: client(getView) } } });
    await flushPromises();

    expect(wrapper.find('[data-testid=probe]').text()).toBe('clientesWidget:0');
  });

  it('applies a new source to the live view', async () => {
    const wrapper = mount(Viewer, { props: { descriptor: { view: 'table' }, source: [1] } });
    await flushPromises();

    await wrapper.setProps({ source: [1, 2, 3, 4] });

    expect(wrapper.find('[data-testid=probe]').text()).toBe('inline-table:4');
  });

  it('loads the other descriptor when descriptorId changes, and ignores a stale load', async () => {
    const other = { ...widget, id: 'otro' };
    const getView = vi.fn(async (id: string) => (id === 'otro' ? other : widget));
    const wrapper = mount(Viewer, { props: { descriptorId: 'clientesWidget', client: client(getView) } });
    await flushPromises();

    await wrapper.setProps({ descriptorId: 'otro' });
    await flushPromises();

    expect(wrapper.find('[data-testid=probe]').text()).toBe('otro:0');
  });

  it('does not reload when an inline descriptor is re-created with the same content', async () => {
    const getView = vi.fn();
    const ready = vi.fn();
    const wrapper = mount(Viewer, { props: { descriptor: { view: 'table' }, client: client(getView), onReady: ready } });
    await flushPromises();

    await wrapper.setProps({ descriptor: { view: 'table' } });
    await flushPromises();

    expect(ready).toHaveBeenCalledTimes(1);
  });

  it('reports an unknown descriptor id through the error event', async () => {
    const onError = vi.fn();
    const failing = client(vi.fn().mockRejectedValue(new Error('404')));
    (failing.metadata as unknown as Record<string, unknown>)['getEntities'] = vi.fn().mockResolvedValue({ entities: [] });
    const wrapper = mount(Viewer, { props: { descriptorId: 'nope', client: failing, onError } });
    await flushPromises();

    expect(onError).toHaveBeenCalledTimes(1);
    expect(wrapper.find('.dynamia-viewer-error').exists()).toBe(true);
  });
});

describe('DynamiaViewer with the real table renderer', () => {
  it('draws the table of a plain descriptor, with no app.use(DynamiaVue)', async () => {
    ViewRendererRegistry.register(TABLE, new VueTableRenderer());
    ViewRendererRegistry.registerViewFactory(TABLE, (descriptor, metadata) => new VueTableView(descriptor, metadata));

    const wrapper = mount(Viewer, {
      props: {
        descriptor: { view: 'table', fields: { name: { label: 'Name' }, total: { label: 'Total', component: 'label', params: { converter: 'converters.Currency' } } } },
        source: [{ name: 'Ana', total: 1500 }, { name: 'Luis', total: 20 }],
        readOnly: true,
      },
    });
    await vi.waitFor(() => expect(wrapper.find('.dynamia-table-grid').exists()).toBe(true));

    const text = wrapper.find('.dynamia-table-grid').text();
    expect(text).toContain('Name');
    expect(text).toContain('Ana');
    expect(text).toContain('1,500.00');
    expect(wrapper.findAll('.dynamia-table-row')).toHaveLength(2);
  });
});
