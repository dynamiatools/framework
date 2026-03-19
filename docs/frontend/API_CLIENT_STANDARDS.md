# API Client Standards & Architecture

## Overview

This document establishes the standards and best practices for all API calls across the **Dynamia Tools** SDK packages. All backend communication must flow through `DynamiaClient` and its `HttpClient` to ensure consistency in:

- **Authentication** (Bearer tokens, Basic Auth, Cookies)
- **Error Handling** (uniform `DynamiaApiError` exceptions)
- **Fetch Implementation** (centralized, testable)
- **Request/Response Types** (TypeScript-first)
- **URL Building** (baseUrl, path normalization, query params)

---

## Architecture

### Core: `@dynamia-tools/sdk`

**Location:** `platform/packages/sdk`

The root client for all Dynamia Platform REST APIs. Exports:

1. **`HttpClient`** (`src/http.ts`)
   - Wraps fetch with auth headers
   - Handles URL building with baseUrl + path + query params
   - Provides `get<T>()`, `post<T>()`, `put<T>()`, `delete<T>()`, `url()`
   - All errors become `DynamiaApiError`

2. **`DynamiaClient`** (`src/client.ts`)
   - Holds configured `HttpClient` instance (public readonly `http` property)
   - Exposes sub-APIs:
     - `metadata: MetadataApi` — app metadata, navigation, views
     - `actions: ActionsApi` — execute global/entity actions
     - `schedule: ScheduleApi` — scheduled tasks
     - `crud(virtualPath)` → `CrudResourceApi<T>` — navigation-based CRUD
     - `crudService(className)` → `CrudServiceApi<T>` — service-based CRUD

3. **API Classes** (`src/metadata/*`, `src/cruds/*`, `src/schedule/*`)
   - Accept `HttpClient` in constructor
   - Call only `this.http.get()`, `this.http.post()`, etc.
   - Return typed promises
   - Example: `MetadataApi.getApp()` → `Promise<ApplicationMetadata>`

### Extension SDKs

All extension SDKs follow the **same pattern**:

#### `@dynamia-tools/reports-sdk`

**Location:** `extensions/reports/packages/reports-sdk`

```typescript
// src/api.ts
export class ReportsApi {
  constructor(http: HttpClient) { /* ... */ }
  list(): Promise<ReportDTO[]> { /* uses this.http.get() */ }
  post(group, endpoint, filters?): Promise<unknown> { /* uses this.http.post() */ }
}
```

**Usage:**
```typescript
const client = new DynamiaClient({ baseUrl: '...', token: '...' });
const reports = new ReportsApi(client.http);
const list = await reports.list();
```

#### `@dynamia-tools/saas-sdk`

**Location:** `extensions/saas/packages/saas-sdk`

Identical pattern:
```typescript
export class SaasApi {
  constructor(http: HttpClient) { /* ... */ }
  getAccount(uuid: string): Promise<AccountDTO> { /* */ }
}
```

#### `@dynamia-tools/files-sdk`

**Location:** `extensions/entity-files/packages/files-sdk`

Identical pattern:
```typescript
export class FilesApi {
  constructor(http: HttpClient) { /* ... */ }
  download(file: string, uuid: string): Promise<Blob> { /* */ }
  getUrl(file: string, uuid: string): string { /* */ }
}
```

### UI Packages

#### `@dynamia-tools/ui-core`

**Location:** `platform/packages/ui-core`

**Purpose:** Framework-agnostic view, action, and form classes.

**API call pattern:**
- Views/Actions **do NOT directly call APIs**
- They accept injected data via loaders/callbacks
- Example: `DataSetView.setLoader(loader)` where `loader` is an async callback
- Caller supplies the actual API calls (usually from Vue composables)

**Example:**
```typescript
// ui-core: Define the view
const view = new TableView(descriptor);
view.setLoader(async (params) => {
  // Caller supplies the loader
  return {
    rows: [...],
    pagination: { page: 1, ... }
  };
});
```

#### `@dynamia-tools/vue`

**Location:** `platform/packages/vue`

**Purpose:** Vue 3 integration + composables + components.

**API call pattern:**
1. **`useDynamiaClient()`** — Composable to inject the global `DynamiaClient` provided by the `DynamiaVue` plugin

   ```typescript
   export const DYNAMIA_CLIENT_KEY: InjectionKey<DynamiaClient> = Symbol('DynamiaClient');
   
   export function useDynamiaClient(): DynamiaClient | null {
     return inject<DynamiaClient | null>(DYNAMIA_CLIENT_KEY, null);
   }
   ```

