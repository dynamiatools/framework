/**
 * Error thrown by the SDK when the Dynamia Platform server returns a non-2xx response.
 */
export class DynamiaApiError extends Error {
  /** HTTP status code */
  readonly status: number;
  /** URL that was requested */
  readonly url: string;
  /** Raw response body (if available) */
  readonly body?: unknown;

  constructor(message: string, status: number, url: string, body?: unknown) {
    super(message);
    this.name = 'DynamiaApiError';
    this.status = status;
    this.url = url;
    this.body = body;

    // Maintain proper prototype chain in transpiled environments
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

