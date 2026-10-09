# Dynamia Movies

A movie catalog in the spirit of IMDb, built to try **everything new in DynamiaTools** in one project that was
generated with the CLI (`dynamia new`, backend Java + frontend Vue) and runs against the **SNAPSHOT** of this
repository (`26.10.1-SNAPSHOT`).

| Folder | What it is |
|---|---|
| [`backend/`](backend) | Spring Boot 4 + DynamiaTools. It is the **backoffice** (the [Dynamical Vue theme](../../themes/theme-dynamical-vue): login, CRUD pages, dashboard, reports) and the **REST API** the public site reads. |
| [`frontend/`](frontend) | The **public site** (Vue 3 + Tailwind): a small "Google for movies" that only uses the automatic REST API of DynamiaTools. It also builds the dashboard/reports module that the backoffice embeds. |

## What it shows

- **Related entities** — `Movie` ⇄ `Genre` (N:M), `Studio` (N:1), `Credit` (movie ⇄ person with a role), `Person`, `Review`.
- **CLI generated** apps, wired to the SNAPSHOT backend and the SNAPSHOT npm packages of the monorepo.
- **Dynamical Vue theme** as backoffice: view descriptors (`META-INF/descriptors/*.yml`) drive the CRUD pages.
- **Automatic REST API** (`CrudPage` → `/api/{module}/{page}`): filters by field (`?year=1999`, `?genresText=Drama`,
  `?movie.id=7`), sorting (`_sort`, `_order`), paging and `json` view descriptors. A hidden `public` module publishes
  read-only copies under `/api/public/**` (open to anonymous users), see `PublicApiModuleProvider`.
- **Reports** (DynamiaReports) — 10 reports stored as data (SQL and JPQL, filters, charts), REST API `/api/reports/v2`,
  the `report` dashboard widget and `@dynamia-tools/reports-vue` (list, viewer, filters, export).
- **Dashboard** (DynamiaDashboard) — `MoviesDashboard.yml` + `@dynamia-tools/dashboard-vue`, with a custom Java widget
  (`LatestReviewsWidget`) and its own Vue renderer.
- **Security** — users/profiles with JWT login; the public API is the only open part.
- Virtual-thread ready, `@InitializeOnLoad`, `CrudServiceListener`, and a demo catalog of **123 movies from 1939 to 2024**
  (about 380 people, 18 genres, 43 studios) loaded on startup from `backend/src/main/resources/data/movies.csv`.

> The ratings, votes and box-office figures are approximate sample data. There are no poster images; the site
> generates a poster from the title (set `posterUrl` in the backoffice to use a real one).

## Run it

Requirements: JDK 25, Node 24, pnpm. Build the monorepo SNAPSHOT first when you changed anything in it
(`mvn install -DskipTests` and `pnpm -r build` at the repository root); the example uses what is in your local Maven
repository and the packages' `dist/` folders.

```bash
# 1. Backend (backoffice + API) -> http://localhost:8484
cd backend
./mvnw spring-boot:run          # or: mvn spring-boot:run

# 2. Public site -> http://localhost:5173
cd frontend
pnpm install
pnpm dev

# 3. Dashboard + reports of the backoffice (once, and after changing frontend/insights)
pnpm build:insights             # writes backend/src/main/resources/static/insights/ ; restart the backend
```

- Backoffice: <http://localhost:8484> — user `admin`, password `adminadmin` (created by the security module).
- Public site: <http://localhost:5173> — try `nolan dicaprio`, `1999`, `animation pixar` or the genre / decade filters.
- OpenAPI: <http://localhost:8484/swagger-ui.html>

The database is HSQLDB in memory: every start loads the catalog again.

## How the pieces connect

```
 browser ── public site (Vue, :5173) ──/api/public/**──┐
                                                       ├── Spring Boot (:8484) ── HSQLDB
 browser ── backoffice (Dynamical Vue theme) ──/api/**─┘      │
            ├─ CRUD pages (view descriptors)                  ├─ reports.core  (/api/reports/v2)
            └─ Dashboard / Reports ← <dynamia-embed> imports  └─ dashboard.core (/api/dashboard)
               /page-embed/insights/* (a JS module that mounts frontend/insights)
```

## Known limits

- The Reports and Dashboard panels use their own dark `insights.css` inside the light shell.
- The backoffice menu lists only `New`, `Edit` and `Delete` for a CRUD: the framework publishes to REST clients only the
  actions written as remote or headless (`Save`, `Delete`). See
  [UI ports for actions](../../docs/design/UI_PORTS_FOR_ACTIONS.md).
- A refresh depends on the `JSESSIONID`, see [Web security & sessions](../../docs/backend/WEB_SECURITY_SESSIONS.md).

## Notes

- The backoffice uses `@EnableDynamiaTools` (the CLI backend template ships `@EnableDynamiaToolsApi`, which is API only and
  has no MVC, theme resources or login page). Change it when you add a themed backoffice to a generated project.
- Dashboard, reports and the reports designer are `ExternalPage`s (`InsightsModuleProvider`) pointing at a JavaScript
  module, `/insights/insights.js?page=dashboard|reports|design`. `<dynamia-embed>` imports it and mounts its custom element in the
  backoffice page itself, which is why the login cookies reach `/api` (a plain HTML embed runs in a sandboxed frame).
- *Reports design* mounts `DynamiaReportDesigner` (`@dynamia-tools/reports-vue`): a *Reports* tab with the platform CRUD
  of the report definitions and a *Tools* tab (query preview, import/export, datasource test). The CRUD needs a visible
  `CrudPage` (`insights/definitions`, "Report definitions" in the menu) because the server resolves the entity of a
  CrudPage from the navigation tree, which leaves out invisible pages.
- The hidden `public` module is what exposes `/api/public/**`; its pages are `alwaysAllowed` so anonymous visitors skip the
  navigation restrictions, and `PublicApiReadOnlyCustomizer` removes every write verb.
- Single tenant: the reports are created with `accountId = 0`, the root tenant (the `SaaS` entities require it).
- Do not add `spring-boot-devtools`: its restart class loader makes the bean classes of the view descriptors differ from the
  entities' classes.
- Building this example found several problems in the framework (themed shell without `Content-Type`, `NavigationNode`
  without the address of external pages, descriptors that did not load for anonymous REST reads, a ZK-only scope in the
  security module, `PUT` wiping many-to-many collections, `Module.visible(false)` ignored, no tenant resolver under Spring
  Boot); they are fixed in the framework and this example needs no workaround.
