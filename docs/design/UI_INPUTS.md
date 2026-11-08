# Portable action controls and the standard list query

**Status:** implemented in this branch for the list of a CRUD page (ZK and Vue verified in a browser against `demo-zk-books`).
Not done: migrating the ERP, the other operations over a `QuerySpec` (count, export, bulk, lookup), and the policies for ZK.
**Context:** [UI_PORTS_WAVE2.md](UI_PORTS_WAVE2.md) §6.3, [dynamia-ui-inventory.md](../next/dynamia-ui-inventory.md).

## Problem

A headless action is launched from a button. Many actions of `dynamia-erp` are not: the user leaves a widget in the toolbar (a
date range, a search box, a combo of states) and the list is filtered when its value changes. In ZK that widget is built by an
`ActionRenderer<Component>`, and `ActionMetadata.renderer` published its class name, which a remote client cannot use.

A filter is also not something to run on the server: it changes the query state of the list (`setParemeter`, `doQuery`), and the
headless controller has no such state. So the value must travel with the list request, not with an action execution.

About 52 actions of the ERP have a renderer of their own (heuristic count, 2026-11-08). Five kinds cover almost all: date, date
range, date and time range, text, choice. A button with another style (`ToolbarbuttonActionRenderer`, 12 uses) is not a control.

## Design

Three pieces that share one vocabulary.

### 1. `QuerySpec`: the standard message of a list query

`POST /api/{path}/find` takes plain JSON; the answer is the same list result as `GET`.

```json
{
  "version": 1,
  "filters": [
    {"field": "stockStatus", "op": "in", "value": ["OUT_STOCK"]},
    {"field": "publishDate", "op": "between", "value": ["2026-01-01", null]},
    {"field": "year", "op": "gte", "value": 2018}
  ],
  "search": "dune",
  "sort": [{"field": "title", "dir": "desc"}],
  "page": 1,
  "size": 20
}
```

- Operators mirror the `QueryConditions` factories: `eq`, `like`, `between`, `gte`, `lte`, `in`, `isNull`. A `null` side of a
  `between` is open. No `QueryCondition` object crosses the wire.
- One filter per field; filters combine with AND. "All" is a missing filter.
- `FilterConditions` (`tools.dynamia.crud.query`) is the one mapping from a filter to a condition, shared by the endpoint and ZK.
  Values are coerced by the type of the field (text, numbers, boolean, enum, `LocalDate`, `LocalDateTime`, `Date`); a date
  without time covers the whole day of a field that has time. Anything invalid is a 422 naming the field, never ignored.
- The descriptor is the whitelist: every field of `filters` and `sort` must be a field of the page's table view descriptor.
- Limits (`QuerySpecLimits`): 20 filters, 100 values of an `in`, 5 sort keys, 200 characters of search, page size 500.
- Same vocabulary as `ReportRunRequest` (`filters`, `page`, `size`, `sort`) so the platform has one request shape.
- `GET /api/{path}` now reads only `page`, `size`, the text search `_q` and the order `_sort`/`_order`. The per-field parameters
  were removed: they could filter on any bean property, hidden ones included.

TS: `client.crud(path).find(spec)`, types `QuerySpec`, `CrudFilter`, `CrudSort`.

### 2. Server filters: `QuerySpecFilter`

Every `QuerySpec` goes through `QuerySpecProcessor` before it becomes a query: limits, then the `QuerySpecFilter` beans by
`order()`, then the descriptor whitelist and the conditions. A filter may change the spec or refuse it:

```java
@Component
class NoAccountFilter implements QuerySpecFilter {
    public QuerySpec filter(QueryContext context, QuerySpec spec) {
        return spec.without("accountId");                           // drop what the client may not choose
        // return spec.with(new Filter("accountId", "eq", mine));   // or impose a condition it cannot override
        // throw new ValidationError("...");                        // or refuse (422); NavigationNotAllowedException is a 403
    }
}
```

The context carries the path, the entity class, the descriptor and the operation; the account comes from `AccountTenants`. A
filter only narrows what the descriptor allows. Dropping a field is not a defence by itself for something like `accountId`: impose
the current account as a condition after the client's filters.

