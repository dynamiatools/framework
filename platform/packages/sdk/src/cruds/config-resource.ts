import type { HttpClient } from '../http.js';

/** Values of a config page by field name. Checkbox fields are booleans, the rest strings. */
export type ConfigValues = Record<string, unknown>;

/** What the server returns for a config page. */
export interface ConfigResult {
  /** Id of the `view: config` descriptor; fetch it with `client.metadata.getView(descriptor)` */
  descriptor: string;
  /** Current values by field name */
  data: ConfigValues;
}

/**
 * Values of a `ConfigPage` (navigation-based endpoint). Base path: /api/{virtualPath}
 *
 * The server maps each field to its application parameter from the descriptor: the client only sends field names, and
 * names that are not fields of the descriptor are ignored.
 */
export class ConfigResourceApi {
  private readonly http: HttpClient;
  private readonly basePath: string;

  constructor(http: HttpClient, virtualPath: string) {
    this.http = http;
    this.basePath = `/api/${virtualPath.replace(/^\/+/, '')}`;
  }

  /** GET /api/{path} — Descriptor id and current values */
  read(): Promise<ConfigResult> {
    return this.http.get<ConfigResult>(this.basePath);
  }

  /** PUT /api/{path} — Saves the given values (fields not sent keep their value) and returns the saved ones */
  save(values: ConfigValues): Promise<ConfigResult> {
    return this.http.put<ConfigResult>(this.basePath, values);
  }
}
