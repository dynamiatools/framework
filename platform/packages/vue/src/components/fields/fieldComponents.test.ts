import { afterEach, describe, expect, it } from 'vitest';
import { FieldComponentRegistry, type ResolvedField } from '@dynamia-tools/ui-core';
import { hasFieldComponent, isCellComponent, resolveFieldComponent } from './fieldComponents.js';

function field(over: Partial<ResolvedField>): ResolvedField {
  return {
    name: 'f', resolvedComponent: 'textbox', resolvedLabel: 'F', gridSpan: 1, resolvedVisible: true,
    resolvedRequired: false, rowIndex: 0, colIndex: 0, ...over,
  } as ResolvedField;
}

describe('fieldComponents', () => {
  afterEach(() => FieldComponentRegistry.clear());

  it('draws a boolean column as a checkbox without a descriptor component', () => {
    expect(isCellComponent(field({ fieldClass: 'java.lang.Boolean', resolvedComponent: 'checkbox' }))).toBe(true);
  });

  it('keeps input-only and unknown names as plain text in a cell', () => {
    expect(isCellComponent(field({ component: 'textbox', resolvedComponent: 'textbox' }))).toBe(false);
    expect(isCellComponent(field({ component: 'nobodyKnowsThis', resolvedComponent: 'nobodyKnowsThis' }))).toBe(false);
    expect(isCellComponent(field({ resolvedComponent: 'textbox' }))).toBe(false);
  });

  it('draws a column that names a built-in or a registered display component', () => {
    expect(isCellComponent(field({ component: 'rating', resolvedComponent: 'rating' }))).toBe(true);
    FieldComponentRegistry.register('Sparkline', {});
    expect(isCellComponent(field({ component: 'sparkline', resolvedComponent: 'sparkline' }))).toBe(true);
  });

  it('prefers the registered component over the built-in one, case-insensitively', () => {
    const custom = { name: 'Custom' };
    FieldComponentRegistry.register('rating', custom);
    expect(resolveFieldComponent(field({ resolvedComponent: 'Rating' }))).toBe(custom);
  });

  it('falls back to the component of the field type when nobody implements the name', () => {
    expect(hasFieldComponent('nobodyKnowsThis')).toBe(false);
    const fallback = resolveFieldComponent(field({ resolvedComponent: 'nobodyKnowsThis', fieldClass: 'java.lang.Boolean' }));
    expect(fallback).toBe(resolveFieldComponent(field({ resolvedComponent: 'checkbox' })));
    expect(fallback).not.toBeNull();
  });
});
