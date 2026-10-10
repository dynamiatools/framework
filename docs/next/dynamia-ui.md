# Dynamia UI: target architecture (ZK as a port)

**Date:** 2026-10-10 · **Base:** `dynamia-tools@feature/next-ui` (13 commits over `next`) · **Main consumer:**
`dynamia-erp@next`.
**Sources:** previous revision of this document, [dynamia-ui-inventory.md](dynamia-ui-inventory.md) and a reading of the code.
**Audience:** whoever implements it. Sections 1 to 10 describe **how the system must end up**; §11 turns that into ordered
work packages (WP), each with files, steps and acceptance criteria.

**Builds on:** [HEADLESS_ACTIONS.md](../design/HEADLESS_ACTIONS.md),
[SERVER_DRIVEN_ACTION_FLOWS.md](../design/SERVER_DRIVEN_ACTION_FLOWS.md),
[UI_PORTS_FOR_ACTIONS.md](../design/UI_PORTS_FOR_ACTIONS.md),
[MIGRATION_ZK_SEPARATION.md](../backend/MIGRATION_ZK_SEPARATION.md) (epic #130).

**Out of scope:** binary compatibility between `dynamia-tools` versions. In this workspace tools is published as a SNAPSHOT
and everything is reinstalled together. Moving or renaming classes is allowed, so the architecture is not designed around it.

### Verified state of the branch (2026-10-10)

| Check | Result |
|---|---|
| `mvn -o -pl platform/ui/ui-shared,platform/core/actions,platform/core/crud,platform/app,platform/ui/zk -am test` | OK, exit 0. `ui-shared` 4, `actions` 47, `crud` 19, `app` 24, `zk` 34 tests, no failures |
| `pnpm exec vitest run` in `platform/packages/vue` | OK, 22 tests |
| Test in ZK or Vue in a browser | **Not done.** The ZK adapters (`ZK*Provider`) compile, but nobody has run them |
| ERP compilation against the branch | Not done |

---

## 1. Goal and invariants

**Goal:** a Java action of Dynamia Tools or the ERP is written once against neutral UI ports and behaves the same in ZK and
in a remote front end (Vue, POS, shop), without importing ZK. ZK becomes one more adapter.

Invariants the architecture must guarantee and the tests must check:

| # | Invariant |
|---|---|
| I1 | No action published to remote clients imports ZK, neither itself nor its ancestors. |
| I2 | An action behaves the same in every adapter: same callbacks, in the same order, with the same context (tenant, user, transaction). Only how it looks changes. |
| I3 | The core (`platform/core/*`, `ui-shared`) names no front end: no `ZK` in types, enums or annotations. |
| I4 | Nothing of arbitrary size travels in memory or inside JSON or tokens. Files move by streaming. |
| I5 | Publishing an action over REST is an **explicit decision, per concrete class**; it is never inherited. |
| I6 | If a port is used where there is no UI, the error is clear and immediate, not a `NullPointerException` inside ZK. |
| I7 | There is a single network protocol (`ActionFlowStep`); Java and TS implement it against the same fixtures. |
| I8 | There is **one source of truth** for each port and for the action catalog: it is defined once (Java) and everything else (TS types, steps, metadata) is derived from it or checked against it. |
| I9 | Every publishable action can be tested **without ZK, without a browser and without an HTTP server**, with the `test` platform (§10), and its test shows that it behaves the same in direct and remote execution. |

### 1.1 Unified development model (Capacitor style)

The DX must feel like **a single framework**, as in Capacitor. There you program against `@capacitor/camera`, each platform
(Android, iOS, web) ships its default implementation, and anything platform-specific is opt-in and isolated. Dynamia UI
adopts the same model:

| Capacitor | Dynamia UI | Where it lives |
|---|---|---|
| Plugin (single API: `Camera.getPhoto()`) | **Port** = facade + SPI + protocol step(s) + contract suite (`UIFiles`, `UIViews`, ...) | `ui-shared` (Java, source of truth) |
| Android / iOS implementation | **Platform adapter**: `zk` (stateful server) and `remote` (replay towards any client) | `platform/ui/zk`, `core/actions` + `crud` |
| Plugin mocks / test implementation | **`test` platform**: implements every port with a "user" script written in the test (§10) | `platform/testing/ui-testing` |
| Web implementation of the plugin | TS implementation of the same port (`ui-core/ports`), used by `ClientAction` and by the flow runner | `platform/packages/ui-core` |
| Native ↔ JS bridge | `ActionFlowStep` protocol + `/api/app/transfers` + `/api/app/jobs` | `core/actions`, `app`, `sdk` |
| `Capacitor.getPlatform()` | `UIPlatform.current().name()` → `"zk"`, `"remote"`, `"none"`, `"test"` | `ui-shared` (`UIEnvironment`, §3) |
| `Capacitor.isPluginAvailable('X')` | `UIPlatform.supports(UIFiles.class)` | `ui-shared` |
| App's own plugin | **Own port**: the developer declares `@UIPort interface DigitalSignature`, implements it in ZK and as a TS renderer of a `CUSTOM` step. This solves the HARD bucket without tying the action to ZK | app module |
| `android/`, `ios/` folders | Platform-specific code only in `*-zk` (or `ui`) modules and TS packages | §9 R1 enforces it |
| `capacitor.config` | `dynamia.ui.*` properties (files, tokens, jobs) | `application.yml` |

How it looks for whoever writes an action. It is what exists today, minus ZK:

```java
@InstallAction
@RunsOn(ActionRuntime.HEADLESS)                      // the only place where "multiplatform" is decided
public class ImportCustomersAction extends AbstractCrudAction {
    public void actionPerformed(CrudActionEvent evt) {
        UIFiles.uploadOne(".xlsx", file ->           // ZK: Fileupload · remote: UPLOAD step + transfers
            UIProgress.run(msg("importing"), monitor -> importer.importFrom(file.openStream(), monitor),
                    () -> { UIMessages.showMessage(msg("done")); evt.getController().doQuery(); },
                    e -> UIMessages.showMessage(e.getMessage(), MessageType.ERROR)));
    }
}
```

When something is **really** platform-specific there are two explicit exits, with no platform `if`s scattered around:

1. **Own port** (preferred): the logic stays a single action and only the widget changes per platform.
2. **Front end action** (`@RunsOn(FRONTEND)`): the action exists **once in the catalog** (id, name, icon, restrictions). ZK
   implements it in Java and Vue/POS implement it as a TS `ClientAction` **with the same id**. A front end without an
   implementation simply does not show it (`isPluginAvailable`). This is the case of the search box, the filters and the
   export of what the grid shows.

### 1.2 What is kept and what changes

| Kept as is | Changes because it is necessary |
|---|---|
| Static facades with callbacks (`UIMessages`, `UIViews`, `UIChoices`, `UIFiles`, `UIProgress`, `UINavigation`) and their names | Resolution through `UIEnvironment` instead of one bean per SPI (§3) |
| `LocalAction`/`CrudAction`, `@InstallAction`, ids, restrictions, `ActionGroup` | Publication by `@RunsOn` declared on the concrete class (§7) |
| Stateless replay (`ReplayExecutor`, `ReplaySession`, per-pass transactions, signed token) | Per-step fingerprint, retry on validation, token bound to user and tenant (§5) |
| `ActionFlowStep` as the only protocol; `runActionFlow` and its handlers | `UploadedFile`/downloads by streaming and refs (§6); runner moved to `ui-core` (WP9) |
| `ViewDescriptor` as the way to describe forms and views | `HeadlessViews` for any class with a descriptor, not only entities |
| `ClientAction`/`registerClientAction` in TS | Single catalog with a shared id for `FRONTEND` actions (§7.3) |
| Current ZK adapters (`ZK*Provider`) | Grouped in `ZKUIEnvironment` and required to meet the contract (§4, §9.3) |
| Architecture baselines that can only go down | Moved to an artifact reusable by the ERP (§9) |

### 1.3 Single source of truth

- **Ports:** each port is declared in Java with `@UIPort(name = "files", steps = {UPLOAD})` on its SPI. A build step
  (`ui-contract-generator`, in `platform/contract`) generates from there the JSON Schema of the steps, the TS types of
  `sdk` (`ActionFlowStep`, `FlowFileRef`...) and the port interfaces of `ui-core/ports`. The fixtures (§9.4) are the safety
  net. This closes design question Q1: Java is the source.
- **Actions:** the catalog is that of `@InstallAction`. Each entry carries `runtime`, and no TS action exists without a
  catalog entry, except purely local actions of the client app, which do not go through the server.
- **Forms and views:** `ViewDescriptor`, as today.

---

## 2. Overview

```
                         Java action (LocalAction / CrudAction)
                                       │  only uses facades
                                       ▼
   ui-shared   UIMessages · UIViews · UIChoices · UIFiles · UIProgress · UINavigation
                                       │  UIFacades.port(Spi.class)
                                       ▼
                               active UIEnvironment
              ┌────────────────────────┼─────────────────────────┬──────────────────┐
              ▼                        ▼                         ▼                  ▼
     ZKUIEnvironment          ReplayUIEnvironment          NoUIEnvironment     TestUIEnvironment
  (platform/ui/zk; active   (core/actions; created by    (ui-shared; jobs,   (ui-testing; user
   only with a ZK Execution)  ReplayExecutor per pass)    threads, no UI)      script, §10)
              │                        │
        ZK widgets             ActionFlowStep (JSON) ──► ui-core/flow (TS, framework-free)
                                       │                         │
                               TransferStore ◄── /api/app/transfers ──► Vue / POS / shop adapter
```

Three cross-cutting axes:

- **Classification and publication** (§7): `ActionRuntime` decides what is published and what is executed.
- **Execution context** (§8): tenant, user, transaction and bindings travel to any thread the adapter uses.
- **Executable architecture rules** (§9): the same for tools and for the ERP.

---

## 3. Facade core: `UIEnvironment`

### 3.1 What changes and why

Today each SPI is resolved separately (`UIFacades.resolve(Spi.class)`): first the per-execution binding and then a container
bean. This leaves three problems:

- `ReplayExecutor` hand-nests 5 bindings and `ReplayBinder` only covers `ViewsProvider`.
- In the hybrid ERP, a call outside ZK and outside replay (a job, a `RemoteAction`, the Kotlin `/api/v2` facade) resolves the
  ZK bean and fails inside ZK. Today only `ZKNavigationProvider` checks it.
- In a deployment without ZK, that same call yields `IllegalStateException("... not found")` without saying what was done wrong.

### 3.2 Design

```java
// ui-shared, tools.dynamia.ui
public interface UIEnvironment {
    String name();                                   // "zk", "replay", "none", "test"
    <S> Optional<S> port(Class<S> spi);              // empty = this environment does not support that port
}

public final class UIFacades {
    // 1. the environment bound to the execution (ScopedValue), if any
    // 2. otherwise, the first UIEnvironmentProvider in the container whose isActive() is true
    //    (ZK: Executions.getCurrent() != null)
    // 3. otherwise, NoUIEnvironment
    public static UIEnvironment current();
    public static <S> S port(Class<S> spi);          // throws UIUnavailableException with environment + port + hint
    public static <R> R with(UIEnvironment env, Supplier<R> work);
}

public interface UIEnvironmentProvider {             // bean; ZK registers one
    boolean isActive();
    UIEnvironment environment();
}
```

- `UIUnavailableException` (unchecked) carries a message such as: `"UIFiles.download used with no UI (environment
  'none'): run it from a ZK event or from a replayed action, or move it out of background code"`.
- `NoUIEnvironment` only implements `MessageDisplayer`, which records messages in the log. Any other port throws
  `UIUnavailableException`.
- `ReplayUIEnvironment` groups all headless ports of a pass in a single object, so there is **one binding**. Ports contributed
  by other modules (for example `HeadlessViews` from `crud`) are contributed through `ReplayPortContributor` (replaces
  `ReplayBinder`): `Map<Class<?>, Object> ports(ReplaySession)`.
- `UIMessages` stops caching the `MessageDisplayer` statically: it resolves it through `UIFacades.port`, like the others.
- ZK providers stop being loose beans with `@ConditionalOnMissingBean`. `ZKAppConfiguration` registers a single
  `ZKUIEnvironmentProvider`; whoever wants to replace a concrete ZK port does so with a bean that wraps that environment.

---

## 4. Semantic contract of each port

This table is **normative**: both adapters (ZK and replay) must meet it, and each row becomes a case of a contract suite (§9.3).

| Port | Common rule (ZK = replay) |
|---|---|
| All | The callback is **the only continuation point**. Code after the facade call in the action body cannot depend on the answer. |
| All | Cancelling never calls the success callback. If the signature has `onCancel`, that one is called. |
| All | The callback runs with the same tenant, user and bindings as the action (§8). |
| `UIMessages.showQuestion` | Yes → `onYes`; No or close → `onNo` if present. |
| `UIMessages.showInput` | Value converted to `valueClass`; if it cannot be converted, it is asked again with the error (in replay: the same step, with `message`). |
| `UIViews.showForm` | `onSubmit(bean, dialog)`. If `onSubmit` throws `ValidationError`, **the form stays open with the submitted values and the message**, in both adapters (§5.2). `dialog.close()` is what closes it; if `onSubmit` returns without closing it, it stays open in ZK and is shown again in replay. |
| `UIViews.showView` | Shown and closed; no callback (or optional `onClose`). Presentation hints (`width`, `height`, collections as tables) come from `ViewOptions`/descriptor and both adapters interpret them. |
| `UIChoices` | Options are identified by a **stable key** (§5.3), not by position. Choosing nothing equals cancelling: the callback is not called, not even in multiple mode. |
| `UIFiles.upload` | The callback receives streaming handles (§6). `accept`, `maxFileSize`, `maxFiles` and `maxTotalSize` are enforced on the server in both adapters. |
| `UIFiles.download` | Streaming source; no fixed in-memory limit. Name and MIME type reach the user. |
| `UIProgress.run` | `run` **returns control without waiting** in both adapters; the continuation goes in `onFinish`/`onError`. The task runs **without an ambient transaction** and with the propagated context (§8). **No UI facade can be used inside the task** (throws `UIUnavailableException`); progress is reported with `ProgressMonitor`. On error, `onError` receives the exception and whatever the task did is **not committed** if it ran in its own transaction. |
| `UINavigation.open` | Terminal in practice: whatever the action does afterwards runs, but the user is already leaving. In replay, the final `REDIRECT` keeps the notifications and they are shown before navigating. |

---

## 5. Replay runtime (headless)

`ReplayExecutor` and `ReplaySession` stay as the base. These are the changes.

### 5.1 Fingerprint of each interaction

Each stored answer carries the fingerprint of the step it answered: `answers: [{fp, value}]`, with
`fp = hash(type, viewDescriptor|viewClass, title, option keys)`. On resume, if the fingerprint of step n does not match the
one of the step the action raises now, **the answer is discarded and the step becomes pending again**. The client receives
the same step with `messageType=WARNING` and the localized message `flow.stepChanged`. This way a non-deterministic action,
or data that changed between passes, never applies an answer to a different question.

### 5.2 Retry on validation

`ReplaySession.interact` receives an `onAnswer` that may throw `ValidationError`. In that case the session:

1. Rolls back the pass (`ReplayTransactions` already commits only when nothing is pending; the pending one becomes this step).
2. Removes the answer from `answers`.
3. Emits the same step again with the submitted values in `data` and the error in `message`/`messageType=ERROR`
   (`fieldErrors` if the `ValidationError` carries a field).

`HeadlessViews` drops `CLIENT_CLOSES`: `ViewDialog.close()` marks the step as finished, and if `onSubmit` returns without
closing, the same re-emission rule applies. Vue (`FormDialogHost`) shows `message` and the `fieldErrors` in the form.

### 5.3 `CHOICE` with keys

```java
public record ChoiceOptions<T>(String title, List<T> options, Function<T, String> label,
                               Function<T, String> key, boolean multiple) { }
```

- Default `key`: the entity id (`DomainUtils.findEntityId`) if there is one; otherwise `label`.
- Step: `data.options = [{key, label}]`. Answer: list of keys. A key that no longer exists → re-emission (§5.1).
- `UIChoices` exposes overloads with `key`.

### 5.4 Resume token

- **Bound to user and tenant.** Adds the `FlowPrincipal` SPI (`String subject(); String tenant();`), implemented by
  `security`/`saas`; without an implementation, `"anonymous"`/`null`. `verify` compares both.
- **Mandatory secret in production.** If `dynamia.actions.flow.secret` is missing, the per-JVM random secret breaks tokens
  on restart and across nodes. With the `prod` profile, or with `dynamia.actions.flow.require-secret=true`, startup fails.
- **Payload size cap** (`dynamia.actions.flow.max-token-bytes`, default 16 KB). If exceeded, a clear error is thrown. With
  §6 files no longer travel inside the token, so only a huge form could exceed it.
- The token is signed only, not encrypted. Since it no longer carries files, it is enough to document that it must not be logged.

### 5.5 Transactions

- A pass that ends with an exception always rolls back, including exceptions the action captures through the default
  `onError` of `UIProgress` (see §4).
- `ReplayProgressRunner` runs the task with the pass transaction suspended (`ReplayTransactions.runOutside`). It is the same
  rule as in ZK: the task manages its own transactions.

### 5.6 Asynchronous progress (phase 2 of this axis)

The first version may keep running the task inside the request, but the final shape is:

- `PROGRESS {jobId, title}` step. The task runs in `SchedulerUtil` with the propagated context (§8).
- The client polls `GET /api/app/jobs/{jobId}` (`{state, current, max, message}`).
- When the task ends, the client answers the step with `{jobId, state}`. The next pass consumes that answer and calls
  `onFinish`/`onError` **without running the task again**: the `jobId` is in `answers` and the fingerprint identifies it.
- This removes `markNonRepeatable`: the task no longer runs inside a pass.

---

## 6. File transfer (full redesign)

### 6.1 Why it has to be redone

Today `UploadedFile` is `byte[]`, uploads travel as Base64 inside the response JSON **and inside the resume token**, and
downloads travel as Base64 inside `params.downloads`. The limits are 1 MB up and 10 MB down. The ERP configures in ZK
`max-upload-size` = 102400 KB (100 MB; `erp-boot/.../zk.xml:36`), so this design does not serve its real cases (Excel
importers, PDFs, images).

### 6.2 Model

```java
// ui-shared, tools.dynamia.ui.files
public interface UploadedFile {
    String name();
    String contentType();          // may be null
    long size();
    InputStream openStream();      // can be opened several times while the handle is alive
    default Path toTempFile() { ... }   // streaming copy if a File is needed
}

public sealed interface DownloadSource permits BytesSource, PathSource, StreamSource {
    String name(); String contentType(); OptionalLong size();
}
// StreamSource(name, contentType, size, Supplier<InputStream> opener)

public record UploadOptions(String title, String accept, int maxFiles, long maxFileSize, long maxTotalSize) {
    // defaults: dynamia.ui.files.max-file-size (100MB), max-files (10)
}
```

Facade:

```java
UIFiles.download(String name, String contentType, byte[] content);    // small ones; becomes a BytesSource
UIFiles.download(Path file, String contentType);                       // streaming
UIFiles.download(String name, String contentType, Supplier<InputStream> opener);
UIFiles.upload(UploadOptions, Consumer<List<UploadedFile>>);
UIFiles.uploadOne(String accept, Consumer<UploadedFile>);
```

### 6.3 Temporary store: `TransferStore`

```java
// ui-shared (interface) + local implementation in platform/app
public interface TransferStore {
    TransferRef put(InputStream data, TransferMeta meta, long maxBytes);    // cuts and deletes if maxBytes is exceeded
    Optional<StoredTransfer> get(String id, FlowPrincipal owner);           // checks owner and expiration
    void delete(String id);
    void purgeExpired();                                                     // called by a @Scheduled
}
// TransferMeta(name, contentType, direction UPLOAD|DOWNLOAD, owner subject+tenant, expiresAt)
```

- Default implementation `LocalTransferStore`: directory `dynamia.ui.files.dir` (default
  `${java.io.tmpdir}/dynamia-transfers`), one data file and one metadata file per id. TTL in `dynamia.ui.files.ttl`
  (default `PT30M`) and total quota in `dynamia.ui.files.quota`.
- With several nodes behind a load balancer a shared store is needed (S3 or a shared FS). The interface allows it; the
  implementation is left for when it is needed (see §12).
- All writes stream (`InputStream.transferTo` over a `BoundedInputStream` that cuts at `maxBytes`). Never `readAllBytes`.

### 6.4 Endpoints (`platform/app`, `TransfersController`)

| Method | Route | Body / response |
|---|---|---|
| `POST` | `/api/app/transfers` | Raw `application/octet-stream` body, with headers `X-File-Name` (URL-encoded), the file's `Content-Type` in `X-File-Type` and `Content-Length`. Streams straight to the store, **bypassing Spring multipart** (so `spring.servlet.multipart.max-file-size`, 10 MB in the ERP, does not affect it). Responds `{ref, name, contentType, size}`, where `ref` is a signed id bound to `FlowPrincipal`. Limit: `dynamia.ui.files.max-file-size`. |
| `GET` | `/api/app/transfers/{ref}` | Streaming with `Content-Disposition: attachment; filename*=UTF-8''...`, `Content-Length` and `Cache-Control: no-store`. Owner only; downloads are deleted after the first complete read or on expiry. |
| `DELETE` | `/api/app/transfers/{ref}` | The client cancels an upload. |

Security: they require authentication (the same filter chain as `/api/app/metadata`), check the owner (`subject` + `tenant`),
and the server rejects `accept` and sizes outside `UploadOptions` when the reference is consumed, not only on the client.

### 6.5 Upload flow in replay

```
action: UIFiles.upload(opts, cb)
  └─ ReplayFileTransfer: UPLOAD step {title, accept, maxFiles, maxFileSize, maxTotalSize}  (pending)
client: picks files → POST /api/app/transfers for each one (with progress) → answers the step with [{ref}]
server (next pass): answers = [{fp, value:[{ref}]}]   ← only references in the token
  └─ ReplayFileTransfer resolves each ref in TransferStore (owner, expiry, accept, sizes)
     └─ cb(List<UploadedFile>) with StoredUploadedFile handles that open the file in the store
when the flow ends successfully (after commit): consumed refs are deleted; if abandoned, the TTL cleans them
```

### 6.6 Download flow in replay

`ReplayFileTransfer.download(source)` writes the source to the store by streaming (direction `DOWNLOAD`) **only in the pass
that ends** (the one that is not left pending), and the registration happens after the commit. The final response carries
`params.downloads = [{name, contentType, size, url}]`, with `url` relative to `/api/app/transfers/{ref}`. The client downloads
with a link or `fetch`; the browser does the streaming and there is no Base64 in the JSON.

So that the source is not read in passes that are later discarded, `ReplaySession.download` stores the `DownloadSource`
(not the bytes) and materializes it at the end.

### 6.7 ZK adapter

- **Upload:** ZK's `Fileupload` with `accept` and `maxFiles` (use the `Fileupload.get` API with parameters/`accept` offered by
  the project's ZK version, or a `Button` with `upload="true,maxsize=...,multiple=...,accept=..."`). The `Media` is wrapped in
  `ZKMediaUploadedFile`: if `isBinary()`, `getStreamData()`; otherwise `getReaderData()` encoded with the media charset (fixes
  the text case that `Uploadlink` handles today and `ZKFileTransfer` does not). If the media is in memory, it is dumped to a
  temp file by streaming. Sizes and `accept` are validated on the server with the same validation class as replay
  (`UploadPolicy`, in `ui-shared`).
- **Download:** `Filedownload.save(InputStream|File, contentType, name)`; never `byte[]` except for `BytesSource`.

### 6.8 TS side

- `sdk`: types `FlowFileRef {ref, name, contentType, size}`, `FlowDownload {name, contentType, size, url}` and the client
  `client.transfers.upload(file, {onProgress, signal})` (XHR or `fetch` with `Blob` streaming; no Base64) and
  `client.transfers.download(url)`.
- `ui-core/flow`: the `UPLOAD` step calls `handlers.pickFiles` (which returns `File[]`), validates `accept`/sizes on the client
  for quick feedback, uploads each file and answers with the refs. The downloads step uses `handlers.saveFile(download)`; by
  default, an `<a href=url download>` over the authenticated URL (if authentication is by header and not cookie, it is fetched
  as a `Blob` with `fetch` and `URL.createObjectURL` is used).
- `FlowUploadedFile` with Base64 `content` is removed.

---

## 7. Action classification and publication

### 7.1 Neutral vocabulary

```java
public enum ActionRuntime {
    HEADLESS,      // written against the facades; the server runs it by replay
    FLOW,          // FlowRemoteAction
    REMOTE,        // single-request RemoteAction
    FRONTEND,      // behaviour specific to each front end (search box, filters, export of what the grid shows,
                   // ZK screens). It is in the catalog but has no endpoint; each front end provides its
                   // implementation with the same id (ZK in Java, Vue/POS as a TS ClientAction), or does not show it
    UNDECLARED     // nobody declared it: it is not published
}
```

`ZK_ONLY` disappears from the core. ERP actions that really are ZK screens are declared `FRONTEND`; the difference between
"ZK implements it" and "Vue implements it" is known by each front end, not by the core.

### 7.2 One declaration rule, not inheritable

- The declaration is `@RunsOn(ActionRuntime.X)` **on the concrete class**. `ActionRuntimes.of(Class)` reads
  `getDeclaredAnnotation`, not `getAnnotation`, so a subclass does not inherit `HEADLESS`.
- Permitted derivation, only for types whose contract already implies the runtime: `FlowRemoteAction` → `FLOW` and
  `RemoteAction` → `REMOTE`.
- `HeadlessCapable` becomes `@Deprecated`. While it exists, it counts as `HEADLESS` **only if the concrete class declares it
  in its own `implements`** (`Arrays.asList(c.getInterfaces()).contains(HeadlessCapable.class)`). `headlessSupported()`
  keeps working as a dynamic veto.
- Result for the ERP: `VerVentaAction extends ViewDataAction` becomes `UNDECLARED` (not published) until someone reviews and
  declares it.
- `HeadlessCrudRemoteAction` reports the runtime of its delegate; the special case with a qualified name in `ActionMetadata`
  goes away.

### 7.3 Who uses the runtime

| Place | Rule |
|---|---|
| `ApplicationMetadataLoader` | Includes in the catalog the `HEADLESS`, `FLOW` and `REMOTE` actions (with `endpoint`) and the `FRONTEND` ones (without `endpoint`), provided they are applicable and pass the user's restrictions. `UNDECLARED` never appears. It stops looking at `HeadlessCapable` directly. |
| `ApplicationMetadataController.executeAction` | Executes only `HEADLESS`, `FLOW` and `REMOTE`. Any other action gets 404, even if someone knows its id (defence in depth). |
| `ActionMetadata.runtime` | Always present. The SDK types it with the new enum. |
| Front end | For a `FRONTEND` action, looks up a `ClientAction` registered with the same id; if not found, does not show it. For the rest, uses the flow runner. |

### 7.4 Global actions endpoint

`executeGlobalAction` (`ApplicationMetadataController.java:150`) receives `ActionExecutionRequest` **without `@RequestBody`**
(already so on `next`). Spring binds it as a model attribute and the JSON body is ignored: a global flow cannot be resumed
because `resumeToken` travels in the body. `@RequestBody` must be added, with a test that resumes a global action.

---

## 8. Execution context

### 8.1 Problem

`SchedulerUtil.getWithContext` propagates `ObjectsContext`, but not the tenant (`AccountTenants`, a `ScopedValue`), nor
Spring Security, nor the `UIFacades` bindings. The Javadoc of `AccountTenants` already warns: *"code that hands work to an
executor must bind the tenant again inside the task"*. `ZKProgressRunner` → `LongOperation` → `SchedulerUtil.run`, so in the
ERP the task of a `UIProgress` in ZK runs without tenant and, being fail-closed, sees nothing.

### 8.2 Design

```java
// platform/core/integration, tools.dynamia.integration.context
public interface ContextPropagator {            // bean; each module contributes its own
    Object capture();                           // on the launching thread
    <T> T runWith(Object captured, Supplier<T> work);   // on the executing thread
}
public final class ExecutionContext {
    public static Snapshot capture();           // ObjectsContext + all ContextPropagators
}
```

| Propagator | Module | What it propagates |
|---|---|---|
| `ObjectsContextPropagator` | `integration` | What `ObjectsContext.capture()` does today |
| `UIEnvironmentPropagator` | `ui-shared` | **Replaces it with `NoUIEnvironment` inside the task** (rule of §4: no UI inside the task) |
| `TenantPropagator` | `saas` | The effective `AccountTenants` (forced, from the request or from the session), re-bound with `AccountTenants.with` |
| `SecurityContextPropagator` | `security` (or `app` if Spring Security is there) | `SecurityContextHolder` |

`SchedulerUtil.getWithContext` switches to `ExecutionContext.capture().wrap(task)`. `LongOperation`, and therefore
`ZKProgressRunner`, inherit it with no changes of their own. ZK's `onFinish`/`onError` return to the desktop event thread (as
today), where the UI is available.

