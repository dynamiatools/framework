import type { HttpClient } from '@dynamia-tools/sdk';
import type { FunctionCallOptions, FunctionParams, FunctionResponse } from './types.js';

/**
 * Access the HTTP Functions extension REST API.
 * Base path: /api/dynamia/fx
 *
 * A function answers either structured JSON (`call`) or a binary payload such as a PDF or an image
 * (`callBinary`). Which one is decided by the function, not by the client.
 *
 * Errors reject with `DynamiaApiError`: `400` for invalid parameters, `404` when the function or the requested
 * version does not exist or is inactive, `500` when the function fails. The server's message is in `error.message`.
 */
export class FunctionsApi {
  private readonly http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  /**
   * POST /api/dynamia/fx/{functionName} — Calls a function that returns JSON and resolves with its `data`.
   *
   * @param functionName - the function name, e.g. `WhatsApp.sendMessage`
   * @param params - the function parameters
   * @param options - optional version selection
   * @throws Error if the function answered with a binary payload; use {@link callBinary} for those
   *
   * @example
   * ```ts
   * const fx = new FunctionsApi(client.http);
   * const result = await fx.call<{ messageId: string }>('WhatsApp.sendMessage', { to: '+57300...', text: 'Hi' });
   * ```
   */
  async call<T = unknown>(functionName: string, params?: FunctionParams, options?: FunctionCallOptions): Promise<T> {
    const response = await this.post<FunctionResponse<T> | Blob>(functionName, params, options);
    if (response instanceof Blob) {
      throw new Error(`Function ${functionName} returned binary content; use callBinary()`);
    }
    return response.data;
  }

  /**
   * POST /api/dynamia/fx/{functionName} — Calls a function that returns a binary payload (e.g. a file).
   *
   * @param functionName - the function name
   * @param params - the function parameters
   * @param options - optional version selection
   * @throws Error if the function answered with JSON; use {@link call} for those
   */
  async callBinary(functionName: string, params?: FunctionParams, options?: FunctionCallOptions): Promise<Blob> {
    const response = await this.post<FunctionResponse | Blob>(functionName, params, options);
    if (!(response instanceof Blob)) {
      throw new Error(`Function ${functionName} returned JSON; use call()`);
    }
    return response;
  }

  private post<T>(functionName: string, params?: FunctionParams, options?: FunctionCallOptions): Promise<T> {
    const query = options?.version !== undefined ? `?v=${encodeURIComponent(String(options.version))}` : '';
    return this.http.post<T>(`/api/dynamia/fx/${encodeURIComponent(functionName)}${query}`, { params: params ?? {} });
  }
}
