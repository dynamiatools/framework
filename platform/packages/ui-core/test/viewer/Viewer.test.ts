import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { DynamiaClient, ViewDescriptor } from '@dynamia-tools/sdk';
import { Viewer } from '../../src/viewer/Viewer.js';
import { ViewRendererRegistry } from '../../src/viewer/ViewRendererRegistry.js';
import { View } from '../../src/view/View.js';
import type { ViewType } from '../../src/view/ViewType.js';

class ProbeView extends View {
  source: unknown;
  constructor(type: ViewType, descriptor: ViewDescriptor) { super(type, descriptor, null); }
  async initialize(): Promise<void> {}
  validate(): boolean { return true; }
  override setSource(source: unknown): void { this.source = source; }
}

const widget: ViewDescriptor = { id: 'clientesWidget', beanClass: '', view: 'table', params: {}, fields: [{ name: 'alias', visible: true }] };

function client(over: Record<string, unknown> = {}) {
  return {
    metadata: {
      getView: vi.fn().mockResolvedValue(widget),
      getEntities: vi.fn().mockResolvedValue({ entities: [] }),
      getEntityView: vi.fn(),
      getEntityViews: vi.fn(),
      ...over,
    },
  } as unknown as DynamiaClient & { metadata: Record<string, ReturnType<typeof vi.fn>> };
}

beforeEach(() => {
  ViewRendererRegistry.registerViewFactory({ name: 'table' }, (descriptor) => new ProbeView({ name: 'table' }, descriptor));
});
afterEach(() => ViewRendererRegistry.clear());

describe('Viewer descriptor resolution', () => {
  it('resolves a descriptor that is not bound to an entity by its id', async () => {
    const c = client();
    const viewer = new Viewer({ descriptorId: 'clientesWidget', client: c, source: [{ alias: 'Ana' }] });

    await viewer.initialize();

    expect(c.metadata['getView']).toHaveBeenCalledWith('clientesWidget');
    expect(c.metadata['getEntities']).not.toHaveBeenCalled();
    expect(viewer.resolvedDescriptor).toBe(widget);
    expect((viewer.view as ProbeView).source).toEqual([{ alias: 'Ana' }]);
  });

  it('falls back to the entity descriptors when the id is not served by /views', async () => {
    const entityDescriptor = { ...widget, id: 'bookTable', beanClass: 'Book' };
    const c = client({
      getView: vi.fn().mockRejectedValue(new Error('404')),
      getEntities: vi.fn().mockResolvedValue({ entities: [{ id: 'Book', actions: [], descriptors: [{ id: 'bookTable', view: 'table' }] }] }),
      getEntityView: vi.fn().mockResolvedValue(entityDescriptor),
    });
    const viewer = new Viewer({ descriptorId: 'bookTable', client: c });

    await viewer.initialize();

    expect(viewer.resolvedDescriptor).toBe(entityDescriptor);
    expect(viewer.beanClass).toBe('Book');
  });

  it('fails with not found when no source has the id', async () => {
    const c = client({ getView: vi.fn().mockRejectedValue(new Error('404')) });
    await expect(new Viewer({ descriptorId: 'nope', client: c }).initialize()).rejects.toThrow("Descriptor with id 'nope' not found");
  });

  it('renders a plain descriptor-shaped object without a client', async () => {
    const viewer = new Viewer({ descriptor: { view: 'table', fields: { name: { label: 'Name' } } }, source: [1, 2] });

    await viewer.initialize();

    expect(viewer.resolvedDescriptor).toMatchObject({ id: 'inline-table', beanClass: '', view: 'table', fields: [{ name: 'name', label: 'Name', visible: true }] });
    expect((viewer.view as ProbeView).source).toEqual([1, 2]);
  });

  it('setSource replaces the source of a live view', async () => {
    const viewer = new Viewer({ descriptor: { view: 'table' } });
    await viewer.initialize();

    viewer.setSource(['x']);

    expect((viewer.view as ProbeView).source).toEqual(['x']);
  });
});
