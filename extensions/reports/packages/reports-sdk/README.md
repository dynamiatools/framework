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

`ReportsApi` has three groups of methods.

### UI API (`/api/reports/v2`)

| method | purpose |
|---|---|
| `catalog()` | Reports the current user can run, grouped |
| `definition(id)` | Filters, declared columns, charts and export formats |
| `filterOptions(id, filter, { q, limit })` | Options of enum, entity, query and static filters |
| `run(id, { filters, page, size, sort, direction })` | Runs a report: `columns`, `rows`, `total`, `truncated`, `durationMs`, `charts` |
| `export(id, format, request)` | Downloads the whole result as a `Blob` (`xlsx`, `csv` or `pdf`) |

```ts
import { DynamiaClient } from '@dynamia-tools/sdk';
import { ReportsApi } from '@dynamia-tools/reports-sdk';

const reports = new ReportsApi(new DynamiaClient({ baseUrl: 'https://app.example.com', token: '...' }).http);

const [group] = await reports.catalog();
const definition = await reports.definition(group.reports[0].id);
const result = await reports.run(definition.report.id, {
  filters: { year: 2026 },
  page: 0,
  size: 25,
  sort: 'TOTAL',
  direction: 'desc',
});
console.log(result.columns.map((c) => c.label), result.rows, result.truncated);
```

Filter values are sent by filter name: text, number, boolean, date `yyyy-MM-dd`, date time `yyyy-MM-dd HH:mm:ss`, time
`HH:mm:ss`, enum name or entity id. A missing required filter, an invalid value or an unknown sort column answers `400`
with the problem in `DynamiaApiError.message`; a report the user cannot access answers `404` or `403`.

### Designer API (`/api/reports/v2/design`)

Only for users with a designer role (`dynamia.reports.designer-roles`), `403` for everyone else.

| method | purpose |
|---|---|
| `designer()` | `{ allowed, previewLimit }` for the current user |
| `preview({ queryLang, queryScript, dataSourceId, parameters })` | First rows of a query being designed |
| `exportDefinition(id)` / `importDefinition(definition)` | Move report definitions between systems as JSON |
| `testDataSource(id)` | Tests the connection of a saved datasource |

### Legacy endpoints (`/api/reports/{group}/{endpoint}`)

`list()`, `get(group, endpoint, params)` and `post(group, endpoint, filters)` for reports published with
`exportEndpoint`. They return `{ data, truncated? }` (`ReportEndpointResult`).

---

## TypeScript types

All types are exported from the package index and mirror the Java records of `tools.dynamia.modules.reports.api.v2`:

- Catalog and definition: `ReportCatalogGroup`, `ReportSummary`, `ReportDefinition`, `ReportColumn`,
  `ReportFilterDefinition` (with `optionsSource`: `NONE`, `STATIC`, `ENUM`, `ENTITY` or `QUERY`), `ReportChartDefinition`.
- Running: `ReportRunRequest`, `ReportRunResult`, `ReportRow`, `ReportChartResult`, `ReportFilterOptionItem`,
  `ReportFilterValue`, `ReportExportFormat`.
- Designer: `ReportDesignerInfo`, `ReportPreviewRequest`, `ReportPreviewResult`, `DataSourceTestResult`.
- Legacy endpoints: `ReportDTO`, `ReportFilterDTO`, `ReportFilterOption`, `ReportFilters`, `ReportEndpointResult`.

Rows contain plain JSON values: dates are ISO text, enums their name and any other object its text.

---

## Exporting files

`export()` returns the file as a `Blob` (the `HttpClient` returns a `Blob` for any response that is not JSON). The
server suggests the name `report-name-yyyy-MM-dd.format`.

```ts
const blob = await reports.export(12, 'xlsx', { filters: { year: 2026 }, sort: 'TOTAL', direction: 'desc' });

// browser
const url = URL.createObjectURL(blob);
const link = Object.assign(document.createElement('a'), { href: url, download: 'sales.xlsx' });
link.click();
URL.revokeObjectURL(url);

// Node.js
await fs.promises.writeFile('./sales.xlsx', Buffer.from(await blob.arrayBuffer()));
```

The export has every row of the result, whatever page you were viewing, up to the server row limit
(`dynamia.reports.max-rows`). `@dynamia-tools/reports-vue` does the download for you.

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


