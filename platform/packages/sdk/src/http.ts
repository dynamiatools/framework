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

  /** Returns a fully-qualified URL for a given path (no request is made). */
  url(path: string, params?: Record<string, string | number | boolean | undefined | null>): string {
    return this.buildUrl(path, params);
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private buildUrl(path: string, params?: Record<string, string | number | boolean | undefined | null>): string {
    const base = this.config.baseUrl.replace(/\/$/, '');
    const normalized = path.startsWith('/') ? path : `/${path}`;
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
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    };

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

    if (this.config.withCredentials) {
      init.credentials = 'include';
    }

    if (body !== undefined) {
      init.body = JSON.stringify(body);
    }

    const response = await this._fetch(url, init);

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

