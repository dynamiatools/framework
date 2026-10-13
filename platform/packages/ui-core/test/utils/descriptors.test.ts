import { describe, expect, it } from 'vitest';
import type { ViewDescriptor } from '@dynamia-tools/sdk';
import { defineViewDescriptor, normalizeViewDescriptor } from '../../src/utils/descriptors.js';

describe('normalizeViewDescriptor', () => {
  it('fills the defaults of a minimal descriptor', () => {
    expect(normalizeViewDescriptor({ view: 'table' })).toEqual({
      id: 'inline-table', beanClass: '', view: 'table', params: {}, fields: [],
    });
  });

  it('accepts fields as names, partial fields or a record keyed by name (YAML shape)', () => {
    const fromArray = normalizeViewDescriptor({ view: 'table', fields: ['name', { name: 'total', label: 'Total' }] });
    expect(fromArray.fields).toEqual([{ name: 'name', visible: true }, { name: 'total', label: 'Total', visible: true }]);

    const fromRecord = normalizeViewDescriptor({
      view: 'table',
      fields: { name: { label: 'Name' }, total: { params: { converter: 'converters.Currency' } }, note: null },
    });
    expect(fromRecord.fields).toEqual([
      { name: 'name', label: 'Name', visible: true },
      { name: 'total', params: { converter: 'converters.Currency' }, visible: true },
      { name: 'note', visible: true },
    ]);
  });

  it('keeps what the caller declared', () => {
    const d = normalizeViewDescriptor({ id: 'mine', beanClass: 'Book', view: 'form', params: { a: 1 }, fields: [{ name: 'x', visible: false }] });
    expect(d).toMatchObject({ id: 'mine', beanClass: 'Book', view: 'form', params: { a: 1 } });
    expect(d.fields).toEqual([{ name: 'x', visible: false }]);
  });

  it('returns the same content for a complete descriptor and does not mutate the input', () => {
    const complete: ViewDescriptor = { id: 'x', beanClass: 'Book', view: 'table', params: {}, fields: [{ name: 'a', visible: true }] };
    const copy = JSON.parse(JSON.stringify(complete));
    expect(normalizeViewDescriptor(complete)).toEqual(complete);
    expect(complete).toEqual(copy);
  });

  it('defineViewDescriptor is the authoring alias', () => {
    expect(defineViewDescriptor({ view: 'tree' }).id).toBe('inline-tree');
  });
});
