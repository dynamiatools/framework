import { afterEach, describe, expect, it } from 'vitest';
import { dateConverter, dateTimeConverter, resolveConverter } from '../../src/utils/converters.js';

const ORIGINAL_TZ = process.env['TZ'];

function inZone(tz: string, fn: () => void): void {
  process.env['TZ'] = tz;
  fn();
}

afterEach(() => {
  if (ORIGINAL_TZ === undefined) delete process.env['TZ'];
  else process.env['TZ'] = ORIGINAL_TZ;
});

describe('dateConverter', () => {
  it.each(['America/Bogota', 'America/Los_Angeles', 'UTC', 'Pacific/Auckland'])(
    'keeps the calendar day of a date-only string in %s',
    (tz) => {
      inZone(tz, () => {
        expect(dateConverter('2026-10-07')).toBe(new Date(2026, 9, 7).toLocaleDateString());
        expect(dateConverter('2026-01-01')).toBe(new Date(2026, 0, 1).toLocaleDateString());
      });
    },
  );

  it('still formats full ISO instants through Date', () => {
    const iso = '2026-10-07T15:30:00Z';
    expect(dateConverter(iso)).toBe(new Date(iso).toLocaleDateString());
  });

  it('returns empty for nullish and the raw value for garbage', () => {
    expect(dateConverter(null)).toBe('');
    expect(dateConverter(undefined)).toBe('');
    expect(dateConverter('not a date')).toBe('not a date');
  });
});

describe('dateTimeConverter', () => {
  it('keeps the calendar day of a date-only string behind UTC', () => {
    inZone('America/Bogota', () => {
      expect(dateTimeConverter('2026-10-07')).toBe(new Date(2026, 9, 7).toLocaleString());
    });
  });

  it('formats full ISO instants through Date', () => {
    const iso = '2026-10-07T15:30:00Z';
    expect(dateTimeConverter(iso)).toBe(new Date(iso).toLocaleString());
  });
});

describe('resolveConverter', () => {
  it('resolves backend-style names', () => {
    expect(resolveConverter('converters.Date')).toBe(dateConverter);
  });
});
