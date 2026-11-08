# UI ports, second wave: plan

**Status:** waves 1 to 3 implemented on branch `int/ui-ports-wave2` (see §13 for what was built, what changed from this
plan and what is not done). Revised after an architecture review (Opus); the review checked its findings against the
source (file references are in §9).
**Builds on:** [dynamia-ui.md](../next/dynamia-ui.md) (architecture, invariants I1 to I9, WP1 to WP10),
[UI_PORTS_FOR_ACTIONS.md](UI_PORTS_FOR_ACTIONS.md), [HEADLESS_ACTIONS.md](HEADLESS_ACTIONS.md),
[dynamia-ui-inventory.md](../next/dynamia-ui-inventory.md).
**Tracking:** a GitHub Issue per wave (to be created once the plan is accepted; see §8).

## 1. Goal

Today an action can talk to the user through six facades: `UIMessages`, `UIViews`, `UIChoices`, `UIFiles`, `UIProgress`
and `UINavigation`. That covers the EASY and part of the MEDIUM buckets of the inventory. What still forces an action to
import ZK, or to be rewritten as a `FRONTEND` client action, is a short list of recurring needs. This plan adds the ports
for them, so the existing actions of `dynamia-erp` and the extensions can be migrated off direct ZK calls and become
`HeadlessCapable`. ZK stays the main front end: **every port has a ZK adapter, and an action migrated to a port must behave
in ZK exactly as before** (§3, piece 5). Other front ends (Vue, POS, shop) get the port through the flow protocol.

Out of scope:

- **Printing.** It is a backend concern solved by dynamia-printing, not a UI port. When printing produces a document, the
  result is shown or downloaded with `UIFiles`. No `UIPrint` port.
- Porting widgets one by one. A need that is really a widget is declared (a descriptor, an input declaration) or stays
  `FRONTEND`.

## 2. The model that shapes this wave: final-pass effects

In replay, an action runs from the start on every pass, and a pass that stops at a question is rolled back. Two kinds of
port follow from that:

| Kind | What it is | Examples | Protocol |
|---|---|---|---|
| **Question** | Waits for an answer; the pass stops there | `showQuestion`, `showInput`, `choose*`, `showForm`, `upload`, `progress` | An `ActionFlowStep` of its own type |
| **Final-pass effect** | Something the user sees or receives once the action has ended; published only by the pass that ran to the end, after the commit | `download`, toasts (`params.notifications`), and in this wave `preview`, `clipboard`, `refresh`, `openPage` | A field of the final response (`params.*`), **no new `ActionFlowStepType`** |

A port that waits for an acknowledgement would leave the pass pending, discard the effect and regenerate the source on
every pass, so preview and clipboard are effects, not steps. Add an `ActionFlowStepType` only for a real question.

## 3. What counts as "a port is done"

Every port must ship all of these (model of `dynamia-ui.md` §1.1, §4 and §9):

| # | Piece | Where |
|---|---|---|
| 1 | Static facade with callbacks, no ZK in any type (I3) | `ui-shared`, or the module that owns the concept (see 5.2) |
| 2 | SPI annotated `@UIPort(name, steps)` | same module |
| 3 | Row(s) in the normative table of `dynamia-ui.md` §4 | docs |
| 4 | Replay adapter | `core/actions` |
| 5 | ZK adapter | `platform/ui/zk` |
| 6 | Test platform driver ("answer as the user") | `platform/testing/ui-testing` |
| 7 | Contract suite case, run against replay, ZK and the test platform | `ui-shared` test-jar |
| 8 | Protocol fixture per new step, effect and answer | `platform/contract/fixtures` |
| 9 | TS: types in `sdk`, handler in `ui-core/flow`, default adapter in `vue` | `platform/packages/*` |
| 10 | `UIPlatform.supports(Port.class)` right in every environment | `ui-shared` |
| 11 | One real action migrated to it, as proof | extension or ERP |

An ERP action migrated as proof needs `dynamia-tools` and `dynamia-erp` in the same batch.