2. **Composables** — Accept custom loaders or use the injected client
   - `useNavigation(client)` — Fetches via `client.metadata.getNavigation()`
   - `useTable(options)` — Accepts optional `loader` callback
   - `useCrud(options)` — Accepts optional `loader` / `onSave` / `onDelete`
   - `useEntityPicker(options)` — Accepts optional `searcher` callback

3. **Plugin** — Registers the global client
   ```typescript
   app.use(DynamiaVue, { client: new DynamiaClient({...}) });
   ```

4. **Views/Renderers** — Call composables; composables wire loaders; loaders use `client.http` or extension APIs

---

## Standard Patterns

### ✅ Correct: Extension SDK with Shared HttpClient

```typescript
// ✅ CORRECT: extension SDKs always accept HttpClient in constructor

// In extension SDK (e.g., @dynamia-tools/reports-sdk/src/api.ts)
export class ReportsApi {
  private readonly http: HttpClient;
  constructor(http: HttpClient) { this.http = http; }
  list(): Promise<ReportDTO[]> { return this.http.get('/api/reports'); }
}

// In application code
const client = new DynamiaClient({ baseUrl: 'https://...', token: '...' });
const reports = new ReportsApi(client.http);  // ← Reuse the same HttpClient
const list = await reports.list();
```

**Why:** Single auth, fetch, error handling; all requests logged/traced consistently.

---

### ✅ Correct: Vue Composable Using Injected Client

```typescript
// ✅ CORRECT: useNavigation accepts DynamiaClient

export function useNavigation(client: DynamiaClient) {
  async function loadNavigation() {
    const tree = await client.metadata.getNavigation();
    // ...
  }
}

// In component
const client = useDynamiaClient();
const { nodes, currentPage } = useNavigation(client!);
```

**Why:** Client flows through a single injection point; all calls originate from one configured instance.

---

### ✅ Correct: Custom Loader in Vue Composable

```typescript
// ✅ CORRECT: useCrud accepts optional loader callback

export interface UseCrudOptions {
  loader?: (params: Record<string, unknown>) => Promise<{ rows: any[]; pagination: any }>;
}

export function useCrud(options: UseCrudOptions) {
  // Caller supplies the loader with their own API logic
  if (options.loader) {
    view.setLoader(options.loader);
  }
}

// In component
const client = useDynamiaClient();
const { view } = useCrud({
  descriptor,
  loader: async (params) => {
    const data = await client!.crud('books').findAll(params);
    return { rows: data.content, pagination: {...} };
  },
});
```

**Why:** Composables are reusable; caller decides where data comes from.

---

### ❌ Incorrect: Direct fetch in Extension SDK

```typescript
// ❌ WRONG: Don't use fetch directly
export class ReportsApi {
  async list() {
    const res = await fetch('/api/reports');  // ❌ No auth headers, error handling
    return res.json();
  }
}
```

**Why:** Bypasses central auth, loses error handling, can't be mocked in tests.

---

### ❌ Incorrect: Direct fetch in View/Composable

```typescript
// ❌ WRONG: Don't use fetch in ui-core Views
export class TableView {
  async load() {
    const res = await fetch(`/api/books?page=${this.page}`);  // ❌ No auth
    this.rows = await res.json();
  }
}
```

**Why:** Views should not know about HTTP; data comes from loaders (injected dependency).

---

### ❌ Incorrect: Hardcoded Fetch in Composable

```typescript
// ❌ WRONG: Direct fetch in composable
export function useBooks() {
  async function load() {
    const res = await fetch('/api/books');  // ❌ No auth, no error handling
    return res.json();
  }
}
```

**Why:** Should use `useDynamiaClient()` → `client.crud('books').findAll()`.

---

## File Structure & Conventions

### Extension SDK Package (template)

```
extensions/MY-EXT/packages/MY-SDK/
├── src/
│   ├── api.ts                 # Main API class, accepts HttpClient
│   ├── types.ts               # DTO / response types
│   └── index.ts               # Exports
├── test/
│   ├── MY-api.test.ts         # Vitest tests
│   └── helpers.ts             # mockFetch, makeHttpClient
├── README.md                  # Usage docs
├── package.json               # Peer dep: @dynamia-tools/sdk
└── vite.config.ts
```

