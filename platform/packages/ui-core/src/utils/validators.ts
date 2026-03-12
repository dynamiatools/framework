// Built-in field value validators

import type { Validator } from '../types/validators.js';

/**
 * Validates that a value is not empty/null/undefined.
 * @param value - Value to check
 * @returns Error message if empty, null if valid
 */
export const requiredValidator: Validator = (value) => {
  if (value === null || value === undefined || value === '') return 'This field is required';
  if (typeof value === 'string' && value.trim() === '') return 'This field is required';
  return null;
};

/**
 * Validates a value against a regex constraint.
 * @param value - Value to validate
 * @param params - Must include `pattern` (regex string) and optional `message`
 * @returns Error message if invalid, null if valid
 */
export const constraintValidator: Validator = (value, params) => {
  if (value === null || value === undefined || value === '') return null;
  const pattern = params?.['pattern'];
  if (typeof pattern === 'string') {
    try {
      const regex = new RegExp(pattern);
      if (!regex.test(String(value))) {
        return typeof params?.['message'] === 'string' ? params['message'] : 'Invalid value';
      }
    } catch { return 'Invalid constraint pattern'; }
  }
  return null;
};

/** Registry of all built-in validators */
export const builtinValidators: Record<string, Validator> = {
  required: requiredValidator,
  constraint: constraintValidator,
};
