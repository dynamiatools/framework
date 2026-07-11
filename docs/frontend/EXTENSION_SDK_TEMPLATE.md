# Extension SDK Template

**Location:** This template should be used when creating new SDK packages for Dynamia Platform extensions.

> **Quick Start:** Copy this entire directory structure and replace `MY_EXTENSION` with your extension name.

---

## Directory Structure

```
extensions/MY_EXTENSION/packages/MY_EXTENSION-sdk/
├── src/
│   ├── api.ts                      # Main API class
│   ├── types.ts                    # TypeScript DTOs / response types
│   ├── index.ts                    # Public exports
│   └── ...                         # Additional API classes/utilities
├── test/
│   ├── MY_EXTENSION.test.ts        # Tests for api.ts
│   ├── helpers.ts                  # Mock utilities (mockFetch, makeHttpClient)
│   └── ...                         # Additional tests
├── README.md                       # User documentation
├── package.json                    # Package metadata
├── vite.config.ts                  # Vite configuration
├── vitest.config.ts                # Vitest configuration
├── tsconfig.json                   # TypeScript configuration
├── LICENSE                         # License (copy from framework root)
└── .npmignore                      # NPM ignore patterns
```

---

## Step-by-Step Creation Guide

### 1. Create Directory

```bash
cd extensions/MY_EXTENSION/packages
mkdir my_extension-sdk
cd my_extension-sdk
```

### 2. Initialize package.json

```json
{
  "name": "@dynamia-tools/my_extension-sdk",
  "version": "1.0.0",
  "description": "Official TypeScript / JavaScript client SDK for the Dynamia My Extension REST API.",
  "type": "module",
  "main": "./dist/index.js",
  "types": "./dist/index.d.ts",
  "exports": {
    ".": {
      "import": "./dist/index.js",
      "types": "./dist/index.d.ts"
    }
  },
  "files": [
    "dist",
    "README.md",
    "LICENSE"
  ],
  "scripts": {
    "build": "tsc --outDir dist",
    "test": "vitest",
    "test:coverage": "vitest --coverage",
    "lint": "eslint src test"
  },
  "keywords": [
    "dynamia",
    "sdk",
    "api",
    "my-extension"
  ],
  "author": "Dynamia Framework",
  "license": "MIT",
  "peerDependencies": {
    "@dynamia-tools/sdk": "^1.0.0"
  },
  "devDependencies": {
    "@dynamia-tools/sdk": "workspace:*",
    "@types/node": "^20.0.0",
    "typescript": "^5.0.0",
    "vitest": "^1.0.0"
  }
}
```

### 3. TypeScript Configuration

**tsconfig.json:**
```json
{
  "extends": "../../../tsconfig.base.json",
  "compilerOptions": {
    "outDir": "./dist"
  },
  "include": ["src"],
  "exclude": ["test"]
}
```

### 4. Vite & Vitest Configuration

**vite.config.ts:**
```typescript
import { defineConfig } from 'vite';

export default defineConfig({
  build: {
    lib: {
      entry: './src/index.ts',
      name: 'DynamiaMyExtensionSdk',
      formats: ['es'],
    },
    rollupOptions: {
      external: ['@dynamia-tools/sdk'],
    },
  },
});
```

**vitest.config.ts:**
```typescript
import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'node',
    globals: true,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: ['node_modules/', 'test/'],
    },
  },
});
```

---

## Template Files

### src/api.ts

