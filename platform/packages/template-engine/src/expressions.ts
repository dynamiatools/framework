import { Decimal } from './decimal.js';
import { dateFields, formatDate, formatNumber } from './format.js';

/**
 * Expressions of the template language: property paths, ternaries, conditions, arithmetic and filters. A line-by-line
 * counterpart of Java's TemplateExpressions: the two engines must print the same text for the same data (see
 * platform/contract/fixtures/templates/conformance.json). Nothing here runs code from the template.
 */

/** Marker for a root variable that does not exist (as opposed to one that exists and is null). */
export const UNRESOLVED: unique symbol = Symbol('unresolved');

export type Vars = Record<string, unknown>;

/** A filter of the template language: the `name` in `${value|name}` or `{{value|name:argument}}`. */
export interface TemplateFilter {
  /** A letter followed by letters, digits or underscores. */
  name: string;
  /** Returns the new value; null prints as nothing. Returning `value` unchanged means "cannot do anything". */
  apply(value: unknown, argument: string | null, variables: Vars): unknown;
}

const LOCALE_VAR = 'locale';

function has(vars: Vars, key: string): boolean {
  return Object.hasOwn(vars, key);
}

/** A scope with no prototype, so a variable called `__proto__` or `constructor` is just a variable. */
export function newScope(base: Vars): Vars {
  return Object.assign(Object.create(null) as Vars, base);
}

// ---------------------------------------------------------------- text

/** Java's `String.valueOf` for the values a template prints. */
export function str(value: unknown): string {
  if (value === null || value === undefined) {
    return 'null';
  }
  if (value instanceof Decimal) {
    return value.toString();
  }
  if (Array.isArray(value)) {
    return '[' + value.map(str).join(', ') + ']';
  }
  if (value instanceof Date) {
    return value.toISOString();
  }
  if (value instanceof Map) {
    return '{' + [...value].map(([k, v]) => `${str(k)}=${str(v)}`).join(', ') + '}';
  }
  if (typeof value === 'object') {
    return '{' + Object.entries(value).map(([k, v]) => `${k}=${str(v)}`).join(', ') + '}';
  }
  return String(value);
}

function isBlank(text: string): boolean {
  return text.trim() === '';
}

// --------------------------------------------------------------- paths

function safe(value: unknown): unknown {
  return value === undefined || typeof value === 'function' || typeof value === 'symbol' ? null : value;
}

function property(target: unknown, name: string): unknown {
  if (target instanceof Map) {
    return safe(target.get(name));
  }
  if (target === null || typeof target !== 'object' || Array.isArray(target) || target instanceof Date || target instanceof Decimal) {
    return null;
  }
  return Object.hasOwn(target, name) ? safe((target as Vars)[name]) : null;
}

/**
 * @returns the value, null when the root exists but the value is null or missing, or UNRESOLVED when the root
 * variable is not defined.
 */
export function resolve(vars: Vars, path: string): unknown {
  const trimmed = path.trim();
  // a variable whose own name has a dot ("empresa.nombre") wins
  if (has(vars, trimmed)) {
    return safe(vars[trimmed]);
  }
  const parts = trimmed.split('.');
  const root = parts[0] ?? '';
  if (!has(vars, root)) {
    return UNRESOLVED;
  }
  let current: unknown = safe(vars[root]);
  for (let i = 1; i < parts.length && current !== null; i++) {
    current = property(current, parts[i] ?? '');
  }
  return current;
}

// ------------------------------------------------------------- ternary

/** Position of the first `c` outside quotes and parentheses, from `from`, or -1. */
function topLevel(text: string, c: string, from: number): number {
  let depth = 0;
  let quote = '';
  for (let i = from; i < text.length; i++) {
    const ch = text.charAt(i);
    if (quote !== '') {
      if (ch === quote) {
        quote = '';
      }
    } else if (ch === "'" || ch === '"') {
      quote = ch;
    } else if (ch === '(') {
      depth++;
    } else if (ch === ')') {
      depth--;
    } else if (depth === 0 && ch === c) {
      return i;
    }
  }
  return -1;
}

