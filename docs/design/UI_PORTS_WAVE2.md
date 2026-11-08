# UI ports, second wave: plan

**Status:** proposal, not implemented. Needs review before any code.
**Builds on:** [dynamia-ui.md](../next/dynamia-ui.md) (architecture, invariants I1 to I9, WP1 to WP10),
[UI_PORTS_FOR_ACTIONS.md](UI_PORTS_FOR_ACTIONS.md), [HEADLESS_ACTIONS.md](HEADLESS_ACTIONS.md),
[dynamia-ui-inventory.md](../next/dynamia-ui-inventory.md).
**Tracking:** a GitHub Issue per wave (to be created once the plan is accepted; see §8).

## 1. Goal

Today an action can talk to the user through six facades: `UIMessages`, `UIViews`, `UIChoices`, `UIFiles`, `UIProgress`
and `UINavigation`. That covers the EASY and part of the MEDIUM buckets of the inventory. What still forces an action to
import ZK, or to be rewritten as a `FRONTEND` client action, is a short list of recurring needs. This plan adds the ports
for them, so more actions can be `HeadlessCapable` and run in ZK, Vue, POS and shop without a line of front end code.

Out of scope:

- **Printing.** It is a backend concern solved by dynamia-printing, not a UI port. When printing produces a document,
  the result is shown or downloaded with `UIFiles` (see `preview` below). No `UIPrint` port.
- Porting widgets one by one. A need that is really a widget is declared (a descriptor, an input declaration) or stays
  `FRONTEND`.

## 2. What counts as "a port is done"

Every port below must ship all of these, or it is not done (this is the existing model of `dynamia-ui.md` §1.1, §4 and §9):

| # | Piece | Where |
|---|---|---|
| 1 | Static facade with callbacks, no ZK in any type (I3) | `ui-shared` |
| 2 | SPI annotated `@UIPort(name, steps)` | `ui-shared` |
| 3 | Row(s) in the normative table of `dynamia-ui.md` §4 | docs |
| 4 | Replay adapter, producing an `ActionFlowStep` only in the pass that ends when it has a side effect | `core/actions` (`ReplayUIEnvironment` and a `ReplayPortContributor` if the port lives in another module) |
| 5 | ZK adapter | `platform/ui/zk` (`ZKUIEnvironment`) |
| 6 | Test platform driver ("answer as the user") | `platform/testing/ui-testing` |
| 7 | Contract suite case, run against replay, ZK and the test platform | `ui-shared` test-jar |
| 8 | Protocol fixture per new step and answer | `platform/contract/fixtures` |
| 9 | TS: step type in `sdk`, handler in `ui-core/flow`, default adapter in `vue` | `platform/packages/*` |
| 10 | `UIPlatform.supports(Port.class)` answers correctly in every environment | `ui-shared` |
| 11 | One real action migrated to it, as proof | extension or ERP |

Rule for new steps: a step type is added to `ActionFlowStepType` only when no existing one can carry the intent. Prefer
`CUSTOM` plus a registered renderer for anything used by a single app.

## 3. The ports

Priority is by how many actions need it and by how little new protocol it needs. Counts come from the inventory and were
not re-measured for this plan.

### Wave 1: files family (smallest, reuses `TransferStore`)

**1.1 `UIFiles.preview(DownloadSource)`**: show a file inline (PDF, image, text) that the backend sends, instead of
saving it. Typical case: "view PDF" for a sale, a report or an entity file.

- New step `PREVIEW`: `data = {name, contentType, size, url}`, same shape as `params.downloads`. The client shows it in a
  viewer and answers with a plain acknowledgement when the user closes it (like `VIEW`).
- Replay: like `download`, the source is stored in the pass that ends, after the commit. The ref is served by
  `TransfersController` with `Content-Disposition: inline`.
- ZK: a window with an `<iframe>` or `<img>` over the same inline URL; `Filedownload` is not used.
- Security (decide before coding): inline only for an allow list of types (`application/pdf`, `image/png|jpeg|gif|webp`,
  `text/plain`). `text/html`, `image/svg+xml` and anything unknown are served as attachment, never inline (script
  execution). Always send `X-Content-Type-Options: nosniff`; the iframe is `sandbox`ed. A type outside the list falls
  back to `download`.
- Serves: `ExportarVentaPDFAction` (the ERP one, which today downloads), `ViewReportAction`, entity-files `FileAction`.

**1.2 `UIClipboard.copy(String)`**: put a text or URL in the clipboard.

