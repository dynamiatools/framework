# Actions in the Vue UI: the gap, and UI ports for what is left

**Status:** problem statement and proposal. Written after bringing up `examples/dynamia-movies` (a Vue backoffice with
no ZK) and finding that the CRUD offers far fewer actions than the ZK UI.

## The problem

An entity page in the Vue backoffice shows `New`, `Edit` and `Delete`; the ZK UI shows every default action of the
framework and of the extensions. The cause is the contract of `ApplicationMetadataLoader`: an action is published to
REST clients only when it is

1. a `CrudRemoteAction` (written for REST), or
2. a `CrudAction` that is `HeadlessCapable` (the *replay* runtime, see [HEADLESS_ACTIONS.md](HEADLESS_ACTIONS.md)).

Today only `SaveAction` and `DeleteAction` are headless. `NewAction`, `EditAction` and `CancelAction` are pure UI state
that the Vue CRUD handles itself. Every other action of the catalog is invisible to a REST client, and so is every
`ApplicationGlobalAction` (the loader only reads `ApplicationGlobalRemoteAction`).

Making `CrudAction` extend `HeadlessCapable` is not a fix: `headlessSupported()` is `true` by default, so the 34
`CrudAction`s of this repository would be published at once, 20 of them importing ZK (they fail when run) and several
that reset passwords, clear caches or reinitialise accounts (exposed with no review).

## The 20 `CrudAction`s that import ZK

| Needs | Actions |
|---|---|
| A dialog with a form or view (`Viewer`, `Window`) | saas: `NewAccountPaymentAction`, `ViewAccountLogAction`, `ViewAccountPayments`, `ViewAccountStatsAction`, `ViewAccountLogoAction`; email: `TestSMSAction`; http-functions: `TestHttpFunctionAction`; security: `ResetPasswordAction` |
| A file download or upload | reports: `ExportReportAction`, `ImportReportAction`; zk: `reports.actions.ExportAction` |
| A long operation or message box | email: `TestEmailAccountAction`, `PreviewEmailTemplateAction`; reports: `TestReportDatasourceAction` |
| Pure UI (buttons, combos, lists) | saas: `FilterAccountByRegionAction`, `ShowAccountAdminActions`; zk: `FiltersAction`, `FindAction`, `ViewDataAction`, `ViewReportParametersAction` |

The other 14 `CrudAction`s do not import ZK: `Save`, `Delete`, `New`, `Edit`, `Cancel` and nine business actions of
the extensions (`ResetAccountBalanceAction`, `ClearAccountCacheAction`, `ReinitAccountAction`,
`SaveAccountFeaturesAction`, `SetPreferredAccountAction`, `SetUserProfilesAction`, `ViewReportAction`, `FileAction`,
`AbstractEntityFileAction`).

## Proposal: UI ports of a higher level

`ui-shared` already has the port idea: `UIMessages` with a swappable `MessageDisplayer` (what replay uses for
`Save`/`Delete`) and `UIToolsProvider` (`createDialog`, `showDialog(title, Object content, ...)`, selectors). The
second one cannot be implemented for a REST client, because `content` is a ZK component and the callbacks are Java
lambdas. The port has to describe an **intent**, not a component, which is what `ActionFlowStep` already is:

| New port (in `ui-shared`) | ZK adapter | TS front end |
|---|---|---|
| `showForm(ViewDescriptor, values) -> values` | window with a `Viewer` | `DIALOG` step with `DynamiaForm` |
| `showView(ViewDescriptor, data)` | `Viewer` in a window | `DIALOG` step with `DynamiaViewer` |
| `download(name, bytes, mime)` | `Filedownload` | step with a signed URL |
| `upload(types) -> file` | `Fileupload` | `UPLOAD` step |
| `choose(options) -> selection` | listbox in a window | `CHOICE` step |
| `progress(operation)` | `LongOperation` | progress step with polling |

An action written once against these runs in ZK as always and, through replay, in REST: it runs until the first intent
without an answer, which goes to the client as a step.

Constraints:

- Keep `UIToolsProvider` as it is (many ZK actions use it); the new ports are a separate interface.
- An intent must be replayable: same request and answers give the same intents in the same order. A form whose fields
  change while the user types stays on the client side. `progress` breaks determinism: prefer a `FlowRemoteAction`.
- Pure UI actions are not ported and not published: they are `ZK_ONLY`, and each front end writes its own client action in TypeScript.

## Suggested order

1. A test that walks every `@InstallAction` and fails when a `CrudAction` is neither `HeadlessCapable` nor declared
   declared `ZK_ONLY`. It makes the inventory above executable.
2. `download`, `upload`, `choose`; migrate `ExportReportAction` and `ImportReportAction` to prove the whole path
   (port, ZK adapter, TS step).
3. Review and mark the nine business actions without ZK, one by one: they are published to REST once marked, so check
   their restrictions first.
4. `showForm` / `showView`, then the global actions loader.
