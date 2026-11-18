import { Decimal } from './decimal.js';

/** Separators of a locale, read from Intl so that `es-CO` gives `1.234,5` as Java's DecimalFormatSymbols does. */
interface Symbols {
  group: string;
  decimal: string;
  minus: string;
}

const symbolsCache = new Map<string, Symbols>();

function resolveLocale(locale: string | undefined): string | undefined {
  if (!locale) {
    return undefined;
  }
  try {
    return Intl.getCanonicalLocales(locale.replace(/_/g, '-'))[0];
  } catch {
    return undefined;
  }
}

function symbols(locale: string | undefined): Symbols {
  const key = locale ?? '';
  let s = symbolsCache.get(key);
  if (!s) {
    const parts = new Intl.NumberFormat(locale, { useGrouping: true }).formatToParts(-1234567.5);
    const find = (type: string, fallback: string) => parts.find((p) => p.type === type)?.value ?? fallback;
    s = { group: find('group', ','), decimal: find('decimal', '.'), minus: find('minusSign', '-') };
    symbolsCache.set(key, s);
  }
  return s;
}

interface Pattern {
  prefix: string;
  suffix: string;
  groupingSize: number;
  minInt: number;
  minFrac: number;
  maxFrac: number;
}

const patternCache = new Map<string, Pattern | null>();

/**
 * Reads a java.text.DecimalFormat pattern such as `#,##0.00`, `#,##0.##` or `$ #,##0`: grouping, minimum integer
 * digits, minimum and maximum decimals and a literal prefix and suffix. Percent, exponent and negative sub-patterns
 * are not supported (null), and the value is then printed as it is.
 */
function parsePattern(text: string): Pattern | null {
  if (patternCache.has(text)) {
    return patternCache.get(text) ?? null;
  }
  let result: Pattern | null = null;
  const m = /^([^#0,.]*)([#0,]*)(?:\.([#0]*))?([^#0,.]*)$/.exec(text);
  if (m && /[#0]/.test((m[2] ?? '') + (m[3] ?? '')) && !/[%‰;E']/.test(text)) {
    const integer = m[2] ?? '';
    const fraction = m[3] ?? '';
    const comma = integer.lastIndexOf(',');
    result = {
      prefix: m[1] ?? '',
      suffix: m[4] ?? '',
      groupingSize: comma < 0 ? 0 : integer.length - comma - 1,
      minInt: (integer.match(/0/g) ?? []).length,
      minFrac: (fraction.match(/0/g) ?? []).length,
      maxFrac: fraction.length,
    };
  }
  patternCache.set(text, result);
  return result;
}

/**
 * Formats like `new DecimalFormat(pattern, symbols(locale)).format(value)`: HALF_EVEN rounding on the exact value.
 *
 * @returns the text, or null when the pattern is not supported
 */
export function formatNumber(value: Decimal, patternText: string, locale: string | undefined): string | null {
  const pattern = parsePattern(patternText);
  if (!pattern) {
    return null;
  }
  const sym = symbols(resolveLocale(locale));
  const rounded = value.setScale(pattern.maxFrac, 'HALF_EVEN');
  // Optional decimals (#) are dropped when they are zero, down to the minimum.
  let fraction = rounded.scale > 0 ? rounded.toPlainString().replace(/^-?\d+\.?/, '') : '';
  while (fraction.length > pattern.minFrac && fraction.endsWith('0')) {
    fraction = fraction.slice(0, -1);
  }
  let integer = rounded.toPlainString().replace(/^-/, '').replace(/\..*$/, '');
  integer = integer.replace(/^0+/, '');
  if (integer.length < pattern.minInt) {
    integer = integer.padStart(pattern.minInt, '0');
  }
  if (pattern.groupingSize > 0) {
    const groups: string[] = [];
    for (let end = integer.length; end > 0; end -= pattern.groupingSize) {
      groups.unshift(integer.slice(Math.max(0, end - pattern.groupingSize), end));
    }
    integer = groups.join(sym.group);
  }
  const body = integer + (fraction.length > 0 ? sym.decimal + fraction : '');
  const negative = value.signum() < 0;
  return (negative ? sym.minus : '') + pattern.prefix + (body === '' ? '0' : body) + pattern.suffix;
}

// ---------------------------------------------------------------- dates

/** The fields of a date, as in Java's LocalDateTime. */
export interface DateFields {
  year: number;
  month: number; // 1-12
  day: number;
  hour: number;
  minute: number;
  second: number;
  weekday: number; // 0 = Sunday
}

function fieldsOfDate(d: Date): DateFields {
  return {
    year: d.getFullYear(),
    month: d.getMonth() + 1,
    day: d.getDate(),
    hour: d.getHours(),
    minute: d.getMinutes(),
    second: d.getSeconds(),
    weekday: d.getDay(),
  };
}

function fieldsOf(year: number, month: number, day: number, hour = 0, minute = 0, second = 0): DateFields | null {
  const d = new Date(Date.UTC(2000, month - 1, day, hour, minute, second));
  d.setUTCFullYear(year);
  if (d.getUTCFullYear() !== year || d.getUTCMonth() !== month - 1 || d.getUTCDate() !== day) {
    return null; // 2026-02-30 is not a date
  }
  return { year, month, day, hour, minute, second, weekday: d.getUTCDay() };
}

/**
 * An ISO date (`2026-11-01`), date and time without zone (`2026-11-01T14:30:00`) or instant (`...Z`, `...+05:00`, shown
 * in the local time zone). Anything else is null.
 */
export function parseIso(text: string): DateFields | null {
  if (text.length < 10 || !/^\d/.test(text)) {
    return null;
  }
  if (text.length === 10) {
    const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(text);
    return m ? fieldsOf(Number(m[1]), Number(m[2]), Number(m[3])) : null;
  }
  if (/(Z|[+-]\d\d:\d\d)$/.test(text)) {
    if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d+)?)?(Z|[+-]\d\d:\d\d)$/.test(text)) {
      return null;
    }
    const d = new Date(text);
    return Number.isNaN(d.getTime()) ? null : fieldsOfDate(d);
  }
  const m = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2})(?:\.\d+)?)?$/.exec(text);
  return m ? fieldsOf(Number(m[1]), Number(m[2]), Number(m[3]), Number(m[4]), Number(m[5]), Number(m[6] ?? 0)) : null;
}