```typescript
// @dynamia-tools/my_extension-sdk — API class for My Extension

import type { HttpClient } from '@dynamia-tools/sdk';
import type { MyExtensionDTO, MyExtensionResponse } from './types.js';

/**
 * Access the My Extension REST API.
 * Base path: /api/my_extension
 */
export class MyExtensionApi {
  private readonly http: HttpClient;

  /**
   * @param http - HttpClient instance (typically from DynamiaClient.http)
   */
  constructor(http: HttpClient) {
    this.http = http;
  }

  /**
   * GET /api/my_extension — Get all items
   *
   * @returns Promise resolving to array of MyExtensionDTO
   */
  list(): Promise<MyExtensionDTO[]> {
    return this.http.get<MyExtensionDTO[]>('/api/my_extension');
  }

  /**
   * GET /api/my_extension/{id} — Get item by ID
   *
   * @param id - Item identifier
   * @returns Promise resolving to MyExtensionDTO
   */
  getById(id: string | number): Promise<MyExtensionDTO> {
    return this.http.get<MyExtensionDTO>(`/api/my_extension/${id}`);
  }

  /**
   * POST /api/my_extension — Create new item
   *
   * @param data - Item data to create
   * @returns Promise resolving to created MyExtensionDTO
   */
  create(data: Partial<MyExtensionDTO>): Promise<MyExtensionDTO> {
    return this.http.post<MyExtensionDTO>('/api/my_extension', data);
  }

  /**
   * PUT /api/my_extension/{id} — Update item
   *
   * @param id - Item identifier
   * @param data - Item data to update
   * @returns Promise resolving to updated MyExtensionDTO
   */
  update(id: string | number, data: Partial<MyExtensionDTO>): Promise<MyExtensionDTO> {
    return this.http.put<MyExtensionDTO>(`/api/my_extension/${id}`, data);
  }

  /**
   * DELETE /api/my_extension/{id} — Delete item
   *
   * @param id - Item identifier
   * @returns Promise resolving when deleted
   */
  delete(id: string | number): Promise<void> {
    return this.http.delete(`/api/my_extension/${id}`);
  }
}
```

### src/types.ts

```typescript
/**
 * Data Transfer Objects and types for My Extension API.
 */

/**
 * Response from GET /api/my_extension endpoints
 */
export interface MyExtensionDTO {
  id: string | number;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
  status: 'ACTIVE' | 'INACTIVE';
}

/**
 * Generic response wrapper (if needed)
 */
export interface MyExtensionResponse<T = unknown> {
  data: T;
  status: string;
  statusCode: number;
}
```

### src/index.ts

```typescript
/**
 * @dynamia-tools/my_extension-sdk
 * Official TypeScript / JavaScript client SDK for the Dynamia My Extension REST API.
 */

export { MyExtensionApi } from './api.js';
export type { MyExtensionDTO, MyExtensionResponse } from './types.js';
```

### test/helpers.ts

```typescript
// Test utilities for mocking HTTP calls

import { vi } from 'vitest';
import { DynamiaClient, HttpClient } from '@dynamia-tools/sdk';

/**
 * Create a mocked fetch function that returns a predefined response.
 *
 * @param status - HTTP status code
 * @param body - Response body
 * @param contentType - Content-Type header (default: 'application/json')
 * @returns Vitest mocked fetch function
 */
export function mockFetch(status: number, body: unknown, contentType = 'application/json') {
  return vi.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    status,
    statusText: status === 200 ? 'OK' : 'Error',
    headers: {
      get: (key: string) => (key === 'content-type' ? contentType : null),
    },
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(String(body)),
    blob: () => Promise.resolve(new Blob()),
  } as unknown as Response);
}

/**
 * Create an HttpClient instance with a mocked fetch for testing.
 *
 * @param fetchMock - Mocked fetch function (from mockFetch)
 * @returns HttpClient configured with mock fetch
 */
export function makeHttpClient(fetchMock: ReturnType<typeof vi.fn>): HttpClient {
  const client = new DynamiaClient({
    baseUrl: 'https://api.example.com',
    token: 'test-token',
    fetch: fetchMock,
  });
  return client.http as HttpClient;
}
```

### test/MY_EXTENSION.test.ts

