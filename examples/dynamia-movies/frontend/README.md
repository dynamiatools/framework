# Dynamia Movies — public site

Vue 3 + Vue Router + Tailwind CSS 4. It reads the **automatic REST API** of DynamiaTools through `@dynamia-tools/sdk`
(`client.crud('public/movies').findAll({...})`); there is no custom endpoint. See the [project README](../README.md).

```bash
pnpm install
pnpm dev                  # http://localhost:5173, /api is proxied to http://localhost:8484
pnpm build                # type check + production build of the site
pnpm build:insights       # dashboard + reports module for the backoffice -> ../backend/src/main/resources/static/insights
```

| Path | Contents |
|---|---|
| `src/lib/api.ts` | Typed API layer: search, filters, sorting, relations (`movie.id`, `person.id`) |
| `src/views` | Home, search (Google-like results, facets, suggestions), movie and person pages |
| `insights/` | The Vue app embedded by the backoffice: `@dynamia-tools/dashboard-vue` and `@dynamia-tools/reports-vue` |

Search: a movie has a denormalized `searchText` column, sent as `?searchText=`. The automatic API applies one value per
field, so with several words the longest goes to the server and the rest narrows the result in the browser.

`pnpm-workspace.yaml` redirects the `workspace:*` dependencies of the extension packages to the local folders, so the
example always runs on the SNAPSHOT sources of this repository.
