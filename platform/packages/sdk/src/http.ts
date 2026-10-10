import { DynamiaApiError } from './errors.js';
import type { DynamiaClientConfig } from './types.js';

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

/**
 * Minimal HTTP client wrapping fetch with auth headers and error handling.
 */
export class HttpClient {
  private readonly config: DynamiaClientConfig;
  private readonly _fetch: typeof fetch;

  constructor(config: DynamiaClientConfig) {
    this.config = config;
    this._fetch =
      config.fetch ?? (typeof globalThis.fetch === 'function' ? globalThis.fetch.bind(globalThis) : undefined as never);

    if (!this._fetch) {
      throw new Error(
        '@dynamia-tools/sdk: No fetch implementation found. ' +
          'Please pass a custom `fetch` function in the client config (e.g. from node-fetch).',
      );
    }
  }

  // ── Public helpers ────────────────────────────────────────────────────────

  async get<T>(path: string, params?: Record<string, string | number | boolean | undefined | null>): Promise<T> {
    const url = this.buildUrl(path, params);
    return this.request<T>('GET', url);
  }

  async post<T>(path: string, body?: unknown): Promise<T> {
    const url = this.buildUrl(path);
    return this.request<T>('POST', url, body);
  }

  async put<T>(path: string, body?: unknown): Promise<T> {
    const url = this.buildUrl(path);
    return this.request<T>('PUT', url, body);
  }

  async delete<T = void>(path: string): Promise<T> {
    const url = this.buildUrl(path);
    return this.request<T>('DELETE', url);
  }

  /**
   * Sends a raw (non JSON) body, for file transfers. `body` is handed to `fetch` as is, so a `Blob` or `File` is streamed
   * by the browser and never read into memory by the SDK. Errors are mapped like in the JSON helpers.
   */
  async sendRaw<T>(method: HttpMethod, path: string, body: BodyInit, headers: Record<string, string>, signal?: AbortSignal): Promise<T> {
    const url = this.buildUrl(path);
    const init: RequestInit = { method, headers: { ...this.authHeaders(), Accept: 'application/json', ...headers }, body };
    if (signal) init.signal = signal;
    if (this.config.corsMode) init.mode = this.config.corsMode;
    if (this.config.withCredentials) init.credentials = 'include';
    return this.handle<T>(await this._fetch(url, init), url);
  }

  /** GET that answers the body as a `Blob` whatever its type: for file downloads. */
  async getBlob(pathOrUrl: string, signal?: AbortSignal): Promise<Blob> {
    const url = /^https?:\/\//.test(pathOrUrl) ? pathOrUrl : this.buildUrl(pathOrUrl);
    const init: RequestInit = { method: 'GET', headers: this.authHeaders() };
    if (signal) init.signal = signal;
    if (this.config.corsMode) init.mode = this.config.corsMode;
    if (this.config.withCredentials) init.credentials = 'include';
    const response = await this._fetch(url, init);
    await this.ensureOk(response, url);
    return response.blob();
  }

  /** Whether requests are authenticated with a header (token or basic) rather than cookies. */
  usesHeaderAuth(): boolean {
    return Boolean(this.config.token || (this.config.username && this.config.password));
  }

  /** Returns a fully-qualified URL for a given path (no request is made). */
  url(path: string, params?: Record<string, string | number | boolean | undefined | null>): string {
    return this.buildUrl(path, params);
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private buildUrl(path: string, params?: Record<string, string | number | boolean | undefined | null>): string {
    const base = this.config.baseUrl.replace(/\/$/, '');
    const normalized = path.startsWith('/') ? path : `/${path}`;

    // Empty baseUrl → relative path (e.g. Vite proxy, same-origin usage)
    if (!base) {
      if (!params) return normalized;
      const query = Object.entries(params)
        .filter(([, v]) => v !== undefined && v !== null)
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
        .join('&');
      return query ? `${normalized}?${query}` : normalized;
    }

    const url = new URL(`${base}${normalized}`);

    if (params) {
      for (const [key, value] of Object.entries(params)) {
        if (value !== undefined && value !== null) {
          url.searchParams.set(key, String(value));
        }
      }
    }

    return url.toString();
  }

  private buildHeaders(): HeadersInit {
    return {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...this.authHeaders(),
    };
  }

  /** Authorization headers for requests the SDK does not send itself (an `XMLHttpRequest` upload with progress). */
  authorizationHeaders(): Record<string, string> {
    return this.authHeaders();
  }

  private authHeaders(): Record<string, string> {
    const headers: Record<string, string> = {};

    if (this.config.token) {
      headers['Authorization'] = `Bearer ${this.config.token}`;
    } else if (this.config.username && this.config.password) {
      const encoded = btoa(`${this.config.username}:${this.config.password}`);
      headers['Authorization'] = `Basic ${encoded}`;
    }

    return headers;
  }

  private async request<T>(method: HttpMethod, url: string, body?: unknown): Promise<T> {
    const init: RequestInit = {
      method,
      headers: this.buildHeaders(),
    };

    if (this.config.corsMode) {
      init.mode = this.config.corsMode;
    }

    if (this.config.withCredentials) {
      init.credentials = 'include';
    }

    if (body !== undefined) {
      init.body = JSON.stringify(body);
    }

    return this.handle<T>(await this._fetch(url, init), url);
  }

  private async ensureOk(response: Response, url: string): Promise<void> {
    if (!response.ok) {
      let errorBody: unknown;
      try {
        errorBody = await response.json();
      } catch {
        errorBody = await response.text().catch(() => undefined);
      }

      const message =
        (errorBody as Record<string, string>)?.message ??
        (errorBody as Record<string, string>)?.error ??
        response.statusText;

      throw new DynamiaApiError(message, response.status, url, errorBody);
    }
  }

  private async handle<T>(response: Response, url: string): Promise<T> {
    await this.ensureOk(response, url);

    // 204 No Content
    if (response.status === 204) {
      return undefined as T;
    }

    const contentType = response.headers.get('content-type') ?? '';
    if (contentType.includes('application/json')) {
      return response.json() as Promise<T>;
    }

    // Blob for binary responses
    return response.blob() as Promise<T>;
  }
}