```typescript
// Tests for MyExtensionApi

import { describe, it, expect } from 'vitest';
import { MyExtensionApi } from '../src/api.js';
import { mockFetch, makeHttpClient } from './helpers.js';

describe('MyExtensionApi', () => {
  it('list() calls GET /api/my_extension', async () => {
    const items = [
      { id: 1, name: 'Item 1', status: 'ACTIVE' as const, createdAt: '2025-01-01', updatedAt: '2025-01-01' },
      { id: 2, name: 'Item 2', status: 'INACTIVE' as const, createdAt: '2025-01-02', updatedAt: '2025-01-02' },
    ];
    const fetch = mockFetch(200, items);
    const api = new MyExtensionApi(makeHttpClient(fetch));

    const result = await api.list();

    expect(result).toEqual(items);
    expect(fetch).toHaveBeenCalledOnce();
  });

  it('getById() calls GET /api/my_extension/{id}', async () => {
    const item = { id: 1, name: 'Item 1', status: 'ACTIVE' as const, createdAt: '2025-01-01', updatedAt: '2025-01-01' };
    const fetch = mockFetch(200, item);
    const api = new MyExtensionApi(makeHttpClient(fetch));

    const result = await api.getById(1);

    expect(result).toEqual(item);
    const [url] = fetch.mock.calls[0] as [string];
    expect(url).toContain('/api/my_extension/1');
  });

  it('create() calls POST /api/my_extension', async () => {
    const created = { id: 1, name: 'New Item', status: 'ACTIVE' as const, createdAt: '2025-01-01', updatedAt: '2025-01-01' };
    const fetch = mockFetch(200, created);
    const api = new MyExtensionApi(makeHttpClient(fetch));

    const result = await api.create({ name: 'New Item' });

    expect(result).toEqual(created);
    const [url, init] = fetch.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/my_extension');
    expect(init.method).toBe('POST');
  });

  it('update() calls PUT /api/my_extension/{id}', async () => {
    const updated = { id: 1, name: 'Updated Item', status: 'ACTIVE' as const, createdAt: '2025-01-01', updatedAt: '2025-01-02' };
    const fetch = mockFetch(200, updated);
    const api = new MyExtensionApi(makeHttpClient(fetch));

    const result = await api.update(1, { name: 'Updated Item' });

    expect(result).toEqual(updated);
    const [url, init] = fetch.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/my_extension/1');
    expect(init.method).toBe('PUT');
  });

  it('delete() calls DELETE /api/my_extension/{id}', async () => {
    const fetch = mockFetch(204);
    const api = new MyExtensionApi(makeHttpClient(fetch));

    await api.delete(1);

    const [url, init] = fetch.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/my_extension/1');
    expect(init.method).toBe('DELETE');
  });
});
```

### README.md Template

