// Validator function signature for field value validation

/**
 * A validator checks a value and returns an error message string if invalid, or null if valid.
 *
 * @param value - The value to validate
 * @param params - Optional params from the field descriptor
 * @returns Error message string, or null if valid
 */
export type Validator = (value: unknown, params?: Record<string, unknown>) => string | null;

/**
 * Registry of named validators. Can be extended at runtime.
 */
export type ValidatorRegistry = Record<string, Validator>;
