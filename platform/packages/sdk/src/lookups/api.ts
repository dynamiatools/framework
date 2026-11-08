import type { HttpClient } from '../http.js';

/** A record found by an `EntityLookup`: the client answers a `CHOICE` step with its `key`. */
export interface LookupResult {
  key: string;
  label: string;
}

/**
 * Searches the server side `EntityLookup` of a `CHOICE` step that names a `lookup`
 * (`UIChoices.pickEntity`). The server applies the tenant and the filters of the lookup and caps the page.
 */
export class LookupsApi {
  private static readonly PATH = '/apps/api/lookups';

  constructor(private readonly http: HttpClient) {
  }

  /**
   * @param name  the lookup named by the step
   * @param query what the user typed
   * @param limit most results (the server caps it)
   */
  search(name: string, query = '', limit?: number): Promise<LookupResult[]> {
    return this.http.get<LookupResult[]>(`${LookupsApi.PATH}/${encodeURIComponent(name)}`, {
      q: query,
      ...(limit !== undefined ? { limit } : {}),
    });
  }
}
