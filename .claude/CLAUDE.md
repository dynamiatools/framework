# CLAUDE.md — Dynamia Tools (Framework Internal)

These guidelines are for contributing to the **Dynamia Tools framework itself**, not for applications that use the framework.
The focus is on keeping the codebase consistent, maintainable, and well-documented.

## Claude Code / IntelliJ MCP Integration

**If the `idea` MCP server (`mcp__idea__*` tools) is available in this session, it takes priority over generic file/shell/grep exploration for everything below.** Check for it before falling back to Bash/Grep/Read. Only use the generic tools when the MCP server is unavailable, or the target is genuinely outside the IDE's index (e.g. a file outside the project).

- **Finding references** — `mcp__idea__search_symbol` (symbol-aware) instead of grep/ripgrep.
- **Reading symbol info** — `mcp__idea__get_symbol_info` for accurate type/member info instead of guessing from text.
- **Renaming symbols** — `mcp__idea__rename_refactoring` for safe, project-wide renames instead of manual find/replace.
- **Structural search** — `mcp__idea__search_structural` / `mcp__idea__get_structural_patterns` for pattern-based code search.
- **Diagnostics/problems** — `mcp__idea__get_file_problems` / `mcp__idea__get_inspections` instead of manually re-reading files for errors.
- **Navigating files** — `mcp__idea__search_file`, `mcp__idea__list_directory_tree`, `mcp__idea__search_text` / `mcp__idea__search_regex` before falling back to `find`/`ls`/`grep`.
- **Reading files** — `mcp__idea__read_file` so content stays in sync with the IDE's live buffers (unsaved changes included).
- **Editing** — `mcp__idea__apply_patch` (existing files) / `mcp__idea__create_new_file` (new files) so changes go through the IDE and stay in sync with its indices; `mcp__idea__apply_quick_fix` for IDE-suggested fixes.
- **Never excavate `.jar` files** (unzipping, extracting classes, browsing decompiled sources from `~/.m2` or `~/.gradle` caches, etc.) to inspect a dependency's API. IntelliJ already indexes all project dependencies — use `mcp__idea__search_symbol`, `mcp__idea__get_symbol_info`, or `mcp__idea__search_file` to resolve classes/methods from JARs directly through the IDE index.

The IDE's semantic understanding is far more accurate than text-based search — treat grep/ripgrep/manual file scanning as a last resort for symbol-level work in this repo, and only when the MCP server isn't available.

---

## Project Structure

Monorepo: Maven reactor (Java, `tools.dynamia.*`) + pnpm workspace (TS, `@dynamia-tools/*`). `deps` = internal deps only, read from `pom.xml`/`package.json`; a change in a module ripples forward to everything depending on it (transitively). Usage patterns/best practices per module: `dynamia-tools` skill (`.claude/skills/dynamia-tools/SKILL.md`).

### Backend `platform/core/*`

| module | deps | role |
|---|---|---|
| commons | — | shared utils/base classes |
| integration | commons | registry/providers/listeners (`Containers`) |
| io | commons, integration | file/stream I/O |
| actions | commons, integration | `Action`/`ActionExecutionRequest` framework |
| navigation | commons, integration, actions | menu/module/page nav model |
| templates | commons, integration | UI/email/doc templates |
| domain | commons, integration, io | entities, `CrudService`/`Validator` contracts (persistence-agnostic) |
| domain-jpa | domain | JPA impl (`EntityManager`-backed `CrudService`) |
| viewers | commons, integration, io, domain, actions | view/viewer model (form/table/tree/json) |
| reports | domain, io | report generation/export |
| crud | actions, viewers, navigation, domain-jpa | `CrudPage`/`ModuleProvider` orchestration |
| web | commons, integration, navigation, viewers, crud | REST endpoints, top of core stack |

### App/UI `platform/app`, `platform/ui/*`, `platform/starters/*`

| module | deps | role |
|---|---|---|
| app | all of the above | Spring Boot entry point, wires core |
| ui-shared (artifact `tools.dynamia.ui`) | integration, commons, io | UI-backend-agnostic presentation contracts |
| zk | web, navigation, ui-shared, domain, viewers, crud, reports, templates | ZK framework UI impl |
| zk-starter | app, commons, zk, domain-jpa | Spring Boot starter (autoconfig) |

### Frontend `platform/packages/*` (pnpm)

| package | deps | role |
|---|---|---|
| sdk | — | `DynamiaClient` REST client, no framework |
| ui-core | — | framework-agnostic view/viewer core (frontend analogue of `viewers`) |
| vue | sdk, ui-core | Vue 3 adapter/plugin |
| microfrontend-bridge | — | bridges JS bundles ↔ backend `tools.dynamia.zk.ui.MicroFrontend` |
| cli | — | scaffolds new projects |
| mcp | — | MCP server package |

### Other

| dir | contents | note |
|---|---|---|
| `extensions/*` | dashboard, email-sms, entity-files, file-importer, finances, http-functions, reports, saas, security | optional add-ons, deps vary per module — check each `pom.xml` |
| `themes/*` | theme-dynamical (ZK), theme-dynamical-vue | presentation only, no business logic |

`extensions/*`/`themes/*` consume `platform/*`, never the reverse.

---

## Coding Guidelines

- Code must be **clean, modular, and reusable**.
- Avoid duplication by placing shared logic in **commons**.
- Respect module boundaries — do not introduce tight coupling across unrelated modules.
- Follow **Java best practices** with Spring Boot, JPA, and ZK integrations.
- Don't add features, refactor, or introduce abstractions beyond what the task requires. Match the existing module's style.

