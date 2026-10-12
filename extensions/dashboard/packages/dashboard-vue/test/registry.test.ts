import { afterEach, describe, expect, it } from 'vitest';
import { defineComponent } from 'vue';
import { WidgetRendererRegistry } from '../src/index.js';

const Stub = defineComponent({ template: '<i />' });

describe('WidgetRendererRegistry', () => {
  afterEach(() => WidgetRendererRegistry.clear());

  it('registers, resolves, replaces and unregisters renderers by widget type', () => {
    const Other = defineComponent({ template: '<b />' });
    expect(WidgetRendererRegistry.has('map')).toBe(false);

    WidgetRendererRegistry.register('map', Stub);
    expect(WidgetRendererRegistry.get('map')).toBe(Stub);

    WidgetRendererRegistry.register('map', Other);
    expect(WidgetRendererRegistry.get('map')).toBe(Other);

    expect(WidgetRendererRegistry.unregister('map')).toBe(true);
    expect(WidgetRendererRegistry.unregister('map')).toBe(false);
    expect(WidgetRendererRegistry.get('map')).toBeUndefined();
  });
});
