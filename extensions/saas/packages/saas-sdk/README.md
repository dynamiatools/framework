# @dynamia-tools/saas-sdk

> TypeScript / JavaScript client SDK for the Dynamia SaaS extension REST API.

`@dynamia-tools/saas-sdk` provides a small, focused client to interact with the SaaS (multi-tenant) extension of a Dynamia Platform backend. It exposes `SaasApi` for common SaaS operations and the `AccountDTO` type.

The package is intentionally minimal: it delegates HTTP, authentication and error handling to the core `@dynamia-tools/sdk` `HttpClient`. The recommended usage is to construct `SaasApi` from an existing `DynamiaClient` (`client.http`).

---

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Direct usage without `DynamiaClient`](#direct-usage-without-dynamiaclient)
- [API Reference](#api-reference)
- [Types](#types)
- [Authentication & Error Handling](#authentication--error-handling)
- [Contributing](#contributing)
- [License](#license)

---

## Installation

Install the package with your preferred package manager:

```bash
# pnpm (recommended)
pnpm add @dynamia-tools/saas-sdk

# npm
npm install @dynamia-tools/saas-sdk

# yarn
yarn add @dynamia-tools/saas-sdk
```

Note: `@dynamia-tools/saas-sdk` declares a peer dependency on `@dynamia-tools/sdk`. The recommended pattern is to install and use the core SDK as well so you can reuse its `DynamiaClient` and `HttpClient`.

---

## Quick Start

The easiest and most robust way to use the SaaS API is via the core `DynamiaClient`. The `DynamiaClient` sets up fetch, authentication headers and error handling; pass its `http` instance to `SaasApi`:

```ts
import { DynamiaClient, DynamiaApiError } from '@dynamia-tools/sdk';
import { SaasApi } from '@dynamia-tools/saas-sdk';

// Create the core client with baseUrl and token/basic auth
const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: 'your-bearer-token' });

// Construct the SaaS API using the client's HttpClient
const saas = new SaasApi(client.http);

// Fetch account information by UUID
try {
  const account = await saas.getAccount('f9a3e8c2-...');
  console.log(account.name, account.status, account.subdomain ?? 'no subdomain');
} catch (err) {
  if (err instanceof DynamiaApiError) {
    console.error(`API error [${err.status}] ${err.message}`, err.body);
  } else {
    throw err;
  }
}
```

This uses the real `SaasApi` method implemented in `src/api.ts`: `getAccount(uuid: string): Promise<AccountDTO>`

---

## Direct usage without `DynamiaClient`

If you do not want to instantiate `DynamiaClient`, you may provide any compatible `HttpClient` implementation that implements `get`, `post`, `put`, `delete`, and follows the same error semantics. In Node.js this usually means wrapping `node-fetch` and implementing the small helper used by the other SDK packages — however, using `DynamiaClient` is simpler and recommended.

---

## API Reference

### SaasApi

Construct with an `HttpClient` (typically `client.http` from `DynamiaClient`).

- `getAccount(uuid: string): Promise<AccountDTO>`
  - GET `/api/saas/account/{uuid}` — Returns account information for the given UUID.

Example:

```ts
const account = await saas.getAccount('f9a3e8c2-...');
console.log(account);
```

---

## Types

Important types are exported from this package's `src/types.ts`.

`AccountDTO` (excerpt)

```ts
interface AccountDTO {
  id: number;
  uuid: string;
  name: string;
  status: string;
  statusDescription?: string;
  subdomain?: string;
  email?: string;
  statusDate?: string;
  expirationDate?: string;
  locale?: string;
  timeZone?: string;
  customerId?: string;
  planId?: string;
  planName?: string;
}
```

Import types together with the API:

```ts
import { SaasApi } from '@dynamia-tools/saas-sdk';
import type { AccountDTO } from '@dynamia-tools/saas-sdk';
```

---

## Authentication & Error Handling

Authentication is handled by the `HttpClient` provided by `DynamiaClient`. Supported strategies:

- Bearer token: pass `token` to `DynamiaClient`.
- HTTP Basic: pass `username` and `password` to `DynamiaClient`.
- Cookie / Form login: use `withCredentials: true` and perform login via HTTP before calling the SDK.

Errors: the core `HttpClient` maps non-2xx responses to `DynamiaApiError` which contains `status`, `url` and `body`. Catch `DynamiaApiError` to implement centralized logging or UI-friendly error messages.

---

## Contributing

See the repository-level `CONTRIBUTING.md` for contribution guidelines.

Quick local steps:

1. Clone the repo and install dependencies: `pnpm install`
2. Work inside `extensions/saas/packages/saas-sdk/`
3. Build and test: `pnpm --filter @dynamia-tools/saas-sdk build` / `pnpm --filter @dynamia-tools/saas-sdk test`

---

## License

[Apache License 2.0](../../../../LICENSE) — © Dynamia Soluciones IT SAS


