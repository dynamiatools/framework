# @dynamia-tools/reports-sdk

> Official TypeScript / JavaScript client SDK for the Dynamia Reports extension REST API.

`@dynamia-tools/reports-sdk` provides a small, focused client to interact with the Reports extension of a Dynamia Platform backend. It exposes a single API class, `ReportsApi`, and the report-related TypeScript types. The implementation intentionally delegates HTTP, auth and error handling to the core `@dynamia-tools/sdk` `HttpClient`.

This README documents how to install the package, how to integrate it with the core SDK and how to use the Reports API methods implemented in `src/api.ts`.

---

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Direct usage notes](#direct-usage-notes)
- [Reports API (methods & examples)](#reports-api)
- [TypeScript types](#typescript-types)
- [Handling exports / binary responses](#handling-exports--binary-responses)
- [Authentication & errors](#authentication--errors)
- [Contributing](#contributing)
- [License](#license)

---

## Installation

Install the package using your preferred package manager:

```bash
# pnpm (recommended)
pnpm add @dynamia-tools/reports-sdk

# npm
npm install @dynamia-tools/reports-sdk

# yarn
yarn add @dynamia-tools/reports-sdk
```

This package declares a peer dependency on `@dynamia-tools/sdk` (see `package.json`). The recommended way to use `ReportsApi` is together with the core `DynamiaClient` so the HTTP client, authentication and fetch implementation are configured consistently across APIs.

---

## Quick Start

The core SDK (`@dynamia-tools/sdk`) constructs and configures an `HttpClient` that handles base URL, authentication headers, fetch implementation and error handling. The `ReportsApi` expects an instance compatible with that `HttpClient` and is therefore typically constructed from an existing `DynamiaClient`:

```ts
import { DynamiaClient } from '@dynamia-tools/sdk';
import { ReportsApi } from '@dynamia-tools/reports-sdk';

// Create the core client (handles fetch, token/basic auth, cookies)
const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: '...' });

// Construct the ReportsApi using the client's internal http helper
const reports = new ReportsApi(client.http);

// List available reports
const allReports = await reports.list();
console.log(allReports.map(r => `${r.group}/${r.endpoint} → ${r.title ?? r.name}`));

// Run a report by POSTing structured options (see types below)
const runResult = await reports.post('sales', 'monthly', { options: [{ name: 'year', value: '2026' }] });
console.log(runResult);

// Or fetch using query-string params (GET)
const table = await reports.get('sales', 'monthly', { year: 2026, region: 'EMEA' });
console.log(table);
```

Notes:
- Constructing `ReportsApi` with `client.http` is the supported pattern: the `HttpClient` instance takes care of Content-Type negotiation (JSON vs binary), error translation to `DynamiaApiError`, and credentials.
- The `post()` method is the structured execution endpoint (POST body uses `ReportFilters.options` — see types). The `get()` method passes simple query-string parameters.

---

## Direct usage notes

Although `ReportsApi` only requires an object that matches the `HttpClient` interface, the SDK authors intentionally rely on the `DynamiaClient`-provided `http` instance to ensure consistent behavior. If you need to use `ReportsApi` without `DynamiaClient`, you must provide a compatible `HttpClient` (a wrapper around `fetch` that implements `get`, `post`, `put`, `delete` and the same error semantics).

In Node.js you will typically use `node-fetch` (or a global `fetch` polyfill) and construct a `DynamiaClient` from `@dynamia-tools/sdk` rather than reimplementing the HTTP helpers yourself.

---

## Reports API

The `ReportsApi` class mirrors the implementation in `src/api.ts` and exposes the following methods (base path: `/api/reports`):

- `list(): Promise<ReportDTO[]>`
  - GET `/api/reports` — Returns a list of available reports with metadata and filters.
- `get(group: string, endpoint: string, params?: Record<string, string|number|boolean>): Promise<unknown>`
  - GET `/api/reports/{group}/{endpoint}` — Fetch report data by passing query-string parameters.
- `post(group: string, endpoint: string, filters?: ReportFilters): Promise<unknown>`
  - POST `/api/reports/{group}/{endpoint}` — Execute a report using a structured body. Note: the package's types declare `ReportFilters.options: ReportFilterOption[]` (the Java model names this field `options`).

Examples:

- List reports and inspect available filters

```ts
const reportsList = await reports.list();
for (const r of reportsList) {
  console.log(r.group, r.endpoint, r.title ?? r.name);
  if (r.filters?.length) {
	console.log('  filters:', r.filters.map(f => `${f.name}${f.required ? ' (required)' : ''}`));
  }
}
```

- Execute a report using structured options (recommended when the report expects typed inputs)

```ts
// ReportFilters uses `options: { name, value }[]`
const filters = { options: [{ name: 'startDate', value: '2026-01-01' }, { name: 'endDate', value: '2026-01-31' }] };
const result = await reports.post('sales', 'monthly', filters);
// `result` shape depends on the server-side report implementation (JSON table, aggregated object, etc.)
console.log(result);
```

- Fetch a report via GET with simple query params

```ts
const table = await reports.get('sales', 'monthly', { year: 2026, region: 'EMEA' });
console.log(table);
```

Because the SDK's `HttpClient` inspects `Content-Type`, these methods will return JSON when the server replies with `application/json` and will return a `Blob` when the server returns binary content (see next section).

---

## TypeScript types

This package exports a small set of types that mirror the server models. The most important are:

- `ReportDTO` — report descriptor returned by `list()`; contains `name`, `endpoint`, `group`, optional `filters` (array of `ReportFilterDTO`) and human-readable metadata.
- `ReportFilterDTO` — metadata describing a single filter (name, datatype, required, values, format).
- `ReportFilterOption` — a resolved filter option `{ name: string; value: string }` used when submitting a report.
- `ReportFilters` — POST body shape `{ options: ReportFilterOption[] }`.

Refer to the package exports (or your editor's intellisense) for exact types and nullable fields. Example (from `src/types.ts`):

```ts
// POST body
interface ReportFilters { options: { name: string; value: string }[] }

// Report descriptor
interface ReportDTO { name: string; endpoint: string; group?: string; filters?: ReportFilterDTO[] }
```

---

## Handling exports / binary responses

The underlying `HttpClient` normalises responses as follows (see `@dynamia-tools/sdk` implementation):

- If the response `Content-Type` includes `application/json` the client returns the parsed JSON.
- Otherwise the client returns a `Blob` (binary response). In browsers you can turn that into a downloadable file; in Node.js you may need to use `arrayBuffer()` and write the buffer to disk.

Browser example (download PDF):

```ts
const blob = await reports.post('sales', 'monthly-export', { options: [] });
// If the server returned binary data this will be a Blob
if (blob instanceof Blob) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'report.pdf';
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
} else {
  // JSON result (e.g. preview data)
  console.log(blob);
}
```

Node.js note: when running under Node you will typically use a `fetch` implementation that returns a Response whose `blob()` or `arrayBuffer()` is available. If you receive an `ArrayBuffer` or a Node `Buffer` you can write it to disk with `fs.writeFile`.

Example using `arrayBuffer()` (generic):

```ts
const maybeBlob = await reports.post('sales', 'monthly-export', { options: [] });
if ((maybeBlob as any).arrayBuffer) {
  const ab = await (maybeBlob as any).arrayBuffer();
  const buffer = Buffer.from(ab);
  await fs.promises.writeFile('./monthly.pdf', buffer);
} else if (maybeBlob instanceof Buffer) {
  await fs.promises.writeFile('./monthly.pdf', maybeBlob as Buffer);
} else {
  console.log('non-binary response:', maybeBlob);
}
```

---

## Authentication & errors

When you use the `DynamiaClient` the authentication strategies and error handling are handled by the core SDK:

- Provide `token` for bearer auth (recommended), or `username`/`password` for Basic auth, or `withCredentials: true` for cookie-based form login.
- Non-2xx responses are translated to `DynamiaApiError` (from `@dynamia-tools/sdk`). Catch this error to inspect `status`, `url` and `body`.

Example:

```ts
import { DynamiaApiError } from '@dynamia-tools/sdk';

try {
  await reports.post('unknown', 'endpoint', { options: [] });
} catch (err) {
  if (err instanceof DynamiaApiError) {
	console.error(`API error [${err.status}] ${err.message}`, err.body);
  } else {
	throw err;
  }
}
```

---

## Contributing

See the repository-level `CONTRIBUTING.md` for contribution guidelines.

Quick local steps:

1. Clone the monorepo and install: `pnpm install`
2. Work inside `extensions/reports/packages/reports-sdk/`
3. Build and test: `pnpm --filter @dynamia-tools/reports-sdk build` / `pnpm --filter @dynamia-tools/reports-sdk test`

---

## License

[Apache License 2.0](../../../../LICENSE) — © Dynamia Soluciones IT SAS