### 3. Controls: `Control` + `HeadlessActionRenderer` + `FilterAction`

A renderer that describes instead of building.

```java
@InstallAction
@RunsOn(ActionRuntime.FRONTEND)
public class FilterBookByStockAction extends FilterAction {
    public FilterBookByStockAction() {
        super(Book.class, Control.choice("stockStatus", StockStatus.class).allLabel("All"));
        setName("Stock");
    }
}
```

- `Control(kind, field, label, options, allLabel, preset)` is plain data. Kinds: `DATE`, `DATE_RANGE`, `DATETIME_RANGE`, `TEXT`,
  `CHOICE`. `preset` (`TODAY`, `THIS_MONTH`, `LAST_MONTH`) is symbolic: each front end resolves it in the user's time zone.
- `HeadlessActionRenderer implements ActionRenderer<Control>`; `FilterAction` sets it. The action declares `FRONTEND`
  (the annotation is not inherited): the client draws the control and does not execute the action.
- `ActionMetadata` publishes `control` and sets `renderer` to the stable key `control` (not a class name, so existing clients
  that look at `renderer` keep working).
- **ZK:** `ControlRenderers.adapt` maps a control to the renderer ZK already had (date range box, combo, search box) wherever an
  action renderer is resolved. On a change `FilterAction.actionPerformed` turns the value into a filter with `FilterValues`, then
  into a condition with `FilterConditions`, sets it on the controller (or removes it) and runs the query.
- **Vue:** `VueControlActionRenderer` (renderer `control`) draws the widget. A change calls `TableView.setQueryFilter(field, filter)`;
  the list loader asks `POST /find` when the table has field filters and the plain `GET` otherwise. The advanced search panel
  uses the same path (`like` for text fields, `eq` for the rest).

## Rules that keep it portable

1. A `Control` is data: no component, callback or class name. A renderer that needs those stays `FRONTEND` and custom.
2. A control filters the list; it does not run the action remotely. An action that must do something with the value (the
   `CapturarItem*` family reads a ViewModel from the event) is not a control and stays `FRONTEND`.
3. Read context from the event, never from the page at construction time (`TipoVentaPage.current()` in a constructor).
4. The list endpoint stays the only place a client reads data; policies go in `QuerySpecFilter`, not in each action.

## Migrating the ERP actions

| Group | What they are | Do |
|---|---|---|
| A. Pure filter | `setParemeter` + `doQuery` behind a date, range, combo or text renderer | `extends FilterAction` with a `Control`; delete `actionPerformed` and `getRenderer` |
| B. Filter with logic | `setQueryResult`, `getParams().remove`, several derived fields | stays `FRONTEND`; some become A if the extra field is declared |
| C. Control that feeds an action | `CapturarItem*`, `AgregarDetalleVenta` | stays `FRONTEND` |

Open before migrating: `FiltrarFechaVentaAction` sets `creationDate` and `creationTimestamp` together; if the first is only an
index helper, one field is enough, otherwise the control needs a second derived field. The "-Todos-" combo item becomes
`allLabel`. About 17 ERP actions override `getRenderer()` instead of calling `setRenderer`; both forms are covered, the count in
the inventory only saw `setRenderer`.

## Known limits

- `QuerySpecFilter` runs for the REST list only. ZK builds its filters from controller parameters, so a policy that forbids a
  field protects REST, not ZK.
- Only the list operation uses a `QuerySpec`; `COUNT`, `EXPORT`, `BULK` and `LOOKUP` are not wired yet.
- `QuerySpec` has no OR groups and no filters across relations.
- Presets are resolved in the server's time zone in ZK (it has no other) and in the browser's in Vue.
- Outside this design, and not fixed: `POST /apps/api/crud-service/{className}/find` binds a `QueryParameters` whose keys reach
  the query text unchecked (`QueryBuilder`), a possible JPQL injection on a class-name endpoint. Validate the keys against the
  descriptor or stop using it from Vue.
- The `endpoint` published for an entity action says `/actions/{id}` but the controller maps `/action/{id}` (the SDK uses the latter).