---

## 9. Executable architecture rules

### 9.1 Reusable artifact

`RepoSources`, `ZkCornerRuleTest` and `ActionInventoryTest` move out of `core/actions/src/test` into a new module
`platform/testing/arch-rules` (artifact `tools.dynamia.arch-rules`, `scope test`), parameterised by repo root, ZK corner
patterns and baseline files. The ERP uses it from its own test module with its baselines.

### 9.2 Rules

| Rule | Detail |
|---|---|
| R1 Front end corner | `org.zkoss` / `tools.dynamia.zk` only in the corner modules. Detects `import`, **fully qualified names in the body**, and Maven dependencies with `groupId org.zkoss` or `artifactId` `tools.dynamia.zk`/`zk-starter`. The `extensions/*/sources/ui` modules stop being a corner as a block: the corner is the classes that need it (or `*-zk` modules when they are split). |
| R2 Inventory | Baseline of ZK-bound actions that can only go down (as today). |
| R3 Publication | No class with a publishable runtime (§7.2) may be ZK-bound, including its ancestors. |
| R4 Declaration | Baseline of `UNDECLARED` actions that can only go down. |
| R5 Vocabulary | No type in `platform/core/**` or in `ui-shared` contains `ZK`/`Zk` in its name, constants or annotations. |

