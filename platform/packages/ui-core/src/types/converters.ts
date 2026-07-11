// Converter function signature for field value transformation

/**
 * A converter transforms a raw value into a display string.
 * Used by TableView columns and FormView fields to format values.
 *
 * @param value - The raw value to convert
 * @param params - Optional params from the field descriptor
 * @returns The formatted display string
 */
export type Converter = (value: unknown, params?: Record<string, unknown>) => string;

/**
 * Registry of named converters. Can be extended at runtime.
 */
export type ConverterRegistry = Record<string, Converter>;
