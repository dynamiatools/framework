# Vue theme for DynamiaTools

Vue 3 + Tailwind CSS 4 application template for DynamiaTools, in the same enterprise-shell spirit
as [theme-dynamical](../theme-dynamical) (sidebar navigation, top bar, centered content, footer),
but with the entire UI client-rendered — no ZK, no server-side view rendering.

The UI shell (layout, sidebar, header, user menu, dark mode, login layout) is not written here: this
theme is a **client of [`@dynamia-tools/tailadmin-vue`](https://github.com/dynamiatools/tailadmin-vue-template)**
and uses its components as-is. What lives in this theme is only what's specific to Dynamia — the
navigation tree → menu mapping, `CrudPage`/embed rendering, the flow-step hosts, skins and login.

## Features

- 3 skins available (Blue, Dynamia, Dark), same `ApplicationTemplate`/skin contract as every other theme.
  A skin is the brand color (the package's `brand-*` palette is derived from it); **Dark** is the Blue brand
  plus the package's dark mode, and stays in sync with the header's theme toggle
- Native login page (`POST /login/json`, session + `DYNAMIA_JWT` cookie, no full-page reload)
- Sidebar + header + content + footer shell from `@dynamia-tools/tailadmin-vue` (`AdminLayout`,
  `AppSidebar`, `AppHeader`, `UserMenu`, `PageBreadcrumb`), with collapsible sidebar and dark mode
- Deep links: pages are routed by their navigation path with hash history (`/#/library/books`)
- **CRUD pages** (`NavigationNode.type === "CrudPage"`) render through `<DynamiaCrudPage>` (full
  client-side CRUD, no iframe)
- **Every other page type** (`Page`, `ExternalPage`, `ConfigPage`, …) renders through
  [`<dynamia-embed>`](../../platform/packages/ui-core/src/embed/README.md) — it auto-detects HTML
  vs. JS and sandboxes accordingly, zero framework/build lock-in on the embedded side
- All-in-one Maven + Node build: `mvn package` builds the Vue frontend too (via
  `frontend-maven-plugin`, Node 24, no separate pnpm bootstrap step required)

## Installation

**Maven**
```xml
<dependency>
  <groupId>tools.dynamia.themes</groupId>
  <artifactId>tools.dynamia.themes.dynamical-vue</artifactId>
  <version>26.7.0</version>
</dependency>
```

`application.properties`
```properties
dynamia.app.template=DynamicalVue
dynamia.app.default-skin=Blue
```

or `application.yml`
```yaml
dynamia:
  app:
    template: DynamicalVue
    skin: Blue
```

## Available skins

- Blue (default)
- Dynamia
- Dark

## Architecture

Unlike `theme-dynamical`, this theme does not register any ZK `ViewTypeFactory` renderer — there
is no ZK on its classpath at all (only `tools.dynamia.templates`, `provided` scope). The whole UI
is a static, client-rendered Vue single-page app, wired into the framework purely through existing
classpath conventions (no core code changes):

| What                                    | Where it's served from                                      | Resolved by |
|------------------------------------------|--------------------------------------------------------------|-------------|
| Authenticated app shell (`GET /`)         | `classpath:/views/index.html`                                 | `ClassPathViewResolver` (view name `"index"`) |
| Login page (`GET /login`)                 | `classpath:/views/login.html`                                  | `ClassPathViewResolver` (view name `"login"`) |
| Hashed JS/CSS bundles (`/assets/*.js/css`) | `classpath:/web/templates/dynamicalvue/assets/...`             | `ApplicationTemplateResourceHandler` |

Login uses the framework's existing `POST /login/json` endpoint (JSON in, JSON out, session +
`DYNAMIA_JWT` cookie set on success) — no backend changes needed. Logout is a plain
`POST /logout` (Spring Security's default logout filter).

### CrudPage vs. everything else

`src/layout/ContentArea.vue` is the whole routing decision:

```vue
<DynamiaCrudPage v-if="node.type === 'CrudPage'" :node="node" :client="client" />
<dynamia-embed v-else-if="embedSrc" :src="embedSrc" />
```

`src/lib/resolveEmbedSrc.ts` resolves the embed URL: `ExternalPage`/HTML `Page` nodes have a real
URL in `file`, used as-is. A ZUL-backed `Page` or `ConfigPage` has no browser-servable URL of its
own — it falls back to `/page-embed/{node.path}`, served by the framework's
`PageEmbedController` (`tools.dynamia.web.navigation`, `platform/core/web`): it renders just that
page's ZK content into a bare `embed.zul` workspace (no header/sidebar/footer, independent of
whichever `ApplicationTemplate` is active), so it iframes cleanly instead of nesting a second full
app shell like `/page/{path}` would. This only works when the running app actually has ZK on its
classpath (this theme itself doesn't) — an app with zero ZK views has no fallback and should point
every non-CrudPage `file` at a real HTTP resource instead.

### How it uses `@dynamia-tools/tailadmin-vue`

The package is published as raw, unbuilt source, so the frontend wires four things up (all in
`sources/src/main/frontend`):

| What | Where |
|------|-------|
| `theme.css` (not `style.css`: no Google Fonts request) + `@source` so Tailwind scans the package's `.vue` files | `src/styles/base.css` |
| Its layout components need a `vue-router` instance | `src/router.ts` (one catch-all route; `AppShell.vue` resolves the path to a navigation node) |
| Its layouts reference `/images/logo/*` and `/images/user/*` | `vite.config.ts` → `publicDir` points at the package's own `public/` |
| `vue-tsc` needs to type its `.vue` imports | `src/vue.shims.d.ts` |

`src/lib/navMenu.ts` maps the Dynamia navigation tree onto the package's `MenuGroup` model
(Module → section, PageGroup → submenu, Page → item).

Known limitations of the package (not worked around here): `UserMenu` hardcodes its
"Edit profile / Account settings / Support" entries (they link to a `/profile` route this app doesn't
have), and the header's theme toggler can't be hidden.

## Frontend development

```bash
cd sources/src/main/frontend
pnpm install
pnpm dev       # Vite dev server, proxies /api, /login, /logout to localhost:8080
               # (DYNAMIA_BACKEND=http://localhost:8484 pnpm dev for another backend)
pnpm typecheck
```

The frontend is a `pnpm` workspace member (see root `pnpm-workspace.yaml`) depending on
`@dynamia-tools/sdk` / `@dynamia-tools/ui-core` / `@dynamia-tools/vue` via `workspace:*` — build
those three packages (`pnpm build` at the repo root, or per-package) before building this theme,
same as any other consumer inside this monorepo.

`mvn package` runs the equivalent of `pnpm install && pnpm run build` automatically
(`frontend-maven-plugin`, Node v24.13.1 via corepack) and copies the output onto the classpath —
see `sources/pom.xml` and `sources/src/main/frontend/scripts/copy-shell-views.mjs`.

## License

Theme Dynamical Vue is available under the Apache 2 License.
