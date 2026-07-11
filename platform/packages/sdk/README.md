# @dynamia-tools/sdk

> Official JavaScript / TypeScript client SDK for **Dynamia Platform** REST APIs.

`@dynamia-tools/sdk` provides a fully typed, zero-dependency-at-runtime client that covers every REST endpoint exposed by a Dynamia Platform backend: application metadata, navigation tree, entity CRUD, actions, reports, files, SaaS accounts, schedules, and more.

Public API exports include:

- `DynamiaClient`, `DynamiaApiError`
- API classes: `MetadataApi`, `ActionsApi`, `CrudResourceApi`, `CrudServiceApi`, `ReportsApi`, `FilesApi`, `SaasApi`, `ScheduleApi`
- Core types for metadata, navigation, CRUD, reports and SaaS responses

---

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Authentication](#authentication)
- [API Reference](#api-reference)
  - [DynamiaClient](#dynamiaclient)
  - [Metadata API](#metadata-api)
  - [Navigation API](#navigation-api)
  - [CRUD Navigation API](#crud-navigation-api)
  - [CRUD Service API](#crud-service-api)
  - [Actions API](#actions-api)
- [TypeScript Types](#typescript-types)
- [Error Handling](#error-handling)
- [Contributing](#contributing)
- [License](#license)

---

## Installation

```bash
# pnpm (recommended)
pnpm add @dynamia-tools/sdk

# npm
npm install @dynamia-tools/sdk

# yarn
yarn add @dynamia-tools/sdk
```

---

## Quick Start

```typescript
import { DynamiaClient } from '@dynamia-tools/sdk';

const client = new DynamiaClient({
  baseUrl: 'https://your-dynamia-app.com',
  // Bearer token (from login or pre-generated access token)
  token: 'eyJhbGciOiJIUzI1NiJ9...',
});

// Fetch application metadata
const metadata = await client.metadata.getApp();
console.log(metadata.name, metadata.version);

// List entities of a CRUD page
const books = await client.crud('books').findAll();
console.log(books.content.length, books.totalPages);
```

---

## Authentication

Dynamia Platform supports two authentication strategies:

### 1. Bearer Token (recommended for SPAs)

Pass a JWT or pre-generated user access token in the `Authorization` header:

```typescript
const client = new DynamiaClient({
  baseUrl: 'https://app.example.com',
  token: 'your-bearer-token',
});
```

### 2. Basic Auth (server-to-server)

```typescript
const client = new DynamiaClient({
  baseUrl: 'https://app.example.com',
  username: 'admin',
  password: 'secret',
});
```

### 3. Form Login (session-based)

Dynamia Platform uses Spring Security form login at `POST /login`. After a successful login the server issues a JWT cookie; pass `credentials: 'include'` in your fetch options if you rely on cookies:

```typescript
const client = new DynamiaClient({
  baseUrl: 'https://app.example.com',
  withCredentials: true, // sends session cookies automatically
});
```

> Note: the SDK does not include a dedicated `login()` helper. Perform login with your preferred HTTP client (or `fetch`) and then instantiate `DynamiaClient` with cookie forwarding (`withCredentials`) or a token.

---

## API Reference

### DynamiaClient

The root client object. Accepts a `DynamiaClientConfig`:

```typescript
interface DynamiaClientConfig {
  /** Base URL of your Dynamia Platform instance, e.g. https://app.example.com */
  baseUrl: string;
  /** Bearer / access token */
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
```

```typescript
import { DynamiaClient } from '@dynamia-tools/sdk';

const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: '...' });
```

---

### Metadata API

Maps to **`GET /api/app/metadata`** and sub-resources.

```typescript
// Application-level metadata (name, version, description, modules, etc.)
const app: ApplicationMetadata = await client.metadata.getApp();

// Full navigation tree
const nav: NavigationTree = await client.metadata.getNavigation();

// All global actions registered in the platform
const actions: ApplicationMetadataActions = await client.metadata.getGlobalActions();

// All entity metadata (class names, descriptors, endpoints)
const entities: ApplicationMetadataEntities = await client.metadata.getEntities();

// Metadata for a single entity by fully-qualified class name
const book: EntityMetadata = await client.metadata.getEntity('com.example.domain.Book');

// All view descriptors (form, table, tree, …) for an entity
const descriptors: ViewDescriptor[] = await client.metadata.getEntityViews('com.example.domain.Book');

// A specific view descriptor
const formDescriptor: ViewDescriptor = await client.metadata.getEntityView(
  'com.example.domain.Book',
  'form',           // view type id: "form" | "table" | "json-form" | "json" | …
);
```

**Endpoint map:**

| Method | Endpoint | SDK call |
|--------|----------|----------|
| `GET` | `/api/app/metadata` | `metadata.getApp()` |
| `GET` | `/api/app/metadata/navigation` | `metadata.getNavigation()` |
| `GET` | `/api/app/metadata/actions` | `metadata.getGlobalActions()` |
| `GET` | `/api/app/metadata/entities` | `metadata.getEntities()` |
| `GET` | `/api/app/metadata/entities/{className}` | `metadata.getEntity(className)` |
| `GET` | `/api/app/metadata/entities/{className}/views` | `metadata.getEntityViews(className)` |
| `GET` | `/api/app/metadata/entities/{className}/views/{view}` | `metadata.getEntityView(className, view)` |

---

### Navigation API

The navigation tree is also accessible through the metadata API (see above). Use it to build menus, breadcrumbs, or dynamic routing:

```typescript
const tree: NavigationTree = await client.metadata.getNavigation();

tree.navigation.forEach(module => {
  console.log(module.name);
  module.children?.forEach(groupOrPage => {
    groupOrPage.children?.forEach(page => {
      console.log(`  ${page.name} → ${page.internalPath}`);
    });
  });
});
```

---

### CRUD Navigation API

Auto-generated REST endpoints for every `CrudPage` registered in the platform's navigation. The base path defaults to `/api/` but can be customized via `RestApiBasePathProvider` on the server.

```typescript
// Get a CRUD resource client by its virtual path (as defined in CrudPage)
const books = client.crud('store/catalog/books');

// List all (with pagination)
const page1: CrudListResult<Book> = await books.findAll({ page: 1, size: 20 });
// page1.content   → Book[]
// page1.total     → total records
// page1.pageSize  → current page size
// page1.totalPages → total pages

// Filter by query parameters
const filtered = await books.findAll({ q: 'clean', author: 'Martin' });

// Get by ID
const book: Book = await books.findById(42);

// Create
const newBook: Book = await books.create({ title: 'Clean Code', author: 'Robert C. Martin' });

// Update
const updated: Book = await books.update(42, { title: 'Clean Code (2nd Ed.)' });

// Delete
await books.delete(42);
```

**Endpoint map** (path = CrudPage virtual path):

| Method | Endpoint | SDK call |
|--------|----------|----------|
| `GET` | `/api/{path}` | `crud(path).findAll(params?)` |
| `GET` | `/api/{path}/{id}` | `crud(path).findById(id)` |
| `POST` | `/api/{path}` | `crud(path).create(entity)` |
| `PUT` | `/api/{path}/{id}` | `crud(path).update(id, entity)` |
| `DELETE` | `/api/{path}/{id}` | `crud(path).delete(id)` |

---

### CRUD Service API

Low-level generic endpoint that works with **fully-qualified Java class names**. Useful when you know the domain class name directly.

```typescript
const svc = client.crudService('com.example.domain.Book');

// Save (create or update depending on entity ID)
const saved = await svc.save({ title: 'Domain-Driven Design' });

// Get by ID
const book = await svc.findById('42');

// Delete by ID
await svc.delete('42');

// Find by query parameters
const results = await svc.find({ author: 'Evans' });

// Get just the ID matching query parameters
const id = await svc.getId({ isbn: '978-0321125217' });
```

**Endpoint map** (prefix = `/crud-service/{className}`):

| Method | Endpoint | SDK call |
|--------|----------|----------|
| `POST` / `PUT` | `/crud-service/{className}` | `crudService(cls).save(entity)` |
| `GET` | `/crud-service/{className}/{id}` | `crudService(cls).findById(id)` |
| `DELETE` | `/crud-service/{className}/{id}` | `crudService(cls).delete(id)` |
| `POST` | `/crud-service/{className}/find` | `crudService(cls).find(params)` |
| `POST` | `/crud-service/{className}/id` | `crudService(cls).getId(params)` |

---

### Actions API

Execute global or entity-scoped actions registered in the platform.

```typescript
// Execute a global action
const response: ActionExecutionResponse = await client.actions.executeGlobal('sendWelcomeEmail', {
  params: { userId: 5 },
});

// Execute an entity action
const entityResponse: ActionExecutionResponse = await client.actions.executeEntity(
  'com.example.domain.Order',  // entity class name
  'approveOrder',              // action id
  { data: { orderId: 99 }, params: { notify: true } },
);
```

**Endpoint map:**

| Method | Endpoint | SDK call |
|--------|----------|----------|
| `POST` | `/api/app/metadata/actions/{action}` | `actions.executeGlobal(action, request)` |
| `POST` | `/api/app/metadata/entities/{className}/action/{action}` | `actions.executeEntity(className, action, request)` |

---


## TypeScript Types

Key types exported by the SDK (mirroring the Java server model):

```typescript
// Core
interface ApplicationMetadata { name: string; version: string; description?: string; /* ... */ }
interface NavigationTree { navigation: NavigationNode[]; }
interface NavigationNode {
  id: string;
  name: string;
  type?: string;              // "Module" | "PageGroup" | "Page" | "CrudPage" | ...
  internalPath?: string;      // route path used by frontends
  path?: string;              // display path
  children?: NavigationNode[];
}

// Entities
interface ApplicationMetadataEntities { entities: EntityMetadata[]; }
interface EntityMetadata extends BasicMetadata { className: string; actions: ActionMetadata[]; descriptors: ViewDescriptorMetadata[]; actionsEndpoint: string; endpoint: string; }
interface BasicMetadata { id: string; name: string; endpoint?: string; description?: string; icon?: string; }

// Actions
interface ApplicationMetadataActions { actions: ActionMetadata[]; }
interface ActionMetadata extends BasicMetadata { /* ... */ }
interface ActionExecutionRequest { data?: Record<string, unknown>; params?: Record<string, unknown>; }
interface ActionExecutionResponse { message: string; status: string; code: number; data?: unknown; }

// Views
interface ViewDescriptor { id: string; beanClass: string; viewTypeName: string; fields: ViewField[]; params: Record<string, unknown>; }
interface ViewDescriptorMetadata { view: string; descriptor: ViewDescriptor; }
interface ViewField { name: string; fieldClass?: string; label?: string; visible?: boolean; required?: boolean; params: Record<string, unknown>; }

// CRUD
interface CrudPageable { totalSize: number; pageSize: number; firstResult: number; page: number; pagesNumber: number; }
interface CrudRawResponse<T = unknown> { data: T[]; pageable: CrudPageable | null; response: string; }
interface CrudListResult<T = unknown> { content: T[]; total: number; page: number; pageSize: number; totalPages: number; }
type CrudQueryParams = Record<string, string | number | boolean | undefined | null>;

// Reports
interface ReportDTO { id: string; name: string; group: string; endpoint: string; description?: string; }
interface ReportFilters { filters: Array<{ name: string; value: string }>; }

// SaaS
interface AccountDTO { id: number; uuid: string; name: string; status: string; statusDescription?: string; subdomain?: string; }
```

---

## Error Handling

All SDK methods throw a `DynamiaApiError` on non-2xx responses:

```typescript
import { DynamiaApiError } from '@dynamia-tools/sdk';

try {
  const book = await client.crud('catalog/books').findById(9999);
} catch (err) {
  if (err instanceof DynamiaApiError) {
    console.error(`[${err.status}] ${err.message}`);
  }
}
```

`DynamiaApiError` also includes `status`, `url`, and `body` to support centralized logging and UI error mapping.

---

## Contributing

See the monorepo [CONTRIBUTING.md](../../../CONTRIBUTING.md) for full guidelines.

1. Clone the repo and install: `pnpm install`
2. Work inside `packages/sdk/`
3. Run tests: `pnpm --filter @dynamia-tools/sdk test`
4. Build: `pnpm --filter @dynamia-tools/sdk build`

---

## License

[Apache License 2.0](../../../LICENSE) — © Dynamia Soluciones IT SAS

