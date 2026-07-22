// Built-in value converters for display formatting

import type { Converter, ConverterRegistry } from '../types/converters.js';

/**
 * Formats a number as currency with two decimal places and comma separators.
 * @param value - Numeric value to format
 * @returns Formatted currency string (e.g. "1,234.56")
 */
export const currencyConverter: Converter = (value) => {
  if (value === null || value === undefined) return '';
  const num = Number(value);
  if (isNaN(num)) return String(value);
  return num.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

/**
 * Formats a number as simplified currency (no cents for whole numbers).
 * @param value - Numeric value to format
 * @returns Simplified currency string
 */
export const currencySimpleConverter: Converter = (value) => {
  if (value === null || value === undefined) return '';
  const num = Number(value);
  if (isNaN(num)) return String(value);
  if (num % 1 === 0) return num.toLocaleString('en-US');
  return num.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

/**
 * Formats a number as a decimal with configurable decimal places.
 * @param value - Numeric value to format
 * @param params - Optional params: `decimals` (default 2)
 * @returns Formatted decimal string
 */
export const decimalConverter: Converter = (value, params) => {
  if (value === null || value === undefined) return '';
  const num = Number(value);
  if (isNaN(num)) return String(value);
  const decimals = typeof params?.['decimals'] === 'number' ? params['decimals'] : 2;
  return num.toFixed(decimals);
};

/**
 * Formats a Date or date string as a locale date string.
 * @param value - Date value to format
 * @returns Formatted date string (locale-dependent)
 */
export const dateConverter: Converter = (value) => {
  if (value === null || value === undefined) return '';
  try {
    const d = new Date(value as string | number);
    return isNaN(d.getTime()) ? String(value) : d.toLocaleDateString();
  } catch { return String(value); }
};

/**
 * Formats a Date or date string as a locale date-time string.
 * @param value - Date-time value to format
 * @returns Formatted date-time string (locale-dependent)
 */
export const dateTimeConverter: Converter = (value) => {
  if (value === null || value === undefined) return '';
  try {
    const d = new Date(value as string | number);
    return isNaN(d.getTime()) ? String(value) : d.toLocaleString();
  } catch { return String(value); }
};

/**
 * Extracts a human-readable label from an entity-reference value — a `{id, name, ...}`-shaped
 * object, mirroring the SDK's `EntityReference` on the wire. Used anywhere a field/column value
 * might be a whole referenced entity rather than a scalar (table cells, read-only labels), so it
 * displays as a name instead of a stringified object.
 *
 * @param value - The raw field value (may be an entity-reference object, a scalar, or nullish)
 * @param labelField - Property to prefer before falling back to `name` / `label` / `id` (default `"name"`)
 *
 * @example
 * entityDisplayLabel({ id: 2, name: 'Novels / Fantasy' }) // → 'Novels / Fantasy'
 * entityDisplayLabel('Clean Code')                        // → 'Clean Code'
 */
export function entityDisplayLabel(value: unknown, labelField?: string): string {
  if (value === null || value === undefined || value === '') return '';
  if (typeof value !== 'object') return String(value);
  const obj = value as Record<string, unknown>;
  const label = obj[labelField ?? 'name'] ?? obj['name'] ?? obj['label'] ?? obj['id'];
  return label !== undefined && label !== null ? String(label) : '';
}

/** Registry of all built-in converters */
export const builtinConverters: Record<string, Converter> = {
  currency: currencyConverter,
  currencySimple: currencySimpleConverter,
  decimal: decimalConverter,
  date: dateConverter,
  dateTime: dateTimeConverter,
};

/**
 * Resolves a field descriptor's `params.converter` (e.g. Java-side `"converters.Currency"`) to
 * a registered {@link Converter}, matching case-insensitively on the last dot-separated segment
 * so both bare names (`"currency"`) and fully-qualified ones (`"converters.Currency"`) work.
 *
 * @param name - Raw converter name from `field.params.converter`, if any
 * @param registry - Converter lookup table (defaults to {@link builtinConverters})
 * @returns The matching converter, or `null` if `name` is unset or unrecognised
 *
 * @example
 * resolveConverter('converters.Currency') // → currencyConverter
 * resolveConverter('date')                // → dateConverter
 * resolveConverter(undefined)             // → null
 */
export function resolveConverter(name: string | undefined, registry: ConverterRegistry = builtinConverters): Converter | null {
  if (!name) return null;
  const key = name.includes('.') ? name.slice(name.lastIndexOf('.') + 1) : name;
  const lowerKey = key.toLowerCase();
  const matchKey = Object.keys(registry).find(k => k.toLowerCase() === lowerKey);
  return matchKey ? (registry[matchKey] ?? null) : null;
}
