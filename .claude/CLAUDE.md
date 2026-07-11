# CLAUDE.md — Dynamia Tools (Framework Internal)

These guidelines are for contributing to the **Dynamia Tools framework itself**, not for applications that use the framework.
The focus is on keeping the codebase consistent, maintainable, and well-documented.

## Claude Code / IntelliJ MCP Integration

- Always prefer the `idea` MCP server tools over generic file/shell exploration when working inside this repo:
  - **Finding references** — use `mcp__idea__search_symbol` / symbol-aware search instead of grep/ripgrep.
  - **Reading symbol info** — use `mcp__idea__get_symbol_info` for accurate type/member info instead of guessing from text.
  - **Renaming symbols** — use `mcp__idea__rename_refactoring` for safe, project-wide renames instead of manual find/replace.
  - **Structural search** — use `mcp__idea__search_structural` / `mcp__idea__get_structural_patterns` for pattern-based code search.
  - **Diagnostics/problems** — use `mcp__idea__get_file_problems` / `mcp__idea__get_inspections` instead of manually re-reading files for errors.
  - **Navigating files** — use `mcp__idea__find_files_by_name_keyword`, `mcp__idea__find_files_by_glob`, `mcp__idea__list_directory_tree` before falling back to `find`/`ls`.
  - **Editing** — use `mcp__idea__replace_text_in_file` / `mcp__idea__apply_patch` so changes go through the IDE and stay in sync with its indices.
- **Never excavate `.jar` files** (unzipping, extracting classes, browsing decompiled sources from `~/.m2` or `~/.gradle` caches, etc.) to inspect a dependency's API. IntelliJ already indexes all project dependencies — use `mcp__idea__search_symbol`, `mcp__idea__get_symbol_info`, or `mcp__idea__find_files_by_name_keyword` to resolve classes/methods from JARs directly through the IDE index. Only fall back to raw shell/grep exploration if the MCP server is unavailable or the target is genuinely outside the IDE's index (e.g. a file outside the project).
- The IDE's semantic understanding is far more accurate than text-based search — treat grep/ripgrep/manual file scanning as a last resort for symbol-level work in this repo.

---

## Project Structure

The framework is organized into modules. Each module has a specific responsibility:

- **actions**
  Handles platform actions, implementing operations users can perform (create, update, delete entities).

- **app**
  Main application module, orchestrating the integration of all other modules and providing the entry point.

- **commons**
  Contains shared utilities and common code used across multiple modules to avoid duplication.

- **crud**
  Provides generic Create, Read, Update, Delete functionalities for entities, simplifying data management.

- **domain**
  Defines core business entities and domain logic, serving as the foundation for other modules.

- **domain-jpa**
  Adds JPA (Java Persistence API) support for domain entities, enabling ORM and database integration.

- **integration**
  Manages integration with external systems and services, handling communication and data exchange.

- **io**
  Responsible for input/output operations, such as file handling and data streams.

- **navigation**
  Implements navigation logic and structures for the application's user interface.

- **reports**
  Generates and manages reports, providing tools for data analysis and export.

- **starter**
  Offers starter templates and configurations to bootstrap new projects or modules.

- **templates**
  Contains reusable templates for UI, emails, or documents.

- **ui**
  Manages user interface components and visual elements.

- **viewers**
  Provides components for viewing and presenting data in various formats.

- **web**
  Exposes web functionalities, including REST endpoints and web resources.

- **zk**
  Integrates ZK framework components for building rich web interfaces.

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
