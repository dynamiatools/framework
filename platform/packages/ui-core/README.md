# @dynamia-tools/ui-core

> Framework-agnostic view system core for **Dynamia Platform** — zero DOM, zero Vue/React.

`@dynamia-tools/ui-core` implements the `ViewType → View → ViewRenderer → Viewer` pattern that mirrors the Dynamia Platform Java backend exactly. It provides all the headless logic for form, table, CRUD, tree and entity-picker views. Any UI framework adapter (Vue, React, Angular, …) is built on top of this package.

---

## Table of Contents

- [Installation](#installation)
- [Architecture](#architecture)
- [ViewType](#viewtype)
- [View](#view)
  - [FormView](#formview)
  - [TableView](#tableview)
  - [CrudView](#crudview)
  - [TreeView](#treeview)
  - [ConfigView](#configview)
  - [EntityPickerView](#entitypickerview)
- [Viewer](#viewer)
- [ViewRendererRegistry](#viewrendererregistry)
- [ViewRenderer interfaces](#viewrenderer-interfaces)
- [FieldComponent](#fieldcomponent)
- [Resolvers](#resolvers)
  - [FieldResolver](#fieldresolver)
  - [LayoutEngine](#layoutengine)
  - [ActionResolver](#actionresolver)
- [Utils](#utils)
  - [Converters](#converters)
  - [Validators](#validators)
- [Extending with custom ViewTypes](#extending-with-custom-viewtypes)
- [Contributing](#contributing)
- [License](#license)

---

## Installation

```bash
# pnpm (recommended)
pnpm add @dynamia-tools/ui-core @dynamia-tools/sdk

# npm
npm install @dynamia-tools/ui-core @dynamia-tools/sdk

# yarn
yarn add @dynamia-tools/ui-core @dynamia-tools/sdk
```

`@dynamia-tools/sdk` is a peer dependency — it provides the HTTP client and all server-mirroring types (`ViewDescriptor`, `EntityMetadata`, `ActionMetadata`, …).

---

## Architecture

```
@dynamia-tools/sdk         ← HTTP client, ViewDescriptor, EntityMetadata, …
        ↓
@dynamia-tools/ui-core     ← ViewType, View, ViewRenderer, Viewer  (framework-agnostic)
        ↓
@dynamia-tools/vue         ← VueViewer, VueFormView, composables, components
        ↓
@dynamia-tools/react (*)   ← ReactViewer, ReactFormView, hooks, components
```

`ui-core` has zero runtime DOM or framework dependencies. It can run in Node.js, a browser, a Web Worker, or any environment.

The layered design means a React or Angular adapter reuses `ui-core` 100% — only the framework-specific rendering layer changes.

---

## ViewType

`ViewType` is an **open extension interface**, not a closed enum. Anyone can define a new view type by creating a plain object that satisfies the interface:

```typescript
import { ViewType, ViewTypes, ViewRendererRegistry } from '@dynamia-tools/ui-core';

// Built-in types shipped with ui-core
ViewTypes.Form         // { name: 'form' }
ViewTypes.Table        // { name: 'table' }
ViewTypes.Crud         // { name: 'crud' }
ViewTypes.Tree         // { name: 'tree' }
ViewTypes.Config       // { name: 'config' }
ViewTypes.EntityPicker // { name: 'entitypicker' }
ViewTypes.EntityFilters// { name: 'entityfilters' }
ViewTypes.Export       // { name: 'export' }
ViewTypes.Json         // { name: 'json' }

// Custom view type — no core modifications needed
const KanbanViewType: ViewType = { name: 'kanban' };
```

---

## View

`View` is the abstract base class for all view types. It owns an event emitter, the view descriptor and entity metadata:

```typescript
import { View, ViewTypes, FormView } from '@dynamia-tools/ui-core';
import type { ViewDescriptor } from '@dynamia-tools/sdk';

// Concrete View subclass
const descriptor: ViewDescriptor = { /* from backend */ };
const form = new FormView(descriptor, entityMetadata);

await form.initialize();

// Event system
form.on('change', ({ field, value }) => console.log(field, '=', value));
form.on('submit', (values) => console.log('submitted', values));

// Value management
form.setValue({ title: 'Clean Code', author: 'Robert C. Martin' });
const values = form.getValue(); // { title: '...', author: '...' }
```

### FormView

Handles field resolution, grid layout, values and validation for entity forms.

```typescript
import { FormView, FieldResolver, LayoutEngine } from '@dynamia-tools/ui-core';

const form = new FormView(descriptor, metadata);
await form.initialize();

// Set / get individual field values
form.setFieldValue('title', 'Clean Code');
form.getFieldValue('title'); // 'Clean Code'

// Get all resolved fields (with component, label, span)
form.getResolvedFields();

// Get computed grid layout
form.getLayout();
// → { columns: 3, groups: [{ name: '', rows: [{ fields: [...] }] }], allFields: [...] }

// Validate (checks required fields)
form.validate(); // true | false
form.getErrors(); // { fieldName: 'Error message', ... }

// Submit (validates + emits 'submit')
await form.submit();

// Reset to empty state
form.reset();
```

### TableView

Manages tabular data: columns, rows, pagination, sorting, search and row selection.

```typescript
import { TableView } from '@dynamia-tools/ui-core';

const table = new TableView(descriptor, metadata);

// Supply a loader function (called on load/nextPage/sort/search)
table.setLoader(async (params) => {
  const result = await client.crud('store/books').findAll(params);
  return {
    rows: result.content,
    pagination: {
      page: result.page,
      pageSize: result.pageSize,
      totalSize: result.total,
      pagesNumber: result.totalPages,
      firstResult: (result.page - 1) * result.pageSize,
    },
  };
});

await table.initialize();
await table.load();

// Navigation
await table.nextPage();
await table.prevPage();

// Sorting (toggles asc/desc)
await table.sort('title');

// Search
await table.search('clean code');

// Row selection
table.on('select', (row) => console.log('selected:', row));
table.selectRow(row);
```

### CrudView

Orchestrates a `FormView` and a `TableView` together with a mode state machine (`list | create | edit`).

```typescript
import { CrudView } from '@dynamia-tools/ui-core';

const crud = new CrudView(descriptor, metadata);
crud.tableView.setLoader(loader);
await crud.initialize();

// Mode transitions
crud.startCreate();          // mode → 'create', resets form
crud.startEdit(entity);      // mode → 'edit', populates form
crud.cancelEdit();           // mode → 'list', resets form

// Listen to save/delete to call your API
crud.on('save', async ({ mode, data }) => {
  if (mode === 'create') await client.crud('books').create(data);
  else await client.crud('books').update(data.id, data);
});
crud.on('delete', async (entity) => {
  await client.crud('books').delete(entity.id);
});

// Trigger save (validates + emits 'save' + refreshes table)
await crud.save();

// Trigger delete
await crud.delete(entity);
```

### TreeView

Hierarchical tree with expand/collapse and node selection.

```typescript
import { TreeView } from '@dynamia-tools/ui-core';

const tree = new TreeView(descriptor);
await tree.initialize();

tree.setSource(nodes); // TreeNode[]
tree.expand(node);
tree.collapse(node);
tree.selectNode(node);
tree.on('select', (node) => console.log(node));
```

### ConfigView

Module configuration parameters with load/save lifecycle.

```typescript
import { ConfigView } from '@dynamia-tools/ui-core';

const config = new ConfigView(descriptor);
config.setLoader(async () => fetchParameters());
config.setSaver(async (values) => saveParameters(values));

await config.initialize();
await config.loadParameters();

config.setParameterValue('theme', 'dark');
await config.saveParameters();
```

### EntityPickerView

Entity search and selection (used for relation pickers).

```typescript
import { EntityPickerView } from '@dynamia-tools/ui-core';

const picker = new EntityPickerView(descriptor);
picker.setSearcher(async (query) => {
  const result = await client.crud('books').findAll({ q: query });
  return result.content;
});

await picker.initialize();
await picker.search('clean');
picker.select(picker.getSearchResults()[0]);
console.log(picker.getValue()); // selected entity
```

---

## Viewer

`Viewer` is the **primary abstraction** — the single entry point for rendering any view. It mirrors the Java `tools.dynamia.zk.viewers.ui.Viewer` component.

Instead of instantiating `FormView`, `TableView`, or `CrudView` directly, consumers use `Viewer` and let it resolve the correct `ViewType → View → ViewRenderer` chain internally via `ViewRendererRegistry`.

```typescript
import { Viewer } from '@dynamia-tools/ui-core';
import { DynamiaClient } from '@dynamia-tools/sdk';

const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: '...' });

// By view type + entity class (fetches descriptor from backend)
const viewer = new Viewer({
  viewType: 'form',
  beanClass: 'com.example.Book',
  client,
});
await viewer.initialize();

// By pre-loaded descriptor (skips network fetch)
const viewer2 = new Viewer({ descriptor: preloadedDescriptor });
await viewer2.initialize();

// By descriptor ID
const viewer3 = new Viewer({ descriptorId: 'BookCustomForm', client });
await viewer3.initialize();

// Value management
viewer.setValue({ title: 'Clean Code' });
viewer.getValue();

// Event buffering — register before initialize, applied after
viewer.on('submit', (values) => saveToBackend(values));

// Read-only mode
viewer.setReadonly(true);

// Access the resolved view instance
const view = viewer.view; // FormView | TableView | CrudView | ...

// Actions
const actions = viewer.getActions(); // ActionMetadata[]
```

**Resolution logic:**

1. If `descriptorId` set → fetch from `client.metadata`
2. If `descriptor` set → use directly
3. Else → fetch via `client.metadata.getEntityView(beanClass, viewType)`
4. With resolved descriptor → `ViewRendererRegistry.createView(viewType, descriptor, metadata)`
5. Apply `value`, `source`, `readOnly` to the view
6. Load actions from entity metadata (if `client` + `beanClass` provided)
7. Event listeners registered before `initialize()` are buffered and applied after

---

## ViewRendererRegistry

Central static registry that maps `ViewType → ViewRenderer` and `ViewType → View factory`.

Framework adapters call `register()` and `registerViewFactory()` during plugin installation:

```typescript
import { ViewRendererRegistry, ViewTypes } from '@dynamia-tools/ui-core';

// Register a renderer (e.g. from a Vue adapter)
ViewRendererRegistry.register(ViewTypes.Form, myVueFormRenderer);

// Register a view factory
ViewRendererRegistry.registerViewFactory(
  ViewTypes.Form,
  (descriptor, metadata) => new MyFormView(descriptor, metadata)
);

// Check if registered
ViewRendererRegistry.hasRenderer(ViewTypes.Form);
ViewRendererRegistry.hasViewFactory(ViewTypes.Form);

// Retrieve renderer
const renderer = ViewRendererRegistry.getRenderer(ViewTypes.Form);
```

---

## ViewRenderer interfaces

```typescript
import type { ViewRenderer, FormRenderer, TableRenderer, CrudRenderer, FieldRenderer } from '@dynamia-tools/ui-core';

// Generic interface — implement for each ViewType
interface ViewRenderer<TView extends View, TOutput> {
  readonly supportedViewType: ViewType;
  render(view: TView): TOutput;
}

// Typed sub-interfaces
interface FormRenderer<TOutput>  extends ViewRenderer<FormView, TOutput>  {}
interface TableRenderer<TOutput> extends ViewRenderer<TableView, TOutput> {}
interface CrudRenderer<TOutput>  extends ViewRenderer<CrudView, TOutput>  {}
interface TreeRenderer<TOutput>  extends ViewRenderer<TreeView, TOutput>  {}

// For individual field rendering
interface FieldRenderer<TOutput> {
  readonly supportedComponent: string;
  render(field: ResolvedField, view: FormView): TOutput;
}
```

---

## FieldComponent

`FieldComponent` is a **const object** (not a TypeScript enum) so external modules can extend it without modifying core. Component names match ZK component names exactly for vocabulary consistency.

```typescript
import { FieldComponents } from '@dynamia-tools/ui-core';

FieldComponents.Textbox       // 'textbox'
FieldComponents.Intbox        // 'intbox'
FieldComponents.Longbox       // 'longbox'
FieldComponents.Decimalbox    // 'decimalbox'
FieldComponents.Spinner       // 'spinner'
FieldComponents.Combobox      // 'combobox'
FieldComponents.Datebox       // 'datebox'
FieldComponents.Checkbox      // 'checkbox'
FieldComponents.EntityPicker  // 'entitypicker'
FieldComponents.EntityRefPicker  // 'entityrefpicker'
FieldComponents.EntityRefLabel   // 'entityreflabel'
FieldComponents.CoolLabel     // 'coollabel'
FieldComponents.Link          // 'link'
FieldComponents.Textareabox   // 'textareabox'
// … and more
```

---

## Resolvers

### FieldResolver

Resolves `ViewField[]` from a descriptor into `ResolvedField[]`, inferring the component type from the field's Java class name when not explicitly specified:

```typescript
import { FieldResolver } from '@dynamia-tools/ui-core';

const resolved = FieldResolver.resolveFields(descriptor, metadata);
// Each ResolvedField has:
//   resolvedComponent: 'textbox' | 'intbox' | ...
//   resolvedLabel: 'Book Title'
//   gridSpan: 2
//   resolvedVisible: true
//   resolvedRequired: false
//   group?: 'Details'
```

**Component inference rules:**

| Java type | Resolved component |
|-----------|-------------------|
| `String` | `textbox` |
| `Integer` / `int` | `intbox` |
| `Long` / `long` | `longbox` |
| `Double` / `Float` / `BigDecimal` | `decimalbox` |
| `Boolean` / `boolean` | `checkbox` |
| `Date` / `LocalDate` / `LocalDateTime` | `datebox` |
| Enum subtypes | `combobox` |
| Unknown / default | `textbox` |

Override per-field with `params.component` in the descriptor YAML:

```yaml
fields:
  description:
    params:
      component: textareabox
```

### LayoutEngine

Computes the grid layout from a descriptor's `params.columns` and the resolved fields:

```typescript
import { LayoutEngine } from '@dynamia-tools/ui-core';

const layout = LayoutEngine.computeLayout(descriptor, resolvedFields);
// layout.columns = 3
// layout.groups = [
//   {
//     name: '',
//     rows: [
//       { fields: [titleField, authorField, yearField] },
//       { fields: [isbnField] },
//     ]
//   },
//   { name: 'Details', rows: [...] }
// ]
```

Fields are wrapped to the next row when their cumulative span would exceed `columns`. The `params.span` per field controls how many columns it occupies (default: 1).

### ActionResolver

Resolves the list of actions applicable to an entity view:

```typescript
import { ActionResolver } from '@dynamia-tools/ui-core';

const actions = ActionResolver.resolveActions(entityMetadata, 'crud');
```

---

## Utils

### Converters

Built-in converters for display formatting:

```typescript
import {
  currencyConverter,
  currencySimpleConverter,
  decimalConverter,
  dateConverter,
  dateTimeConverter,
  builtinConverters,
} from '@dynamia-tools/ui-core';

currencyConverter(1234.5);          // '1,234.50'
currencySimpleConverter(1234);      // '1,234'
decimalConverter(3.14159, { decimals: 2 }); // '3.14'
dateConverter(new Date());          // locale date string
dateTimeConverter(new Date());      // locale date-time string

// All built-ins as a map
builtinConverters['currency'](value);
```

The `Converter` function signature is:

```typescript
type Converter = (value: unknown, params?: Record<string, unknown>) => string;
```

### Validators

Built-in field value validators:

```typescript
import { requiredValidator, constraintValidator, builtinValidators } from '@dynamia-tools/ui-core';

requiredValidator('');       // 'This field is required'
requiredValidator('hello');  // null (valid)

constraintValidator('abc', { pattern: '^[0-9]+$', message: 'Numbers only' });
// 'Numbers only'

constraintValidator('123', { pattern: '^[0-9]+$' });
// null (valid)
```

The `Validator` function signature is:

```typescript
type Validator = (value: unknown, params?: Record<string, unknown>) => string | null;
```

---

## Extending with custom ViewTypes

The open extension model allows any module to add new view types and renderers without modifying `ui-core`:

```typescript
import { ViewType, ViewRendererRegistry, View, ViewRenderer } from '@dynamia-tools/ui-core';
import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';

// 1. Define your view type
const KanbanViewType: ViewType = { name: 'kanban' };

// 2. Implement a View subclass
class KanbanView extends View {
  constructor(d: ViewDescriptor, m: EntityMetadata | null) {
    super(KanbanViewType, d, m);
  }
  async initialize() { /* fetch columns, cards, etc. */ }
  validate() { return true; }
}

// 3. Implement a renderer (outputs whatever the framework expects)
class VueKanbanRenderer implements ViewRenderer<KanbanView, unknown> {
  readonly supportedViewType = KanbanViewType;
  render(view: KanbanView) { return MyKanbanVueComponent; }
}

// 4. Register — now <Viewer view-type="kanban" /> works automatically
ViewRendererRegistry.register(KanbanViewType, new VueKanbanRenderer());
ViewRendererRegistry.registerViewFactory(KanbanViewType, (d, m) => new KanbanView(d, m));
```

---

## Contributing

See the monorepo [CONTRIBUTING.md](../../../CONTRIBUTING.md) for full guidelines.

```bash
# Install all workspace dependencies
pnpm install

# Build ui-core
pnpm --filter @dynamia-tools/ui-core build

# Type-check
pnpm --filter @dynamia-tools/ui-core typecheck

# Run tests
pnpm --filter @dynamia-tools/ui-core test
```

---

## License

[Apache License 2.0](../../../LICENSE) — © Dynamia Soluciones IT SAS
