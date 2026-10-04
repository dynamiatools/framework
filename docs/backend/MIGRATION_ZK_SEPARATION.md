# Migration: ZK separation (first release after 26.10.0)

Part of the effort to remove ZK from Dynamia (epic #130, issues #118–#129 and #143). Two kinds of changes can affect
an application that uses Dynamia Tools. Read both sections; the first one needs code changes, the second one usually
does not but changes **what gets registered** in some deployments.

| | Needs code changes | Who is affected |
|---|---|---|
| [1. Platform class renames](#1-platform-class-renames) | Yes, imports | Code that imports one of 12 classes that used to be in `tools.dynamia.zk` |
| [2. Extensions: `ui` code moved to `core`](#2-extensions-zk-free-code-moved-from-ui-to-core) | No | Applications that depend on an extension's `core` artifact **without** its `ui` artifact |
| [3. Dashboard](#3-dashboard) | No | Applications with dashboard widgets |

---

## 1. Platform class renames

Twelve classes that did not need ZK moved out of `tools.dynamia.zk.*` (module `tools.dynamia.zk`) to the module that
owns the concept. The package changed, so imports must be updated. The simple class names did not change, so Spring
bean names are the same, and no YAML descriptor referenced these classes by name.

| Old (`tools.dynamia.zk...`) | New | Module |
|---|---|---|
| `crud.CrudVDReaderCustomizer` | `tools.dynamia.crud.CrudVDReaderCustomizer` | `tools.dynamia.crud` |
| `crud.AutoExecuteActionCrudStateChangedListener` | `tools.dynamia.crud.AutoExecuteActionCrudStateChangedListener` | `tools.dynamia.crud` |
| `crud.actions.FastCrudAction` | `tools.dynamia.crud.actions.FastCrudAction` | `tools.dynamia.crud` |
| `viewers.table.TableVDReaderCustomizer` | `tools.dynamia.viewers.impl.TableVDReaderCustomizer` | `tools.dynamia.viewers` |
| `viewers.tree.TreeVDReaderCustomizer` | `tools.dynamia.viewers.impl.TreeVDReaderCustomizer` | `tools.dynamia.viewers` |
| `viewers.form.FormViewAction` | `tools.dynamia.viewers.FormViewAction` | `tools.dynamia.viewers` |
| `viewers.form.FormViewModel` | `tools.dynamia.viewers.FormViewModel` | `tools.dynamia.viewers` |
| `viewers.tree.TreeViewNode` | `tools.dynamia.viewers.TreeViewNode` | `tools.dynamia.viewers` |
| `navigation.PageRequest` | `tools.dynamia.navigation.PageRequest` | `tools.dynamia.navigation` |
| `ui.model.ProviderMetadata` | `tools.dynamia.ui.ProviderMetadata` | `tools.dynamia.ui` (ui-shared) |
| `ui.MicroFrontendHostContextProvider` | `tools.dynamia.web.MicroFrontendHostContextProvider` | `tools.dynamia.web` |
| `DefaultApplicationTemplate` | `tools.dynamia.app.DefaultApplicationTemplate` | `tools.dynamia.app` |

Not moved: `ZKException`, `ZKViewResolverException` and `LoadableOnly` (ZK-specific).

### How to migrate

```bash
# run from your project root; review the diff before committing
find . -name '*.java' -not -path '*/target/*' -print0 | xargs -0 sed -i \
  -e 's/tools\.dynamia\.zk\.crud\.CrudVDReaderCustomizer/tools.dynamia.crud.CrudVDReaderCustomizer/g' \
  -e 's/tools\.dynamia\.zk\.crud\.AutoExecuteActionCrudStateChangedListener/tools.dynamia.crud.AutoExecuteActionCrudStateChangedListener/g' \
  -e 's/tools\.dynamia\.zk\.crud\.actions\.FastCrudAction/tools.dynamia.crud.actions.FastCrudAction/g' \
  -e 's/tools\.dynamia\.zk\.viewers\.table\.TableVDReaderCustomizer/tools.dynamia.viewers.impl.TableVDReaderCustomizer/g' \
  -e 's/tools\.dynamia\.zk\.viewers\.tree\.TreeVDReaderCustomizer/tools.dynamia.viewers.impl.TreeVDReaderCustomizer/g' \
  -e 's/tools\.dynamia\.zk\.viewers\.form\.FormViewAction/tools.dynamia.viewers.FormViewAction/g' \
  -e 's/tools\.dynamia\.zk\.viewers\.form\.FormViewModel/tools.dynamia.viewers.FormViewModel/g' \
  -e 's/tools\.dynamia\.zk\.viewers\.tree\.TreeViewNode/tools.dynamia.viewers.TreeViewNode/g' \
  -e 's/tools\.dynamia\.zk\.navigation\.PageRequest/tools.dynamia.navigation.PageRequest/g' \
  -e 's/tools\.dynamia\.zk\.ui\.model\.ProviderMetadata/tools.dynamia.ui.ProviderMetadata/g' \
  -e 's/tools\.dynamia\.zk\.ui\.MicroFrontendHostContextProvider/tools.dynamia.web.MicroFrontendHostContextProvider/g' \
  -e 's/tools\.dynamia\.zk\.DefaultApplicationTemplate/tools.dynamia.app.DefaultApplicationTemplate/g'
```

Two cases the `sed` does not cover: a class in the **same old package** that used one of these names without an import
(for example code in `tools.dynamia.zk.viewers.tree` using `TreeViewNode`) needs an explicit import now, and Kotlin or
Groovy sources need the same replacement. To find leftovers:

```bash
grep -rnE "\b(CrudVDReaderCustomizer|AutoExecuteActionCrudStateChangedListener|FastCrudAction|TableVDReaderCustomizer|TreeVDReaderCustomizer|FormViewAction|FormViewModel|TreeViewNode|PageRequest|ProviderMetadata|MicroFrontendHostContextProvider|DefaultApplicationTemplate)\b" --include=*.java --include=*.kt --include=*.groovy .
```

### Behaviour changes in this section

- **The three descriptor reader customizers now work without ZK.** `CrudVDReaderCustomizer`,
  `TableVDReaderCustomizer` and `TreeVDReaderCustomizer` are beans; before, they were only registered when
  `tools.dynamia.zk` was on the classpath. Applications without ZK now get the YAML `actions`, `controller`,
  `dataSetView`, `parentName`, `formView` (crud) and `frozenColumns`/`actions` (table, tree) settings applied to the
  descriptors they serve over `/api/app/metadata`.
- `TreeVDReaderCustomizer` still has no `@Provider`, as before, so tree `frozenColumns` is still not applied. This
  looks like an existing bug; it was not changed here.
- `DefaultApplicationTemplate` is now in `tools.dynamia.app`, so applications without ZK also get the default template.
- `tools.dynamia.web` is now exported by the `tools.dynamia.web` module (`module-info.java`).
- `AutoExecuteActionCrudStateChangedListener` uses `tools.dynamia.integration.sterotypes.Component` (meta-annotated
  with Spring's `@Component`) instead of Spring's annotation directly, because `tools.dynamia.crud` does not read
  `spring.context`.

---

## 2. Extensions: ZK-free code moved from `ui` to `core`

For `security`, `saas`, `entity-files`, `email-sms`, `http-functions` and `reports`, code that did not need ZK moved
from the `ui` artifact to the `core` artifact. **Package names did not change**, so nothing has to be recompiled or
renamed. The full list per extension is in [EXTENSIONS.md](./EXTENSIONS.md#module-layout-core-and-ui).

### Who is affected

- **Applications that depend on the `ui` artifact (the normal ZK application):** nothing changes. `ui` depends on
  `core`, so the classpath contains the same classes and resources as before.
- **Applications or libraries that depend on `core` only** (for example an API-only deployment, or a library module
  that builds against the extension's services): they now receive things that used to come only with `ui`, and Spring
  registers them through component scanning (`tools.dynamia`). Check this list for each extension you use:

| Extension (`core` artifact) | Newly present in `core`-only applications |
|---|---|
| security (`tools.dynamia.modules.security`) | `SecurityModuleProvider` (Users, Profiles and Access Tokens pages in the `system` module), `UserNavigationRestriction`, `UserActionRestriction`, `UserInterfaceController` (`uiController`, session scope), `LoginMvcController` (`GET /login`), `SetupApplicationUserInfoListener`, `StaticResourcesIgnoringAntMatcher`, table descriptors |
| saas (`tools.dynamia.modules.saas`) | `SaasModuleProvider` (the `saas` module with its CRUD pages), `AccountNavigationRestriction`, `HelpSystemNavigationRestriction`, `EntityFilesConfigNavigationRestriction`, **`HttpAccountResolver`** (an `AccountResolver` bean), account actions, 31 descriptors |
| entity-files (`tools.dynamia.modules.entityfiles`) | `EntityFilesModuleProvider` (the *Entity Files* config page), `ClearEntityFileCacheAction`, 4 descriptors |
| email-sms (`tools.dynamia.modules.email`) | `EmailInstaller` (the *Email* pages), **`EmailMessageListener`** (consumes the email message channel and sends mail), `SetPreferredAccountAction`, 14 descriptors. `core` now also depends on `tools.dynamia.crud` |
| http-functions (`tools.dynamia.modules.functions.core`) | `DynamiaHttpFunctionsModuleProvider` (Http Functions page in the `saas` module), 5 descriptors |
| reports (`tools.dynamia.modules.reports.core`) | 12 view descriptors |

The two to review first are `HttpAccountResolver` (a second `AccountResolver` bean can make injection by type
ambiguous if your application defines its own) and `EmailMessageListener` (a second consumer of the email channel).
If an application must not get a piece, define its own bean or exclude it from component scanning, or keep depending
on the `ui` artifact only where you want everything.

Other points:

- **Descriptors that name a ZK class stay in `ui`** (`controller:`, `customizer:`, `customView:` zul). The YAML reader
  turns `customizer` into a `Class` while loading, so they cannot live in a module without ZK. In an application
  without ZK, entities whose *form* or *crud* descriptor stays in `ui` fall back to auto-generated fields.
- **`My Profile`** (`system/security/myProfile`, a zul page) is now registered by `SecurityProfileModuleProvider` in
  `security.ui`. It is merged into the same module and group as before and keeps its position (first in the group).
- **Descriptor precedence:** if your application defines a descriptor with the same id as one that moved, which one
  wins depends on classpath order. Descriptors that were in `ui` now come from the `core` jar.
- **`saas-sdk`** is server-side only; migration, stats and parameter endpoints intentionally have no SDK.

### Bug fix that changes responses

`AccountApiController` (`/api/saas/account/{uuid}`, #129): it no longer fails with a 500 in these cases.

- The request has no subdomain or no account matches it: `401` (before: NullPointerException).
- The account requires an instance uuid and the request has no `uuid` parameter: `400` (before: NullPointerException).
- No system account is configured: the request is not treated as authorized (before: NullPointerException).

---

## 3. Dashboard

The extension is now two artifacts:

- `tools.dynamia.modules.dashboard.core`: UI-agnostic API (no ZK) and the REST endpoint.
- `tools.dynamia.modules.dashboard` (the same coordinates as before): the ZK implementation. It depends on `core`, so
  **existing applications keep the same dependency and need no change**.

Source compatibility: `DashboardWidget`, `AbstractDashboardWidget`, `DashboardContext`, `InstallDashboardWidget`,
`UserInfoProvider`, `DashboardAction`, `ChartjsDashboardWidget`, `ViewerDashboardWidget`, `ZulDashboardWidget` keep
their names and packages. `DashboardContext` now extends the new `WidgetContext` and `DashboardWidget` extends
`DashboardWidgetDefinition`; every method that existed is still there. `DashboardWidget.init(WidgetContext)` is a
default method that calls `init(DashboardContext)`.

New: `GET /api/dashboard/{descriptorId}/widgets/{field}` serves widget data to JS frontends. When a ZK widget is served
that way it receives a **headless** `DashboardContext` (no dashboard, no widget window), so code that calls
`getDashboard()` or `getWindow()` inside `init` must not be exposed through this endpoint.

---

## Assessment of dynamia-erp (26.9.0)

Checked by reading its sources (no build):

- **Renames (section 1):** `dynamia-erp` imports 447 files from `tools.dynamia.zk.*`, but **none of the 12 renamed
  classes**. No code change is needed for them.
- **Dashboard (section 3):** it uses `InstallDashboardWidget`, `DashboardContext`, `AbstractDashboardWidget`,
  `ViewerDashboardWidget`, `ChartjsDashboardWidget`, `ZulDashboardWidget` and `UserInfoProvider`; all keep their names
  and packages and are reachable through the unchanged `tools.dynamia.modules.dashboard` dependency.
- **Extensions (section 2):** many of its library modules depend on `saas`, `entityfiles`, `email` or `reports.core`
  without the matching `ui` artifact, but the application assembly (`sources/erp/erp-boot`, `erp-ui`) also includes
  `saas.ui`, `email.ui`, `entityfiles.ui` and `reports.ui`, so the final classpath is the same as before. It uses
  its own `HttpAccountResolver` (defined as a `@Bean`) and its own security module (`modulo-seguridad`), not
  `tools.dynamia.modules.security`.
- **To verify when upgrading:** any ERP build or image that assembles only `core` artifacts (an API-only variant, if
  one is built from different modules than `erp-boot`) and the `HttpAccountResolver` / `EmailMessageListener` points
  above.

## Upgrade checklist

1. Apply the renames in section 1 and compile.
2. For each extension you use, check whether the application has `core` without `ui` and read the section 2 table.
3. Start the application and check navigation (pages in `system`/`saas`), `GET /login` and the email channel.
4. If you serve dashboards to JS, see the Dashboard section of [EXTENSIONS.md](./EXTENSIONS.md).
