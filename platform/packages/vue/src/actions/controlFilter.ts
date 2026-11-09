// controlFilter.ts — turns the value of a toolbar control into the filter of the list query.
import type { ActionControl, CrudFilter } from '@dynamia-tools/sdk';

/** What a control widget holds: text for one value, a pair for a range. */
export type ControlValue = string | [string, string];

/**
 * The filter a control produces for its current value, or `null` when the value means "no filter" (empty text, an empty range,
 * the "all" option of a choice). The values stay in the wire form of the server: ISO dates, the key of an option.
 */
export function controlFilter(control: ActionControl, value: ControlValue): CrudFilter | null {
  const { field } = control;
  if (Array.isArray(value)) {
    const [from, to] = value;
    if (!from && !to) return null;
    return { field, op: 'between', value: [from || null, to || null] };
  }
  const text = value.trim();
  if (text === '') return null;
  return { field, op: control.kind === 'TEXT' ? 'like' : 'eq', value: text };
}

/**
 * The dates of a symbolic preset of a control, resolved in the browser's time zone (the server cannot know the user's).
 *
 * @returns `[from, to]` as `YYYY-MM-DD`, or `null` for no preset
 */
export function resolvePreset(preset: string | undefined, today: Date = new Date()): [string, string] | null {
  if (!preset) return null;
  const iso = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  const y = today.getFullYear();
  const m = today.getMonth();
  switch (preset) {
    case 'TODAY': return [iso(today), iso(today)];
    case 'THIS_MONTH': return [iso(new Date(y, m, 1)), iso(new Date(y, m + 1, 0))];
    case 'LAST_MONTH': return [iso(new Date(y, m - 1, 1)), iso(new Date(y, m, 0))];
    default: return null;
  }
}