```markdown
# @dynamia-tools/my_extension-sdk

> Official TypeScript / JavaScript client SDK for the Dynamia My Extension REST API.

\`@dynamia-tools/my_extension-sdk\` provides a small, focused client to interact with the My Extension of a Dynamia Platform backend. It exposes a single API class, \`MyExtensionApi\`, and TypeScript types for the extension's data models.

The package delegates HTTP, authentication and error handling to the core \`@dynamia-tools/sdk\` \`HttpClient\`. The recommended usage is to construct \`MyExtensionApi\` from an existing \`DynamiaClient\` (\`client.http\`).

---

## Installation

Install the package using your preferred package manager:

\`\`\`bash
# pnpm (recommended)
pnpm add @dynamia-tools/my_extension-sdk

# npm
npm install @dynamia-tools/my_extension-sdk

# yarn
yarn add @dynamia-tools/my_extension-sdk
\`\`\`

Note: \`@dynamia-tools/my_extension-sdk\` declares a peer dependency on \`@dynamia-tools/sdk\`. The recommended pattern is to install and use the core SDK as well.

---

## Quick Start

The easiest and most robust way to use the My Extension API is via the core \`DynamiaClient\`:

\`\`\`ts
import { DynamiaClient, DynamiaApiError } from '@dynamia-tools/sdk';
import { MyExtensionApi } from '@dynamia-tools/my_extension-sdk';

// Create the core client
const client = new DynamiaClient({
  baseUrl: 'https://app.example.com',
  token: 'your-bearer-token',
});

// Construct the API using the client's HttpClient
const api = new MyExtensionApi(client.http);

// Use the API
try {
  const items = await api.list();
  console.log(items);
} catch (err) {
  if (err instanceof DynamiaApiError) {
    console.error(\`API error [\${err.status}] \${err.message}\`, err.body);
  } else {
    throw err;
  }
}
\`\`\`

---

## API Reference

### MyExtensionApi

Main class for interacting with the My Extension API.

#### Methods

- **\`list()\`** — GET /api/my_extension  
  Returns all items.

  \`\`\`ts
  const items = await api.list();
  \`\`\`

- **\`getById(id)\`** — GET /api/my_extension/{id}  
  Retrieves a single item by ID.

  \`\`\`ts
  const item = await api.getById('item-123');
  \`\`\`

- **\`create(data)\`** — POST /api/my_extension  
  Creates a new item.

  \`\`\`ts
  const newItem = await api.create({ name: 'My Item' });
  \`\`\`

- **\`update(id, data)\`** — PUT /api/my_extension/{id}  
  Updates an existing item.

  \`\`\`ts
  const updated = await api.update('item-123', { name: 'Updated Name' });
  \`\`\`

- **\`delete(id)\`** — DELETE /api/my_extension/{id}  
  Deletes an item.

  \`\`\`ts
  await api.delete('item-123');
  \`\`\`

---

## Types

\`\`\`ts
import type { MyExtensionDTO } from '@dynamia-tools/my_extension-sdk';

const item: MyExtensionDTO = {
  id: '123',
  name: 'My Item',
  description: 'Item description',
  status: 'ACTIVE',
  createdAt: '2025-01-01T00:00:00Z',
  updatedAt: '2025-01-02T00:00:00Z',
};
\`\`\`

---

## Authentication & Errors

Authentication is handled by \`DynamiaClient\` (bearer token, basic auth, or cookies). When constructing \`MyExtensionApi\`, ensure the client is configured with the appropriate credentials.

All API errors are thrown as \`DynamiaApiError\` exceptions (from \`@dynamia-tools/sdk\`):

\`\`\`ts
try {
  await api.getById('nonexistent');
} catch (err) {
  if (err instanceof DynamiaApiError) {
    console.log(\`Status: \${err.status}\`);
    console.log(\`Message: \${err.message}\`);
    console.log(\`Body: \${err.body}\`);
  }
}
\`\`\`

---

## Contributing

See [CONTRIBUTING.md](../../CONTRIBUTING.md) in the framework root.

---

## License

MIT — See [LICENSE](./LICENSE)
```

---

## Usage in Applications

### Option A: Direct API Usage

```typescript
import { DynamiaClient } from '@dynamia-tools/sdk';
import { MyExtensionApi } from '@dynamia-tools/my_extension-sdk';

const client = new DynamiaClient({ baseUrl: '...', token: '...' });
const api = new MyExtensionApi(client.http);
const items = await api.list();
```

### Option B: Vue Composable

```typescript
// composables/useMyExtension.ts
import { ref } from 'vue';
import { useDynamiaClient } from '@dynamia-tools/vue';
import { MyExtensionApi } from '@dynamia-tools/my_extension-sdk';

export function useMyExtensionItems() {
  const client = useDynamiaClient();
  const items = ref<MyExtensionDTO[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  async function load() {
    if (!client) return;
    loading.value = true;
    try {
      const api = new MyExtensionApi(client.http);
      items.value = await api.list();
    } catch (e) {
      error.value = String(e);
    } finally {
      loading.value = false;
    }
  }

  return { items, loading, error, load };
}
```

---

## Checklist for New Extension SDKs

- [ ] Directory structure created
- [ ] package.json configured with peer dependency on `@dynamia-tools/sdk`
- [ ] TypeScript config extends `tsconfig.base.json`
- [ ] API class created in `src/api.ts` (accepts `HttpClient`)
- [ ] Types defined in `src/types.ts`
- [ ] Public exports in `src/index.ts`
- [ ] Test helpers in `test/helpers.ts`
- [ ] Tests created in `test/*.test.ts`
- [ ] README.md with usage examples
- [ ] LICENSE file copied from root
- [ ] vite.config.ts and vitest.config.ts configured
- [ ] Build script works: `npm run build`
- [ ] Tests pass: `npm test`

---

## References

- [API Client Standards](../../API_CLIENT_STANDARDS.md)
- [API Client Audit Report](../../API_CLIENT_AUDIT_REPORT.md)
- [Core SDK README](../../platform/packages/sdk/README.md)
- [Reports SDK Example](../../extensions/reports/packages/reports-sdk)

