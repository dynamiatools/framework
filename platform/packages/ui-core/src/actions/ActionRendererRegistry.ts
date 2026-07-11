import { Registry } from '../registry/Registry.js';

function toKebabCase(value: string): string {
  return value
    .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
    .replace(/[._\s]+/g, '-')
    .toLowerCase();
}

function getSimpleRendererName(value: string): string {
  const normalized = value.trim();
  const withoutPackage = normalized.includes('.')
    ? normalized.substring(normalized.lastIndexOf('.') + 1)
    : normalized;

  return withoutPackage.includes('$')
    ? withoutPackage.substring(withoutPackage.lastIndexOf('$') + 1)
    : withoutPackage;
}

export function getActionRendererKeyCandidates(renderer?: string | null): string[] {
  if (!renderer) return [];

  const raw = renderer.trim();
  if (!raw) return [];

  const simpleName = getSimpleRendererName(raw);
  return [...new Set([
    raw,
    raw.toLowerCase(),
    simpleName,
    simpleName.toLowerCase(),
    toKebabCase(simpleName),
  ])];
}

/**
 * Registry for action renderer components.
 * Keys are normalised using {@link getActionRendererKeyCandidates} so that
 * fully-qualified Java class names, simple names, and kebab-case variants all
 * resolve to the same registered renderer.
 *
 * Backed by the generic {@link Registry} — public static API is unchanged.
 */
export class ActionRendererRegistry {
  private static readonly _registry = new Registry<unknown>(getActionRendererKeyCandidates);

  static register<TRenderer>(
    key: string,
    renderer: TRenderer,
    aliases: string[] = [],
  ): void {
    ActionRendererRegistry._registry.register(key, renderer, aliases);
  }

  static get<TRenderer = unknown>(renderer?: string | null): TRenderer | null {
    return ActionRendererRegistry._registry.get(renderer) as TRenderer | null;
  }

  static has(renderer?: string | null): boolean {
    return ActionRendererRegistry._registry.has(renderer);
  }

  static clear(): void {
    ActionRendererRegistry._registry.clear();
  }
}

