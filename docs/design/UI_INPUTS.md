# UIInputs: declarative parameters of an action (design brief)

**Status:** problem statement for a separate design review. No code.
**Context:** [UI_PORTS_WAVE2.md](UI_PORTS_WAVE2.md) §6.3, [dynamia-ui-inventory.md](../next/dynamia-ui-inventory.md).

## Problem

About 50 actions of `dynamia-erp` (count from the 2026-11-08 inventory in that repo, heuristic) put their own widget in the
toolbar through an `ActionRenderer`: a date range, a "find by number" box, a combo of states. In ZK the renderer is a
Java class that builds ZK components, so these actions cannot be headless: the parameter is collected by a widget, not by a
port, and a remote front end has nothing to render.

## Proposal to evaluate

Let an action declare its parameters instead of drawing them:

```java
setInputs(Input.dateRange("period"), Input.text("number"), Input.choice("state", EstadoVenta.class));
```

Each front end renders the declaration with its own widgets (ZK: the current renderers become adapters; Vue: components),
and the values arrive in `ActionEvent.getParams()`, which headless actions already read.

## Questions the review must answer

1. Where the declaration lives: `ActionMetadata` (so REST clients receive it) and how `ActionRenderer` consumes it in ZK
   without breaking the actions that keep a custom renderer.
2. Which input kinds cover the real cases (date, date range, text, number, choice, entity via `EntityLookup`) and which
   stay `FRONTEND`.
3. Validation: server side, using the same rules for ZK and remote.
4. Interaction with `FindAction` and `FiltersAction`, which are `FRONTEND` today.
5. Migration order, starting from the `Filtrar*` actions of the ERP.

Do not start before these are answered; it changes the metadata contract.