### ZK adapter of each port (required, same WP)

| Port | ZK adapter |
|---|---|
| `UIFiles.preview` | Window with `<iframe>`/`<img>` over the inline URL; types outside the allow list use `Filedownload` |
| `UIClipboard.copy` | Client-side command that writes the clipboard, with the same dialog fallback |
| `UINavigation.openPage` / `refresh` | Navigate to the `CrudPage` path through the existing ZK navigation; `refresh` reloads the current `CrudController` view |
| `UISelection` (`getSelection()`) | Read the selected rows from the ZK `CrudController` |
| `UIChoices.pickEntity` | Searchable selector window over the same registered lookup (`Combobox`/`Bandbox` with server search) |
| `shareLink` / `pickFromStorage` | Plain Java call in ZK (no UI for `shareLink`); entity-files file chooser dialog for `pickFromStorage` |

Each ZK adapter runs the same contract case as replay. The proof action of a WP is migrated in the ERP and checked in ZK
first, since that is where it is used today.

## 4. Wave 1: files family and final-pass effects

### 4.1 `UIFiles.preview(DownloadSource)`

Show a file inline (PDF, image, text) that the backend sends, instead of saving it.

- **No new step.** The effect travels in `params.downloads` of the final response, as a new `disposition: "inline"` on the
  entry (`{name, contentType, size, url, disposition}`); default stays `attachment`. This also answers former Q1.