### API Class Conventions

```typescript
// ✅ PATTERN FOR ALL API CLASSES

import type { HttpClient } from '@dynamia-tools/sdk';

/**
 * API description.
 * Base path: /api/resource
 */
export class MyApi {
  private readonly http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  /** GET /api/resource — Description */
  async getAll(): Promise<MyDTO[]> {
    return this.http.get<MyDTO[]>('/api/resource');
  }

  /** POST /api/resource — Description */
  async create(data: Partial<MyDTO>): Promise<MyDTO> {
    return this.http.post<MyDTO>('/api/resource', data);
  }

  /** PUT /api/resource/{id} — Description */
  async update(id: string, data: Partial<MyDTO>): Promise<MyDTO> {
    return this.http.put<MyDTO>(`/api/resource/${id}`, data);
  }

  /** DELETE /api/resource/{id} — Description */
  async delete(id: string): Promise<void> {
    return this.http.delete(`/api/resource/${id}`);
  }
}
```

---

## Testing

### ✅ Correct: Mock HttpClient in Tests

```typescript
// ✅ CORRECT: Use test helpers that create a mock HttpClient

import { vi } from 'vitest';
import { DynamiaClient, HttpClient } from '@dynamia-tools/sdk';

export function mockFetch(status: number, body: unknown) {
  return vi.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(String(body)),
  } as unknown as Response);
}

export function makeHttpClient(fetchMock) {
  const client = new DynamiaClient({
    baseUrl: 'https://api.example.com',
    token: 'test-token',
    fetch: fetchMock,
  });
  return client.http;
}

// In test
const fetch = mockFetch(200, [{ id: 1, name: 'Item' }]);
const http = makeHttpClient(fetch);
const api = new MyApi(http);
const result = await api.getAll();
expect(result).toEqual([{ id: 1, name: 'Item' }]);
```

**Why:** Tests the real API class logic without network; can verify URL and method.

---

## Audit Summary

### ✅ Compliant Packages

| Package | Location | Pattern | Status |
|---------|----------|---------|--------|
| `@dynamia-tools/sdk` | `platform/packages/sdk` | Central HttpClient + sub-APIs | ✅ |
| `@dynamia-tools/reports-sdk` | `extensions/reports/packages/reports-sdk` | Accepts HttpClient in API class | ✅ |
| `@dynamia-tools/saas-sdk` | `extensions/saas/packages/saas-sdk` | Accepts HttpClient in API class | ✅ |
| `@dynamia-tools/files-sdk` | `extensions/entity-files/packages/files-sdk` | Accepts HttpClient in API class | ✅ |
| `@dynamia-tools/ui-core` | `platform/packages/ui-core` | No direct API calls; uses loaders | ✅ |
| `@dynamia-tools/vue` | `platform/packages/vue` | Uses `useDynamiaClient()` injection | ✅ |

### 📋 Key Findings

1. **No direct fetch/axios calls** detected in any package.
2. **Extension SDKs** all follow the HttpClient pattern correctly.
3. **Vue composables** properly inject DynamiaClient and pass it to sub-APIs.
4. **ui-core views** correctly delegate API calls to loaders (no direct HTTP).

---

## Recommendations for New Packages

### When Creating a New Extension SDK

1. Create the API class with `HttpClient` in constructor:
   ```typescript
   export class MyNewApi {
     constructor(http: HttpClient) { this.http = http; }
     // methods using this.http.get/post/etc.
   }
   ```

2. Add README with usage example:
   ```typescript
   import { DynamiaClient } from '@dynamia-tools/sdk';
   import { MyNewApi } from '@dynamia-tools/my-new-sdk';
   
   const client = new DynamiaClient({ baseUrl: '...', token: '...' });
   const api = new MyNewApi(client.http);
   ```

3. Add tests with mockFetch helper:
   ```typescript
   const http = makeHttpClient(mockFetch(200, testData));
   const api = new MyNewApi(http);
   ```

4. Declare peer dependency on `@dynamia-tools/sdk` in package.json.

---

## References

- **SDK README:** `platform/packages/sdk/README.md`
- **Extension SDK Examples:**
  - Reports: `extensions/reports/packages/reports-sdk/README.md`
  - SaaS: `extensions/saas/packages/saas-sdk/README.md`
  - Files: `extensions/entity-files/packages/files-sdk/README.md`
- **Vue Plugin Usage:** `examples/demo-vue-books/src/App.vue`

