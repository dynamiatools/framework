import { defineAsyncComponent, type Component } from 'vue';
import { FieldComponentRegistry, FieldComponents, FieldResolver, type ResolvedField } from '@dynamia-tools/ui-core';

type Loader = () => Promise<Component>;

/** Components of this package by descriptor name (`component:`). Several names may share one component. */
const BUILTIN: Record<string, Loader> = {
  [FieldComponents.Textbox]: () => import('./Textbox.vue'),
  [FieldComponents.Intbox]: () => import('./Intbox.vue'),
  // Longbox (64-bit integer) shares the integer input component; browser inputs handle the range
  [FieldComponents.Longbox]: () => import('./Intbox.vue'),
  [FieldComponents.Decimalbox]: () => import('./Spinner.vue'),
  [FieldComponents.Spinner]: () => import('./Spinner.vue'),
  [FieldComponents.Combobox]: () => import('./Combobox.vue'),
  [FieldComponents.Datebox]: () => import('./Datebox.vue'),
  [FieldComponents.Checkbox]: () => import('./Checkbox.vue'),
  [FieldComponents.EntityPicker]: () => import('./EntityPicker.vue'),
  [FieldComponents.EntityRefPicker]: () => import('./EntityRefPicker.vue'),
  [FieldComponents.EntityRefMultiPicker]: () => import('./EntityRefMultiPicker.vue'),
  [FieldComponents.EntityRefLabel]: () => import('./EntityRefLabel.vue'),
  [FieldComponents.Label]: () => import('./Label.vue'),
  [FieldComponents.EnumLabel]: () => import('./EnumLabel.vue'),
  [FieldComponents.CoolLabel]: () => import('./CoolLabel.vue'),
  [FieldComponents.Link]: () => import('./Link.vue'),
  [FieldComponents.Textareabox]: () => import('./Textareabox.vue'),
  // Basic versions of names a theme usually draws better (it registers its own in FieldComponentRegistry).
  rating: () => import('./Rating.vue'),
  switch: () => import('./Checkbox.vue'),
  toggle: () => import('./Checkbox.vue'),
  badge: () => import('./EnumLabel.vue'),
  status: () => import('./EnumLabel.vue'),
};

/**
 * Components that only make sense as an input. A table cell never draws them (a `component: textbox` column stays plain
 * text), except the checkbox, which is how a boolean is shown.
 */
const INPUT_ONLY = new Set<string>([
  FieldComponents.Textbox, FieldComponents.Intbox, FieldComponents.Longbox, FieldComponents.Decimalbox,
  FieldComponents.Spinner, FieldComponents.Doublespinner, FieldComponents.Combobox, FieldComponents.Datebox,
  FieldComponents.Timebox, FieldComponents.Textareabox, FieldComponents.EntityPicker,
  FieldComponents.EntityRefPicker, FieldComponents.EntityRefMultiPicker,
]);

const asyncCache = new Map<Loader, Component>();

function toComponent(loader: Loader): Component {
  let component = asyncCache.get(loader);
  if (!component) {
    component = defineAsyncComponent(loader);
    asyncCache.set(loader, component);
  }
  return component;
}

function builtin(name: string): Component | null {
  const loader = BUILTIN[name.trim().toLowerCase()];
  return loader ? toComponent(loader) : null;
}

/** Whether a component exists (registered or built in) for the descriptor component `name`. */
export function hasFieldComponent(name?: string | null): boolean {
  return !!name && (FieldComponentRegistry.has(name) || name.trim().toLowerCase() in BUILTIN);
}

/**
 * The component that draws `field`: the one registered for its descriptor name (theme or application), else the one
 * of this package, else, when nobody implements that name, the basic one for the field type (a boolean stays a
 * checkbox, a number a number box). `null` only when not even the type is known; the caller shows a text input.
 */
export function resolveFieldComponent(field: ResolvedField): Component | null {
  const name = field.resolvedComponent;
  return FieldComponentRegistry.get<Component>(name)
    ?? builtin(name)
    ?? builtin(String(FieldResolver.inferComponent(field.fieldClass)));
}

/**
 * Whether a table cell draws `field` with its component instead of text: a boolean (a disabled checkbox) or a column
 * that names a display component in the descriptor. Names nobody implements and input-only ones stay plain text.
 */
export function isCellComponent(field: ResolvedField): boolean {
  const name = (field.component ?? '').trim().toLowerCase();
  if (name) return name === FieldComponents.Checkbox || (!INPUT_ONLY.has(name) && hasFieldComponent(name));
  return field.resolvedComponent === FieldComponents.Checkbox;
}