- Step `NOTIFY`-like, fire and forget: `CLIPBOARD` with `data.text`.
- Known risk: browsers require a user gesture to write the clipboard, and in replay the answer arrives after an async
  request. The client handler must try `navigator.clipboard.writeText` and, if refused, show the text in a dialog with a
  "copy" button. The contract says "the text reaches the user", not "it is in the clipboard". ZK does the same through
  its client-side command.
- Serves: `ViewFileURLAction` (today a `<pre>` with the URL).

**1.3 `UIFiles.shareLink(DownloadSource, Duration)`**: returns a signed, expiring public URL for a file, to send by
WhatsApp or mail.

- Not a step: it returns a value to the action, so it must be deterministic under replay (same request, same link in all
  passes) or be created only in the final pass. Open question Q3.
- Needs a public (no session) endpoint with its own signature and TTL, separate from `/apps/api/transfers`, which is
  bound to the owner. This is the one item of the wave with real security design; it can move to wave 3.

**1.4 `UIFiles.pickFromStorage`**: choose a file already stored in entity-files instead of uploading. Depends on the
entity-files extension, so the port lives there (an own `@UIPort`), not in `ui-shared`. Wave 3.

### Wave 2: context and navigation

**2.1 `UINavigation.openPage(path, params)` and `refresh()`.** Today only `open(url)` exists. `openPage` goes to a
`CrudPage` virtual path with parameters (the replay `REDIRECT` step carries it); `refresh()` asks the client to reload the
current view. The Vue CRUD and the ZK `CrudController` already know how to reload; the work is the contract.
`dynamia-ui.md` lists "navigation / refresh" as missing. The inventory counts 28 ERP actions with a navigation need.

**2.2 `UIContext`.** Read-only access to user, account, locale and time zone for an action, without ZK. Most of this
already travels with the replay execution context (§8 of `dynamia-ui.md`); the port makes it explicit and testable. It
needs no step.

**2.3 `UISelection`.** Read the selected rows of the current CRUD and set the selection. In replay the selection already
arrives in the request (`ids`), so the read side is a view over the request; setting the selection is a client step.
Verify first how `HeadlessCrudController` exposes `ids` before designing this.

### Wave 3: richer input

**3.1 `UIChoices.pickEntity(Class, filters)`.** A searchable selector for large catalogs (customers, items). `chooseOne`
takes an already loaded list, which does not scale. The step is a `CHOICE` with a server-side search: the client sends
the query text, the server answers with a page of `{key, label}`. This needs a small search endpoint tied to the signed
token, not a new step type. Replay must keep the key stable across passes (already true for `CHOICE`).

**3.2 `UIMessages.confirmWithInput`.** Confirm and ask for a reason in one step ("why are you voiding this sale?").
Can be an `INPUT` step with `confirm = true`; check that the existing `INPUT` answer can carry a required text before
adding anything.

**3.3 `UIInputs` (declarative action parameters).** Today 42 ERP actions use a custom ZK renderer for a date range, a
search box or a combo in the toolbar. The proposal is not to port widgets but to let an action declare its parameters
(`DATE_RANGE`, `TEXT`, `ENTITY`, `CHOICE`), and for each front end to render them. This is the biggest item of the plan
and the one that needs a design review of its own: it changes how `ActionRenderer` and the metadata work, and touches
`FRONTEND` actions such as `FindAction` and `FiltersAction`. Treat it as a separate design document before coding.

**3.4 `UIFiles.pickFromStorage`** (see 1.4) and **`shareLink`** (see 1.3), if moved here.

### Wave 4: composition

**4.1 `UIForms.wizard(steps)`.** A multi-step form with validation between steps. Today it would be chained
`showForm` calls; under replay each pass re-runs all previous steps with their answers, which is correct but verbose. A
`WIZARD` step lets the client keep the steps and send the answers at the end. Weigh against the determinism rule: a wizard
whose later steps depend on earlier answers still has to be replayable.

**4.2 `UIExport.export(view, format)`.** `ExportAction`, `ExportCSV`, `ExportExcel` and `ExportJson` live in ZK today
(`dynamia-ui-inventory.md` marks three as EASY/MEDIUM and one as HARD, because of its renderer). One port that combines
progress and download removes the repeated pattern. It builds on `UIProgress` and `UIFiles`.

**4.3 `UIEvents`.** Tell other views that data changed ("sale paid, refresh the KDS"), replacing ZK event queues. This is
a push channel to the client, which does not exist for the flow protocol today (the protocol is request and answer). It
needs a transport decision (SSE or WebSocket) and tenant isolation; do not start before that is decided.

