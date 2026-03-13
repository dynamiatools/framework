// Built-in value converters for display formatting

import type { Converter } from '../types/converters.js';

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

/** Registry of all built-in converters */
export const builtinConverters: Record<string, Converter> = {
  currency: currencyConverter,
  currencySimple: currencySimpleConverter,
  decimal: decimalConverter,
  date: dateConverter,
  dateTime: dateTimeConverter,
};