Inheritance is resolved by fully qualified name (package + parent `import`), not by simple name.

### 9.3 Contract suites per port

In the `ui-shared` test-jar: `MessagesPortContract`, `ViewsPortContract`, `ChoicesPortContract`, `FilesPortContract`,
`ProgressPortContract` and `NavigationPortContract`. They are abstract classes with a `driver()` method that knows how to
"answer as the user" in that adapter. Replay extends them in `core/actions` and `crud`, and ZK extends them in
`platform/ui/zk` with ZATS or a simulated desktop. If ZATS is not available, the ZK driver may directly invoke the listeners
the adapter registers. Each row of the §4 table is a case.

### 9.4 Protocol fixtures

`platform/contract/fixtures/*.json`: one example per `ActionFlowStep` type (including `UPLOAD` with refs, `CHOICE` with keys
and `DIALOG` with errors) and per answer. The Java tests (Jackson) and the TS ones (vitest) parse and re-serialise them and
must get the same result.

---

## 10. Testing platform (third port)

### 10.1 Why a platform and not a mock

Today the ERP's actions are not tested. There are 12 test files across all `*-ui` modules and none runs an action. The
`erp-integration-tests` module (real MySQL with Testcontainers) **depends on no `*-ui`**, and `AccionesAdminIT` says so in its
own Javadoc: actions use `UIMessages` (ZK) and do not run headless, so the test **reproduces the pattern and JPQL of
`DesvincularTodosItemsInventarioAction` instead of running the action**. So the test can pass while the real action is broken.