export function dateFields(value: unknown): DateFields | null {
  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : fieldsOfDate(value);
  }
  if (typeof value === 'string') {
    return parseIso(value.trim());
  }
  return null;
}

function twoDigits(n: number): string {
  return String(n).padStart(2, '0');
}

/**
 * Formats with the common letters of java.time.format.DateTimeFormatter: `yyyy yy MMMM MMM MM M dd d EEEE EEE HH H hh h
 * mm ss a` and quoted text (`'de'`). Month, day and AM/PM names come from the locale.
 *
 * @returns the text, or null when the pattern has a letter that is not supported
 */
export function formatDate(f: DateFields, pattern: string, locale: string | undefined): string | null {
  const tag = resolveLocale(locale);
  const reference = new Date(Date.UTC(2000, f.month - 1, f.day, f.hour, f.minute, f.second));
  reference.setUTCFullYear(f.year); // the real year, so the weekday is the right one
  const names = (options: Intl.DateTimeFormatOptions): string =>
    new Intl.DateTimeFormat(tag, { ...options, timeZone: 'UTC' }).format(reference);
  let out = '';
  for (let i = 0; i < pattern.length; ) {
    const c = pattern.charAt(i);
    if (c === "'") {
      const end = pattern.indexOf("'", i + 1);
      if (end < 0) {
        return null;
      }
      out += end === i + 1 ? "'" : pattern.slice(i + 1, end);
      i = end + 1;
      continue;
    }
    if (!/[A-Za-z]/.test(c)) {
      out += c;
      i++;
      continue;
    }
    let n = 1;
    while (pattern.charAt(i + n) === c) {
      n++;
    }
    i += n;
    switch (c) {
      case 'y':
      case 'u':
        out += n === 2 ? twoDigits(f.year % 100) : String(f.year).padStart(n, '0');
        break;
      case 'M':
        out += n >= 4 ? names({ month: 'long' }) : n === 3 ? names({ month: 'short' }) : n === 2 ? twoDigits(f.month) : String(f.month);
        break;
      case 'd':
        out += n >= 2 ? twoDigits(f.day) : String(f.day);
        break;
      case 'E':
        out += names({ weekday: n >= 4 ? 'long' : 'short' });
        break;
      case 'H':
        out += n >= 2 ? twoDigits(f.hour) : String(f.hour);
        break;
      case 'h': {
        const h = f.hour % 12 === 0 ? 12 : f.hour % 12;
        out += n >= 2 ? twoDigits(h) : String(h);
        break;
      }
      case 'm':
        out += n >= 2 ? twoDigits(f.minute) : String(f.minute);
        break;
      case 's':
        out += n >= 2 ? twoDigits(f.second) : String(f.second);
        break;
      case 'a':
        out += names({ hour: 'numeric', hour12: true }).replace(/[\d\s.:\u202f\u00a0]/g, '') || (f.hour < 12 ? 'AM' : 'PM');
        break;
      default:
        return null;
    }
  }
  return out;
}
