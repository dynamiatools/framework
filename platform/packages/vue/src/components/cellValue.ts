import { entityDisplayLabel, resolveConverter } from '@dynamia-tools/ui-core';
import type { ResolvedField } from '@dynamia-tools/ui-core';

/**
 * Text shown in a table cell that is not drawn with a field component. A referenced entity displays its name, and a
 * column that declares `params.converter` (e.g. `converters.Date`) formats the value with it, as `Label` does.
 */
export function cellValue(row: unknown, field: ResolvedField): unknown {
  if (!row || typeof row !== 'object') return '';
  const value = (row as Record<string, unknown>)[field.name];
  if (value && typeof value === 'object') return entityDisplayLabel(value);
  const converter = resolveConverter(field.params?.['converter'] as string | undefined);
  return converter ? converter(value, field.params) : value;
}
