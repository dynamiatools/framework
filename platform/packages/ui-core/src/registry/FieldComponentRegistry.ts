import { Registry } from './Registry.js';

/** Normalizes a descriptor component name (`component: Rating`, `rating`, ` RATING `) to one registry key. */
function fieldComponentKeys(name: string): string[] {
  const key = name.trim().toLowerCase();
  return key ? [key] : [];
}

/**
 * Registry of the components a front end draws for the `component:` name of a view descriptor field
 * (`rating`, `badge`, `switch`...). The core knows only names: each UI adapter registers its own implementation
 * (Vue components in `@dynamia-tools/vue`, TailAdmin-based ones in a theme) and an application can override any
 * of them by registering the same name later. A name nobody registered is not an error: the adapter falls back
 * to the component that matches the field type.
 *
 * Example:
 * <pre>{@code
 * FieldComponentRegistry.register('rating', StarRating, ['stars']);
 * FieldComponentRegistry.get('Rating'); // StarRating
 * }</pre>
 */
export class FieldComponentRegistry {
  private static readonly _registry = new Registry<unknown>(fieldComponentKeys);

  /** Registers `component` under `name` and the optional `aliases`; the last registration of a name wins. */
  static register<TComponent>(name: string, component: TComponent, aliases: string[] = []): void {
    FieldComponentRegistry._registry.register(name, component, aliases);
  }

  /** The component registered under `name` (case-insensitive), or `null`. */
  static get<TComponent = unknown>(name?: string | null): TComponent | null {
    return FieldComponentRegistry._registry.get(name) as TComponent | null;
  }

  /** Whether a component is registered under `name`. */
  static has(name?: string | null): boolean {
    return FieldComponentRegistry._registry.has(name);
  }

  /** Removes every registration (tests). */
  static clear(): void {
    FieldComponentRegistry._registry.clear();
  }
}
