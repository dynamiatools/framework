# @dynamia-tools/vue

> Vue 3 adapter for **Dynamia Platform** — reactive views, composables and components built on `@dynamia-tools/ui-core`.

`@dynamia-tools/vue` wraps every `ui-core` view class with Vue `ref`/`computed` reactivity, provides composables for a clean developer experience, and ships a set of thin Vue components — all the way down to individual field inputs. The central component is `<DynamiaViewer>`, which resolves any view type automatically, mirroring ZK's `Viewer` on the backend. For full app shells driven by `NavigationTree`, `<DynamiaCrudPage>` can render `NavigationNode` entries of type `CrudPage` end-to-end.

---

## Table of Contents

- [Installation](#installation)
- [Plugin Setup](#plugin-setup)
- [Quick Start](#quick-start)
- [Universal Component: `<DynamiaViewer>`](#universal-component-dynamiaviewer)
- [Composables](#composables)
  - [useViewer](#useviewer)
  - [useView](#useview)
  - [useForm](#useform)
  - [useTable](#usetable)
  - [useCrud](#usecrud)
  - [useCrudPage](#usecrudpage)
  - [useEntityPicker](#useentitypicker)
  - [useNavigation](#usenavigation)
- [Full App Shell: Auto navigation + CrudPage rendering](#full-app-shell-auto-navigation--crudpage-rendering)
- [Vue-reactive View classes](#vue-reactive-view-classes)
  - [VueViewer](#vueviewer)
  - [VueFormView](#vueformview)
  - [VueTableView](#vuetableview)
  - [VueCrudView](#vuecrudview)
- [Components](#components)
  - [Form.vue](#formvue)
  - [Table.vue](#tablevue)
  - [Crud.vue](#crudvue)
  - [CrudPage.vue](#crudpagevue)
  - [Field.vue](#fieldvue)
  - [Field components](#field-components)
  - [Actions.vue](#actionsvue)
  - [NavMenu.vue](#navmenuvue)
  - [NavBreadcrumb.vue](#navbreadcrumbvue)
- [Custom ViewType example](#custom-viewtype-example)
- [Architecture](#architecture)
- [Contributing](#contributing)
- [License](#license)

---

## Installation

```bash
# pnpm (recommended)
pnpm add @dynamia-tools/vue @dynamia-tools/ui-core @dynamia-tools/sdk vue

# npm
npm install @dynamia-tools/vue @dynamia-tools/ui-core @dynamia-tools/sdk vue

# yarn
yarn add @dynamia-tools/vue @dynamia-tools/ui-core @dynamia-tools/sdk vue
```

`vue >= 3.4`, `@dynamia-tools/ui-core` and `@dynamia-tools/sdk` are peer dependencies.

---

## Plugin Setup

Register the plugin once in your application entry point. It registers all built-in view renderers, view factories and global components:

```typescript
// main.ts
import { createApp } from 'vue';
import { DynamiaVue } from '@dynamia-tools/vue';
import App from './App.vue';

const app = createApp(App);
app.use(DynamiaVue);
app.mount('#app');
```

After `app.use(DynamiaVue)` the following components are available globally (no import needed in templates):

| Component | Description |
|-----------|-------------|
| `<DynamiaViewer>` | Universal view host — resolves any view type |
| `<DynamiaForm>` | Form rendering |
| `<DynamiaTable>` | Table rendering |
| `<DynamiaCrud>` | Full CRUD (form + table + actions) |
| `<DynamiaField>` | Single field dispatcher |
| `<DynamiaActions>` | Action toolbar |
| `<DynamiaNavMenu>` | Navigation sidebar/menu |
| `<DynamiaNavBreadcrumb>` | Navigation breadcrumb |
| `<DynamiaCrudPage>` | Fully wired CRUD page for `NavigationNode.type === 'CrudPage'` |

---

## Quick Start

```vue
<script setup lang="ts">
import { DynamiaClient } from '@dynamia-tools/sdk';
import { useViewer } from '@dynamia-tools/vue';

const client = new DynamiaClient({ baseUrl: '/api', token: 'your-token' });

const { viewer, loading, error } = useViewer({
  viewType: 'crud',
  beanClass: 'com.example.Book',
  client,
});
</script>

<template>
  <DynamiaViewer
    view-type="crud"
    bean-class="com.example.Book"
  />
</template>
```

---

## Universal Component: `<DynamiaViewer>`

`<DynamiaViewer>` is the **primary component** for rendering any view type. It handles descriptor resolution, view initialization, loading state, and error display automatically.

```vue
<!-- By view type + entity class (descriptor fetched from backend) -->
<DynamiaViewer view-type="form"  bean-class="com.example.Book" v-model="book" @submit="onSave" />
<DynamiaViewer view-type="table" bean-class="com.example.Book" />
<DynamiaViewer view-type="crud"  bean-class="com.example.Book" />

<!-- By descriptor ID (fetched by ID from backend) -->
<DynamiaViewer descriptor-id="BookCustomForm" v-model="book" />

<!-- With a pre-loaded descriptor (skips network fetch) -->
<DynamiaViewer :descriptor="myDescriptor" v-model="book" :read-only="true" />

<!-- Custom view type registered by a third-party module -->
<DynamiaViewer view-type="kanban" bean-class="com.example.Task" />

<!-- With custom loading and error slots -->
<DynamiaViewer view-type="form" bean-class="com.example.Book">
  <template #loading>
    <MySpinner />
  </template>
  <template #error="{ error }">
    <MyAlert :message="error" />
  </template>
</DynamiaViewer>
```

**Props:**

| Prop | Type | Description |
|------|------|-------------|
| `viewType` | `string` | View type name: `'form'`, `'table'`, `'crud'`, `'tree'`, `'kanban'`, … |
| `beanClass` | `string` | Fully-qualified entity class name |
| `descriptor` | `ViewDescriptor` | Pre-loaded descriptor (skips backend fetch) |
| `descriptorId` | `string` | Descriptor ID to fetch from backend |
| `readOnly` | `boolean` | Propagates to the inner view |

**Events:**

| Event | Payload | Description |
|-------|---------|-------------|
| `ready` | `View` | Emitted when the view is initialized |
| `error` | `string` | Emitted on initialization failure |

**Slots:**

| Slot | Props | Description |
|------|-------|-------------|
| `loading` | — | Shown during initialization |
| `error` | `{ error: string }` | Shown on failure |
| `unsupported` | — | Shown when no renderer is registered for the view type |

---

## Composables

### useViewer

The primary composable. Creates a `VueViewer`, initializes it on mount, destroys it on unmount.

```typescript
import { useViewer } from '@dynamia-tools/vue';
import { DynamiaClient } from '@dynamia-tools/sdk';

const client = new DynamiaClient({ baseUrl: '/api', token: '...' });

const { viewer, view, loading, error, getValue, setValue, setReadonly } = useViewer({
  viewType: 'form',
  beanClass: 'com.example.Book',
  client,
  value: { title: 'Clean Code' },   // optional initial value
  readOnly: false,
});

// viewer  — VueViewer instance (full class API)
// view    — ShallowRef<View | null> — reactive resolved view
// loading — Ref<boolean>
// error   — Ref<string | null>
```

### useView

Generic lifecycle composable for any `VueView` subclass.

```typescript
import { useView, VueFormView } from '@dynamia-tools/vue';

const { view, loading, error, initialized } = useView(
  () => new VueFormView(descriptor, metadata),
);

// view        — VueFormView
// loading     — Ref<boolean>
// error       — Ref<string | null>
// initialized — Ref<boolean>
```

### useForm

Direct access to a `VueFormView`:

```typescript
import { useForm } from '@dynamia-tools/vue';

const { view, values, errors, loading, fields, layout, validate, submit, reset, setFieldValue } =
  useForm({
    descriptor,                    // required: ViewDescriptor
    entityMetadata,                // optional: EntityMetadata
    initialData: { title: '...' }, // optional initial form data
  });

// values   — Ref<Record<string, unknown>>
// errors   — Ref<Record<string, string>>
// fields   — ComputedRef<ResolvedField[]>
// layout   — ComputedRef<ResolvedLayout | null>

values.value.title = 'New Title';
validate();           // boolean
await submit();       // emits 'submit' on view
reset();
```

### useTable

Direct access to a `VueTableView`:

```typescript
import { useTable } from '@dynamia-tools/vue';

const { view, rows, columns, pagination, loading, sort, search, load, nextPage, prevPage } =
  useTable({
    descriptor,
    entityMetadata,
    autoLoad: true,  // load on mount (default: true)
    loader: async (params) => {
      const result = await client.crud('store/books').findAll(params);
      return {
        rows: result.content,
        pagination: {
          page: result.page, pageSize: result.pageSize,
          totalSize: result.total, pagesNumber: result.totalPages,
          firstResult: 0,
        },
      };
    },
  });

// rows       — Ref<unknown[]>
// columns    — ComputedRef<ResolvedField[]>
// pagination — Ref<CrudPageable | null>

await sort('title');
await search('clean');
await nextPage();
```

### useCrud

Full CRUD lifecycle (form + table + mode state machine):

```typescript
import { useCrud } from '@dynamia-tools/vue';

const { view, mode, form, table, showForm, showTable, startCreate, startEdit, cancelEdit, save, remove } =
  useCrud({
    descriptor,
    loader: async (params) => { /* fetch rows */ },
    onSave: async (data, mode) => {
      if (mode === 'create') await client.crud('books').create(data);
      else await client.crud('books').update(data.id, data);
    },
    onDelete: async (entity) => {
      await client.crud('books').delete(entity.id);
    },
  });

// mode      — Ref<'list' | 'create' | 'edit'>
// showForm  — ComputedRef<boolean>
// showTable — ComputedRef<boolean>
// form      — VueFormView
// table     — VueTableView
```

### useEntityPicker

Entity search and selection:

```typescript
import { useEntityPicker } from '@dynamia-tools/vue';

const { view, searchResults, selectedEntity, searchQuery, loading, search, select, clear } =
  useEntityPicker({
    descriptor,
    searcher: async (query) => {
      const result = await client.crud('books').findAll({ q: query });
      return result.content;
    },
    initialValue: currentBook,
  });
```

### useCrudPage

Builds a complete CRUD page from a navigation node of type `CrudPage`. It resolves metadata + descriptor via `CrudPageResolver`, wires table loading and save/delete handlers, initializes the view, and loads the first page.

```typescript
import { useCrudPage } from '@dynamia-tools/vue';

const { view, loading, error, reload } = useCrudPage({
  node,   // NavigationNode (type: 'CrudPage')
  client, // DynamiaClient
});

// view    — Ref<VueCrudView | null>
// loading — Ref<boolean>
// error   — Ref<string | null>
// reload  — () => Promise<void>
```

### useNavigation

Fetches and caches the application navigation tree. Uses SDK types directly — no new types defined.

```typescript
import { useNavigation } from '@dynamia-tools/vue';

const {
  tree,
  nodes,
  currentModule,
  currentGroup,
  currentPage,
  currentPath,
  loading,
  navigateTo,
  clearCache,
  reload,
} = useNavigation(client);

// tree    — Ref<NavigationTree | null>
// nodes   — ComputedRef<NavigationNode[]>
// currentModule — ComputedRef<NavigationNode | null>
// currentGroup  — ComputedRef<NavigationNode | null>
// currentPage   — ComputedRef<NavigationNode | null>
// currentPath   — Ref<string | null>

navigateTo('/pages/store/books');
```

The navigation tree is cached in module memory after the first fetch. Call `clearCache()` and then `reload()` to force a re-fetch.

---

## Full App Shell: Auto navigation + CrudPage rendering

With `useNavigation` + `useCrudPage` + `<DynamiaCrudPage>`, you can build complete metadata-driven apps where:

- navigation loads automatically on mount,
- the first available page is selected automatically,
- `CrudPage` nodes render instantly without manual CRUD wiring.

```vue
<script setup lang="ts">
import { computed, watch } from 'vue';
import type { NavigationNode, DynamiaClient } from '@dynamia-tools/sdk';
import { useNavigation } from '@dynamia-tools/vue';

const client = new DynamiaClient({ baseUrl: '/api', token: '...' });

const {
  nodes,
  currentPath,
  currentPage,
  currentModule,
  currentGroup,
  navigateTo,
} = useNavigation(client);

function findFirstPage(list: NavigationNode[]): NavigationNode | null {
  for (const node of list) {
    if (node.children?.length) {
      const nested = findFirstPage(node.children);
      if (nested) return nested;
    } else if (node.internalPath) {
      return node;
    }
  }
  return null;
}

watch(
  nodes,
  (value) => {
    if (currentPath.value) return;
    const first = findFirstPage(value);
    if (first?.internalPath) navigateTo(first.internalPath);
  },
  { immediate: true },
);

const activeNode = computed(() => currentPage.value);
</script>

<template>
  <aside>
    <DynamiaNavMenu :nodes="nodes" :current-path="currentPath" @navigate="navigateTo" />
  </aside>

  <header>
    <DynamiaNavBreadcrumb
      :module="currentModule"
      :group="currentGroup"
      :page="activeNode"
    />
  </header>

  <main>
    <DynamiaCrudPage
      v-if="activeNode?.type === 'CrudPage'"
      :node="activeNode"
      :client="client"
    />

    <p v-else-if="activeNode">
      Node type "{{ activeNode.type }}" is selected. Provide a renderer for this type.
    </p>
  </main>
</template>
```

---

## Vue-reactive View classes

All view classes extend their `ui-core` counterparts and replace state with Vue `ref`/`computed`:

### VueViewer

```typescript
import { VueViewer } from '@dynamia-tools/vue';

const viewer = new VueViewer({ viewType: 'form', beanClass: 'com.example.Book', client });
await viewer.initialize();

viewer.loading.value        // Ref<boolean>
viewer.error.value          // Ref<string | null>
viewer.currentView.value    // ShallowRef<View | null>
viewer.currentDescriptor.value // ShallowRef<ViewDescriptor | null>
```

### VueFormView

```typescript
import { VueFormView } from '@dynamia-tools/vue';

const form = new VueFormView(descriptor, metadata);
await form.initialize();

form.values.value          // Ref<Record<string, unknown>>
form.errors.value          // Ref<Record<string, string>>
form.isLoading.value       // Ref<boolean>
form.isDirty.value         // Ref<boolean>
form.resolvedFields.value  // ComputedRef<ResolvedField[]>
form.layout.value          // ComputedRef<ResolvedLayout | null>
```

### VueTableView

```typescript
import { VueTableView } from '@dynamia-tools/vue';

const table = new VueTableView(descriptor, metadata);
table.setLoader(loader);
await table.initialize();
await table.load();

table.rows.value       // Ref<unknown[]>
table.columns.value    // ComputedRef<ResolvedField[]>
table.pagination.value // Ref<CrudPageable | null>
table.isLoading.value  // Ref<boolean>
table.selectedRow.value// Ref<unknown>
```

### VueCrudView

```typescript
import { VueCrudView } from '@dynamia-tools/vue';

const crud = new VueCrudView(descriptor, metadata);
await crud.initialize();

crud.mode.value      // Ref<'list' | 'create' | 'edit'>
crud.showForm.value  // ComputedRef<boolean>
crud.showTable.value // ComputedRef<boolean>
crud.formView        // VueFormView
crud.tableView       // VueTableView
```

---

## Components

### Form.vue

Renders a `VueFormView` using the computed grid layout. Uses `<Field>` to render each cell.

```vue
<DynamiaForm :view="formView" @submit="onSubmit" @cancel="onCancel">
  <!-- Override action buttons -->
  <template #actions>
    <button type="submit">Save</button>
    <button type="button" @click="cancel">Discard</button>
  </template>
</DynamiaForm>
```

### Table.vue

Renders a `VueTableView` with header, rows, empty state and pagination.

```vue
<DynamiaTable :view="tableView">
  <!-- Custom cell rendering -->
  <template #cell-status="{ row }">
    <span :class="`badge-${row.status}`">{{ row.status }}</span>
  </template>
  <!-- Row action buttons -->
  <template #actions="{ row }">
    <button @click="edit(row)">Edit</button>
    <button @click="remove(row)">Delete</button>
  </template>
  <!-- Empty state -->
  <template #empty>
    <p>No books found.</p>
  </template>
</DynamiaTable>
```

### Crud.vue

Combines `<Table>` and `<Form>` into a full CRUD interface with mode transitions.

```vue
<DynamiaCrud :view="crudView" :actions="entityActions" @save="onSave" @delete="onDelete" />
```

### CrudPage.vue

Renders a full CRUD page directly from a navigation node of type `CrudPage`.

```vue
<DynamiaCrudPage
  :node="selectedNode"
  :client="client"
  :read-only="false"
  :actions="extraActions"
  @save="onSave"
  @delete="onDelete"
/>
```

`<DynamiaCrudPage>` internally uses `useCrudPage()` and exposes loading/error slots:

```vue
<DynamiaCrudPage :node="selectedNode" :client="client">
  <template #loading>
    <MySpinner />
  </template>
  <template #error="{ error }">
    <MyAlert :message="error" />
  </template>
</DynamiaCrudPage>
```

### Field.vue

Dispatches to the correct field component based on `field.resolvedComponent`. Falls back to a plain `<input type="text">` for unknown component types.

```vue
<DynamiaField
  :field="resolvedField"
  :view="formView"
  v-model="values[field.name]"
  :read-only="false"
/>
```

### Field components

All field components live in `src/components/fields/` and are loaded lazily via `defineAsyncComponent`:

| Component | ZK Equivalent | Description |
|-----------|--------------|-------------|
| `Textbox.vue` | `Textbox` | Single-line text input |
| `Textareabox.vue` | `Textareabox` | Multi-line textarea |
| `Intbox.vue` | `Intbox` | Integer number input |
| `Spinner.vue` | `Spinner` / `Doublespinner` | Decimal number input |
| `Combobox.vue` | `Combobox` | Dropdown select |
| `Datebox.vue` | `Datebox` | Date / datetime-local input |
| `Checkbox.vue` | `Checkbox` | Boolean checkbox |
| `EntityPicker.vue` | `EntityPicker` | Search-based entity selection |
| `EntityRefPicker.vue` | `EntityRefPicker` | Reference entity picker |
| `EntityRefLabel.vue` | `EntityRefLabel` | Read-only entity reference display |
| `CoolLabel.vue` | `CoolLabel` | Image + title + subtitle + description |
| `Link.vue` | `Link` | Clickable link that triggers an action/event |

All field components accept these common props:

```typescript
interface FieldProps {
  field: ResolvedField;           // resolved field descriptor
  modelValue?: unknown;           // current value (v-model)
  readOnly?: boolean;             // disables editing
  params?: Record<string, unknown>; // descriptor params
}
```

And emit `update:modelValue` for `v-model` support.

**Combobox options** can be provided via `params.options` or `params.values`:

```yaml
# descriptor YAML
fields:
  status:
    params:
      options:
        - { value: 'ACTIVE', label: 'Active' }
        - { value: 'INACTIVE', label: 'Inactive' }
```

**EntityPicker search** is provided at runtime via `params.searcher`:

```typescript
field.params['searcher'] = async (query: string) => {
  return (await client.crud('authors').findAll({ q: query })).content;
};
```

### Actions.vue

Renders a list of `ActionMetadata` as buttons in a toolbar.

```vue
<DynamiaActions :actions="entityActions" :view="crudView" @action="handleAction" />
```

**Props:**

| Prop | Type | Description |
|------|------|-------------|
| `actions` | `ActionMetadata[]` | Actions to render |
| `view` | `View` | The view this toolbar belongs to |

**Events:**

| Event | Payload | Description |
|-------|---------|-------------|
| `action` | `ActionMetadata` | Emitted when an action button is clicked |

### NavMenu.vue

Renders a `NavigationTree` as a sidebar menu. Uses SDK types directly.

```vue
<DynamiaNavMenu
  :nodes="nodes"
  :current-path="currentPath"
  @navigate="navigateTo"
/>
```

**Props:**

| Prop | Type | Description |
|------|------|-------------|
| `nodes` | `NavigationNode[]` | Top-level navigation nodes (modules) |
| `currentPath` | `string \| null` | Currently active virtual path |

**Events:**

| Event | Payload | Description |
|-------|---------|-------------|
| `navigate` | `string` | Emitted with virtual path when a page is clicked |

### NavBreadcrumb.vue

Renders the current page location as a breadcrumb trail.

```vue
<DynamiaNavBreadcrumb
  :module="currentModule"
  :group="currentGroup"
  :page="currentPage"
/>
```

---

## Custom ViewType example

The plugin architecture is open — add new view types without modifying the core packages:

```typescript
// kanban-plugin.ts
import type { App } from 'vue';
import type { ViewType, View, ViewRenderer, ResolvedField } from '@dynamia-tools/ui-core';
import { ViewRendererRegistry } from '@dynamia-tools/ui-core';
import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import KanbanBoard from './KanbanBoard.vue';

// 1. Define the view type
const KanbanViewType: ViewType = { name: 'kanban' };

// 2. Implement a View subclass (in ui-core)
class KanbanView extends View {
  constructor(d: ViewDescriptor, m: EntityMetadata | null) {
    super(KanbanViewType, d, m);
  }
  async initialize() { /* fetch board data */ }
  validate() { return true; }
}

// 3. Implement a Vue renderer
class VueKanbanRenderer implements ViewRenderer<KanbanView, unknown> {
  readonly supportedViewType = KanbanViewType;
  render(_view: KanbanView) { return KanbanBoard; }
}

// 4. Export as a Vue plugin
export const KanbanPlugin = {
  install(app: App) {
    ViewRendererRegistry.register(KanbanViewType, new VueKanbanRenderer());
    ViewRendererRegistry.registerViewFactory(KanbanViewType, (d, m) => new KanbanView(d, m));
    app.component('KanbanBoard', KanbanBoard);
  },
};
```

```typescript
// main.ts
app.use(DynamiaVue);
app.use(KanbanPlugin);
```

```vue
<!-- Now works automatically -->
<DynamiaViewer view-type="kanban" bean-class="com.example.Task" />
```

---

## Architecture

```
@dynamia-tools/vue
│
├── views/
│   ├── VueView.ts              ← abstract: extends View + Vue reactivity base
│   ├── VueViewer.ts            ← extends Viewer — reactive universal resolution host
│   ├── VueFormView.ts          ← extends FormView — values/errors as Vue refs
│   ├── VueTableView.ts         ← extends TableView — rows/pagination as Vue refs
│   ├── VueCrudView.ts          ← extends CrudView — owns VueFormView + VueTableView
│   ├── VueTreeView.ts
│   ├── VueConfigView.ts
│   └── VueEntityPickerView.ts
│
├── renderers/
│   ├── VueFormRenderer.ts      ← implements FormRenderer<Component>
│   ├── VueTableRenderer.ts     ← implements TableRenderer<Component>
│   ├── VueCrudRenderer.ts      ← implements CrudRenderer<Component>
│   └── VueFieldRenderer.ts     ← implements FieldRenderer<Component>
│
├── composables/
│   ├── useViewer.ts            ← primary API (resolves any view type)
│   ├── useView.ts              ← generic view lifecycle
│   ├── useForm.ts
│   ├── useTable.ts
│   ├── useCrud.ts
│   ├── useCrudPage.ts          ← auto-builds VueCrudView from a CrudPage node
│   ├── useEntityPicker.ts
│   └── useNavigation.ts
│
├── components/
│   ├── Viewer.vue              ← universal host (primary component)
│   ├── Form.vue
│   ├── Table.vue
│   ├── Crud.vue
│   ├── CrudPage.vue            ← renders CrudPage NavigationNodes end-to-end
│   ├── Field.vue               ← field dispatcher
│   ├── Actions.vue
│   ├── NavMenu.vue
│   ├── NavBreadcrumb.vue
│   └── fields/
│       ├── Textbox.vue
│       ├── Textareabox.vue
│       ├── Intbox.vue
│       ├── Spinner.vue
│       ├── Combobox.vue
│       ├── Datebox.vue
│       ├── Checkbox.vue
│       ├── EntityPicker.vue
│       ├── EntityRefPicker.vue
│       ├── EntityRefLabel.vue
│       ├── CoolLabel.vue
│       └── Link.vue
│
└── plugin.ts                   ← DynamiaVue plugin (registers all renderers + components)
```

**Design principles:**

- **No Pinia** — state lives inside `View`/`Viewer` instances as `ref`/`shallowRef`. Pinia integration is an application-level concern.
- **No type duplication** — all SDK types (`ViewDescriptor`, `EntityMetadata`, `NavigationTree`, …) are imported, never redefined.
- **Lazy field components** — field components are loaded via `defineAsyncComponent` to keep the main bundle small.
- **Open extension** — `ViewType` and `FieldComponent` are plain objects, not enums. Third-party modules extend them without touching core.

---

## Contributing

See the monorepo [CONTRIBUTING.md](../../../CONTRIBUTING.md) for full guidelines.

```bash
# Install all workspace dependencies
pnpm install

# Build vue package (builds ui-core first as dependency)
pnpm --filter @dynamia-tools/vue build

# Type-check
pnpm --filter @dynamia-tools/vue typecheck

# Build entire workspace
pnpm run build
```

---

## License

[Apache License 2.0](../../../LICENSE) — © Dynamia Soluciones IT SAS
