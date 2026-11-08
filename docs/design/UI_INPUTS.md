# Portable action controls: `HeadlessActionRenderer` (design)

**Status:** proposal for review. No code yet.
**Context:** [UI_PORTS_WAVE2.md](UI_PORTS_WAVE2.md) §6.3, [dynamia-ui-inventory.md](../next/dynamia-ui-inventory.md), `UIMessages.showInput` with
`LocalDateRange` (already implemented, see "What exists").

## Problem

A headless action is launched from a button. Many actions of `dynamia-erp` are not launched from a button: the user leaves a
widget in the toolbar (a date range, a "find by number" box, a combo of states) and the action runs when its value changes.

In ZK that widget is built by an `ActionRenderer<Component>` assigned with `Action.setRenderer(...)`. The renderer is a Java
class that creates ZK components, and `ActionMetadata.renderer` publishes its class name, which a remote client cannot use.
So these actions cannot be headless: the parameter is collected by a widget, not by a port.

Count in `dynamia-erp` (heuristic, 2026-11-08): about 52 actions put their own renderer in the toolbar. The renderers they
use today:

| Renderer | Uses | What it is |
|---|---|---|
| `ToolbarbuttonActionRenderer` | 12 | A button with another style. Not a control |
| `LocalDateboxRangeActionRenderer` | 5 | Date range, runs on change |
| `FindActionRenderer` | 5 | Text box, runs on Enter |
| `LocalDateboxActionRenderer` | 1 | Single date |
| `ComboboxActionRenderer` | 1 | Choice among values |
| other (`ActionRenderer<EntityReferencePickerBox>`, ...) | few | Entity picker; stays custom |

Five kinds cover almost everything: button, date, date range, text, choice.

## What exists

- `ActionRenderer<T>` is generic: `T render(Action, ActionEventBuilder)`. Every renderer returns a ZK `Component` today, but
  nothing in the interface requires it.
- `Action.setRenderer(ActionRenderer)`; `ActionMetadata` copies the renderer class name.
- `ActionToolbar` renders with `(Component) Actions.render(renderer, action, builder)`.
- `UIMessages.showInput(title, LocalDateRange.class, ...)` asks for a range in a modal dialog. The `INPUT` step carries
  `inputType` (`text`, `number`, `date`, `daterange`) and a range travels as `start/end`; `InputValues` converts it. This
  solves the actions that ask when they run, not the ones that leave a widget in the toolbar.

## Proposal

A renderer that describes instead of building.

```java
setRenderer(HeadlessActionRenderer.dateRange());
setRenderer(HeadlessActionRenderer.choice(EstadoVenta.class));
setRenderer(HeadlessActionRenderer.search("Number"));
```

`HeadlessActionRenderer implements ActionRenderer<Control>`. `render` returns a `Control`: plain, serializable data, with no
ZK type and no callbacks.

```java
record Control(Kind kind, String label, List<Option> options, Object defaultValue) { enum Kind { BUTTON, DATE, DATE_RANGE, TEXT, CHOICE } }
```

### Metadata

`ActionMetadata` recognizes a `HeadlessActionRenderer` and publishes `control: { kind, label, options, defaultValue }`
instead of the renderer class name. The generated TS contract gets the `Control` type and fixtures. An action with a
renderer of any other class keeps publishing the class name and stays `FRONTEND`.

### When it runs and what it sends

| Kind | Runs when | Value sent |
|---|---|---|
| `DATE` | the user picks a date | `YYYY-MM-DD` |
| `DATE_RANGE` | both sides were chosen or cleared | `start/end`, either side may be empty |
| `TEXT` | Enter | the text |
| `CHOICE` | a value is selected | the option key |

The value arrives in `ActionEvent.getData()`, as it does in ZK today, and in `ActionExecutionRequest.data` for a remote
client. The server converts and validates it with `InputValues`, the same code the modal prompt uses; a value that does not
convert is a validation error, never a 500. Options of a `CHOICE` are enumerated on the server (an enum or a fixed list);
an answer that is not one of them is refused, as `CHOICE` steps already do.

### ZK

`ActionToolbar` stops casting blindly. If the renderer is a `HeadlessActionRenderer`, a `ControlZKAdapter` maps the `Control`
to the existing renderer of its kind (`DATE_RANGE` to `LocalDateboxRangeActionRenderer`, `CHOICE` to
`ComboboxActionRenderer`, `TEXT` to `FindActionRenderer`, `DATE` to `LocalDateboxActionRenderer`, `BUTTON` to the default).
The existing renderers do not change, so what the user sees in ZK is the same. A renderer that is not headless is rendered as
before.

### Vue and other clients

One component per kind in the toolbar; the range reuses the two date inputs of `PromptHost`. When the value changes the
client runs the action with that value and then re-reads the list.

## Rules that keep it portable

1. A `Control` is data. It never holds a component, a `Consumer` or a class name. A renderer that needs those is a
   `FRONTEND` renderer and says so.
2. The action does not know how it is drawn. It reads `getData()` and works the same from a button, a widget or a REST call.
3. No new ports: the action still talks to the user through `UIMessages`, `UIFiles` and the rest.

## Questions for the review

1. **Typing.** `AbstractAction` stores a raw `ActionRenderer`, `Actions.render` returns `T`, and `ActionToolbar` casts to
   `Component`. Is a `Control` as `T` safe, or does it need a marker interface so the cast fails with a clear error?
2. **Where the value goes.** `getData()` or `getParams()`. ZK uses `data` for a widget value today; does a remote request
   keep the two apart?
3. **State of the widget.** In ZK the widget keeps its value after the action ran. A remote client re-reads and redraws:
   who holds the value, and does the server need to know it to render the list?
4. **Several controls in one bar.** The ERP puts a range and a search together and the filters combine through controller
   parameters. Is that ordering and combination left to the actions, or does the metadata need to say it?
5. **Defaults and presets** (`LocalDateRange.lastMonth()`, current period): are they part of `Control` or of the action?
6. **`ActionLifecycleAware.afterRenderer(renderer, component)`** receives the component today; what does it receive now?
7. **Security.** A control is a new way to feed a value to an action. Is the `InputValues` validation plus the existing
   `HEADLESS` runtime check enough, and what stops a `TEXT` control from reaching an action not declared headless?
8. **`FindAction` and `FiltersAction`,** `FRONTEND` today: do they become `TEXT` and a set of controls, or stay as they are?
9. **Localization** of labels and of date formats (ZK formats by locale; the wire format is ISO).
10. **Contract versioning.** `control` is a new optional field; which existing clients break, if any?

## Migration order

1. Contract: `Control`, `HeadlessActionRenderer`, `ActionMetadata.control`, TS type and fixtures, contract test.
2. ZK adapter for `DATE_RANGE`, `TEXT` and `CHOICE`; `ActionToolbar` branch; test in the demo.
3. Vue toolbar components for `DATE_RANGE` and `TEXT`.
4. Demo: one real action per kind in `demo-zk-books`, headless, run from ZK and from REST.
5. ERP: migrate one `Filtrar*` action before the rest.

## Not in this proposal

- A list of parameters per action (several inputs for one run). A modal form (`UIViews.showForm`) already covers it.
- Entity pickers in the toolbar (`EntityReferencePickerBox`); they need `EntityLookup` and a search UI of their own.
- Changing how buttons are laid out or grouped.
