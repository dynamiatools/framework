// ─── Core client configuration ───────────────────────────────────────────────

export interface DynamiaClientConfig {
  /** Base URL of your Dynamia Platform instance, e.g. https://app.example.com */
  baseUrl: string;
  /** Bearer / JWT access token */
  token?: string;
  /** HTTP Basic username */
  username?: string;
  /** HTTP Basic password */
  password?: string;
  /** Forward cookies on cross-origin requests */
  withCredentials?: boolean;
  /** Custom fetch implementation (useful for Node.js or tests) */
  fetch?: typeof fetch;
}

