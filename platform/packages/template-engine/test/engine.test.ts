import { afterEach, describe, expect, it } from 'vitest';
import { Decimal, DynamiaTemplateEngine, registerFilter, unregisterFilter } from '../src/index.js';

const engine = new DynamiaTemplateEngine();

describe('safety', () => {
  it('never reaches the prototype chain or functions', () => {
    const data = { user: { name: 'Ada' }, fn: () => 'boom' };
    expect(engine.evaluate('[${user.constructor}][${user.__proto__}][${user.toString}]', data)).toBe('[][][]');
    expect(engine.evaluate('[${fn}][${fn.name}]', data)).toBe('[][]');
    expect(engine.evaluate('${constructor}|${__proto__}|${toString}', {})).toBe('${constructor}|${__proto__}|${toString}');
  });

  it('does not pollute Object.prototype through an each alias', () => {
    engine.evaluate('{{#each items as __proto__}}${__proto__}{{/each}}', { items: [{ polluted: true }] });
    expect(({} as Record<string, unknown>)['polluted']).toBeUndefined();
  });

  it('leaves the caller data untouched', () => {
    const data = { items: [1, 2] };
    engine.evaluate('{{#each items as i}}${i}{{/each}}', data);
    expect(Object.keys(data)).toEqual(['items']);
  });
});

describe('decimal arithmetic', () => {
  it('is exact where floating point is not', () => {
    expect(engine.evaluate('${a + b}', { a: 0.1, b: 0.2 })).toBe('0.3');
    expect(engine.evaluate('${a * b}', { a: 1.1, b: 1.1 })).toBe('1.21');
  });

  it('rounds division to ten decimals', () => {
    expect(engine.evaluate('${a / b}', { a: 1, b: 3 })).toBe('0.3333333333');
  });

  it('treats a non numeric variable as zero', () => {
    expect(engine.evaluate('${a + b}', { a: 5, b: 'x' })).toBe('5');
  });

  it('parses and prints like BigDecimal', () => {
    expect(Decimal.parse('1e3')?.toPlainString()).toBe('1000');
    expect(Decimal.parse('0.0000001')?.toString()).toBe('1E-7');
    expect(Decimal.parse('-.5')?.toPlainString()).toBe('-0.5');
    expect(Decimal.parse('abc')).toBeNull();
    expect(Decimal.exact(0.15)?.toPlainString().startsWith('0.1499999999999999944')).toBe(true);
  });
});

describe('locale option', () => {
  it('is the default of the locale-aware filters and the data wins', () => {
    const es = new DynamiaTemplateEngine({ locale: 'es-CO' });
    expect(es.evaluate('${n|number:#,##0.0}', { n: 1234.5 })).toBe('1.234,5');
    expect(es.evaluate('${n|number:#,##0.0}', { n: 1234.5, locale: 'en-US' })).toBe('1,234.5');
  });

  it('accepts a JVM style tag and survives a bad one', () => {
    expect(engine.evaluate('${n|number:#,##0.0}', { n: 1234.5, locale: 'es_CO' })).toBe('1.234,5');
    expect(() => engine.evaluate('${n|number}', { n: 1, locale: '!!' })).not.toThrow();
  });
});

describe('custom filters', () => {
  afterEach(() => unregisterFilter('shout'));

  it('runs a registered filter, and a failing one lets the value through', () => {
    registerFilter({ name: 'shout', apply: (v) => String(v).toUpperCase() + '!' });
    expect(engine.evaluate('${n|shout}', { n: 'hi' })).toBe('HI!');
    unregisterFilter('shout');
    registerFilter({
      name: 'shout',
      apply: () => {
        throw new Error('x');
      },
    });
    expect(engine.evaluate('${n|shout}', { n: 'hi' })).toBe('hi');
  });

  it('refuses built-in, invalid and duplicate names', () => {
    expect(() => registerFilter({ name: 'upper', apply: (v) => v })).toThrow();
    expect(() => registerFilter({ name: '1x', apply: (v) => v })).toThrow();
    expect(() => registerFilter({ name: 'list', apply: (v) => v })).toThrow();
  });
});

describe('other values', () => {
  it('accepts null, undefined and Date', () => {
    expect(engine.evaluate(null)).toBe('');
    expect(engine.evaluate(undefined)).toBe('');
    expect(engine.evaluate('${d|date:yyyy-MM-dd}', { d: new Date(2026, 10, 1, 9, 5) })).toBe('2026-11-01');
  });

  it('reads Map data', () => {
    expect(engine.evaluate('${m.name}', { m: new Map([['name', 'Ada']]) })).toBe('Ada');
  });
});