With the model of §1.1, testing needs no tricks: it is **one more platform**, at the same level as ZK and remote. It is like
Capacitor, where plugins are tested with their web implementation or official mocks, without a phone. The `test` platform
implements **all** ports, meets the same contract (§4, checked by the same suites of §9.3) and lets the test drive the "user".

| Platform | Who answers the user | What for |
|---|---|---|
| `zk` | A person in the ZK browser | Production (back office) |
| `remote` | The client (Vue, POS, shop) through the step protocol | Production (front ends) |
| `test` | **A script written in the test** | Unit and integration tests of actions, without ZK or a browser |

### 10.2 Artifact

`platform/testing/ui-testing` → `tools.dynamia.ui.testing` (`scope test`). Depends on `ui-shared`, `actions` and `crud`
(headless). **It does not depend on ZK or Spring**; Spring integration is optional (§10.5).

Components:

| Class | What it does |
|---|---|
| `TestUIEnvironment` | `UIEnvironment` named `"test"` that implements all ports. Records each interaction in order and gets the answer from the script. |
| `UIScript` | The "user": answers in order (`confirm(true)`, `input("x")`, `fillForm(Map)`, `submitForm(b -> …)`, `cancel()`, `choose(key)`, `upload(TestFiles.of(...))`) or by rules (`whenQuestion(containing("Void")).confirm(true)`). |
| `UIInteraction` | Record of an interaction: `type` (`CONFIRM`, `INPUT`, `DIALOG`, `VIEW`, `CHOICE`, `UPLOAD`, `NOTIFY`, `PROGRESS`, `REDIRECT`), `title`, `message`, `messageType`, `payload` (form values, option labels and keys, upload options). |
| `TestFiles` | Creates `UploadedFile` by streaming from a classpath resource, a `Path` or bytes. Downloads are kept as `CapturedDownload` (`name`, `contentType`, `size`, `openStream()`, `asString()`, `saveTo(Path)`). |
| `InMemoryTransferStore` | `TransferStore` for the tester's remote mode. |
| `TestCrud` | Builds a `CrudActionEvent` with `HeadlessCrudController` and `HeadlessCrudView` for an entity class and a `CrudService` (real or mock). Exposes what happened: `queried()`, `saved()`, `state()`. |
| `ActionTester` | Entry point (§10.3). |
| `ActionResult` | Interactions, notifications, downloads, redirect, exception, CRUD state and, in remote mode, the steps and the token size. |

Script rules:

- **Strict by default.** If the action raises an interaction the script does not answer, the test fails with a message
  showing the pending interaction (`"Unanswered CONFIRM 'Void sale 123?' at interaction #2"`). If answers are left over at
  the end, it also fails. `lenient()` turns it off.
- If `onSubmit` throws `ValidationError`, the form is raised again with the error, as in ZK and remote (§4), and the script
  can answer again. This is how the validation path is tested.
- `UIProgress` runs without an extra thread but **applies the contract**: no UI inside the task (`NoUIEnvironment`), no
  ambient transaction and with the propagated context (§8). `asyncProgress()` runs it on another thread to test continuations.

### 10.3 Two execution modes: this is how an action is shown to be multiplatform

```java
ActionResult r = ActionTester.of(voidSaleAction)
        .crud(Sale.class, crudService)          // TestCrud: HeadlessCrudController + HeadlessCrudView
        .on(sale)                               // data of the CrudActionEvent
        .asAccount(accountId)                   // tenant (via saas ContextPropagator)
        .user(u -> u.confirm(true).input("Customer returned it").confirm(true))
        .run();                                 // DIRECT mode

assertThat(r.types()).containsExactly(CONFIRM, INPUT, CONFIRM, NOTIFY);
assertThat(r.crud().queried()).isTrue();
```

| Mode | How it runs | What it shows |
|---|---|---|
| `run()` / `DIRECT` | Immediate callbacks, as in ZK | The action's logic |
| `runRemote()` | Through the real `ReplayExecutor`: one pass per answer, signed token, per-pass transactions, `InMemoryTransferStore` | That it works for Vue/POS: it is deterministic, the fingerprints match and the token is within the cap |
| `runEverywhere()` | Both, and compares the interactions and the effects | **The action is multiplatform.** It is the acceptance condition of any action declared `HEADLESS` |

The `zk` mode is not tested per action: the contract suites of the ZK adapter cover it (§9.3). If the adapter meets the
contract and the action passes `runEverywhere()`, the action works in ZK.

### 10.4 What must be designed into actions to make them testable

