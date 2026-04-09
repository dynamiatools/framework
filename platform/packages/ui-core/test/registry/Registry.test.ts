import { describe, it, expect, beforeEach } from 'vitest';
import { Registry } from '../../src/registry/Registry.js';

describe('Registry', () => {
  it('stores and retrieves a value by exact key', () => {
    const reg = new Registry<string>();
    reg.register('foo', 'bar');
    expect(reg.get('foo')).toBe('bar');
  });

  it('returns null for an unknown key', () => {
    const reg = new Registry<string>();
    expect(reg.get('unknown')).toBeNull();
  });

  it('returns null for null / undefined key', () => {
    const reg = new Registry<string>();
    reg.register('x', 'val');
    expect(reg.get(null)).toBeNull();
    expect(reg.get(undefined)).toBeNull();
  });

  it('registers aliases alongside the primary key', () => {
    const reg = new Registry<string>();
    reg.register('primary', 'value', ['alias1', 'alias2']);
    expect(reg.get('primary')).toBe('value');
    expect(reg.get('alias1')).toBe('value');
    expect(reg.get('alias2')).toBe('value');
  });

  it('uses key normalizer during registration and lookup', () => {
    const reg = new Registry<string>(k => [k, k.toLowerCase()]);
    reg.register('MyKey', 'val');
    expect(reg.get('MyKey')).toBe('val');
    expect(reg.get('mykey')).toBe('val');
  });

  it('has() returns true for registered keys, false otherwise', () => {
    const reg = new Registry<number>();
    reg.register('x', 42);
    expect(reg.has('x')).toBe(true);
    expect(reg.has('z')).toBe(false);
    expect(reg.has(null)).toBe(false);
  });

  it('clear() removes all entries', () => {
    const reg = new Registry<string>();
    reg.register('k', 'v');
    reg.clear();
    expect(reg.get('k')).toBeNull();
    expect(reg.size).toBe(0);
  });

  it('size reflects number of stored entries (including alias keys)', () => {
    const reg = new Registry<string>();
    reg.register('a', 'v1', ['b', 'c']);
    // 3 stored keys: 'a', 'b', 'c'
    expect(reg.size).toBe(3);
  });

  it('last registration wins for the same normalised key', () => {
    const reg = new Registry<string>(k => [k.toLowerCase()]);
    reg.register('KEY', 'first');
    reg.register('key', 'second');
    expect(reg.get('KEY')).toBe('second');
  });

  // ── values() ───────────────────────────────────────────────────────────────

  it('values() returns all unique values (deduped across alias keys)', () => {
    const reg = new Registry<string>(k => [k, k.toLowerCase()]);
    reg.register('Alpha', 'a');
    reg.register('Beta', 'b');
    const vals = reg.values();
    expect(vals).toHaveLength(2);
    expect(vals).toContain('a');
    expect(vals).toContain('b');
  });

  it('values() returns empty array for an empty registry', () => {
    const reg = new Registry<string>();
    expect(reg.values()).toEqual([]);
  });

  // ── filter() ───────────────────────────────────────────────────────────────

  it('filter() returns values matching the predicate', () => {
    const reg = new Registry<number>();
    reg.register('one', 1);
    reg.register('two', 2);
    reg.register('three', 3);
    expect(reg.filter(v => v > 1)).toEqual(expect.arrayContaining([2, 3]));
    expect(reg.filter(v => v > 1)).toHaveLength(2);
  });

  it('filter() returns empty array when nothing matches', () => {
    const reg = new Registry<number>();
    reg.register('one', 1);
    expect(reg.filter(v => v > 99)).toEqual([]);
  });

  it('filter() deduplicates values stored under alias keys', () => {
    const reg = new Registry<{ n: number }>(k => [k, k.toLowerCase()]);
    const obj = { n: 7 };
    reg.register('MyKey', obj); // stored under 'MyKey' and 'mykey'
    const result = reg.filter(v => v.n === 7);
    expect(result).toHaveLength(1);
    expect(result[0]).toBe(obj);
  });
});
