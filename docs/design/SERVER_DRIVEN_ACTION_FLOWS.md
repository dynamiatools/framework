# Server-Driven Action Flows for `RemoteAction`

**Status:** Phases 0–4 implemented (commits `0ddfbe66` "Phases 0-3" and `d03b14c5` "Phase 4", Aug 2026), plus
follow-up hardening: generic `CrudState` enforcement (#83, closed — see §7.6 point 2), `INPUT`/`DIALOG`/
`CUSTOM` flow step renderers on the Vue side (#81, scoped down to `REDIRECT`/`CALL` — see §6), and the
422-vs-406 validation-error shape reconciliation (#82, closed — see §7.6 point 3), and `REDIRECT`/`CALL`
step renderers (#81, **experimental** — see §6 and §9). Tracked in #79. See inline notes throughout (§4, §6,
§7.6, §8, §9) for the delta between this design and what actually shipped.

> **Experimental.** The whole flow protocol (`FlowRemoteAction`, `ActionFlowStep`, `resumeToken`, the
> `runActionFlow` client loop) is experimental: it's implemented and tested, but has had limited real-world
> use and its API/wire format may still change without a deprecation cycle. `REDIRECT`/`CALL` are the least
> settled part (see §6 for the semantics chosen and what's deliberately unsupported).
**Scope:** `platform/core/actions`, `platform/core/crud`, `platform/app` (controller), `platform/packages/sdk`, `platform/packages/ui-core`, `platform/packages/vue`.
**Author context:** design discussion between Mario A. Serrano Leones and Claude, July 2026.

---

## 1. Problem statement

A ZK `LocalAction` (e.g. `SaveAction`, `platform/core/crud/src/main/java/tools/dynamia/crud/actions/SaveAction.java`)
runs inside the same AU-request thread that owns the live ZK component tree (`Desktop`). It can call
`crud.getController().onSave(callback)`, block conceptually on a `Messagebox.show` confirm, and resume
in the *same call stack*, because ZK keeps server-side state alive across the whole session and multiplexes
UI events over its own polling/Comet channel. `MessageDialog.showQuestion(...)`
(`platform/ui/zk/src/main/java/tools/dynamia/zk/ui/MessageDialog.java`) is the concrete example: the callback
literally re-enters live server objects once the user clicks Yes/No.

`RemoteAction` (`platform/core/actions/src/main/java/tools/dynamia/actions/RemoteAction.java`) has none of
this. Today it is one HTTP round trip, one bean instance, one answer:

```java
public interface RemoteAction extends Action {
    ActionExecutionResponse execute(ActionExecutionRequest request);
}
```

`ApplicationMetadataController.executeAction(...)` resolves a **fresh prototype-scoped bean** per request
(`Containers.get().findObject(actionMetadata.getAction().getClass())`) — there is no `Desktop`, no live
component tree, no session-pinned Java object to call back into. This is why no `RemoteAction` implementation
exists anywhere in the framework yet: as soon as an action needs to ask the user something ("are you sure?",
"pick one of these three", "here's the computed total, confirm?"), the current contract has nowhere to put
that interaction.

`theme-dynamical-vue` (the fully client-rendered Vue theme, see `themes/theme-dynamical-vue/README.md`) is the
first consumer that will actually need this: it has no ZK fallback for anything, so every interactive backend
action has to go through `RemoteAction` — and confirm/wizard-style actions are a normal CRUD requirement
(bulk delete confirmation, multi-step approval, "generate report — pick options — download").

This document proposes a **server-driven flow protocol**: a `RemoteAction` can return "not done yet, show the
user this, then call me back with their answer" instead of only ever returning a final result. It is additive
to the existing contract — nothing about today's `RemoteAction`/`ActionExecutionResponse` behavior changes.

---

## 2. Constraints that shape the design

- **No server affinity.** Per `[[dynamia-tools]]`-style principles already in this codebase (multi-tenant,
  multi-region, cost-aware — see `CLAUDE.md`), a flow must not depend on a Java object surviving between
  requests, a sticky session, or an in-memory map on one pod. The controller already discards the action bean
  after every call; the design should embrace that instead of fighting it.
- **Backward compatible.** `ActionExecutionRequest`/`ActionExecutionResponse` are real, working DTOs
  (`platform/core/actions/src/main/java/tools/dynamia/actions/ActionExecution{Request,Response}.java`) used by
  every future `RemoteAction`/`CrudRemoteAction`. Both already carry `@JsonInclude(NON_NULL)` +
  `@JsonIgnoreProperties(ignoreUnknown = true)` — new fields must be optional and ignorable by old clients.
- **Zero controller changes.** `ApplicationMetadataController` (`platform/app/.../ApplicationMetadataController.java:141-177`)
  and `Actions.execute(RemoteAction, ActionExecutionRequest)` (`platform/core/actions/.../Actions.java:193-210`)
  should not need to know a flow is happening. The dispatch stays `action.execute(request)` in, response out.
- **No new form-description language.** DynamiaTools already has a YAML view-descriptor system
  (`platform/core/viewers`, `tools.dynamia.viewers.ViewDescriptor`) rendered on the Vue side by
  `VueFormRenderer` → `DynamiaForm` (`platform/packages/vue/src/renderers/VueFormRenderer.ts`,
  `global-components.ts`). A flow step that needs user input should reuse that, not invent a second one.
- **Frontend primitives are a real gap, not just wiring.** A repo-wide search for
  `showDialog|showConfirm|useToast|useDialog|useConfirm` across `platform/packages/ui-core` and
  `platform/packages/vue` returns **zero hits**. There is currently no confirm dialog, toast, or modal
  abstraction on the Vue/ui-core side at all. Any flow design must treat this as Phase 0, not assume it exists.

---

## 3. Wire protocol

Add one optional field to each existing DTO. Everything else about them is unchanged.

```java
// ActionExecutionResponse — one new field
public class ActionExecutionResponse {
    private Object data;
    private Map<String, Object> params;
    private String source;
    private String status;
    private int statusCode;
    private String dataType;
    private String dataId;
    private String dataName;

    private ActionFlowStep flow;   // NEW — null/absent = today's exact behavior (terminal, one-shot)
}
```

```java
// ActionExecutionRequest — two new fields
public class ActionExecutionRequest {
    private Object data;           // reused as-is: on a continuation call, `data` IS the user's answer
    private Map<String, Object> params;
    private String source;
    private String dataType;
    private String dataId;
    private String dataName;

    private String flowId;         // NEW — correlates request to a flow instance
    private String resumeToken;    // NEW — opaque, signed continuation state (echoed back verbatim by the client)
}
```

`ActionFlowStep` is the new type carrying the "what to show" descriptor:

```java
public class ActionFlowStep implements Serializable {
    private String flowId;
    private ActionFlowStepType type;      // CONFIRM, INPUT, DIALOG, NOTIFY, REDIRECT, CALL, DONE, CUSTOM
    private String title;
    private String message;
    private MessageType messageType;      // reuse tools.dynamia.ui.MessageType (NORMAL/ERROR/WARNING/INFO/CRITICAL/SPECIAL)
    private String viewDescriptor;        // for DIALOG: name of an existing ViewDescriptor to render as a form
    private Map<String, Object> data;     // step payload: prefilled entity, redirect URL, action-to-CALL id, custom component name…
    private String resumeToken;           // signed, opaque — the client must echo this back unmodified
}

public enum ActionFlowStepType {
    CONFIRM,   // yes/no question -> answer: boolean
    INPUT,     // single value prompt -> answer: string/number
    DIALOG,    // render `viewDescriptor` as a DynamiaForm, collect the whole entity/form -> answer: Map<String,Object>
    NOTIFY,    // fire-and-forget toast; client auto-continues without waiting for user input
    REDIRECT,  // client navigates to `data.url`; flow ends or continues depending on `data.awaitReturn`
    CALL,      // client invokes another action (`data.action`) first, feeds its response back as the answer
    DONE,      // terminal — identical semantics to a normal `ActionExecutionResponse` today
    CUSTOM     // escape hatch: `data.component` names a client-registered step renderer for anything not listed above
}
```

A response with `flow == null` is exactly what every `RemoteAction` returns today — **existing implementations
need no changes and old frontend code ignoring the field keeps working.**

### Why `CUSTOM` + `data` map instead of a fixed closed enum

`Action`/`ActionReference` already lean on a free-form `renderer` string elsewhere in this same module for
pluggable UI (`Action.toReference()`, `platform/core/actions/.../Action.java`). `CUSTOM` follows the same
pattern: a fixed core vocabulary covers the 90% case type-safely, `CUSTOM` lets an app register its own step
renderer client-side without touching the enum — same trade-off already accepted for `renderer`.

---

## 4. Stateless continuation: the `resumeToken`

This is the load-bearing decision, and it's settled: **no Redis, no server-side flow store, no new
infrastructure at all.** All flow state travels in the `resumeToken` itself, round-tripping through the
client on every request/response. The server holds nothing between calls — this was evaluated against a
Redis-backed alternative (cache entry keyed by `flowId`) and rejected: it would add a new infra dependency,
TTL/eviction handling, and a cache-miss failure mode for a problem the token approach already solves for
free. It also fits the "no server affinity" constraint from §2 for free — any pod can handle the next step,
flows survive pod restarts/rolling deploys automatically, no shared state to reason about.

Consequence, made explicit: the token is not a place to put large or secret intermediate data — it's visible
to the client (unless separately encrypted) and inflates every request/response in the flow. This is fine for
the realistic use cases (confirm dialogs, short wizards, a handful of accumulated answers/ids). A flow that
genuinely needs to hide a large or sensitive payload from the client is out of scope for this design — solve
that case specifically if/when it comes up, not by reintroducing a general server-side store here.

**Security requirement, non-negotiable:** the token **must be signed** (HMAC-SHA256). Without a signature, a
client could forge a `resumeToken` to jump straight to a `DONE` step with fabricated `data`, or
replay/tamper with accumulated answers (e.g. change which entity ids a confirmed bulk-delete applies to).
Signing prevents tampering; it does not provide secrecy — see the paragraph above for why that's an accepted
trade-off here, not a gap to patch with encryption by default.

**As implemented, this does *not* reuse the JWT cookie's signing key** — `platform/core/actions` cannot
depend on the security extension that owns it. `FlowTokenSigner`
(`platform/core/actions/src/main/java/tools/dynamia/actions/flow/FlowTokenSigner.java`) manages its own
independent secret, read from `dynamia.actions.flow.secret` (min 32 chars); if unset/too short it falls back
to a random per-JVM secret with a logged warning — fine for dev, but flows won't resume across a
restart/rolling deploy in that mode, so **this property must be set explicitly in production**. Token TTL is
`dynamia.actions.flow.token-ttl` (ISO-8601 duration), defaulting to `PT10M` — this settles the "timeout
policy" open question from the original draft.

This WARN-and-fall-back-to-an-ephemeral-secret pattern is not special-cased or improvised: it's the same
pattern `JWTServiceImpl.getJwtSecretKey()` already uses for `JWT_SECRET`
(`extensions/security/.../JWTServiceImpl.java`) — same 32-char minimum, same log-and-continue instead of
failing startup. `dynamia.actions.flow.secret` follows that established repo convention deliberately, so
there's nothing to reconcile between the two; deploying either without its secret set has the identical
failure mode (works fine single-pod/dev, silently stops surviving a restart/rolling deploy/multi-pod
routing in production) and the identical fix (set the property). No fail-fast startup check was added here
for the same reason none exists for `JWT_SECRET`: introducing one only for flow tokens would be a new,
unprecedented pattern in this codebase rather than a bug fix — worth revisiting for *both* properties
together if stricter startup validation is ever wanted, not as a one-off for this feature.

### Server-side shape

```java
public class ActionFlowContext {
    String flowId();
    String currentStep();
    <T> T get(String key, Class<T> type);   // typed accessor into the decoded, verified token payload
    void put(String key, Object value);     // accumulated into the NEXT signed resumeToken
}
```

`ActionFlows.dispatch(action, request)` verifies the signature and rejects/`401`s a tampered or expired token
before any implementor code runs. **As implemented this is not an `ActionFilter` hook** (the
`ActionRequestAutoconvertDataFilter` extension point Phase 1 originally intended to reuse) — verification
happens inline inside `ActionFlows.dispatch()`, which `FlowRemoteAction.execute()`'s default method calls
directly. Simpler than routing through the filter chain, and it still satisfies "zero controller changes":
`Actions.execute()`/`ApplicationMetadataController` call `action.execute(request)` exactly as before and
never know a flow is happening.

---

## 5. Java API

New interface, purely additive — does not touch `RemoteAction`, `AbstractRemoteAction`, or any existing
implementor:

```java
public interface FlowRemoteAction extends RemoteAction {

    // Bridges into the flow engine so existing dispatch (Actions.execute, both controller endpoints)
    // needs zero changes — this is the only method RemoteAction actually requires.
    default ActionExecutionResponse execute(ActionExecutionRequest request) {
        return ActionFlows.dispatch(this, request);
    }

    // First call in a flow (no resumeToken on the incoming request).
    ActionFlowStep start(ActionFlowContext ctx);

    // Every subsequent call (request carries a verified resumeToken); `answer` is request.getData().
    ActionFlowStep resume(ActionFlowContext ctx, Object answer);
}
```

`CrudRemoteAction`/`AbstractCrudRemoteAction` (`platform/core/crud/.../CrudRemoteAction.java`,
`AbstractCrudRemoteAction.java`) gain an analogous `FlowCrudRemoteAction` only if/when a concrete use case needs
it — no reason to add it speculatively.

### Example — bulk-delete-with-confirm as a `FlowRemoteAction`

```java
@InstallAction
public class BulkDeleteAction extends AbstractCrudAction implements FlowRemoteAction {

    @Override
    public ActionFlowStep start(ActionFlowContext ctx) {
        var ids = ctx.get("ids", List.class);
        ctx.put("ids", ids);
        return ActionFlowStep.confirm("Delete " + ids.size() + " records?", "Confirm");
    }

    @Override
    public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
        if (Boolean.TRUE.equals(answer)) {
            crudService().delete(ctx.get("ids", List.class));
            return ActionFlowStep.done(null, "Deleted.", MessageType.INFO); // DONE + a toast on the way out
        }
        return ActionFlowStep.done("cancelled");
    }
}
```

> As shipped: `ActionFlowStep` has no `.thenDone()` chaining — `done(Object data, String message,
> MessageType messageType)` is the actual overload for "terminal step plus a toast" (see
> `DeleteFlowRemoteAction`/`SaveFlowRemoteAction` in `platform/core/crud/.../actions/remote/` for the real
> equivalents of this example, renamed per §7.2). `ActionFlowContext` also gained an `asMap()` accessor in
> Phase 4 (a read-only snapshot of everything accumulated so far) — needed once `SaveFlowRemoteAction` had
> to recover a whole entity payload across `start()`/`resume()`, not just one key at a time.

No new endpoint, no new controller code — `POST /api/app/metadata/entities/{class}/action/bulkDelete` handles
both the initial call and every continuation, distinguished only by the presence of `resumeToken` in the
request body.

---

## 6. Frontend

### Phase 0 (prerequisite, not optional): step-renderer primitives

None of `confirm`, `toast`, `modal dialog`, `prompt` exist today in `ui-core`/`vue` (§2). These are useful on
their own (e.g. CRUD delete confirmation) independent of this flow feature, so build them first as ordinary
Vue components/composables: `useConfirm()`, `useToast()`, a `<DynamiaDialog>` wrapping arbitrary content
(including `<DynamiaForm>` for `DIALOG` steps, closing the loop with §2's "no new form language" constraint).

### Phase 1: SDK loop

`ActionsApi.execute(...)` (`platform/packages/sdk/src/metadata/actions.ts:19-64`) and the TS mirrors of
`ActionExecutionRequest`/`ActionExecutionResponse` (`platform/packages/sdk/src/metadata/types.ts:146-189`) gain
the same optional fields as §3. A new composable drives the loop; today `Actions.vue:100-105` does a single
`await client.actions.execute(...)` — this becomes:

```ts
async function runFlow(action: ActionMetadata, request: ActionExecutionRequest) {
  let response = await client.actions.execute(action, request, { className });
  while (response.flow && response.flow.type !== 'DONE') {
    const answer = await renderStep(response.flow); // dispatches by type to the Phase-0 primitives
    response = await client.actions.execute(action, {
      ...request,
      flowId: response.flow.flowId,
      resumeToken: response.flow.resumeToken,
      data: answer,
    }, { className });
  }
  emit('action-response', { action, request, response, local: false });
}
```

`renderStep` for `CUSTOM` looks up a client-registered handler — same registry shape as the existing
`ClientActionRegistry` (`platform/packages/ui-core/src/actions/ClientAction.ts`), just keyed by
`data.component` instead of action id.

> **Update (#81): `CONFIRM`/`NOTIFY`/`INPUT`/`DIALOG`/`CUSTOM` are now all wired.** `runActionFlow`
> (`platform/packages/vue/src/actions/runActionFlow.ts`, extracted out of `Actions.vue` in Phase 4 so
> `useCrudPage`'s save/delete handlers could share it per §7.5):
> - `INPUT` → `useInput()`/`promptManager` (ui-core `PromptManager`, mirrors `ConfirmManager`) +
>   `<DynamiaPromptHost>`.
> - `DIALOG` → `runActionFlow` itself fetches `step.viewDescriptor` for the request's `dataType` via
>   `client.metadata.getEntityView`/`getEntity`, builds and prefills a `VueFormView` from `step.data`, and
>   hands it to `useFormDialog()`/`dialogFormManager` + `<DynamiaFormDialogHost>` (renders it as a
>   `<DynamiaForm>` inside a `<DynamiaDialog>`) — closing the loop on §2's "no new form-description
>   language" constraint exactly as originally planned.
> - `CUSTOM` → `FlowStepRendererRegistry`/`registerFlowStepRenderer` (ui-core), the registry this
>   paragraph describes, keyed by `step.data.component` — same shape as `ClientActionRegistry`.
>
> **Update (#81, experimental): `REDIRECT`/`CALL` are wired, with deliberately narrow semantics.** They need
> the loop's control flow rather than a plain renderer, so they live in `runActionFlow`'s `driveFlow`:
> - `REDIRECT` is **terminal**. The client navigates (`handlers.navigate`, default
>   `window.location.assign`) and the loop ends; `runActionFlow` resolves with the response carrying the
>   `REDIRECT` step. Only relative and `http(s)` URLs are followed (`javascript:`/`data:` are refused).
>   `data.awaitReturn = true` is **rejected** with an explicit error, not silently ignored: resuming a flow
>   after the page navigates away needs the token persisted somewhere and an app-level "on return" hook,
>   and no real use case has asked for it yet.
> - `CALL` runs `data.action` **inside the same loop** (so the called action can itself be a flow), against
>   the global endpoint, or the entity endpoint when `data.className` is present. Remaining `data`
>   entries become the nested request's `data`. When the nested flow reaches `DONE`, the calling flow
>   resumes with the nested `ActionExecutionResponse` **minus its `flow`** as the answer. Nesting is capped
>   at 5 levels to fail fast on a runaway server-side loop. A nested flow that ends in `REDIRECT` ends the
>   outer flow too.
>
> The server side needed no change: `ActionFlowStep.redirect(...)`/`call(...)` already existed.

---

## 7. CRUD parity actions for the REST/Vue frontends

### 7.1 Decision: ZK stays untouched

`SaveAction`, `DeleteAction`, `EditAction`, `NewAction`, `CancelAction`, `SaveAndEditAction`,
`SaveAndNewAction` (`platform/core/crud/src/main/java/tools/dynamia/crud/actions/*.java`) are ZK
`LocalAction`/`CrudAction` implementations, consumed only by ZK's `CrudView`
(`platform/ui/zk/src/main/java/tools/dynamia/zk/crud/CrudView.java`). **They are not touched by this work.**
Instead, new classes implementing `CrudRemoteAction`/`FlowRemoteAction` are added *alongside* them for the
REST/Vue frontends, named after their ZK counterpart with a `RemoteAction`/`FlowRemoteAction` suffix
(`SaveRemoteAction`, `SaveFlowRemoteAction`, `DeleteRemoteAction`, `DeleteFlowRemoteAction`, …) — same naming
shape the codebase already uses for `LocalAction` vs `RemoteAction` at the interface level.

This matters more than it might look: today, `client.crud(path)`'s plain REST verbs
(`RestNavigationCreateOperation`/`UpdateOperation`/`DeleteOperation`,
`platform/core/web/src/main/java/tools/dynamia/web/navigation/`) call `CrudService.create/update/delete(...)`
**directly**, completely bypassing the Action framework — `Actions.execute()`
(`platform/core/actions/.../Actions.java:193-210`) and its `ActionFilter` hooks never run for a plain Vue CRUD
save/delete today. `CrudServiceListener` before/after hooks and validation fire either way (both paths end at
the same `CrudService`), but `ActionFilter`-based cross-cutting logic (auditing, per-action authorization via
`ActionRestrictions`) does not. Introducing `SaveRemoteAction`/`DeleteRemoteAction` gives Vue-side CRUD writes
the same extensibility surface ZK's `SaveAction`/`DeleteAction` already have — via `ActionFilter`, not by
adding anything ZK-specific.

### 7.2 Which ZK actions get a Remote/Flow counterpart

| ZK action | New class(es) | Why |
|---|---|---|
| `SaveAction` | `SaveRemoteAction` (one-shot) + `SaveFlowRemoteAction` (adds an optional `CONFIRM` step) | Real server mutation; `isConfirmBeforeSave()` is a ZK-only concept today ( `CrudControllerAPI` ) with no REST equivalent — `SaveFlowRemoteAction` gives entities that need "are you sure you want to save this?" a real mechanism, not just a client-side `confirm()` that skips server-side re-validation. |
| `DeleteAction` | `DeleteRemoteAction` (one-shot) + `DeleteFlowRemoteAction` (`CONFIRM` step first) | Same reasoning, and delete-without-confirm is the classic footgun — `DeleteFlowRemoteAction` should be the one actually wired into the default CRUD toolbar; `DeleteRemoteAction` stays available for programmatic/already-confirmed contexts. One class handles both single-id and list `data` (no separate `BulkDeleteRemoteAction`; see §5's `BulkDeleteAction` example, which should be renamed to this convention). |
| `EditAction` | none | Loading an entity into the form is already `client.crud(path).findById(id)` — plain GET, nothing to gate behind an action. |
| `NewAction` | none *(for now)* | Blank-entity defaults are computed client-side today. Only add `NewRemoteAction` if/when a concrete entity needs server-computed defaults (e.g. a sequential invoice number) — don't build it speculatively. |
| `CancelAction` | none | Pure client-side state transition (`CrudState` back to `READ`), never touches the server in either theme. |
| `SaveAndEditAction` / `SaveAndNewAction` | none | These are UI sequencing sugar ("save, then open Edit/New") around `SaveAction`. On the Vue side the same sequencing is just "the frontend calls `SaveRemoteAction`/`SaveFlowRemoteAction`, and on `DONE` decides whether to transition to edit-mode or reset to a new blank form" — no separate backend class needed. |

### 7.3 Where they live

Same module as the ZK ones (`platform/core/crud` has no ZK dependency itself — `SaveAction` etc. only depend
on the framework-agnostic `CrudViewComponent`/`CrudControllerAPI` abstractions, ZK is just one implementor),
new sibling package: `tools.dynamia.crud.actions.remote`, next to the existing
`tools.dynamia.crud.actions` (ZK) package. No new Maven module needed.

### 7.4 Flow variant: composition, not inheritance — a Java gotcha to avoid

ZK's own precedent for this (`SaveAndEditAction extends SaveAction`, overriding a `protected afterSave(...)`
template method) doesn't carry over cleanly to `SaveFlowRemoteAction extends SaveRemoteAction implements
FlowRemoteAction`. `FlowRemoteAction.execute(...)` is a **default interface method**; `SaveRemoteAction`
already provides a concrete `execute(...)` **class** method (it implements `CrudRemoteAction extends
RemoteAction` directly). Java's resolution rule is "a class's own/inherited method always wins over an
interface default" — so `SaveFlowRemoteAction` would silently keep running `SaveRemoteAction`'s plain
one-shot `execute()` and never reach the flow dispatch, unless it explicitly overrides `execute()` again with
`FlowRemoteAction.super.execute(request)`. That works, but it's a footgun for the next person who instinctively
copies the ZK `extends` pattern. **Recommendation:** don't extend; share a small `protected static` persist
helper (e.g. `SaveSupport.persist(CrudService, ActionFlowContext)`) between `SaveRemoteAction` and
`SaveFlowRemoteAction`, and have each implement its own interface directly. Same effective code reuse, no
diamond.

### 7.5 The one integration point that makes these reachable from the UI

Research turned up **two independent CRUD-invocation paths** in the Vue side, and only one of them matters
here:

- `Crud.vue` (`platform/packages/vue/src/components/Crud.vue`) renders **hardcoded** New/Edit/Delete/Save/Cancel
  buttons that call `CrudView` methods (`view.save()`, `view.delete(row)`, …) **directly** — this is what
  actually fires for the default CRUD toolbar today, and it does not go through `Actions.vue` at all.
- `Actions.vue`'s generic toolbar (`resolvedActions`, driven by `entityMetadata.actions`) is a *second*,
  separate mechanism, today essentially dormant for CRUD verbs because `tryHandleCrudActionLocally()`
  (`platform/packages/vue/src/components/Actions.vue:169-207`) already intercepts anything matching
  `isSaveCrudAction`/`isDeleteCrudAction` (`platform/packages/vue/src/actions/crudActionUtils.ts`) and redirects
  it back to the same `CrudView` methods, before it can ever reach `client.actions.execute(...)`.

Because both paths bottom out at the same `CrudView`/`useCrudPage` methods
(`platform/packages/vue/src/composables/useCrudPage.ts`, `crudView.on('save', …)` /
`on('delete', …)`, lines ~108-148), there is exactly **one place to change**, not two: make `useCrudPage`'s
save/delete handlers action-aware —

```ts
crudView.on('save', async (payload) => {
  const saveAction = context.entityMetadata?.actions?.find(isSaveCrudAction);
  if (saveAction) {
    await runFlow(client, saveAction, buildActionExecutionRequest(payload));  // §6 loop; ends in DONE with the saved entity
  } else {
    await (mode === 'create' ? api.create(data) : api.update(id, data));     // unchanged today's behavior
  }
  await crudView.dataSetView.load();
});
```

Register the new classes with the conventional ids `"save"`/`"delete"` (`AbstractAction.setId(...)`) so the
*existing* `isSaveCrudAction`/`isDeleteCrudAction` matchers pick them up for free — no new matching concept
needed. Consequence: **an entity with no registered `SaveRemoteAction`/`DeleteFlowRemoteAction` bean behaves
exactly as it does today** (`entityMetadata.actions` stays empty, plain REST runs) — this is fully opt-in per
entity, not a behavior change for existing apps.

### 7.6 Two latent bugs found while researching this — both prerequisites, not part of this feature per se

1. **`ActionMetadata` only checks `instanceof CrudAction` (ZK), not `CrudRemoteAction`**
   (`platform/app/src/main/java/tools/dynamia/app/metadata/ActionMetadata.java:95`) — so `applicableStates`
   never serializes to the client for any `CrudRemoteAction`, including the new ones. Needs
   `if (action instanceof CrudAction crudAction) { ... } else if (action instanceof CrudRemoteAction crudRemoteAction) { ... }`
   (or unify both branches) before `applicableStates`-based toolbar filtering can work for these at all.
   **Fixed in Phase 4** (`d03b14c5`) exactly as described.
2. **`CrudState` is not enforced server-side.** `ApplicationMetadataController.executeAction(...)`
   (lines 155-177) only checks `ActionRestrictions.allowAccess(...)` before calling `execute()` — never
   `CrudRemoteAction.getApplicableStates()`. Today that's harmless because no action does a real mutation from
   this endpoint; once `SaveRemoteAction`/`DeleteFlowRemoteAction` exist, add a cheap state check there (or
   inside each action) so a hand-crafted request can't invoke `delete` while the declared applicable state is
   `CREATE`, etc. **Fixed in two steps.** Phase 4 (`d03b14c5`) fixed it scoped narrower than written here:
   `SaveSupport.persist` enforced `applicableStates` for `SaveRemoteAction`/`SaveFlowRemoteAction`
   specifically, not any `CrudRemoteAction` — noted as a real open item in that commit message, not silently
   dropped. **Generalized afterwards (#83)**: `ApplicationMetadataController.executeAction` now enforces
   `getApplicableStates()` for every `CrudRemoteAction`, inferring existing-vs-new from the request the same
   way `SaveSupport` does (entity id present → existing, allowed when READ/UPDATE/DELETE is declared; absent
   → new, allowed only when CREATE is declared) rather than hitting the database. This only distinguishes
   existing-vs-new — an action needing the finer READ/UPDATE/DELETE distinction (as `SaveRemoteAction` does
   for CREATE-vs-UPDATE) still enforces that itself; see `ApplicationMetadataController.isApplicableState`'s
   javadoc for the full reasoning.
3. **Validation-error response shape differs between the two entry points** — plain REST CRUD writes surface
   `ValidationError` as HTTP 422 with an `ErrorResult` body
   (`RestApiExceptionHandler.handleValidationError`), while the Action-framework path wraps the same exception
   as HTTP 406 inside an `ActionExecutionResponse` (`ApplicationMetadataController.executeAction`, lines
   168-169). Once `SaveRemoteAction` exists side-by-side with plain `client.crud(path).create()` for the same
   entity, this inconsistency becomes visible to the same frontend form — worth reconciling (pick one shape)
   before Phase 4 below, not left as an accidental difference. The Phase 4 commit explicitly left this open
   (see §9) — **resolved afterwards (#82)**: `ApplicationMetadataController.executeAction` now returns a
   genuine HTTP `422` with an `ErrorResult` body for `ValidationError`, byte-for-byte matching
   `RestApiExceptionHandler.handleValidationError`'s shape (same `VALIDATION_ERROR` code,
   `invalidProperty`/`invalidValue` detail keys). Every other outcome (success, 403/404/409/500) is
   unchanged — still a real HTTP `200` carrying an `ActionExecutionResponse` whose own
   `status`/`statusCode` fields describe the result, exactly as before; only the validation-error case
   moved. This is a breaking change for `ApplicationMetadataController`'s two `execute*Action` endpoints
   — deliberately: any client awaiting `client.actions.execute(...)` now has that call *throw*
   `DynamiaApiError` on a validation failure (same as `client.crud(path).create()` already did) instead of
   resolving with `response.statusCode === 406`. No SDK/frontend code changes were needed to support this:
   `HttpClient.request` already throws `DynamiaApiError` uniformly for any non-2xx response regardless of
   body shape, and `useCrudPage.ts`'s save/delete handlers already catch generically
   (`catch (e) { crudView.errorMessage.value = String(e); }`).

---

## 8. Rollout plan

1. **Phase 0** — ✅ done (`0ddfbe66`). `ConfirmManager`/`ToastManager` in `ui-core`
   (`platform/packages/ui-core/src/feedback/`), `Dialog.vue`/`ConfirmHost.vue`/`ToastHost.vue` +
   `useConfirm`/`useToast` in `vue`.
2. **Phase 1** — ✅ done (`0ddfbe66`). `ActionFlowStep`/`ActionFlowStepType`/`ActionFlowContext`/
   `FlowRemoteAction`/`ActionFlows` in `platform/core/actions`, resumeToken signing, unit tests for
   tamper/expiry rejection (`ActionFlowsTest`, `FlowTokenSignerTest`). The signer does not reuse the JWT
   cookie's key as originally planned — resolved deliberately, not a gap; see §4's inline note and #80.
3. **Phase 2** — ✅ done (`0ddfbe66`). SDK/TS type additions + the flow-driving loop, wired into `Actions.vue`
   (later extracted to `runActionFlow.ts` in Phase 4). **Deviation:** only `CONFIRM`/`NOTIFY` steps are
   rendered — see §6's inline note.
4. **Phase 3** — ✅ done (`0ddfbe66`). One real `FlowRemoteAction` example, `MarkOutOfStockAction`
   (`examples/demo-zk-books/.../actions/MarkOutOfStockAction.java`). Landed in `demo-zk-books` rather than
   `demo-vue-books`/`theme-dynamical-vue` as this section originally called for, but that's cosmetic, not a
   gap: the action's own javadoc confirms it's reached through the REST/Vue generic actions toolbar via
   `ApplicationMetadataController` (part of `platform/app`, present regardless of which theme module a demo
   happens to bundle), and its implementation notes a real bug found while verifying it end-to-end (a
   `Book`'s lazy JPA associations aren't serializable once the Hibernate session closes — worth being aware
   of for any other `FlowRemoteAction` returning a managed entity directly instead of a flat projection).
5. **Phase 4** — ✅ done (`d03b14c5`). §7.6 bug #1 fixed generically; bug #2 fixed scoped to `SaveSupport` only
   at the time; `SaveRemoteAction`/`SaveFlowRemoteAction`/`DeleteRemoteAction`/`DeleteFlowRemoteAction`
   (`tools.dynamia.crud.actions.remote`) added; `useCrudPage`'s save/delete handlers wired per §7.5.
   §7.6 bug #3 (validation-error shape) explicitly **not** addressed — still open, see §9.
6. **Post-Phase-4 hardening (#83)** — ✅ done. §7.6 bug #2 generalized: `ApplicationMetadataController`
   now enforces `CrudRemoteAction.getApplicableStates()` for *any* such action, not just ones routed through
   `SaveSupport` — see §7.6's updated note.
7. **Post-Phase-4 hardening (#81)** — ✅ done, partially. `INPUT`/`DIALOG`/`CUSTOM` flow step renderers
   implemented on the Vue side (`useInput`/`PromptManager`, `useFormDialog`/`DialogFormManager`,
   `FlowStepRendererRegistry`) — see §6's updated note. `REDIRECT`/`CALL` deliberately left unimplemented;
   their semantics are still an open design question, see §9.
8. **Post-Phase-4 hardening (#82)** — ✅ done. §7.6 bug #3 resolved: `ApplicationMetadataController`'s
   `execute*Action` endpoints now return a genuine HTTP `422`/`ErrorResult` for `ValidationError`, matching
   plain REST CRUD writes byte-for-byte — a deliberate breaking change to those two endpoints' error
   contract, approved explicitly rather than shipped as a silent side effect of other work. See §7.6's
   updated note for what changed and why no SDK/frontend changes were needed.

9. **`REDIRECT`/`CALL` (#81)** — ✅ done, **experimental**. Semantics in §6. Unit-tested against a mocked
   client (`runActionFlow.test.ts`); not yet exercised by a real `FlowRemoteAction` in a browser.

**Not done, no phase currently owns it:** `REDIRECT` with `awaitReturn` (resume after navigating away).

---

## 9. Open questions

- **ZK/`LocalAction` parity**: deliberately out of scope, and settled — see §7.1. ZK actions already have full
  synchronous UI power (§1) and gain nothing from this protocol; forcing them through it would be a
  regression, not an improvement.
- **`REDIRECT`/`CALL` step semantics**: ✅ provisionally resolved (#81, experimental) — `REDIRECT` is
  terminal, `CALL` nests and resumes the caller with the nested response; see §6. **Still open:**
  `REDIRECT` with `awaitReturn: true` (currently rejected). Needs a real use case: where the pending
  `resumeToken` lives across a page navigation, and what triggers the resume on return.
- **`INPUT`/`DIALOG`/`CUSTOM` step renderers**: ✅ resolved (#81) — see §6's updated note.
- **Timeout/expiry policy for `resumeToken`**: ✅ resolved — 10-minute default, configurable via
  `dynamia.actions.flow.token-ttl` (§4).
- **Validation-error shape reconciliation** (§7.6, point 3): ✅ resolved (#82) — both entry points now
  return 422/`ErrorResult` for a failed validation. See §7.6's updated note.
- **Signer key source**: ✅ resolved (see #80) — `FlowTokenSigner`'s independent secret
  (`dynamia.actions.flow.secret`) is the permanent answer, not a placeholder: it deliberately mirrors the
  established `JWT_SECRET`/`JWTServiceImpl` convention already in this codebase (§4), rather than reusing
  the JWT cookie key as the original draft assumed. Deployments must set the property explicitly or flows
  silently stop surviving restarts/rolling deploys — identical to the existing `JWT_SECRET` requirement, not
  a new deployment burden.