**4.4 `UIUndo`.** Offer "undo" for a few seconds after a delete or void. Depends on the action being reversible; a
generic port cannot guarantee that. Low priority, and possibly an application pattern and not a framework port.

### Wave 5: device (only when a consumer asks)

`UIDevice` (barcode scanner, camera, signature), `UIPermissions` (ask the browser for camera or location) and
`UISpeak`/`UIBeep`. These are mostly for POS and kitchen screens. Per `dynamia-ui.md` §1.1 they are the textbook case
of an own port: one SPI, a ZK stub that throws `UIUnavailableException` with a clear message, and a TS renderer of a
`CUSTOM` step. Build them in the ERP/POS repos if the consumer is only there.

## 4. Order of work

| WP | Content | Depends on | Acceptance (summary) |
|---|---|---|---|
| W1a | `PREVIEW` step, `UIFiles.preview`, inline serving rules, ZK window, TS handler | none | PDF opens inline in ZK and Vue; `text/html` and SVG are served as attachment; a 50 MB PDF with `-Xmx256m` streams; contract case; fixture |
| W1b | `UIClipboard.copy` with dialog fallback | none | the fallback shows when the clipboard is refused; contract case |
| W1c | Migrate `ExportarVentaPDFAction` (ERP) and entity-files `FileAction` to `preview` | W1a | the action is `HeadlessCapable` and passes the test platform in direct and remote mode |
| W2a | `UINavigation.openPage` and `refresh` | none | replay returns `REDIRECT` to a page path; ZK reloads the current view |
| W2b | `UIContext`, `UISelection` read side | W2a | action reads user, account and selection with no ZK import |
| W3a | `pickEntity` and its search endpoint | none | 10,000-row catalog answers in pages; key stable across passes; tenant isolation test |
| W3b | `confirmWithInput` | none | reason is required and reaches the callback |
| W3c | Design document for `UIInputs`, then implement | W2, W3a | separate review |
| W3d | `shareLink` and `pickFromStorage` | W1a | public link expires; another tenant's file is refused |
| W4 | `wizard`, `UIExport`, `UIEvents` (after transport decision), `UIUndo` | W3 | per port, using §2 |
| W5 | Device ports | a consumer | per port, using §2 |

Each WP ends in a working, tested commit. The migration of a real action is part of the WP, not a later step.

## 5. Risks

- **Replay determinism.** Ports that return a value to the action (`shareLink`, `pickEntity` search) are the ones that
  can break "same request and answers, same questions". Each needs an explicit rule and a replay test that runs the action
  twice.
- **Inline file serving is a security surface** (XSS through HTML or SVG, MIME sniffing). §3 1.1 sets the allow list;
  review it with the security owner before merge.
- **Scope creep in `UIInputs`.** It touches the action metadata contract. Do not start it inside a feature WP.
- **ERP consumers.** Actions are migrated one at a time; each one published to REST becomes visible to clients, so its
  restrictions must be reviewed first (`UI_PORTS_FOR_ACTIONS.md`, "Suggested order" step 3).
- **Counts in this document** (28, 42, 100 actions) come from `dynamia-ui-inventory.md`, written on 2026-10-10 and by
  static analysis; re-measure before using them to justify priority.

## 6. Open questions

| # | Question |
|---|---|
| Q1 | `PREVIEW` as a new step, or `VIEW` with a file view class? This plan proposes a new step because `VIEW` expects a `ViewDescriptor`. |
| Q2 | Which types are allowed inline, and who signs off the list? |
| Q3 | `shareLink` returns a value: created only in the final pass, or idempotent by content hash? |
| Q4 | `UIEvents` transport (SSE or WebSocket) and whether it belongs in `ui-shared` at all. |
| Q5 | Do device ports live in `ui-shared` or in the ERP/POS repos? |
| Q6 | Which wave 1 action in the ERP is the first proof, `ExportarVentaPDFAction` or a report? |

## 7. Not verified while writing this plan

- No code was run. The facades, steps and files named here were read in `ui-shared`, `core/actions` and the design docs
  on branch `next`; whether `UIMessages` already exposes `NOTIFY` as a toast, and how `INPUT` answers carry a required
  text, were not checked.
- The ZK and Vue adapters were not run (the existing docs state the same for the first wave).

## 8. Tracking

When the plan is accepted: one GitHub Issue per wave in `dynamia-tools`, linked from here. No Issue number exists yet.
