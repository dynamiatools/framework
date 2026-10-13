import { afterEach, describe, expect, it } from 'vitest';
import type { ResolvedField } from '@dynamia-tools/ui-core';
import { cellValue } from './cellValue.js';

const field = (name: string, params?: Record<string, unknown>) => ({ name, params }) as unknown as ResolvedField;

const ORIGINAL_TZ = process.env['TZ'];
afterEach(() => {
  if (ORIGINAL_TZ === undefined) delete process.env['TZ'];
  else process.env['TZ'] = ORIGINAL_TZ;
});

describe('cellValue', () => {
  it('returns the raw value when the column declares no converter', () => {
    expect(cellValue({ name: 'Ana' }, field('name'))).toBe('Ana');
  });

  it('applies params.converter (backend-style name) to the cell', () => {
    expect(cellValue({ total: 1234.5 }, field('total', { converter: 'converters.Currency' }))).toBe('1,234.50');
  });

  it('keeps the calendar day of a date-only value behind UTC', () => {
    process.env['TZ'] = 'America/Bogota';
    expect(cellValue({ d: '2026-10-07' }, field('d', { converter: 'converters.Date' }))).toBe(new Date(2026, 9, 7).toLocaleDateString());
  });

  it('shows the name of a referenced entity and ignores unknown converters', () => {
    expect(cellValue({ c: { id: 2, name: 'Fantasy' } }, field('c'))).toBe('Fantasy');
    expect(cellValue({ x: 'raw' }, field('x', { converter: 'converters.Nope' }))).toBe('raw');
  });

  it('returns empty for a missing row', () => {
    expect(cellValue(null, field('x'))).toBe('');
  });
});
