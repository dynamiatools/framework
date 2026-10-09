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
- **Caches.** Report lists are cached per account.
- **REST errors.** Errors use the platform `ErrorResult`; unexpected failures return a generic message plus a
  `details.reference` id that is also written to the server log.

# License
Opensource project using Apache 2.0 license




