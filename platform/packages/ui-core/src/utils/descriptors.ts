// Authoring helpers: build a ViewDescriptor from a plain JS/TS object

import type { ViewDescriptor, ViewField } from '@dynamia-tools/sdk';

/**
 * A field in a {@link ViewDescriptorInput}: just its name, or a partial {@link ViewField}.
 */
export type ViewFieldInput = string | (Partial<ViewField> & { name: string });

/**
 * A descriptor written by hand. It has the shape of a backend {@link ViewDescriptor} (the same structure as a YAML view
 * descriptor), but only `view` is required. `fields` can be an array of names / partial fields, or a record keyed by
 * field name as in the YAML descriptors (`{ name: { label: 'Name' }, total: {} }`).
 */
export type ViewDescriptorInput =
  Partial<Omit<ViewDescriptor, 'fields' | 'view'>> & {
    /** View type name: `table`, `form`, `tree`, … */
    view: string;
    fields?: ViewFieldInput[] | Record<string, Omit<Partial<ViewField>, 'name'> | null | undefined>;
  };

/**
 * Builds a complete {@link ViewDescriptor} from a hand-written one, so a view can be rendered from plain JS/TS without a
 * backend descriptor. Missing parts get defaults: `id` (`inline-<view>`), `beanClass` (empty), `params` (empty) and, for
 * every field, `visible: true`. A descriptor that is already complete is returned with the same content.
 *
 * @param input - The descriptor-shaped object
 * @returns A new, complete descriptor (the input is not modified)
 *
 * @example
 * const descriptor = normalizeViewDescriptor({
 *   view: 'table',
 *   fields: { name: { label: 'Name' }, total: { params: { converter: 'converters.Currency' } } },
 * });
 */
export function normalizeViewDescriptor(input: ViewDescriptorInput): ViewDescriptor {
  const { fields, ...rest } = input;
  return {
    ...rest,
    id: input.id ?? `inline-${input.view}`,
    beanClass: input.beanClass ?? '',
    view: input.view,
    params: input.params ?? {},
    fields: normalizeFields(fields),
  };
}

/** Authoring alias of {@link normalizeViewDescriptor}: declares a descriptor and returns it complete. */
export const defineViewDescriptor = normalizeViewDescriptor;

function normalizeFields(fields: ViewDescriptorInput['fields']): ViewField[] {
  if (!fields) return [];
  const list: ViewFieldInput[] = Array.isArray(fields)
    ? fields
    : Object.entries(fields).map(([name, field]) => ({ ...(field ?? {}), name }));
  return list.map((field) => {
    const f = typeof field === 'string' ? { name: field } : field;
    return { ...f, visible: f.visible ?? true };
  });
}
