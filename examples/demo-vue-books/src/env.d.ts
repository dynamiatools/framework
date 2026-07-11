/// <reference types="vite/client" />


interface ImportMetaEnv {
  readonly VITE_DYNAMIA_API_URL?: string;
  readonly VITE_DYNAMIA_TOKEN?: string;
  readonly VITE_DYNAMIA_USERNAME?: string;
  readonly VITE_DYNAMIA_PASSWORD?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

