/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Backend origin. Leave empty in development: Vite proxies /api to the backend. */
  readonly VITE_API_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
