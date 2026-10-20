import type { UserConfig } from 'vite';

export interface DynamiaViteOptions {
  /** Backend origin the dev server proxies `/api`, `/login` and `/logout` to. Defaults to `DYNAMIA_BACKEND` or `http://localhost:8080`. */
  backend?: string;
  /** Rollup HTML entry points (name → absolute path). Defaults to Vite's own `index.html`. */
  input?: Record<string, string>;
}

export function dynamiaViteConfig(options?: DynamiaViteOptions): UserConfig;
