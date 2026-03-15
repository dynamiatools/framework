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

export class ActionRendererRegistry {
  private static readonly renderers = new Map<string, unknown>();

  static register<TRenderer>(
    key: string,
    renderer: TRenderer,
    aliases: string[] = [],
  ): void {
    for (const candidate of [key, ...aliases]) {
      for (const normalized of getActionRendererKeyCandidates(candidate)) {
        ActionRendererRegistry.renderers.set(normalized, renderer);
      }
    }
  }

  static get<TRenderer = unknown>(renderer?: string | null): TRenderer | null {
    for (const candidate of getActionRendererKeyCandidates(renderer)) {
      const registered = ActionRendererRegistry.renderers.get(candidate);
      if (registered) {
        return registered as TRenderer;
      }
    }

    return null;
  }

  static has(renderer?: string | null): boolean {
    return ActionRendererRegistry.get(renderer) != null;
  }

  static clear(): void {
    ActionRendererRegistry.renderers.clear();
  }
}

