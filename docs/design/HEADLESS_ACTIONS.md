# Headless actions: one action, many front ends

**Status:** implemented for the CRUD actions `DeleteAction` and `SaveAction`; the rest of
the action catalog is the plan below. Writing guide: [DEVELOPMENT_PATTERNS.md](../backend/DEVELOPMENT_PATTERNS.md).

## The three layers

| Layer | What it is | Who uses it |
|---|---|---|
| **Headless actions (replay)** | A `LocalAction` written once against `UIMessages` / `CrudControllerAPI`, marked `HeadlessCapable` | Almost every action; ZK runs it as always, REST clients through the replay runtime |
| **`FlowRemoteAction`** | An explicit state machine (`start` / `resume`) | Actions that exist only for REST clients, need a step replay does not produce yet, or are not deterministic |
| **Flow protocol** | `ActionFlowStep`, signed `resumeToken`, `ActionFlows`, `runActionFlow` on the client | The two above produce it, every client consumes it; see [SERVER_DRIVEN_ACTION_FLOWS.md](SERVER_DRIVEN_ACTION_FLOWS.md) |

The protocol is the stable border: a new front end implements the steps once and never needs to know how an action was written.

## Problem it solves

A `LocalAction` such as `DeleteAction` talks to the user through UI-neutral ports. `UIMessages.showQuestion("Delete?", () -> delete())`
keeps *what happens after the answer* as a callback in the Java stack. ZK holds that stack between events; a REST server cannot.
So the actions that matter used to be written again as `RemoteAction`s (`DeleteFlowRemoteAction`, `SaveFlowRemoteAction`, and
their one-shot `*Direct` variants, all removed): two sources of truth per action and one more copy for every new front end.

Only 15 of the 53 local action classes of the framework import ZK; the other 72 % already use the ports.

## How: replay

The action runs from the start; its questions are answered, in order, with the answers the user already gave; the first
question without an answer stops the pass and goes to the client as an `ActionFlowStep`; the original request and the answers
travel in the signed, expiring resume token (`FlowTokenSigner`). With the answer, the action runs again from the start.

```
POST /entities/Review/action/delete {dataId}              -> PENDING  CONFIRM "Are you sure you want to delete Review Elena: 9/10?"  + token(request, [])
POST ... {resumeToken, data:true}   runs again, answers=[true] -> SUCCESS  DONE    "Review deleted successfully!"
```

No server state, no sticky sessions, nothing to serialize but plain data, the wire protocol of `FlowRemoteAction`.

Rules that make it safe:

1. **Every pass runs in a transaction (`ReplayTransactions`) that only the pass that ran to the end commits.** A pass that
   stopped at a question is rolled back. What is not a database write (mail, files) must wait for the last callback.
2. The action must be deterministic: same request and answers, same questions in the same order.
3. `HeadlessCapable` declares that the action only uses the ports. `headlessSupported()` lets a subclass opt out
   (`SaveAndEditAction` and `SaveAndNewAction` leave the form on a record: that is the client's job).
4. The token is signed and expires (`dynamia.actions.flow.secret`, `dynamia.actions.flow.token-ttl`), is bound to the action
   id and carries the original request, so a client cannot change what it confirmed.

## Pieces

| Module | Piece |
|---|---|
| `ui-shared` | `UIMessages.withDisplayer(...)`: a `ScopedValue` displayer per execution |
| `actions` | `ReplayInteractions` (answers from the token, records the pending question), `ReplayExecutor`, `ReplayTransactions`, `HeadlessCapable` |
| `crud` | `HeadlessCrudController`, `HeadlessCrudView`, `HeadlessCrudRemoteAction` (adapts a `CrudAction` to the REST endpoint; handles one record and bulk `ids`) |
| `app` | `SpringReplayTransactions`; `ApplicationMetadataLoader` publishes headless actions in the entity metadata and the controller builds a fresh adapter per request |

The entity class of an entity action comes from the URL (`EntityMetadata.getClassName()`), never from the client.
Restrictions (`ActionRestrictions`), state checks and `ActionFilter`s apply as for any `RemoteAction`.

## What is not done

- `HeadlessCrudController` copies the save/delete flow of `zk.crud.CrudController`. Next step: extract it into one class both use.
- Ports still missing for the other actions: navigation / refresh, dialogs with a form (`UIToolsProvider`), long operations.
  Files are done: `UIFiles` (`download`, `upload`) over the `FileTransfer` SPI; headless, an upload is an `UPLOAD` step answered with
  Base64 files (limit 1 MB) and downloads travel in `params.downloads` of the final response (limit 10 MB). Bigger files need a
  stream endpoint. See [dynamia-ui.md](../next/dynamia-ui.md) for the facade pattern.
  Until then `DIALOG`, `REDIRECT`, `CALL` and `CUSTOM` steps come from `FlowRemoteAction`.
- Deferring non-database side effects (`afterCommit`) is a rule, not an API yet.
- Actions that are pure UI (`FindAction`, `FiltersAction`, renderers): declare a client hint in the metadata; each front end implements it once.
- A test that walks every `@InstallAction` and fails when an action is neither `HeadlessCapable` nor declared client side.
