[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia.modules/tools.dynamia.modules.reports.core)](https://search.maven.org/search?q=tools.dynamia.modules.reports)
![Java Version Required](https://img.shields.io/badge/java-25-blue)


# DynamiaReports

Is a small reporting framework built using DynamiaTools for generate reports in app storing queries metadata in database and allowing users to run, edit, copy the reports. 

## Initial Features
- Create Queries using JPSQL or native SQL
- Create or use current database datasource
- Use filters
- Show reports in screen
- Exports reports to CVS, Excel and PDF
- Send reports to email
- View Reports 
- Embed reports in other apps
- Integrate as a module with any DynamiaTools modules
- View reports data as charts


## Architecture
Modules organization

### Core Module
Main module with all reporting logic, allowing to run, store, filter and generate report data. 

### UI Module
- View report data in table view
- Edit reports
- Show report filters
- Export to CSV, Excel and PDF actions
- Custom report actions for extensions

### Datasources Modules
Additional module for  create and connect to external datasources. Mainly SQL databases, later NoSQL and plain files

### Boot module
A spring boot application to run DynamiaReports as a standalone app.

## Front end

The navigation pages of the module follow `dynamia.reports.ui`:

| value | behaviour |
|---|---|
| `vue` (default) | The viewer page is a `ReportViewerPage` (type `ReportViewerPage` in the navigation API). A Vue shell renders it natively; the ZK shell still opens its legacy view. |
| `zk` | Legacy behaviour: a plain page that renders the ZK viewer. A Vue shell embeds it with its ZK bridge. |

The `core` module has no ZK dependency and contains the whole backend (domain, services, REST API, exports,
`DynamiaReportsModule`). The `ui` module is the legacy ZK front end (`ReportViewer`, `ReportPage`, actions); existing
applications keep depending on it unchanged. The design pages (groups, reports, datasources) are CRUD pages and work
in both shells.

## REST API for UIs

`/api/reports/v2` serves any front end. The caller must be authenticated; only active reports of the current account
(and the system account) that pass the access policies are visible, anything else answers `404`/`403`. Errors use the
platform `ErrorResult`.

| endpoint | purpose |
|---|---|
| `GET /api/reports/v2/catalog` | Groups with their reports |
| `GET /api/reports/v2/{id}` | Definition: summary, filters (type, required, options source), declared columns, charts, export formats |
| `GET /api/reports/v2/{id}/filters/{filter}/options?q=&limit=` | Options of enum, entity (only entities published by an `EntityFilterProvider`), query and static filters |
| `POST /api/reports/v2/{id}/run` | Body `{filters, page, size, sort, direction}`. Returns `{columns, rows, total, page, size, truncated, durationMs, charts}` |
| `POST /api/reports/v2/{id}/export?format=xlsx\|csv\|pdf` | Same body; downloads the file. `X-Report-Truncated: true` when the row limit was reached |

Filter values are sent by filter name: text, number, boolean, date `yyyy-MM-dd`, date time `yyyy-MM-dd HH:mm:ss`,
time `HH:mm:ss`, enum name or entity id. Values in rows are plain JSON (dates as ISO text, enums by name, other objects
as text). Chart data follows the Chart.js structure (`labels` and `datasets`). CSV cells that start with `=`, `+`, `-`
or `@` are prefixed with `'` so spreadsheets do not run them.

The older `/api/reports/{group}/{endpoint}` endpoints keep working for existing integrations.

## Security and limits

Reports run user-defined queries, so execution is restricted:

- **Read-only queries.** A report query must be a single `SELECT` / `WITH` statement (JPQL: `select` / `from`). Data
  modification, DDL, `;` separators and executable comments are rejected when the report is saved and again on
  execution. SQL runs on a read-only connection.
- **Limits.** `dynamia.reports.max-rows` (default `100000`) and `dynamia.reports.query-timeout` (seconds, default `60`).
  When the row limit is reached the result is flagged as truncated (`"truncated": true` in the REST response).
- **Filters.** Filter values are always bound as named parameters. Filter conditions use `:name` placeholders and are
  inserted at the `<FILTERS>` marker, or before the first top level `GROUP BY` / `ORDER BY` / `LIMIT` clause.
- **Datasources.** External datasources use read-only connection pools. Allowed JDBC drivers default to MySQL, MariaDB,
  PostgreSQL, Oracle and SQL Server; replace the list with `dynamia.reports.allowed-drivers` (comma separated class
  names). JDBC URLs with options that load code or remote content are rejected.
- **Passwords.** Set `dynamia.reports.encryption-key` to store datasource passwords encrypted (AES-256-GCM). Passwords
  saved before the key was set are still readable and are encrypted the next time they are saved. The key must not
  change afterwards. The password is never serialized to JSON.
- **Authorization.** `Report.accessRoles` (comma separated roles, empty = any user) restricts who sees and runs a
  report in the catalog, the REST API and the viewer. The default policy checks `HttpServletRequest.isUserInRole`;
  register a `ReportAccessPolicy` bean to apply your own rules (all policies must allow). A report with roles is denied
  when no request is available to evaluate them.
- **Front end.** `dynamia.reports.ui` selects `vue` (default) or `zk`, see above.
- **Caches.** Report lists are cached per account.
- **REST errors.** Errors use the platform `ErrorResult`; unexpected failures return a generic message plus a
  `details.reference` id that is also written to the server log.

# License
Opensource project using Apache 2.0 license