- **Actions depend on `CrudControllerAPI`, not on ZK controllers.** Today many ERP controllers extend
  `tools.dynamia.zk.crud.CrudController` (`VentaCrudController`, `ClienteCrudController`, `OrdenMesaCrudController`, among
  others) and the actions convert them with casts. The business logic of those controllers moves to services or to a neutral
  `CrudControllerExtension`, and the ZK controller only delegates. If an action casts to a ZK controller, it is bound to ZK
  (rule R1 with cast detection, §9.2).
- **Dependencies by constructor or Spring**, not `Containers.get()` inside `actionPerformed`. `ActionTester` supports both
  (a `SimpleObjectContainer` in the test), but the constructor eases unit tests with mocks.
- **Texts through `Messages`/`ClassMessages`.** The script can use rules by message key
  (`whenQuestion(key("confirmVoid"))`) as well as by text, so tests do not break when a translation changes. For that,
  `UIInteraction` keeps the key when the action uses `msg(...)`.

### 10.5 JUnit 5 and Spring integration

- `@DynamiaUITest` (JUnit 5 extension): binds a clean `TestUIEnvironment` per test and injects `ActionTester` and `UIScript`
  as parameters. Without Spring it uses a `SimpleObjectContainer`; with `@SpringBootTest` it uses the context (the tester
  gets the action with `tester.action(VoidSaleAction.class)`, which creates it as a prototype like the app does).
- In `erp-integration-tests`: with the `*-ui` → `*-actions` split (WP10), the module depends on the `*-actions` and runs the
  **real actions** against MySQL. `AccionesAdminIT` stops copying the JPQL and calls
  `ActionTester.of(unlinkAllItems).asAccount(a).user(u -> u.confirm(true)).run()`.
- Fast unit tests per module: `module-x/actions/src/test` with `ActionTester` and a mock `CrudService` (Mockito).

### 10.6 TS side

`ui-core/testing` offers the same idea for front ends: `createTestPorts(script)` implements the TS ports (those of §1.3) with
a script, and `runFlowScripted(client, action, script)` drives the flow runner against a simulated `DynamiaClient` or against
a test server that replays the fixtures of §9.4. It serves to test Vue, POS and shop `ClientAction`s without a browser.

### 10.7 Metric

`ActionInventoryTest` publishes, besides the ZK-bound actions, how many `HEADLESS` actions have at least one test with
`runEverywhere()`. The baseline of "published without a multiplatform test" can only go down. This makes "testable" part of
the definition of done of a migrated action.

---

## 11. Implementation plan

Rules for all packages: branch `feature/next-ui` of `dynamia-tools` (check with `git branch --show-current`). Code, Javadoc
and commits in English. No commit or push unless the user asks. Each package ends with `mvn -o -pl <modules> -am test` green
(and `pnpm exec vitest run` if it touches TS) and with this document updated in its *Implementation status* section.

### WP1 · `UIEnvironment` and clear errors (§3)

- **Files:** `ui-shared/.../UIFacades.java`, `UIMessages.java` and new `UIEnvironment`, `UIEnvironmentProvider`,
  `NoUIEnvironment`, `UIUnavailableException`; `core/actions/.../replay/ReplayExecutor.java`, `ReplayBinder` →
  `ReplayPortContributor`, new `ReplayUIEnvironment`; `crud/.../headless/HeadlessViewsBinder` → contributor;
  `zk/ZKAppConfiguration.java`, new `zk/ui/ZKUIEnvironmentProvider`.
- **Also:** `UIPlatform` (`current()`, `supports(Class)`) as the public facade of `UIEnvironment`, and the `@UIPort` annotation
  on each existing SPI (`MessageDisplayer`, `ViewsProvider`, `ChoicesProvider`, `FileTransfer`, `ProgressRunner`,
  `NavigationProvider`). The §1.3 generator goes in WP7.
- **Acceptance:** `ReplayExecutor` makes a single `UIFacades.with(env, ...)`; a facade called outside ZK and outside replay
  throws `UIUnavailableException` with the port and environment names; `UIMessages` without static cache; current tests green,
  plus new resolution tests (bound > active provider > none).

### WP1b · Testing platform (§10) — right after WP1

- **Files:** new module `platform/testing/ui-testing` (`TestUIEnvironment`, `UIScript`, `UIInteraction`, `TestFiles`,
  `CapturedDownload`, `TestCrud`, `ActionTester`, `ActionResult`, `@DynamiaUITest` extension). Move `RecordingDisplayer` from
  the `ui-shared` tests to this platform. Rewrite the existing headless tests of `crud` (`SaveAction`, `DeleteAction`) and of
  the already converted actions (`ExportReportAction`, `ImportReportAction`, `NewAccountPaymentAction`,
  `MoveEntityFileLocalToRemoteStorageAction`, `ViewDataAction`) with `ActionTester`.
- **Order:** `run()` mode goes with WP1. `runRemote()`/`runEverywhere()` are enabled with what exists of replay and grow with
  WP4 (files by ref, `InMemoryTransferStore`) and WP5 (fingerprints, re-emission). The `test` platform also extends the WP6
  contract suites.
- **Acceptance:** each converted tools action has a green `runEverywhere()` test; an incomplete script fails with the pending
  interaction message; a `ValidationError` in `showForm` can be answered again from the script; the module has no ZK
  dependencies (rule R1).

### WP2 · Classification and publication (§7)

- **Files:** `ActionRuntime` (with `FRONTEND`; remove `ZK_ONLY`), `ActionRuntimes` (declared and not inherited), `RunsOn`,
  `HeadlessCapable` (`@Deprecated`), `ActionMetadata`, `ApplicationMetadataLoader:149`, `ApplicationMetadataController` (filter
  in `executeAction`, `@RequestBody` in `executeGlobalAction`), `HeadlessCrudRemoteAction`, `sdk/src/metadata/types.ts`. Change
  `@RunsOn(ZK_ONLY)` → `FRONTEND` in `FindAction`, `FiltersAction`, `SaveConfigAction` and `Export*Action`. Add
  `@RunsOn(HEADLESS)` where `HeadlessCapable` is today.
- **Single catalog:** `FRONTEND` actions appear in the metadata without `endpoint`. In `ui-core`, the `ClientAction` registry
  resolves by catalog id, and the UI hides `FRONTEND` actions without a local implementation.
- **Acceptance:** test where a subclass of a `HEADLESS` action without declaration yields `UNDECLARED` and does not appear in
  the metadata; `FindAction` appears as `FRONTEND` without endpoint; the endpoint returns 404 for a `FRONTEND` action; test
  resuming a global action with a JSON body.

### WP3 · Execution context (§8)

- **Files:** `integration/.../context/ContextPropagator`, `ExecutionContext`, `SchedulerUtil.getWithContext`;
  `UIEnvironmentPropagator` in `ui-shared`; `TenantPropagator` in `extensions/saas/sources/core`; `SecurityContextPropagator`
  where Spring Security lives.
- **Acceptance:** test in `saas` where a `SchedulerUtil.run` launched inside `AccountTenants.with(5L, ...)` sees tenant 5; test
  where a facade inside a `SchedulerUtil` task throws `UIUnavailableException`.

### WP4 · Streaming files (§6)

- **Files:** `ui-shared`: `UploadedFile` (interface), `DownloadSource`, `UploadOptions`, `UploadPolicy`, `TransferStore`,
  `TransferRef`, `TransferMeta`, `FlowPrincipal` (or in `actions` if preferred next to the token); `app`: `LocalTransferStore`,
  `TransfersController`, scheduled purge, `dynamia.ui.files.*` properties; `actions`: `ReplayFileTransfer`, `ReplaySession`
  (downloads as sources, delete after commit), `ActionFlowStep.upload(...)` with limits and `ReplayExecutor`
  (`params.downloads` with URL); `zk`: `ZKFileTransfer` (`ZKMediaUploadedFile`, text/binary, `accept`, limits); actions that use
  `f.content()` today: `ImportReportAction`; `sdk`: `transfers` and types; `vue`/`ui-core`: `UPLOAD` step and downloads.
