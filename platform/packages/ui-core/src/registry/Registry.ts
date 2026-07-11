// Registry.ts — generic key→value registry with optional key normalisation

/**
 * Optional key normaliser. Given a raw key it returns the ordered list of
 * candidate strings to try when storing or looking up a value.
 *
 * If not supplied, every key is used as-is (exact match only).
 */
export type KeyNormalizer = (key: string) => string[];

/**
 * Lightweight, framework-agnostic generic key → value registry.
 *
 * Subclasses (or callers) can supply a {@link KeyNormalizer} to support
 * case-folding, aliases, package-stripped lookups, kebab-case variants, etc.
 * — all without duplicating the bookkeeping logic.
 *
 * Example — case-insensitive registry:
 * <pre>{@code
 * const reg = new Registry<() => void>(k => [k, k.toLowerCase()]);
 * reg.register('MyHandler', fn);
 * reg.get('myhandler'); // → fn
 * }</pre>
 */
export class Registry<TValue> {
  private readonly _map = new Map<string, TValue>();
  protected readonly _normalize: KeyNormalizer;

  constructor(normalizer?: KeyNormalizer) {
    this._normalize = normalizer ?? (k => [k]);
  }

  /**
   * Register `value` under `key` and any optional `aliases`.
   * All keys are normalised before storage.
   */
  register(key: string, value: TValue, aliases: string[] = []): void {
    for (const candidate of [key, ...aliases]) {
      for (const normalized of this._normalize(candidate)) {
        this._map.set(normalized, value);
      }
    }
  }

  /**
   * Retrieve the value registered under `key`.
   * Returns `null` when nothing is found.
   */
  get(key?: string | null): TValue | null {
    if (!key) return null;
    for (const normalized of this._normalize(key)) {
      const value = this._map.get(normalized);
      if (value !== undefined) return value;
    }
    return null;
  }

  /** Returns `true` when a value is registered for `key`. */
  has(key?: string | null): boolean {
    return this.get(key) != null;
  }

  /** Remove all registered entries. */
  clear(): void {
    this._map.clear();
  }

  /** Number of raw stored keys (including alias entries). */
  get size(): number {
    return this._map.size;
  }

  /**
   * Returns all uniquely registered values.
   * Values stored under multiple alias / normalised keys are deduplicated by
   * reference, so each logical entry appears exactly once.
   */
  values(): TValue[] {
    return [...new Set(this._map.values())];
  }

  /**
   * Returns all uniquely registered values that satisfy `predicate`.
   * Each value is tested only once regardless of how many alias keys it was
   * stored under.
   *
   * Example — filter ClientActions by entity class:
   * <pre>{@code
   * const bookActions = ClientActionRegistry.filter(
   *   a => a.applicableClass === 'Book'
   * );
   * }</pre>
   */
  filter(predicate: (value: TValue) => boolean): TValue[] {
    return this.values().filter(predicate);
  }
}
