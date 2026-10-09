# Dynamia UI: one UI contract, any front end

**Status:** design, first iteration in progress on `feature/next-ui`. Branch `next`.

> **Scope of the first iteration: `dynamia-tools` only, `dynamia-erp` untouched.** The goal is that ZK lives in its own
> corner of the repository (`platform/ui/zk`, `zk-starter`, `theme-dynamical`, the `ZK_ONLY` extension `ui` modules and
> `dashboard/zk`) and everything else is ZK-free, enforced by a test. Everything is additive: no class is removed or
> renamed, the ERP must keep compiling against it. In scope: facades + SPIs + replay (§2b, §3.2), the `runtime`
> classification (§5), converting the actions of tools (inventory), the ZK-corner rule. **Deferred:** shared view models
> (§3.3), the generated Java-to-TS contract (§3.1), a Vue-side rewrite, and Phase 5 (ERP).
**Scope:** `platform/ui/ui-shared`, `platform/core/{actions,crud,viewers,navigation}`, `platform/packages/{sdk,ui-core,vue}`,
and, as the first big consumer, `dynamia-erp`.
**Inventory:** [dynamia-ui-inventory.md](dynamia-ui-inventory.md) (every action of tools and the ERP, classified).
**Builds on:** [HEADLESS_ACTIONS.md](../design/HEADLESS_ACTIONS.md),
[SERVER_DRIVEN_ACTION_FLOWS.md](../design/SERVER_DRIVEN_ACTION_FLOWS.md),
[UI_PORTS_FOR_ACTIONS.md](../design/UI_PORTS_FOR_ACTIONS.md) (this document generalises it),
[MIGRATION_ZK_SEPARATION.md](../backend/MIGRATION_ZK_SEPARATION.md) (epic #130).

---

## 1. Goal

Reach the point where **no feature of Dynamia Tools, Dynamia ERP or their extensions needs ZK to be used from a Vue front
end**, while ZK keeps working as it does today. ZK becomes one front end among several, not the definition of the UI.

The way to get there is not "port every ZK class to Vue". It is to give the framework a **UI-neutral vocabulary** that
both languages share, so that an action, a view or a navigation entry is written once and each front end only
implements the vocabulary:

> **Dynamia UI** = descriptors + intents + view models + a wire protocol, defined once, implemented in Java and in
> TypeScript, rendered by adapters (ZK, Vue, anything else).

### Non-goals

- Not a component library and not a design system. Dynamia UI never says "a `Window` with a `Listbox`"; it says "ask
  the user to choose one of these options". Rendering stays in the adapter.
- Not a replacement for ZK. ZK keeps its adapter and every ZK-only feature stays available there (§8).
- Not server-side rendering of components. The server sends data and intents, never markup.
- Not a new form-description language. `ViewDescriptor` (YAML/JSON) stays the way to describe forms, tables and trees.

---

## 2. Where we are

What already exists and is the base of this design:

| Piece | Where | State |
|---|---|---|
| UI-neutral presentation contracts (`UIMessages`, `MessageDisplayer`, `UIToolsProvider`, icons) | `ui-shared` (`tools.dynamia.ui`) | Exists. `UIMessages` is a real port; `UIToolsProvider` is not (takes a ZK component as `content`) |
| Descriptors (`ViewDescriptor`, fields, groups, JSON serializer) | `core/viewers` | Exists, already consumed by Vue (`DynamiaForm`, `Table`, `Tree`) |
| Action metadata + REST execution (`RemoteAction`, `ActionExecutionRequest/Response`) | `core/actions`, `app` | Exists |
| Headless actions (replay): a `LocalAction` written once against ports runs without ZK | `core/actions` (`ReplayExecutor`...), `core/crud` | Done for `SaveAction`, `DeleteAction` |
| Flow protocol: `ActionFlowStep` (`CONFIRM`, `INPUT`, `DIALOG`, `NOTIFY`, `REDIRECT`, `CALL`, `DONE`, `CUSTOM`), signed `resumeToken` | `core/actions`, `sdk`, `vue` (`runActionFlow`) | Implemented, experimental |
| Client side actions (`ClientAction`, registries, renderers, feedback managers) | `ui-core`, `vue` | Exists |
| Core is ZK-free | `platform/core/*` | Verified: no `org.zkoss` import in `core` |

What is missing:

1. **Ports for most interactions.** Replay only works for what `UIMessages` can express (confirm, message, question).
   Forms, downloads, uploads, choices, progress, navigation have no port. This is the 20 `CrudAction`s that import ZK in
   the framework (see UI_PORTS_FOR_ACTIONS.md) and, in the ERP, roughly a quarter of the `@InstallAction` classes
   (93 of 355 files with `@InstallAction` import ZK; measured with grep on `next`, 2026-10-09, the real number of
   *actions* is lower because some files hold helpers).
2. **A contract that is checked, not just agreed.** Java and TS types are written by hand on both sides
   (`ActionFlowStep` in Java, `ActionFlowStep` in the SDK). Drift is a matter of time.
3. **A client-side way to write actions and view models.** `ClientAction` exists, but there is no shared notion of a view
   model (state + commands) between `CrudController` (Java) and `useCrud`/`CrudView` (TS), so each is a re-implementation.
4. **A classification of every action** (runs headless / needs a flow / is client side / is ZK only) that is explicit and
   tested, instead of "it is invisible to REST because it does not implement a marker".
5. **In the ERP:** actions and views that call ZK directly (`Window`, `Messagebox`, `Filedownload`, `Executions`) instead
   of ports, and ERP-specific UI pieces (dashboards, wizards) with no descriptor at all.

---

## 2b. Core principle: actions stay as they are, the facades react to the environment

**Existing actions of Dynamia Tools and Dynamia ERP are not rewritten.** What changes is *what they call*: the few
ZK-specific calls (a message box, a window with a viewer, a download) are replaced, one by one, by a **Dynamia facade**
with the same shape, and the facade finds its implementation from the environment. `SaveAction` already works this way
and is the reference:

```java
// SaveAction (unchanged by this design): no ZK, no REST, only tools-level APIs
crud.getController().onSave(afterSave);     // asks "save?" through UIMessages.showQuestion(...)
```

```
UIMessages.showQuestion(...)            static facade, in ui-shared
        │
        ▼  MessageDisplayer chosen by the environment
        ├── ZK running normally ........ ZK bean (MessageDialog)            -> real dialog, waits for the click
        ├── REST request (headless) .... ReplayInteractions, bound by      -> answered from the token, or stops
        │                                ReplayExecutor with
        │                                UIMessages.withDisplayer(...)       and sends a CONFIRM step to Vue
        └── tests ...................... any stub
```

Three pieces make it work, and they are the template for everything new:

1. **A facade with a stable API** (`UIMessages`) that actions import. It lives in `ui-shared`, has no UI dependency.
2. **An SPI** (`MessageDisplayer`) with one implementation per environment. The facade resolves it from a scoped
   value first (set per execution by the headless runtime) and from the container otherwise (ZK registers its own).
3. **A runtime that binds the right implementation for the execution** (`ReplayExecutor`). The action never knows.

So the work is a list of facades, not a new action model:

| ZK-specific thing used by actions today | Facade (tools level, in `ui-shared`) | ZK implementation | Headless / Vue implementation |
|---|---|---|---|
| `Messagebox`, `MessageDialog` | `UIMessages` (exists) | exists | `ReplayInteractions` (exists) |
| `Window` + `Viewer`/form with fields | **`UIViews.showForm / showView`** (new) | window + `Viewer` | `DIALOG` step with the `ViewDescriptor` name; the answer is the submitted values |
| `Filedownload` | **`UIFiles.download`** (new) | `Filedownload` | `DOWNLOAD` step (signed URL) |
| `Fileupload` | **`UIFiles.upload`** (new) | `Fileupload` | `UPLOAD` step |
| listbox / combo in a window | **`UIChoices.choose`** (new) | window | `CHOICE` step |
| `LongOperation` | **`UIProgress.run`** (new) | `LongOperation` | progress step with polling |
| `Executions.sendRedirect`, page switch | **`UINavigation.open`** (new) | ZK navigation | `REDIRECT` step |
| `CrudViewComponent` (what `evt.getCrudView()` returns) | exists as an interface in `crud` | ZK view | `HeadlessCrudView` (exists) |

They may live in one class or several; what matters is the pattern. Names are provisional.

**"A viewer that does not depend on ZK".** `UIViews.showForm(descriptorName, values, onSubmit)` takes a view
*descriptor name* and plain values, never a `Viewer` or a `Window`. The ZK implementation builds the usual `Viewer`
inside a window; the headless one describes it as a step and Vue renders `DynamiaForm`. An action that today builds
`new Viewer(...)` inside a `Window` changes those lines, not its logic.

**Migration rule per action:** replace the ZK lines by the facade; if the action then has no `org.zkoss` import, mark it
`HeadlessCapable` after reviewing its restrictions. Nothing else in the action changes. Actions that stay on ZK keep
working exactly as now, because ZK's implementation of each facade is what they would have done anyway.

The callback style (`showQuestion(text, () -> ...)`) is kept on purpose: it is what existing actions are written with,
and replay already makes it work in REST by re-running the action. A facade method may also offer a "returning" form
for new code, but is never required.

---

## 3. The model: four layers

```
                    ┌────────────────────────────────────────────────┐
  Business code ───►│ 4. Actions  (Java LocalAction / TS ClientAction) │  written once
                    ├────────────────────────────────────────────────┤
                    │ 3. View models  (state + commands, no widgets)   │  spec shared, impl in Java and TS
                    ├────────────────────────────────────────────────┤
                    │ 2. Intents  (ports: ask, show, choose, download) │  spec shared, impl in Java and TS
                    ├────────────────────────────────────────────────┤
                    │ 1. Descriptors + protocol (JSON, versioned)      │  single source of truth
                    └───────────────┬────────────────────────────────┘
                                    │ adapters
                       ┌────────────┼─────────────┐
                       ▼            ▼             ▼
                      ZK          Vue         anything else
```

### 3.1 Layer 1: descriptors and protocol (the contract)

Everything that crosses a boundary is plain data with a JSON Schema:

- `ViewDescriptor`, `Field`, `FieldGroup` (exist).
- `ActionMetadata` (exists) **plus** a `runtime` classification (§5).
- `NavigationMetadata` (exists).
- `Intent` and `IntentResult` (new, §3.2). `ActionFlowStep` becomes the serialised form of an `Intent`.
- `ViewModelState` (new, §3.3).

**Single source of truth.** The Java types are the origin; a build step generates the JSON Schema and the TS types in
`@dynamia-tools/sdk` from them (or the reverse; see open question Q1). A conformance suite of JSON fixtures
(`contract/fixtures/*.json`) is replayed by both the Java and the TS test suites: same fixture, same parsed object, same
re-serialised JSON. This is what makes "implemented in two languages" safe.

Versioning: the contract carries `uiProtocolVersion`; adapters declare which they support. Additive changes only inside
a major version.

### 3.2 Layer 2: intents (the ports)

An **intent** is "what the program needs from the user or the environment", described as data, with a typed answer.
This is the generalisation of `UIMessages` and of the flow steps.

| Intent | Answer | ZK adapter | Vue adapter | Today's flow step |
|---|---|---|---|---|
| `confirm(message)` | `boolean` | `Messagebox` | confirm dialog | `CONFIRM` |
| `input(prompt, type)` | value | prompt window | prompt dialog | `INPUT` |
| `notify(message, level)` | none | `Clients.showNotification` | toast | `NOTIFY` |
| `form(viewDescriptor, values)` | values (or cancel) | window + `Viewer` | `DynamiaForm` dialog | `DIALOG` |
| `view(viewDescriptor, data)` | none | window + `Viewer` | `DynamiaViewer` dialog | `DIALOG` (read only) |
| `choose(options, multi)` | selection | listbox window | select / list dialog | new |
| `upload(accept)` | file reference | `Fileupload` | file picker | new |
| `download(name, mime, ref)` | none | `Filedownload` | signed URL / browser download | new |
| `progress(operation)` | none (completes) | `LongOperation` | progress + polling | new |
| `navigate(target)` | none | `Executions` / page switch | router | `REDIRECT` |
| `refresh(scope)` | none | reload view | reload view | new |
| `callAction(id, request)` | response | run action | run action | `CALL` |
| `custom(component, data)` | any | registered component | registered renderer | `CUSTOM` |

Rules:

1. **An intent describes, never contains, a component.** No `Object content`. This is why `UIToolsProvider.showDialog`
   can not be the port: it can not cross a network or a language.
2. **Intents are answerable by any runtime.** In Java they are the facades of §2b (`UIMessages`, `UIViews`, `UIFiles`...)
   in `ui-shared`, backed by one SPI each; in TS they are the same-named functions in `ui-core`. A single `Intent` data
   type underneath is what `ReplayInteractions` records and serialises as an `ActionFlowStep`.
3. **Two execution modes, one action.**
   - *Direct* (ZK, or a TS action in the browser): the adapter shows the UI and the call returns/continues with the
     answer.
   - *Replay* (a Java action reached from a REST client): the existing `ReplayInteractions` answers from the token or
     stops at the first unanswered intent, which goes to the client as an `ActionFlowStep`. This is exactly what
     headless actions do now; the change is that it works for every intent, not only confirm and message.
4. **Replay constraints stay** (HEADLESS_ACTIONS.md): same request + same answers must yield the same intents in the same
   order; every pass runs in a transaction that only the last pass commits. `progress` breaks determinism, so it is
   only allowed in a `FlowRemoteAction` or as the last intent of an action.
5. `UIMessages` and `UIToolsProvider` stay as they are (many ZK actions use them). `UIToolsProvider` is not touched; new
   facades are separate, and its users are migrated action by action only when someone needs that action in Vue.

### 3.3 Layer 3: view models (deferred)

A view model is **state and commands of a screen, with no widgets**. Dynamia already has two incompatible
implementations of the same idea: `CrudController` / `CrudState` (Java, drives ZK) and `useCrud` / `CrudView` /
`crudActionState.ts` (TS, drives Vue). Both encode the same state machine (`READ`, `CREATE`, `UPDATE`, `DELETE`),
selection, filters, paging and "which actions apply in this state".

Proposal: specify the view models once (state shape, transitions, events), implement them in both languages, and make
the adapters bind widgets to them.

| View model | State | Commands | Replaces |
|---|---|---|---|
| `CrudViewModel` | state, selected, filters, paging, form values, errors | `create`, `edit(id)`, `save`, `delete`, `cancel`, `query`, `applyFilter` | `CrudController`, `useCrud` |
| `FormViewModel` | values, errors, dirty, readonly fields | `setValue`, `validate`, `submit` | `FormViewModel` (exists, Java), `FormView` (TS) |
| `TableViewModel` | columns, rows, sort, selection, page | `sort`, `select`, `page` | `TableView` |
| `TreeViewModel` | nodes, expanded, selected | `expand`, `select` | `TreeView` |
| `NavigationViewModel` | current module/page, breadcrumb | `open(page)` | `NavigationResolver` |

Two consequences:

- A **headless action's** `CrudControllerAPI` (what `SaveAction` calls today) is the Java face of `CrudViewModel`. The
  extraction already planned in HEADLESS_ACTIONS.md ("`HeadlessCrudController` copies the save/delete flow of
  `zk.crud.CrudController`") is the first concrete step: one implementation, two consumers.
- A **TS action** receives the same `CrudViewModel` API, so a `ClientAction` can do what a Java action does.

The view model *state* is serialisable (`ViewModelState` in layer 1) so a server-side model and a client-side one can
exchange it when needed (e.g. an embedded ZK view inside Vue, see `ZkEmbed.vue`).

### 3.4 Layer 4: actions

Actions are **not** a new API. They are the current `LocalAction` / `CrudAction` classes. The only change is the facade
they call for UI (§2b):

```java
// Before: ZK inside the action, only runs in ZK
Filedownload.save(bytes, "application/pdf", "report.pdf");

// After: same action, a tools facade; ZK downloads, Vue receives a DOWNLOAD step
UIFiles.download("report.pdf", "application/pdf", bytes);
```

New front-end-only logic can still be a TS `ClientAction` (it already exists) that uses the TS twin of the same
facades. `ActionMetadata.runtime` (§5) says which of the two a front end will get.

---

## 4. Module placement

No new top-level product; the vocabulary lands where the code already lives.

| Concern | Java | TypeScript |
|---|---|---|
| `Intent` data type, facades (`UIViews`, `UIFiles`, `UIChoices`...) and their SPIs | `ui-shared`, new package `tools.dynamia.ui.intent` + facades in `tools.dynamia.ui` | `ui-core`, new folder `intents/` |
| View model spec + base impl | `ui-shared` (state/transition types, no persistence); `crud` keeps `CrudControllerAPI` as the facade over it | `ui-core` (`viewmodel/`) |
| Wire types (`ActionFlowStep`, `Intent`, `ViewModelState`) | generated schema from Java | `sdk` (generated types) |
| Replay (intent -> `ActionFlowStep`) | `actions` (existing `ReplayInteractions`) | `vue` `runActionFlow` generalised to all intents, moved to `ui-core` once framework-free |
| Adapters | `zk` (+ `zk-starter`) | `vue`; future `react`, `web-components` |
| Conformance fixtures | `platform/contract/` (new, language-neutral JSON) | same files |

Dependency consequence to check: `actions` would depend on `ui-shared` (for `Intent`). `ui-shared` depends only on
`commons`, `integration` and `io`, so no cycle appears, but `ui-shared` must never import `viewers` or `actions`:
intents refer to views and actions **by name** (`viewDescriptor: "ResetPasswordForm"`), not by type.

`ui-core` already has no Vue dependency; the generalised flow runner moves there so a second front end (React, web
components, Svelte) reuses it instead of copying `packages/vue/.../runActionFlow.ts`.

---

## 5. Classifying every action

Today an action reaches REST only if it is a `CrudRemoteAction` or `HeadlessCapable`. That is implicit and, for
`HeadlessCapable`, defaults to "yes" (UI_PORTS_FOR_ACTIONS.md explains why making all `CrudAction`s headless by default
is unsafe). Replace the implicit rule by an explicit `runtime`, published in `ActionMetadata`:

| `runtime` | Meaning | Who executes it |
|---|---|---|
| `HEADLESS` | Local action written against intents; replay works | Server (replay), or ZK directly |
| `FLOW` | `FlowRemoteAction` state machine | Server |
| `CLIENT` | Pure UI behaviour (`FindAction`, `FiltersAction`, `NewAction`...); the metadata carries a hint, each front end implements it once | Front end |
| `ZK_ONLY` | Needs ZK components on purpose (legacy screens, `ZkEmbed` targets) | ZK only; Vue shows it embedded (`ZkEmbed`) or hides it |

Rules:

- `runtime` is **declared** (annotation attribute on `@InstallAction` or a method on the action), never inferred, and is
  **reviewed per action**: publishing an action to REST means exposing it, so each one that resets a password, clears a
  cache or reinitialises an account gets its restrictions checked first.
- **A test walks every `@InstallAction`** in the framework and in the ERP and fails when `runtime` is missing or when an
  action declared `HEADLESS` imports `org.zkoss`. This turns the inventory into something executable and gives a
  progress number ("n of N actions are not `ZK_ONLY`").
- `ApplicationGlobalAction` gets the same treatment (the loader currently only reads `ApplicationGlobalRemoteAction`).

---

## 6. Migrating the ZK-bound code

The order below reduces risk: every step is useful alone and keeps ZK working.

### Phase 0: make the problem measurable
1. The `@InstallAction` walking test with a baseline file (`runtime` unset allowed, listed). Fails only on regressions.
2. The conformance fixture suite for the existing `ActionFlowStep` / `ActionMetadata` / `ViewDescriptor` JSON (it
   documents today's contract and catches drift before the design changes anything).

### Phase 1: intents on top of what exists
1. Generalise the `UIMessages` mechanism: a common way to register an SPI per facade, bind it per execution
   (`withDisplayer` becomes a generic scoped binding) and record an interaction as an `Intent` that serialises to an
   `ActionFlowStep`. `UIMessages` keeps its public API; existing headless tests must pass untouched.
2. The first new facade, `UIViews.showForm/showView`, with its ZK implementation (window + `Viewer`) and its headless
   one (`DIALOG` step). Vue already renders `DIALOG` in `runActionFlow`; move that loop to `ui-core`.

### Phase 2: the other facades, driven by real actions
`UIChoices`, `UIFiles` (upload, download), `UINavigation`, then `UIProgress`. Each one lands with **one real action
migrated end to end by replacing only its ZK lines** (facade, ZK implementation, headless implementation, TS renderer,
test), starting with `ExportReportAction` / `ImportReportAction`
(UI_PORTS_FOR_ACTIONS.md step 2). Then the nine business actions without ZK are reviewed and marked `HEADLESS`.

### Phase 3: view models (deferred)
Extract `CrudViewModel` from `CrudController` (the `HeadlessCrudController` duplication is the trigger), then align
`useCrud`/`CrudView` with the spec. `Form`, `Table`, `Tree` follow. ZK keeps its widgets, now bound to the shared model.

### Phase 4: global actions, navigation and the rest of the framework
Global actions loader, navigation metadata completeness, dashboard widgets (`extensions/dashboard`), the remaining
`ui` modules of extensions that still hold logic next to ZK code (MIGRATION_ZK_SEPARATION.md continues).

### Phase 5: Dynamia ERP (deferred, out of the first iteration)
Same method on `dynamia-erp`, in this order, because each step is independently shippable:

1. Run the walking test over the ERP's `@InstallAction`s, classify each (`HEADLESS` / `CLIENT` / `ZK_ONLY`), publish the
   numbers. Do not start rewriting before this list exists.
2. Replace direct ZK calls inside actions (`Messagebox`, `Window`, `Filedownload`) by the facades. The actions keep their
   logic, class names, ids and restrictions; this alone converts most of the 93 files, since the common cases are
   confirm, form, choose, download. Actions that stay on ZK are not touched.
3. Screens that are real ZK layouts (`erp-ui`, `erp-widgets`) either get a `ViewDescriptor` or remain `ZK_ONLY` and are
   embedded in Vue (`ZkEmbed`) during the transition.
4. Align with `erp-next` (`/api/v2`, Kotlin facade): DynamiaNext and the Dynamia UI protocol are two ways of exposing the
   same actions to Vue/POS/shop. They must not diverge: either `/api/v2` forwards flow steps unchanged, or the facade
   translates them in one place. **Decision needed with the POS and shop owners** (Q4).
5. Per-module PRs against `next`, each with its `ZK_ONLY` count going down.

Cross-repo: a change in `ui-shared`, `actions` or the wire types can break `dynamia-erp` and, through `/api/v2`,
`dynamia-pos` and `tienda-shop`; every phase lists its impact on them in the PR.

---

## 7. Open questions

- **Q1. Which side generates which?** Java as source (schema generated from classes) is simplest for the maintainers
  and keeps the wire format close to what exists, but TS developers can not evolve the contract first. The alternative,
  JSON Schema by hand as source and both languages generated, is cleaner but adds a toolchain. Recommendation: Java as
  source, schema + TS types generated at build time, fixtures as the safety net.
- **Q2. Is `Intent` a new type or just `ActionFlowStep`?** Recommendation: keep `ActionFlowStep` as the wire form (it is
  already shipped and has a token), add the new step types, and treat `Intent` as the in-process Java/TS API that
  serialises to it. Avoids a second protocol.
- **Q3. Does a TS `ClientAction` get server-side effects?** It runs in the browser; anything that must persist goes
  through `callAction` to a server action (same restrictions and auditing). No TS code runs on the server.
- **Q4. `/api/v2` (DynamiaNext) vs the flow protocol.** One surface or two? See Phase 5.4.
- **Q5. Versioning of the experimental flow protocol.** It is marked experimental; moving it to a stable
  `uiProtocolVersion: 1` means committing to its wire format. Decide before Phase 1 ships.
- **Q6. Offline.** `dynamia-pos` is offline-first. Replay needs the server; intents that a POS must run offline
  (confirm, choose) need a client implementation of the action, i.e. a `ClientAction`. Out of scope here, but it is
  a reason to keep the TS action API first class.

---

## 8. What stays ZK on purpose

Some things are not worth abstracting: ZK-specific components used by large legacy screens, the ZK template/theme
(`theme-dynamical`), `MicroFrontend` hosting, and extensions that exist only for the ZK back office. They are marked
`ZK_ONLY`, keep working unchanged, and are reachable from Vue through the embed (`ZkEmbed`, INLINE_ZK_EMBED.md) while
a Dynamia-UI-native replacement does not exist. The success metric is not "zero ZK classes"; it is **"every user-facing
capability is reachable without ZK, or is explicitly and knowingly `ZK_ONLY`"**.

---

## 9. Risks

| Risk | Mitigation |
|---|---|
| Two implementations (Java + TS) of view models drift | Shared fixtures and spec tests run in both suites (§3.1); view model logic stays small |
| Replay determinism broken by a new intent | Intents are listed with their determinism rule; `progress` restricted; replay tests per intent |
| Publishing an action to REST exposes it | `runtime` is declared and reviewed per action; restrictions checked; the walking test forces a decision |
| Big-bang rewrite temptation | Every phase ships alone and keeps ZK working; real action migrated with each new intent |
| Breaking the flow protocol used by the current Vue backoffice | Phase 0 fixtures pin today's behaviour first; changes are additive within `uiProtocolVersion` |
| `next` is explosive and the ERP depends on it | Per-phase impact check on `dynamia-erp`, `/api/v2` consumers; no cross-repo breaking change without the ERP PR ready |

---

## 10. Suggested first deliverables

1. This document agreed (answers to Q1, Q2, Q4, Q5).
2. Phase 0: walking test + baseline, contract fixtures.
3. Phase 1 on `ui-shared` / `ui-core`, with the existing headless tests as the regression net.
4. GitHub issues per phase under the ZK-separation epic (#130); issues are the backlog, this document is the design.