- **Acceptance:** `TransfersController` test with a 50 MB file generated by streaming without `OutOfMemory` and with a limited
  test heap (`-Xmx256m` in that test's surefire); an upload over the limit → 413 and nothing in the store; another
  user's/tenant's ref → 404; complete replay flow upload → confirm → callback, with a token under 2 KB; final download with
  `params.downloads[].url` and no Base64; purge by TTL; vitest test of the `UPLOAD` step with a simulated `fetch`.

### WP5 · Robust replay (§5.1 to §5.5)

- **Files:** `ReplaySession` (fingerprint, re-emission, `ValidationError`), `ReplayExecutor` (`{fp, value}` format, rollback on
  error), `FlowTokenSigner` (`FlowPrincipal`, mandatory secret, size cap), `HeadlessViews` (explicit close, re-emission with
  errors), `ChoiceOptions`/`UIChoices`/`ReplayChoicesProvider`/`ZKChoicesProvider` (keys; empty multiple = cancel),
  `ReplayProgressRunner` (outside the transaction), `vue/FormDialogHost.vue` (message and per-field errors), `runActionFlow`
  (`CHOICE` with `{key,label}`).
- **Acceptance:** tests that options changing between passes → re-emission; `ValidationError` in `onSubmit` → same `DIALOG`
  with the error and the submitted values; another user's token → 401; startup with `require-secret` and no secret → fails.

### WP6 · Per-port contract and ZK adapter (§4, §9.3)

- **Files:** abstract suites in the `ui-shared` test-jar; driver implementations in `actions`/`crud` (replay) and `zk`;
  adjustments to the `ZK*Provider`s to meet the §4 table (`ZKProgressRunner` without UI inside the task, `ZKViewsProvider`
  unchanged apart from `onClose`, `showView` hints moved to `ViewOptions`/descriptor).
- **Acceptance:** all rows of §4 covered in both adapters.

### WP7 · Reusable architecture rules (§9.1, §9.2, §9.4)

- **Files:** new `platform/testing/arch-rules`; move `RepoSources` and the two tests; rules R3 to R5; FQN and `groupId`
  detection; FQN-based inheritance resolution; `platform/contract/fixtures` plus Java and TS tests;
  `platform/contract/ui-contract-generator` (§1.3), which generates the SDK step and port types from `@UIPort` and
  `ActionFlowStep`. The handwritten types in `sdk/src/metadata/types.ts` must be replaced and a CI check added that fails if
  the generated output is stale.
- **Acceptance:** tools green with their baselines; a short README in the module explaining how the ERP adopts it.

### WP8 · Asynchronous progress (§5.6) — after WP3 and WP5

- **Files:** `PROGRESS` step, `JobsController` (`/api/app/jobs/{id}`), asynchronous `ReplayProgressRunner`, step in
  `ui-core/flow`, delete `markNonRepeatable`.
- **Acceptance:** a 5 s task does not block the request; the client polls the state; `onFinish` runs exactly once.

### WP9 · Framework-free `ui-core/flow` (§2, I7)

- Move the loop of `vue/src/actions/runActionFlow.ts` to `ui-core/src/flow/` with injectable handlers; Vue becomes a thin
  layer. This way `dynamia-pos` and `tienda-shop` reuse it.
- **Acceptance:** the current `runActionFlow` tests pass against `ui-core`, and Vue only re-exports and provides the handlers.

### WP10 · ERP (Phase 5), once WP1 to WP7 are in

1. Adopt `arch-rules` with ERP baselines (R1 to R4).
2. Split each `module-x/ui` into `module-x/actions` (no ZK: FREE and converted actions, with its own package and its own
   `Messages.properties`) and `module-x/ui` (ZK). Moving to a new package is what avoids the same-name bundle clash found in #208.
3. Convert first the bases that drag many actions (`VentaAction` 15, `TableViewRowAction` 6, `AbstractCrearCuentaRapidaAction`
   4, `CompraAction` 4), then the EASY and MEDIUM actions of the inventory. Each is declared `@RunsOn(...)` after reviewing its
   restrictions.
4. Each converted action enters with its `ActionTester` test (`runEverywhere()`), unit in `module-x/actions` or integration in
   `erp-integration-tests`, which comes to depend on the `*-actions`. First `AccionesAdminIT` is rewritten to run the real
   `DesvincularTodosItemsInventarioAction`. The logic of the ERP's ZK controllers (`VentaCrudController` and similar) used by
   the actions moves to services (§10.4).
5. Test in the real ERP (ZK and Vue) the tenant cases: `UIProgress` (18 actions), importers with large files (`ImportarExcel*`,
   `VerImportador*`).

---

## 12. Decisions that need the user

| # | Decision | Proposal | Status |
|---|---|---|---|
| D1 | `/api/v2` (DynamiaNext) versus the flow protocol for POS and shop (design Q4) | `/api/v2` forwards `ActionFlowStep` untranslated, to have a single protocol | **Open** (no value received) |
| D2 | Does the ERP run on more than one node? | If so, `TransferStore` and the WP8 jobs need shared storage (S3 or a shared FS) before production | **Open** (no value received); does not block WP1 to WP3 |
| D3 | Default maximum file size | 100 MB, same as the current ZK `max-upload-size` in the ERP | **Decided: 100 MB** |
| D4 | Name of the unpublished runtime | `FRONTEND` (alternatives: `LOCAL_UI`, `NOT_PUBLISHED`) | **Decided: `FRONTEND`** |

---

## 13. Map: current state → target

| Topic | Today (`feature/next-ui`) | Target | WP |
|---|---|---|---|
| Port resolution | One SPI per facade; static cache in `UIMessages`; ZK beans are used outside ZK | Single `UIEnvironment`, active provider according to `Execution`, `NoUIEnvironment` | 1 |
| Replay bindings | 5 nested `with` + `ReplayBinder` for one | One `ReplayUIEnvironment` + contributors | 1 |
| Publication | By inheritable `HeadlessCapable` (`VerVentaAction` and 3 more in the ERP end up published); `runtime` only informs | `@RunsOn` declared on the concrete class; loader and endpoint use it | 2 |
| Vocabulary | `ZK_ONLY` in `core/actions` and `core/crud` | `FRONTEND` | 2 |
| Global actions | JSON body ignored (no `@RequestBody`) | Fixed and tested | 2 |
| `UIProgress` in ZK | Another thread without tenant or security; UI inside the task fails confusingly | Propagated context; no UI inside the task in both adapters | 3, 6 |
| Files | `byte[]`, Base64 in JSON and in the token; 1 MB / 10 MB | Streaming, `TransferStore`, signed refs, configurable 100 MB | 4 |
| `ZKFileTransfer` | Ignores `accept`/title; `getStreamData` on text media | `ZKMediaUploadedFile`, `UploadPolicy` | 4 |
| Replay answers | By position, unchecked | Per-step fingerprint and re-emission; `CHOICE` by key | 5 |
| Form validation | ZK reopens; replay fails the whole action | Re-emission with errors in both | 5 |
| Token | Bound to action and expiry; random per-JVM secret if missing | Bound to user and tenant; mandatory secret in prod; size cap | 5 |
| Headless `UIProgress` | Inside the request and the pass transaction; error = commit | Outside the transaction; later asynchronous with jobs | 5, 8 |
| Contract between adapters | Javadoc only | Per-port contract suites | 6 |
| Architecture rules | Tools only; `import` and simple name; `extensions/*/ui` is a whole corner | Reusable artifact, FQN, `groupId`, R3 to R5 | 7 |
| Action testing | Loose `RecordingDisplayer` in `ui-shared` tests; the ERP does not test actions (`AccionesAdminIT` copies the action's JPQL) | `test` platform with script, direct/remote/everywhere modes, JUnit 5 and Spring, and its TS equivalent | 1b |
| TS runner | In `vue` | In `ui-core/flow` | 9 |
| ERP | 0 uses of facades; 128 of 131 FREE actions in `*-ui` modules with ZK | `*-actions` modules without ZK, conversion by bases | 10 |

### Minor details (include them when passing through each file)

- Texts without i18n: `"OK"` (`ZKViewsProvider`), `"Close"` (`FormDialogHost.vue`), `"Error: "` (`UIProgress`),
  `"Imported OK"` (`ImportReportAction`).
- JSDoc of `ActionFlowStep` in the SDK: the `{ accept, multiple }` shape appears assigned to `VIEW` when it is the `UPLOAD` one.
- `CrudControllerAPI.getAttributes()` as a `default` method returning an empty map, or document that it must be implemented.
- There are two `CrudControllerAware` (`tools.dynamia.zk.crud` and `tools.dynamia.crud`): delete the ZK one and keep only the
  `crud` one.
- Fully qualified names in code (`java.util.Map`, `tools.dynamia.crud.actions.remote.SaveSupport`, `java.nio.file.Files`):
  turn them into `import`s.
- `HeadlessViews` only serves entities (it uses `SaveSupport.jsonFormDescriptor`). The target is to serialise and apply values
  through the `ViewDescriptor` of any class with a descriptor, so DTO forms work (`ResetPasswordAction`,
  `TestHttpFunctionAction`). It goes with WP5.

---

## Implementation status

First iteration, `dynamia-tools` only (branch `feature/next-ui`), done before this architecture:

| Issue | Done |
|---|---|
| #200 | `ActionInventoryTest`: every `@InstallAction` classified, baseline of ZK-bound actions |
| #201 | `ZkCornerRuleTest`: ZK only in its modules (no violation today) |
| #202 | `UIFacades`, `ReplaySession`, `ReplayBinder`: one lookup and one numbering of interactions for all facades |
| #203 | `UIFiles` (+ `UPLOAD` step, `params.downloads`, Vue renderers) |
| #204 | `UIProgress` |
| #205 | `UIViews.showForm` (+ `viewClass` in `DIALOG`) |
| #206 | `UIChoices` (+ `CHOICE` step), `UINavigation` (+ `newWindow` in `REDIRECT`) |
| #207 | `ActionRuntime`, `@RunsOn`, `runtime` in `ActionMetadata` and the SDK |
| #208 | Actions that no longer import ZK moved to `core` where their resources allow it |

Actions converted: `ExportReportAction` (also headless), `ImportReportAction`, `NewAccountPaymentAction`,
`ReloadEntityFileStoragesAction`, `MoveEntityFileLocalToRemoteStorageAction`, `DownloadFileAction`. ZK-bound actions went from
34 to 28 of 61 (baseline file).

Known limits of that iteration (addressed by the work packages above):

- Not verified in a running ZK or Vue application (the `ZK*Provider` adapters compile but were not exercised in a browser).
- Per-package `Messages.properties` keep ZK-free actions in their `ui` modules (WP10.2).
- `DownloadFileAction` casts to a ZK controller; the inventory scanner does not follow casts (R1 cast detection, §9.2).
- `UIViews` supports forms of entity classes only (WP5, last minor detail).
- Files travel inline as Base64: 1 MB up, 10 MB down (fixed by WP4).
- `UIProgress` headless runs inside the request (fixed by WP8).

### Work packages

| WP | Status |
|---|---|
| WP1 | Done, see below |
| WP1b | Done except the items listed below |
| WP2 | Done, see below |
| WP3 | Done, see below |
| WP4 | Done, see below |
| WP5 | Done, see below |
| WP6 | Done except the ZK driver, see below |
| WP7 | Done except the items listed below |
| WP8 | Done, see below |
| WP9 to WP10 | Not started |

#### WP1 · `UIEnvironment` and clear errors

- `ui-shared`: `UIEnvironment`, `UIEnvironmentProvider`, `NoUIEnvironment`, `UIUnavailableException`, `UIPlatform`
  (`current()`, `supports(Class)`), `@UIPort` on `MessageDisplayer`, `ViewsProvider`, `ChoicesProvider`, `FileTransfer`,
  `ProgressRunner` and `NavigationProvider`. `UIFacades` resolves bound environment, then the active provider, then
  `NoUIEnvironment`; `UIFacades.port(Spi)` replaces `resolve`/`bound`. `UIFacades.with(Class, impl, work)` stays as an
  overlay of one port on the active environment (used by tests). `UIMessages` has no static cache and
  `setCurrentMessageDisplayer` is gone.
- `core/actions`: `ReplayUIEnvironment` (all headless ports of a pass plus `ReplaySession`) and `ReplayPortContributor`
  replace the nested bindings and `ReplayBinder`; `ReplayExecutor` makes a single `UIFacades.with(env, ...)`. `crud`:
  `HeadlessViewsBinder` is now `HeadlessViewsContributor`.
- `zk`: `ZKUIEnvironment` and `ZKUIEnvironmentProvider` (active while `Executions.getCurrent() != null`);
  `ZKAppConfiguration` registers the provider instead of six port beans. A container bean of a port SPI (the ERP's and the
  themes' `MessageDisplayer`) still replaces the ZK default for that port.
- Not in WP1: the `@UIPort` generator (WP7); `UIEnvironmentPropagator` (WP3, so a `LongOperation` thread still sees the
  environment of the ZK execution that is active in it only if ZK activated one).
- **Breaking for the ERP:** `UIMessages.setCurrentMessageDisplayer` (used by 8 ERP view-model tests in `erp-pagos-ui` and
  `erp-soporte-ui`) no longer exists; those tests must bind the displayer with `UIMessages.withDisplayer` or
  `UIFacades.with(...)`.

#### WP1b · Testing platform

New module `platform/testing/ui-testing` (`tools.dynamia.ui.testing`, test scope for consumers, no ZK dependency):
`TestUIEnvironment`, `UIScript`, `UIInteraction`, `TestFiles`, `CapturedDownload`, `TestCrud`, `ActionTester`, `ActionResult`.

- `run()`, `runRemote()` (through the real `ReplayExecutor`, signed tokens) and `runEverywhere()` (compares questions,
  notifications, downloads, redirect and exception) are implemented with what replay supports today.
- Strict scripts: an unanswered interaction fails with `Unanswered CONFIRM '...' at interaction #n`; leftover answers fail.
  `ValidationError` in a form's `onSubmit` opens the form again with the error (direct mode).
- Every converted action has a green `runEverywhere()` test: `SaveAction`, `DeleteAction`, `ViewDataAction` (in
  `ui-testing`), `ExportReportAction`, `ImportReportAction` (`reports/ui`), `NewAccountPaymentAction` (`saas/ui`),
  `MoveEntityFileLocalToRemoteStorageAction` (`entity-files/core`).
- Behaviour change found by those tests: `HeadlessCrudController.query()`/`doQuery()` threw `UnsupportedOperationException`
  headless, so `ImportReportAction` and `NewAccountPaymentAction` failed after doing their work when reached remotely. They
  now record the request (`isQueryRequested()`), because the remote client re-queries when the action ends.
- `ViewDataAction.actionPerformed` ignores a null selection silently; its "select a row" message is unreachable.

Not done yet: the `@DynamiaUITest` JUnit extension, `InMemoryTransferStore` and streaming `TestFiles` (WP4),
`asAccount(...)` and context propagation into `UIProgress` tasks (WP3), `asyncProgress()`, rules by message key, remote
re-ask after `ValidationError` (WP5), `submitForm(...)` in remote mode (use `fillForm`), moving `RecordingDisplayer`
(`ui-shared` tests cannot depend on a module that depends on `ui-shared`), the contract suites (WP6) and TS side (§10.6).

#### WP2 · Classification and publication

- `ActionRuntime.ZK_ONLY` is now `FRONTEND`. `ActionRuntimes.of` reads `@RunsOn` with `getDeclaredAnnotation`, so a subclass
  does not inherit it; `HeadlessCapable` is `@Deprecated` and counts only when the concrete class lists it in its own
  `implements`. `@RunsOn(HEADLESS)` was added to `SaveAction`, `DeleteAction`, `ViewDataAction`, `ExportReportAction`
  and `HeadlessCrudRemoteAction`, which removes the special case in `ActionMetadata`.
- `FindAction`, `FiltersAction`, `SaveConfigAction` and the ZK `Export*Action`s are `FRONTEND`.
- `ApplicationMetadataLoader` publishes `HEADLESS` actions with endpoint and `FRONTEND` ones without; undeclared local actions
  never appear. `ApplicationMetadataController.executeAction` answers 404 for anything that is not `HEADLESS`, `FLOW` or
  `REMOTE`. `executeGlobalAction` now has `@RequestBody` (the test `GlobalActionFlowTest` fails without it).
- SDK: `ActionRuntime` is typed with `FRONTEND`. `ActionResolver` (`ui-core`) hides a `FRONTEND` action unless a `ClientAction`
  with the same id or class name is registered.
- Not in WP2: `ApplicationGlobalAction` (local global actions) is still not published, and the baseline of `UNDECLARED`
  actions (rule R4) comes with WP7.

#### WP3 · Execution context

The branch already had the mechanism §8 asks for: `ContextCapturer` beans, asked by `ObjectsContext.capture()`, which
`SchedulerUtil` uses for every task (and `LongOperation`, hence `ZKProgressRunner`, goes through `SchedulerUtil.run`). So
`ContextPropagator`/`ExecutionContext` were not added as a second API; the propagators of §8.2 are `ContextCapturer`s:

| Capturer | Module | State |
|---|---|---|
| `ObjectsContext` snapshot | `integration` | Existing |
| `AccountTenantContextCapturer` | `saas/core` | Existing (tenant bound, of the request or of the session); tests already cover `AccountTenants.with(7L, ...)` and the root tenant |
| `UIEnvironmentContextCapturer` | `ui-shared` | New: the task runs in `NoUIEnvironment`, so a UI facade there throws `UIUnavailableException` |
| `SecurityContextCapturer` | `security/core` | New: the task acts as the authenticated user of the caller; the pooled thread's context is restored afterwards |

#### WP4 · Streaming files

- `ui-shared` (`tools.dynamia.ui.files`): `UploadedFile` is an interface (`openStream`, `size`, `toTempFile`); `UploadOptions`
  carries `maxFiles`, `maxFileSize` (default 100 MB, D3) and `maxTotalSize`; `DownloadSource` (`BytesSource`, `PathSource`,
  `StreamSource`); `UploadPolicy` (the same server-side check in every adapter); `TransferStore`, `TransferRef`,
  `TransferMeta`, `FlowPrincipal` and an `InMemoryTransferStore`. `FileTransfer.download` takes a `DownloadSource`;
  `UIFiles` has overloads for bytes, `Path`/`File` and a `Supplier<InputStream>`.
- `app`: `LocalTransferStore` (data + metadata file per reference, hard size limit, quota, TTL, purge every five minutes),
  `TransfersConfiguration` (`dynamia.ui.files.*`) and `TransfersController` (`POST`/`GET`/`DELETE /api/app/transfers`,
  raw body, no multipart, 413 on a file over the limit, a foreign reference is 404). The `app` surefire runs with
  `-Xmx256m` and a test moves 50 MB through the controller.
- `actions`: `ReplayFileTransfer` answers an `UPLOAD` step from references (resolved with owner, expiry, `UploadPolicy`) and
  `ReplayExecutor` writes downloads to the store only in the pass that ends, returning `params.downloads[] = {name,
  contentType, size, url}`; consumed upload references are deleted after the flow ends. Tokens carry references only.
- `zk`: `ZKFileTransfer` streams downloads and wraps each uploaded `Media` as a temp-file handle (text media decoded by ZK,
  stored as UTF-8), then applies `UploadPolicy`.
- `security`: `SecurityFlowPrincipal` (authenticated user + current account) so references belong to a user and tenant.
- TS: `client.transfers.upload/cancel/download`, `FlowFileRef`/`FlowDownload` replace `FlowUploadedFile`; the Vue runner
  checks the limits, uploads each file and answers with `{ref}`; the default saver uses a link with cookie authentication and
  a fetched blob with a token. `ImportReportAction` uses `toTempFile()`; `ExportReportAction` hands over bytes because it
  deletes its file right after.
- Deviations from §6: `InMemoryTransferStore` lives in `ui-shared` (the `actions` tests need it, and `ui-testing` depends
  on `actions`); a reference is an unguessable UUID checked against the owner rather than a signed id; the progress callback
  of the upload (`onProgress`) needs `XMLHttpRequest`.
- Not done: a shared `TransferStore` for several nodes (waits for D2); an HTTP-level test of the 413 mapping (the handler is
  tested directly); the `UploadedFile` of `ZKFileTransfer` was not exercised in a browser.

#### WP5 · Robust replay

- **Fingerprints (§5.1):** `ReplaySession.Answer(fp, value)`; the token carries `{fp, value}` and the fingerprint of the pending
  step. If the action asks something else now, the answer and the ones after it are dropped and the new question is asked
  with a `WARNING` and `flow.stepChanged`. The fingerprint covers type, view, title, option keys and, for `CONFIRM`/`INPUT`,
  the text of the question (the spec lists title only, but the text is what changes when the data changes). For those two
  the notice is prefixed to the question because their `message` is the question.
- **Retry (§5.2):** `ReplayRetry` thrown while handling an answer rolls the pass back, drops the answer and asks the same step
  again with the error (`messageType=ERROR`), the submitted values in `data` and `fieldErrors` (new in `ActionFlowStep`).
  `HeadlessViews` turns a `ValidationError` (message and invalid property) into it and also reopens a form that returned
  without `ViewDialog.close()`, unless the action went on to another interaction; `CLIENT_CLOSES` is gone. The test platform
  follows the same rule (direct mode), and `fillForm` works through it remotely. Forms of non-entity beans (DTOs) work as
  long as the class has a descriptor (tested with a plain bean and an auto-fields descriptor).
- **Choices (§5.3):** `ChoiceOptions` has a `key` (default `getId()` of the option if it has one, else the label); the step
  carries `options: [{key, label}]`, the answer is a list of keys, an unknown key asks again, an empty selection cancels
  (also for `chooseMany` in ZK, done in the facade).
- **Token (§5.4):** bound to `FlowPrincipal` (user and tenant, else 401); `dynamia.actions.flow.require-secret=true` or the
  `prod` profile makes startup fail without a secret; `dynamia.actions.flow.max-token-bytes` (16 KB) gives a clear error.
- **Transactions (§5.5):** a pass whose `UIProgress` task failed is rolled back even if the action handled the error and
  publishes nothing; the task runs with the transaction suspended (`ReplayTransactions.runOutside`, implemented in `app`).
- **TS:** the Vue runner passes `message`/`messageType` to `FormDialogHost` and the field errors to the form view; `choose`
  receives `{key, label}` options and returns keys.
- Not done: asynchronous progress (WP8); a fingerprint cannot tell two questions with identical text apart.

#### WP6 · Contract per port

- **Suites** (`platform/testing/ui-contract`, `tools.dynamia.ui.contract`): `MessagesPortContract`, `ViewsPortContract`,
  `ChoicesPortContract`, `FilesPortContract`, `ProgressPortContract` and `NavigationPortContract`, 36 cases that follow the
  rows of §4. A `PortDriver` runs the action on one platform and plays the user with `Reply`s; it reports an `Outcome`
  (what was asked, the effects of the last run, notices, downloads, redirect, failure).
- **Adapters that run them** (in `ui-testing`): the replay adapter (`ReplayPortDriver`: the real `ReplayExecutor`, tokens and
  a transfer store) and the test platform (`TestPlatformDriver`), 12 classes, all green.
- **Fixes the suites forced:** the task of a headless `UIProgress` now runs without UI (it used to be able to register
  questions); `UIViews.showView(options, onClose)` / `ViewsProvider.showView(options, onClose)` with the user's close
  reported (ZK: `ON_CLOSE`; replay: the acknowledgement of the `VIEW` step; default: immediately); `ZKProgressRunner` runs
  its task in `NoUIEnvironment` itself; `showView`/`showForm` width and height travel as `hints` in the step and the Vue
  dialog applies them.
- **Deviations from §9.3:** the suites live in their own module and not in a `ui-shared` test-jar, and the replay driver in
  `ui-testing` and not in `actions`/`crud`. A test-jar of a modular artifact is not visible to the modules that use it, and
  `ui-testing` already depends on `actions` and `crud`, so a driver there avoids a cycle.
- **Not done: the ZK contract run.** ZATS is not available and the ZK providers need a desktop (`Executions`, windows,
  `Fileupload`). `ZKAdaptersContractTest` covers what needs none: upload handles, the shared `UploadPolicy`, and the task
  without UI. Rows of §4 that only the ZK adapter can prove (a question in a window, `Filedownload`, the redirect) are
  unverified until someone runs ZK in a browser or adds ZATS.
- `ZKViewsProvider.showView` keeps its field-count sizing heuristics in ZK; the hints are the explicit size.

#### WP7 · Reusable architecture rules and contract

- **`platform/testing/arch-rules`** (`tools.dynamia.arch-rules`, test scope): `ArchRulesConfig`, `RepoSources`, `ArchBaseline` and the
  rules `FrontendCornerRule` (R1), `ActionInventoryRule` (R2, R3, R4) and `VocabularyRule` (R5), with a README that explains how
  the ERP adopts them. `RepoSources` ignores comments and string literals, detects the front end by `import`, by fully
  qualified name in the body and by Maven `groupId`/`artifactId`, and resolves inheritance by fully qualified name (package,
  explicit and wildcard imports). The old `RepoSources`, `ZkCornerRuleTest` and `ActionInventoryTest` of `actions` are gone.
- **Baselines** (`arch-rules/src/test/resources/arch`): `frontend-corner.baseline` (59), `frontend-bound-actions.baseline` (25)
  and `undeclared-actions.baseline` (42), all of which only shrink. The corner no longer includes `extensions/*/sources/ui` in
  block, so the ZK use of those modules is now a counted baseline; R3 and R5 pass with no baseline.
- **`platform/contract/fixtures`**: 12 steps (one per `ActionFlowStepType`), 4 answers and 2 responses. Java
  (`FixturesTest`, Jackson) parses each into its class and writes it back; the SDK test (`fixtures.test.ts`) checks the same
  files against the generated types, so a step type without a fixture fails both.
- **`platform/contract/ui-contract-generator`**: generates `sdk/src/generated/ui-contract.ts` (`ActionFlowStepType`,
  `ActionFlowStep`, `ActionRuntime` and the catalog of ports declared with `@UIPort` with the steps each produces). The
  handwritten versions of those types in `sdk/src/metadata/types.ts` are replaced by re-exports. `GeneratedContractTest` fails
  when the committed file is stale, which is the CI check (it runs with `mvn test`); regenerate with
  `mvn -pl platform/contract/ui-contract-generator test -Dcontract.update=true`.
- Not done: generating the TS interfaces of `ui-core/ports` (the TS ports do not exist yet; they come with WP9), generating the
  other protocol types (`ActionExecutionRequest/Response`, `FlowFileRef`, `FlowDownload` are still handwritten), and the
  per-property JSDoc of `ActionFlowStep` (the generated file carries no field docs; the Javadoc of `ActionFlowStepType` has them).
- The source-level R4 approximates `ActionRuntimes`: a class whose type is `RemoteAction` is detected by name (`*RemoteAction`).

#### WP8 · Asynchronous progress

- **Protocol:** new `PROGRESS` step (`data: {title, jobId}`), generated into the SDK types and covered by two fixtures. The client
  polls `GET /api/app/jobs/{id}` (`{id, title, state, current, max, message, error}`) and answers with `{jobId, state}`.
- **Server:** `JobRegistry` / `InMemoryJobRegistry` (`ui-shared`) runs the task with `SchedulerUtil`, so it gets the context of the
  caller (tenant, security) and no UI; `JobsController` and `JobsConfiguration` (`app`, purged every five minutes). The pass
  that reaches `UIProgress` starts the job once (`ReplaySession.interact` now tells whether the question is new) and stops at the
  step. On resume the **server reads the real state of the job** (the client's `state` is only a hint that it stopped
  waiting): still running → the same step with the same job; done → `onFinish`; failed → `onError`; the task never runs twice.
  A pass that consumed a failed job is rolled back; consumed jobs are forgotten when the flow ends. `markNonRepeatable` is gone.
- **Ownership:** a job belongs to the user and tenant that started it (snapshotted, not the live principal). Another user gets 404.
- **Client:** `client.jobs.status(id)`; the Vue runner follows the job (`showProgress`, `progressIntervalMs`) and answers.
- **Tests:** the replay tests now prove the request returns while the task still waits, `onFinish` runs once with the task run
  once across several passes, and the client cannot claim a running job is done; the contract suite records what the task does
  apart from the callbacks, because they now run in different places.
- Not done: cancelling a job from the client (the monitor has `stop`, there is no endpoint); a shared `JobRegistry` for several
  nodes (D2); the ZK adapter was already asynchronous; no browser test of the polling.