export function isTernary(text: string): boolean {
  const q = topLevel(text, '?', 0);
  return q > 0 && ternaryColon(text, q) > q;
}

function ternaryColon(text: string, q: number): number {
  let nested = 0;
  let from = q + 1;
  for (;;) {
    const colon = topLevel(text, ':', from);
    const question = topLevel(text, '?', from);
    if (colon < 0) {
      return -1;
    }
    if (question >= 0 && question < colon) {
      nested++;
      from = question + 1;
    } else if (nested > 0) {
      nested--;
      from = colon + 1;
    } else {
      return colon;
    }
  }
}

export function ternary(text: string, vars: Vars): unknown {
  const q = topLevel(text, '?', 0);
  const colon = ternaryColon(text, q);
  const holds = test(text.substring(0, q), vars);
  return branch(text.substring(holds ? q + 1 : colon + 1, holds ? colon : text.length).trim(), vars);
}

function branch(text: string, vars: Vars): unknown {
  return isTernary(text) ? ternary(text, vars) : operand(text, vars);
}

// ---------------------------------------------------------- arithmetic

const ARITHMETIC = /[\w.)]\s*[*/+-]\s*[\w.(-]|^\s*\([\s\S]*\)\s*[*/+-]/;

/** Whether the text looks like `qty * price` or `(a + b) / 2`: operators between operands. */
export function isArithmetic(text: string): boolean {
  return ARITHMETIC.test(text);
}

class Unresolved extends Error {}
class Invalid extends Error {}

function toDecimal(value: unknown): Decimal {
  if (value === null || value === undefined) {
    return Decimal.ZERO;
  }
  if (value instanceof Decimal) {
    return value;
  }
  if (typeof value === 'number') {
    const d = Decimal.fromNumber(value);
    if (!d) {
      throw new Invalid('not a number');
    }
    return d;
  }
  return Decimal.parse(str(value)) ?? Decimal.ZERO;
}

function combine(a: Decimal | null, b: Decimal | null, op: string): Decimal | null {
  if (a === null || b === null) {
    return null;
  }
  switch (op) {
    case '+':
      return a.add(b);
    case '-':
      return a.subtract(b);
    case '*':
      return a.multiply(b);
    default:
      return b.signum() === 0 ? null : a.divide(b, 10, 'HALF_UP');
  }
}

/** A small recursive descent parser: expression, term, factor. */
class Arithmetic {
  private pos = 0;

  constructor(
    private readonly text: string,
    private readonly resolver: (name: string) => unknown,
  ) {}

  expression(): Decimal | null {
    let value = this.term();
    for (;;) {
      this.skip();
      const c = this.peek();
      if (c === '+' || c === '-') {
        this.pos++;
        value = combine(value, this.term(), c);
      } else {
        return value;
      }
    }
  }

  private term(): Decimal | null {
    let value = this.factor();
    for (;;) {
      this.skip();
      const c = this.peek();
      if (c === '*' || c === '/') {
        this.pos++;
        value = combine(value, this.factor(), c);
      } else {
        return value;
      }
    }
  }

  private factor(): Decimal | null {
    this.skip();
    const c = this.peek();
    if (c === '(') {
      this.pos++;
      const value = this.expression();
      this.skip();
      if (this.peek() !== ')') {
        throw new Invalid('Missing )');
      }
      this.pos++;
      return value;
    }
    if (c === '-') {
      this.pos++;
      const value = this.factor();
      return value === null ? null : value.negate();
    }
    const start = this.pos;
    if (/\p{Nd}/u.test(c)) {
      while (this.pos < this.text.length && /[\p{Nd}.]/u.test(this.text.charAt(this.pos))) {
        this.pos++;
      }
      const d = Decimal.parse(this.text.substring(start, this.pos));
      if (!d) {
        throw new Invalid('Bad number');
      }
      return d;
    }
    if (/[\p{L}_]/u.test(c)) {
      while (this.pos < this.text.length && /[\p{L}\p{Nd}_.]/u.test(this.text.charAt(this.pos))) {
        this.pos++;
      }
      const value = this.resolver(this.text.substring(start, this.pos));
      if (value === UNRESOLVED) {
        throw new Unresolved();
      }
      return toDecimal(value);
    }
    throw new Invalid('Unexpected ' + c);
  }

  private skip(): void {
    while (this.pos < this.text.length && /\s/.test(this.text.charAt(this.pos))) {
      this.pos++;
    }
  }

  private peek(): string {
    return this.pos < this.text.length ? this.text.charAt(this.pos) : '\0';
  }

  end(): void {
    this.skip();
    if (this.pos < this.text.length) {
      throw new Invalid('Unexpected ' + this.text.charAt(this.pos));
    }
  }
}

/**
 * Evaluates `+ - * /` with parentheses over numbers and variable paths. A variable that is null or not a number counts
 * as 0; dividing by zero gives null (empty text).
 *
 * @returns the number, null when it cannot be computed, or UNRESOLVED when a variable is not defined at all
 */
export function arithmetic(expression: string, resolver: (name: string) => unknown): Decimal | null | typeof UNRESOLVED {
  try {
    const parser = new Arithmetic(expression, resolver);
    const value = parser.expression();
    parser.end();
    return value === null ? null : value.strip();
  } catch (e) {
    if (e instanceof Unresolved) {
      return UNRESOLVED;
    }
    if (e instanceof Invalid) {
      return null;
    }
    throw e;
  }
}

// ---------------------------------------------------------- conditions

/**
 * `a == b`, `a != b`, `a > b`, `a >= b`, `a < b`, `a <= b`, a single operand (truthiness) or `!operand`, combined with
 * `&&` and `||` (no parentheses; `&&` binds tighter). Operands are paths, numbers or quoted text.
 */
export function test(expression: string, vars: Vars): boolean {
  if (isBlank(expression)) {
    return true;
  }
  for (const orPart of expression.split('||')) {
    let all = true;
    for (const andPart of orPart.split('&&')) {
      if (!atom(andPart.trim(), vars)) {
        all = false;
        break;
      }
    }
    if (all) {
      return true;
    }
  }
  return false;
}

function atom(text: string, vars: Vars): boolean {
  for (const op of ['==', '!=', '>=', '<=', '>', '<']) {
    const i = text.indexOf(op);
    if (i > 0) {
      return compare(operand(text.substring(0, i), vars), operand(text.substring(i + op.length), vars), op);
    }
  }
  if (text.startsWith('!')) {
    return !truthy(operand(text.substring(1), vars));
  }
  return truthy(operand(text, vars));
}

const NUMBER = /^-?\d+(\.\d+)?$/;

function operand(text: string, vars: Vars): unknown {
  const t0 = text.trim();
  if (t0.length >= 2 && ((t0.startsWith("'") && t0.endsWith("'")) || (t0.startsWith('"') && t0.endsWith('"')))) {
    return t0.substring(1, t0.length - 1);
  }
  const t = unwrap(t0);
  if (NUMBER.test(t)) {
    return Decimal.parse(t);
  }
  if (t === 'true' || t === 'false') {
    return t === 'true';
  }
  const value = resolve(vars, t);
  return value === UNRESOLVED ? null : value;
}

function decimalOf(o: unknown): Decimal | null {
  if (o instanceof Decimal) {
    return o;
  }
  if (typeof o === 'number') {
    return Decimal.fromNumber(o);
  }
  if (typeof o === 'string' && NUMBER.test(o.trim())) {
    return Decimal.parse(o);
  }
  return null;
}

function compare(left: unknown, right: unknown, op: string): boolean {
  const a = decimalOf(left);
  const b = decimalOf(right);
  let cmp: number;
  if (a && b) {
    cmp = a.compareTo(b);
  } else {
    const l = left === null || left === undefined ? '' : str(left);
    const r = right === null || right === undefined ? '' : str(right);
    cmp = l === r ? 0 : l < r ? -1 : 1;
  }
  switch (op) {
    case '==':
      return cmp === 0;
    case '!=':
      return cmp !== 0;
    case '>':
      return cmp > 0;
    case '>=':
      return cmp >= 0;
    case '<':
      return cmp < 0;
    default:
      return cmp <= 0;
  }
}

export function truthy(value: unknown): boolean {
  if (value === null || value === undefined) {
    return false;
  }
  if (typeof value === 'boolean') {
    return value;
  }
  if (typeof value === 'number') {
    return value !== 0 && !Number.isNaN(value);
  }
  if (value instanceof Decimal) {
    return value.signum() !== 0;
  }
  if (Array.isArray(value)) {
    return value.length > 0;
  }
  const s = str(value).trim();
  return s !== '' && s.toLowerCase() !== 'false' && s !== '0';
}

/** The name inside a whole `${name}` or `{{name}}` (trimmed); any other text comes back as it is. */
export function unwrap(text: string): string {
  const t = text.trim();
  if (t.startsWith('${') && t.endsWith('}') && t.length >= 3) {
    return t.substring(2, t.length - 1).trim();
  }
  if (t.startsWith('{{') && t.endsWith('}}') && t.length >= 4) {
    return t.substring(2, t.length - 2).trim();
  }
  return text;
}

// ------------------------------------------------------------- filters

const RESERVED = new Set(['upper', 'lower', 'trim', 'default', 'number', 'currency', 'date', 'count', 'sum', 'avg', 'min', 'max']);
const FILTER_NAME = /^[A-Za-z][A-Za-z0-9_]*$/;
const filters = new Map<string, TemplateFilter>();

/** Adds a filter. Throws when the name is invalid, built in or taken. */
export function registerFilter(filter: TemplateFilter): void {
  if (!FILTER_NAME.test(filter.name)) {
    throw new Error('Invalid filter name: ' + filter.name);
  }
  if (RESERVED.has(filter.name)) {
    throw new Error(`'${filter.name}' is a built-in filter and cannot be replaced`);
  }
  if (filters.has(filter.name)) {
    throw new Error(`There is already a filter named '${filter.name}'`);
  }
  filters.set(filter.name, filter);
}

export function unregisterFilter(name: string): void {
  filters.delete(name);
}

export function filterNames(): string[] {
  return [...filters.keys()];
}

function listFilter(name: string, bullet: string | null, numbered: boolean): TemplateFilter {
  return {
    name,
    apply(value, argument) {
      if (value === null || value === undefined) {
        return null;
      }
      if (!Array.isArray(value)) {
        return value;
      }
      const join = name === 'join';
      const field = join || argument === null || isBlank(argument) ? null : argument.trim();
      const texts: string[] = [];
      for (const item of value) {
        const picked = field === null ? item : resolve({ x: item }, 'x.' + field);
        if (picked !== null && picked !== undefined && picked !== UNRESOLVED) {
          texts.push(picked instanceof Decimal ? picked.strip().toPlainString() : str(picked));
        }
      }
      if (bullet !== null) {
        return texts.map((t) => bullet + t).join('\n');
      }
      if (numbered) {
        return texts.map((t, i) => `${i + 1}. ${t}`).join('\n');
      }
      const separator = join ? (argument === null || isBlank(argument) ? ', ' : argument.trim().split('\\n').join('\n')) : ', ';
      return texts.join(separator);
    },
  };
}

registerFilter(listFilter('join', null, false));
registerFilter(listFilter('list', null, false));
registerFilter(listFilter('list_dots', '\u2022 ', false));
registerFilter(listFilter('list_number', null, true));

/** Applies a chain such as `upper|default:-`; unknown filters are ignored. */
export function applyFilters(value: unknown, chain: string[], vars: Vars): unknown {
  let current = value;
  for (const filter of chain) {
    const f = filter.trim();
    const colon = f.indexOf(':');
    const name = colon < 0 ? f : f.substring(0, colon).trim();
    const arg = colon < 0 ? null : f.substring(colon + 1);
    current = applyFilter(current, name, arg, vars) ?? null;
  }
  return current;
}

function localeOf(vars: Vars): string | undefined {
  const l = vars[LOCALE_VAR];
  return typeof l === 'string' && !isBlank(l) ? l : undefined;
}

function applyFilter(value: unknown, name: string, arg: string | null, vars: Vars): unknown {
  switch (name) {
    case 'upper':
      return value === null ? null : str(value).toLocaleUpperCase(localeOf(vars));
    case 'lower':
      return value === null ? null : str(value).toLocaleLowerCase(localeOf(vars));
    case 'trim':
      return value === null ? null : str(value).trim();
    case 'default':
      return value === null || isBlank(str(value)) ? arg : value;
    case 'number':
      return number(value, arg === null || isBlank(arg) ? '#,##0.##' : arg, vars);
    case 'currency':
      return number(value, decimalsPattern(arg), vars);
    case 'date':
      return date(value, arg === null || isBlank(arg) ? 'dd/MM/yyyy' : arg, vars);
    case 'count':
    case 'sum':
    case 'avg':
    case 'min':
    case 'max':
      return aggregate(value, name, arg);
    default:
      return custom(value, name, arg, vars);
  }
}

/** A registered filter; an unknown one, or one that fails, lets the value through. */
function custom(value: unknown, name: string, arg: string | null, vars: Vars): unknown {
  const filter = filters.get(name);
  if (!filter) {
    return value;
  }
  try {
    return filter.apply(value, arg, vars);
  } catch {
    return value;
  }
}

function decimalsPattern(decimals: string | null): string {
  let n = 2;
  if (decimals !== null && !isBlank(decimals) && /^[+-]?\d+$/.test(decimals.trim())) {
    n = Math.max(0, Math.min(6, Number.parseInt(decimals.trim(), 10)));
  }
  return n === 0 ? '#,##0' : '#,##0.' + '0'.repeat(n);
}

function number(value: unknown, pattern: string, vars: Vars): unknown {
  if (value === null) {
    return null;
  }
  const n = typeof value === 'number' ? Decimal.exact(value) : value instanceof Decimal ? value : Decimal.parse(str(value));
  if (!n) {
    return value;
  }
  return formatNumber(n, pattern, localeOf(vars)) ?? value;
}

function date(value: unknown, pattern: string, vars: Vars): unknown {
  if (value === null) {
    return null;
  }
  const fields = dateFields(value);
  if (!fields) {
    return value;
  }
  return formatDate(fields, pattern, localeOf(vars)) ?? value;
}

/**
 * `${items|sum:price}`, `count`, `avg:price`, `min:price`, `max:price` over a list. Items that are not numbers are
 * skipped (the average is over the numbers); the average, minimum and maximum of nothing are empty.
 */
function aggregate(value: unknown, name: string, field: string | null): unknown {
  if (!Array.isArray(value)) {
    return value;
  }
  let count = 0;
  let numeric = 0;
  let sum = Decimal.ZERO;
  let min: Decimal | null = null;
  let max: Decimal | null = null;
  for (const item of value) {
    count++;
    if (field === null || isBlank(field)) {
      continue;
    }
    const scope: Vars = { i: item === null || item === undefined ? '' : item };
    const raw = isArithmetic(field)
      ? arithmetic(field, (n) => resolve(scope, 'i.' + n))
      : resolve(scope, 'i.' + field.trim());
    if (raw === UNRESOLVED || raw === null) {
      continue;
    }
    const n = raw instanceof Decimal ? raw : typeof raw === 'number' ? Decimal.fromNumber(raw) : Decimal.parse(str(raw));
    if (!n) {
      continue;
    }
    numeric++;
    sum = sum.add(n);
    min = min === null || n.compareTo(min) < 0 ? n : min;
    max = max === null || n.compareTo(max) > 0 ? n : max;
  }
  switch (name) {
    case 'count':
      return count;
    case 'sum':
      return sum;
    case 'avg':
      return numeric === 0 ? null : sum.divide(Decimal.of(BigInt(numeric)), 6, 'HALF_UP').strip();
    case 'min':
      return min;
    default:
      return max;
  }
}