---

## JavaScript/TypeScript (SDK + Vue) Guidelines

When generating frontend code for Dynamia Platform, prefer the current APIs from:

- `platform/packages/sdk/src/index.ts`
- `platform/packages/sdk/src/client.ts`
- `platform/packages/vue/src/index.ts`
- `platform/packages/vue/src/plugin.ts`

### `@dynamia-tools/sdk`

- Use `new DynamiaClient({ baseUrl, token? })` as the entry point.
- Prefer `baseUrl` as app origin (for example `https://app.example.com`), because SDK endpoints already include `/api/...` internally.
- Use `client.metadata.getNavigation()` for menus/routing (shape: `NavigationTree.navigation`, not `modules/groups/pages`).
- Use `client.crud(path)` for `CrudPage` virtual paths (`findAll`, `findById`, `create`, `update`, `delete`).
- Use `client.crudService(className)` only for class-name based `/crud-service` use cases.
- `findAll()` returns `CrudListResult` with `content`, `total`, `page`, `pageSize`, `totalPages`.
- Handle API failures with `DynamiaApiError` (`status`, `url`, `body`).

### `@dynamia-tools/vue`

- Register the plugin once: `app.use(DynamiaVue)`.
- Use global components provided by the plugin: `DynamiaViewer`, `DynamiaForm`, `DynamiaTable`, `DynamiaCrud`, `DynamiaCrudPage`, `DynamiaNavMenu`, `DynamiaNavBreadcrumb`, etc.
- Prefer composables over manual wiring: `useViewer`, `useView`, `useForm`, `useTable`, `useCrud`, `useCrudPage`, `useEntityPicker`, `useNavigation`.
- For app shells driven by navigation, use `useNavigation(client)` and render by node type.
- For nodes with `node.type === 'CrudPage'`, render with `DynamiaCrudPage` or wire with `useCrudPage`.
- In menu/breadcrumb code use `NavigationNode.internalPath` and `children`.

### Accuracy Rules for Generated Examples

- Do not invent SDK or Vue APIs that are not exported from the package `index.ts` files.
- Keep examples aligned with real return types (for example `CrudListResult`, `NavigationNode`).
- If an API is uncertain, prefer a short TODO comment over guessing a method/signature.

---

## Work Tracking — GitHub Issues

- **GitHub Issues are the source of truth** for work to be done in this repo (improvements, new features, bugs, progress tracking). Do not build a parallel task-tracking system (TODO files, ad-hoc markdown checklists, etc.) when Issues already cover it.
- Given a task: identify the active Issue → read its title, description, labels, comments, and relevant linked issues/PRs → inspect the affected module(s) (see Project Structure above) → clarify important ambiguities with Mario before implementing.
- Keep the implementation traceable to the Issue through the branch name, commits, and/or PR.
- Update the Issue when there's meaningful progress, a decision, a blocker, or a validation result — don't let it go stale.
- Before calling work done, make sure the Issue and the PR both reflect the final state.
- No Issue number given: simple, self-contained task → proceed. Substantial task → search first for an existing Issue (`gh issue list`). If none exists and creating one is clearly appropriate, create it autonomously; if it requires a product/business decision, ask Mario first. Never invent an Issue number.

## Project Knowledge — docs/

- GitHub Issues track work status; `docs/backend/` and `docs/frontend/` store durable project knowledge — architecture, module responsibilities, development patterns, extension SDK, coherence notes.
- Persist to `docs/` when the information is useful beyond the current task. Don't duplicate task status between Issues and `docs/`.
- Prefer updating an existing document (e.g. `docs/backend/ARCHITECTURE.md`, `docs/backend/CORE_MODULES.md`, `docs/frontend/API_CLIENT_STANDARDS.md`) over creating a new one when the knowledge belongs there. Don't document what's directly obvious from the code — keep `docs/` concise and focused on knowledge that's hard to reconstruct from the code alone.

---

## Documentation Guidelines (Javadoc)

- Every class and public method must have **Javadoc in English**.
- Keep comments **descriptive**: explain what the class or method does and why it exists.
- Do **not** alter original code logic when adding documentation.
- Include:
    - **Purpose of the class/method**
    - **Parameters** with `@param`
    - **Return values** with `@return`
    - **Exceptions** with `@throws` if applicable
- Add **examples** using `<pre>{@code ... }</pre>` when usage is clear.

### Example for a Class
```java
/**
 * Provides generic CRUD operations for domain entities.
 * This class acts as a helper to reduce boilerplate code
 * when managing persistent objects.
 *
 * Example:
 * <pre>{@code
 * CrudService service = new CrudService();
 * service.save(new Customer("John Doe"));
 * }</pre>
 */
public class CrudService {
    ...
}
```

### Example for a Method
```java
/**
 * Finds a domain entity by its unique identifier.
 *
 * @param id the unique identifier of the entity
 * @return the entity if found, otherwise null
 *
 * Example:
 * <pre>{@code
 * Customer customer = service.findById(123L);
 * }</pre>
 */
public Customer findById(Long id) {
    ...
}
```

---

## Extra Notes

- Use **descriptive class names** aligned with their module purpose.
- Place reusable constants and helpers in **commons**.
- Use **domain** only for business entities and logic.
- Keep **zk** and **ui** focused on presentation concerns, separate from business rules.
- Ensure all Javadocs compile with automated tools (`mvn javadoc:javadoc`).
