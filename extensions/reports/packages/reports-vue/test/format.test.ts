import { describe, expect, it } from 'vitest';
import { exportFileName, formatCell, toApiValue, toInputValue } from '../src/index.js';
import { column, filter } from './helpers.js';

describe('formatCell', () => {
  it('formats numbers and currency with the given locale', () => {
    expect(formatCell(column('n', { dataType: 'NUMBER' }), 1234.5, 'en-US')).toBe('1,234.5');
    expect(formatCell(column('n', { dataType: 'CURRENCY' }), 1234.5, 'en-US')).toBe('1,234.50');
    expect(formatCell(column('n', { dataType: 'NUMBER' }), 'abc', 'en-US')).toBe('abc');
  });

  it('shows empty for null and empty values, and booleans as marks', () => {
    expect(formatCell(column('x'), null)).toBe('');
    expect(formatCell(column('x'), undefined)).toBe('');
    expect(formatCell(column('x'), '')).toBe('');
    expect(formatCell(column('b', { dataType: 'BOOLEAN' }), true)).toBe('✓');
    expect(formatCell(column('b', { dataType: 'BOOLEAN' }), false)).toBe('✗');
  });

  it('formats dates in the local calendar day without time zone shifts', () => {
    const text = formatCell(column('d', { dataType: 'DATE' }), '2026-03-01', 'en-US');
    expect(text).toBe('3/1/2026');
    expect(formatCell(column('d', { dataType: 'DATE' }), 'not a date')).toBe('not a date');
    expect(formatCell(column('d', { dataType: 'DATE_TIME' }), 'garbage')).toBe('garbage');
  });

  it('upper cases when the column asks for it', () => {
    expect(formatCell(column('t', { upperCase: true }), 'abc')).toBe('ABC');
  });
});

describe('filter values', () => {
  it('converts date times and times between the form controls and the API', () => {
    const dateTime = filter('at', { dataType: 'DATE_TIME' });
    expect(toApiValue(dateTime, '2026-03-01T15:30')).toBe('2026-03-01 15:30:00');
    expect(toInputValue(dateTime, '2026-03-01 15:30:00')).toBe('2026-03-01T15:30');

    const time = filter('t', { dataType: 'TIME' });
    expect(toApiValue(time, '18:45')).toBe('18:45:00');
    expect(toInputValue(time, '18:45:10')).toBe('18:45');
  });

  it('keeps other types as they are and drops empty values', () => {
    expect(toApiValue(filter('d', { dataType: 'DATE' }), '2026-03-01')).toBe('2026-03-01');
    expect(toApiValue(filter('n', { dataType: 'NUMBER' }), '12')).toBe('12');
    expect(toApiValue(filter('x'), '')).toBeUndefined();
    expect(toApiValue(filter('x'), undefined)).toBeUndefined();
    expect(toInputValue(filter('x'), null)).toBe('');
  });
});

describe('exportFileName', () => {
  it('builds a safe file name with the date', () => {
    expect(exportFileName('Ventas por Región / 2026', 'csv', new Date(2026, 9, 8))).toBe('ventas-por-region-2026-2026-10-08.csv');
    expect(exportFileName('???', 'pdf', new Date(2026, 0, 2))).toBe('report-2026-01-02.pdf');
  });
});