- **Inline is decided by the server when the file is published,** only for `DOWNLOAD`-direction transfers, and only for an
  allow list (`application/pdf`, `image/png|jpeg|gif|webp`, `text/plain`). Anything else is published as `attachment`.
  It is never decided by a query parameter or by a header the client sends (an upload's `X-File-Type` is client-set).
- **Serving rules on `TransfersController`** for an inline ref: `X-Content-Type-Options: nosniff` and a `Content-Security-Policy: sandbox`
  header on the response itself (an iframe `sandbox` attribute does not protect a URL opened directly).
- **Reads.** Today a download is deleted after the first complete read, so a reloaded iframe, the "download" button of the
  PDF viewer or a second mount would get 404. An inline ref allows a few reads until its TTL.
- **Client XSS.** With header authentication the TS client fetches the file as a `Blob` and uses an object URL; a `blob:`
  URL of type `text/html` in an iframe runs in the app origin and server headers no longer apply. The allow list is
  enforced in the TS handler too, and a type outside it falls back to `saveFile`.
- **ZK:** a window with an `<iframe>`/`<img>` over the same inline URL; `Filedownload` is not used.
- **Open risk:** Chrome's built-in PDF viewer does not render inside a sandboxed iframe. The acceptance "PDF opens inline"
  and the `sandbox` rule conflict; resolve it with a spike (open the PDF with `CSP: sandbox allow-scripts`-less variants, or
  in a new tab) before writing the handler.
- **Memory.** A 50 MB preview with header auth becomes a 50 MB `Blob` in the browser. Set a size limit for inline
  (above it, `attachment`).
- **Serves:** the ERP `ExportarVentaPDFAction` (to verify whether dynamia-printing hands the PDF over as a stream or as a
  `byte[]`), `ViewReportAction`, entity-files `FileAction`.

### 4.2 `UIClipboard.copy(String)`

- A final-pass effect, `params.clipboard = {text}`. `NOTIFY` is not used: in replay `NOTIFY` is never emitted mid-flow
  (`showMessage` feeds `params.notifications`).
- Browsers require a user gesture to write the clipboard and the response arrives after an async request, so the handler
  tries `navigator.clipboard.writeText` and, if refused, shows the text in a dialog with a "copy" button. The contract
  says "the text reaches the user", not "it is in the clipboard".
- **Serves:** `ViewFileURLAction` (today a `<pre>` with the URL).

### 4.3 Acceptance for W1

- A second read of an inline ref works within the TTL; after it, 404.
- A forged inline flag on an upload is ignored (served as attachment).
- `text/html`, `image/svg+xml` and unknown types are served and published as attachment.
- The TS client refuses a `blob:` of a type outside the allow list.
- A 50 MB file streams on the server with `-Xmx256m` (existing `TransfersController` test pattern).
- Contract case and fixture for `disposition: inline` and for `params.clipboard`.

## 5. Wave 2: context and navigation

### 5.1 `UINavigation.openPage(path, params)` and `refresh()`

- `REDIRECT` requires `data.url` to be relative or http(s) (checked in `runActionFlow.ts`), so a `CrudPage` virtual path
  must not be overloaded into `url`. `openPage` is a final-pass effect with its own field, `data.page = {path, params}`.
- `refresh()` is a final-pass flag next to the `saved`/`deleted` result of `HeadlessCrudRemoteAction`, not a step.
- **Re-measure before building:** the inventory's "28 ERP actions with a navigation need" were redirect needs that
  `open(url)` already covers. Count the real `openPage` need first.
- Side bug found by the review: `ReplayExecutor` drops the notification title when it builds `params.notifications`. Fix it
  in the same change, with a test.

### 5.2 `UISelection`

- It belongs in `core/crud`, not `ui-shared` (which does not depend on crud). The read side is a `getSelection()` on
  `CrudControllerAPI`; `HeadlessCrudController.selection` is private with no getter today.
- I4: the `ids` of the request are loaded one `find` at a time with no limit; add a cap before exposing the selection.
- Setting the selection from the server is a client concern; do not build it until a case asks for it.

### 5.3 `UIContext` is dropped

It would duplicate `UserInfo` (commons), `FlowPrincipal` (`ui-shared`) and `AccountServiceAPI` (saas), and `ui-shared`
cannot name an account. If a need remains, limit it to locale and time zone. Note for authors: "now" is not deterministic
across passes, so a confirmation text that contains a timestamp changes its fingerprint.

## 6. Wave 3: richer input

### 6.1 `UIChoices.pickEntity(Class, filters)`

- `CHOICE` is safe today because the answer is checked against the options in the step. `pickEntity` has no options in the
  step, so a client could send any key. **Every pass must re-check the key** against the same filters and the tenant, and a
  failed check goes through `ReplayRetry`.
- The action is **not** re-run on each search keystroke. The step names a registered server-side lookup that a search
  endpoint resolves, bound to the `FlowPrincipal`.
- Acceptance: a 10,000-row catalog answers in pages; a key from another tenant is refused; the key stays stable across passes.

### 6.2 `confirmWithInput`: no new port

An `INPUT` step already returns a `String`, and a `null` answer means cancel. "Required" is the action throwing `ReplayRetry`
on a blank answer, which asks the same question again. At most add a `data.required` hint so the client can disable the
button. Document the pattern in `DEVELOPMENT_PATTERNS.md`; write no code.

### 6.3 `UIInputs` (declarative action parameters)

Today 42 ERP actions (inventory count, to re-measure) use a custom ZK renderer for a date range, a search box or a combo.
Letting an action declare its parameters changes `ActionRenderer` and the metadata contract. It gets **its own design
document** and review, outside this plan.

### 6.4 `shareLink` and `pickFromStorage`

- The transfer stores are temporary and node-local (`InMemoryTransferStore` keeps bytes in memory, `LocalTransferStore`
  writes to the local disk), which is wrong for a link sent by WhatsApp or mail under a load balancer.
- `shareLink` lives in **entity-files** as a stateless HMAC URL over a stored file, with the expiry taken from the flow
  start carried in the token. That makes it deterministic across passes with no storage.
- `pickFromStorage` also lives in entity-files, with its own `@UIPort`.

## 7. Dropped from the first draft

| Port | Why |
|---|---|
| `PREVIEW` step | A step that waits leaves the pass pending and discards the effect; replaced by `disposition: inline` (4.1) |
| `confirmWithInput` as a port | `INPUT` plus `ReplayRetry` already does it (6.2) |
| `UIContext` | Duplicates existing types (5.3) |
| `UIExport` | The inventory marks Export CSV/Excel/Json as `FRONTEND`, not conversion targets (`dynamia-ui-inventory.md` "Corrections") |
| `UIForms.wizard` | Chained `DIALOG` steps already replay correctly; a `WIZARD` step adds protocol and determinism risk without a new capability |
| `UIEvents` | A transport and infrastructure topic (SSE or WebSocket, tenant isolation), not an action port |
| `UIUndo` | An application pattern: reversibility cannot be guaranteed by a generic port |
| Device ports (scanner, camera, signature, permissions, sound) | Out of this plan. They are not needed to migrate the existing ERP actions; a consumer can declare its own `@UIPort` later (`dynamia-ui.md` §1.1) |

## 8. Order of work

| WP | Content | Depends on |
|---|---|---|
| **W1** | Final-pass effects: `disposition: inline` on `params.downloads`, inline rules on `TransfersController`, TS preview handler with client allow list and download fallback, `params.clipboard`, fixtures. First proof: `ViewFileURLAction` (clipboard) and a report or sale PDF (preview) | none; starts with the Chrome spike |
| W1c | Migrate `ExportarVentaPDFAction` (ERP) and entity-files `FileAction` | W1 |
| W2a | `openPage` (`data.page`) and `refresh` flag; fix the dropped notification title | W1 pattern; re-measure need first |
| W2b | `UISelection` read side in `core/crud` with a cap on `ids` | none |
| W3a | `pickEntity` with a registered lookup endpoint and key re-check | W2 not required |
| W3b | Document the `INPUT` + `ReplayRetry` pattern | none |
| W3c | `UIInputs` design document | W3a |
| W3d | `shareLink` and `pickFromStorage` in entity-files | W1 |

Each WP ends in a working, tested commit; migrating a real action is part of the WP.

## 9. Findings of the review, with where they were checked

All of these are from the Opus review and were not re-checked by the author of this plan.

- Downloads are published only by the pass that ran to the end: `ReplaySession.java:237`, `ReplayExecutor.java:112-113`.
- `VIEW` renders a descriptor: `runActionFlow.ts` (VIEW case). `NOTIFY` is not emitted mid-flow; `showMessage` feeds
  `params.notifications`: `ReplayInteractions.java:75`, `ReplayExecutor.java:117-125`; the TS `NOTIFY` case calls
  `handlers.showToast` (`runActionFlow.ts:264`); notification title dropped at `ReplayExecutor.java:121`.
- Deleted after first complete read; `X-File-Type` set by the client: `TransfersController.java:86-88, 109-126`.
- Blob and object URL: `runActionFlow.ts:470-475`. `REDIRECT` `data.url` rule: `runActionFlow.ts:197`.
- `CHOICE` answer checked against the step options: `ReplayChoicesProvider.java:49`. `INPUT` answer type:
  `ActionFlowStep.java:72`; `null` means cancel: `ReplayInteractions.java:125`; `ReplayRetry`: `ReplaySession.java:118-122`;
  fingerprint: `ReplaySession.java:168`.
- Selection: `HeadlessCrudController.java:41` (private); `ids` loading: `HeadlessCrudRemoteAction.java:116-127`;
  result flags: `HeadlessCrudRemoteAction.java:86-90`.
- Inventory corrections on export actions: `dynamia-ui-inventory.md:71-73`; redirect needs: `:56`.

## 10. Open questions

| # | Question |
|---|---|
| Q2 | Who signs off the inline allow list and the `CSP: sandbox` choice (security owner)? |
| Q6 | Is the PDF of `ExportarVentaPDFAction` produced by dynamia-printing as a stream or as a `byte[]`? It decides whether the 50 MB / `Blob` limits matter. |
| Q7 | Chrome PDF viewer under a sandboxed response: which variant works (spike in W1)? |
| Q8 | Do we run more than one node in production? It decides whether the node-local transfer store needs a shared one before inline refs allow repeated reads. |

## 11. Not verified

- No code was run. The review read the source but did not execute anything; Chrome's PDF behaviour under a sandboxed
  iframe or `CSP: sandbox` is from memory. The ERP actions and the ZK adapter were not inspected.
- Counts of actions come from the inventory of 2026-10-10 (static analysis); re-measure before using them to rank work.

## 12. Tracking

When the plan is accepted: one GitHub Issue per wave in `dynamia-tools`, linked from here. No Issue number exists yet.

## 13. Implementation status

Built, each with tests (Java: JUnit; TS: vitest) and a commit per piece:

| Port | What exists |
|---|---|
| `UIFiles.preview` | `InlinePolicy`; `FileTransfer.preview` (default falls back to download); replay publishes `disposition` and `?inline=1`; `TransfersController` serves inline only for `DOWNLOAD` refs of allowed types, with `nosniff`, `CSP: sandbox` for non-PDF, and keeps them readable until expiry; `ZKFileTransfer.preview` (Iframe dialog, 25 MB cap); TS `previewFile`/`browserPreviewFile` with the client allow list; fixture |
| `UIClipboard.copy` | `Clipboard` SPI + facade; `ReplayClipboard` (`params.clipboard`); `ZKClipboard` (escaped script, prompt fallback); TS `showClipboardFallback`; test platform; fixture |
| `UINavigation.openPage` / `refresh` | `NavigationProvider` default methods; `UINavigation` validates the path; replay `params.page` / `params.refresh`; ZK through `NavigationManager`; TS `openPage`/`refresh` handlers; test platform; fixture |
| Notification title | `ReplayExecutor` keeps `title` in `params.notifications` |
| `getSelection()` | `CrudControllerAPI.getSelection()` (default: the selected record), headless override, cap `MAX_BULK_IDS = 1000` on bulk `ids` |
| `UIChoices.pickEntity` | `EntityLookup`/`EntityLookups`; `ChoicesProvider.pickEntity`; `ActionFlowStep.entityChoice`; replay re-checks the key every pass; `LookupsController`; ZK picker dialog; test platform; `LookupsApi` and `pickEntity` handler in TS; fixture |
| `pickFromStorage` | `EntityFileLookup` (entity-files): files of the current account only, fails closed without an account |
| `confirmWithInput` | Documented pattern (`INPUT` + `ReplayRetry`) in `DEVELOPMENT_PATTERNS.md`; no code |

### Changes from the plan above

- **Final effects use `params.*`, not `data.page`.** `openPage` is `params.page = {path, params}` because the final response
  is a `DONE` step; this keeps `REDIRECT` for URLs only.
- **Inline is requested with `?inline=1` in the URL the server builds,** and the controller still checks the direction and
  the type, so the flag cannot make anything unsafe inline. The ref is not stored with a disposition.
- **`shareLink` was not built.** entity-files already has the extension point: shared files are public at
  `/storage/{uuid}/{file}` and `EntityFileSecurityProvider.generateToken` issues a tokenized URL for private ones. A second
  HMAC scheme would duplicate it. Whatever expiry is wanted belongs in the security provider.
- **`pickFromStorage` is an `EntityLookup`,** not a separate port, so it reuses `UIChoices.pickEntity` in ZK and remotely.

### Not done

- **`UIInputs`:** it needs its own design document, see [UI_INPUTS.md](UI_INPUTS.md); no code.
- **Default Vue components** for `previewFile`, `pickEntity` and the clipboard fallback: the handlers are in `ui-core`; an
  application passes its own, as it does for `choose`.
- **The Chrome PDF viewer spike (Q7):** PDFs are served without the `sandbox` directive; revisit after trying it in a browser.
  Nothing in this branch was run in a browser, in ZK or in Vue.
- **Refresh in ZK** reloads through `NavigationManager.refresh()`; whether that reloads the current CRUD list was not checked.
- **A shared `TransferStore`** for several nodes (Q8) and a size limit for the browser `Blob` of a large inline file.
- **Migration of the ERP actions** (`ExportarVentaPDFAction` to `preview`, `ViewFileURLAction` to `copy`): needs
  `dynamia-erp` in the same batch.
