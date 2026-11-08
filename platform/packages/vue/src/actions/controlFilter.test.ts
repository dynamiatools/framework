import { describe, expect, it } from 'vitest';
import type { ActionControl } from '@dynamia-tools/sdk';
import { controlFilter, resolvePreset } from './controlFilter.js';

const control = (kind: ActionControl['kind'], extra: Partial<ActionControl> = {}): ActionControl => ({ kind, field: 'f', ...extra });

describe('controlFilter', () => {
  it('turns a range into a between with open sides as null', () => {
    expect(controlFilter(control('DATE_RANGE'), ['2026-01-01', '2026-01-31'])).toEqual({ field: 'f', op: 'between', value: ['2026-01-01', '2026-01-31'] });
    expect(controlFilter(control('DATE_RANGE'), ['2026-01-01', ''])).toEqual({ field: 'f', op: 'between', value: ['2026-01-01', null] });
  });

  it('means no filter for an empty range, empty text or the "all" option', () => {
    expect(controlFilter(control('DATE_RANGE'), ['', ''])).toBeNull();
    expect(controlFilter(control('TEXT'), '   ')).toBeNull();
    expect(controlFilter(control('CHOICE', { allLabel: 'All' }), '')).toBeNull();
  });

  it('searches text with like and compares the rest with eq', () => {
    expect(controlFilter(control('TEXT'), ' dune ')).toEqual({ field: 'f', op: 'like', value: 'dune' });
    expect(controlFilter(control('CHOICE'), 'OPEN')).toEqual({ field: 'f', op: 'eq', value: 'OPEN' });
    expect(controlFilter(control('DATE'), '2026-03-05')).toEqual({ field: 'f', op: 'eq', value: '2026-03-05' });
  });
});

describe('resolvePreset', () => {
  const day = new Date(2026, 10, 8); // 8 Nov 2026, local time

  it('resolves the presets in the given day', () => {
    expect(resolvePreset('TODAY', day)).toEqual(['2026-11-08', '2026-11-08']);
    expect(resolvePreset('THIS_MONTH', day)).toEqual(['2026-11-01', '2026-11-30']);
    expect(resolvePreset('LAST_MONTH', day)).toEqual(['2026-10-01', '2026-10-31']);
  });

  it('knows no preset when there is none or it is unknown', () => {
    expect(resolvePreset(undefined, day)).toBeNull();
    expect(resolvePreset('NEXT_DECADE', day)).toBeNull();
  });
});
