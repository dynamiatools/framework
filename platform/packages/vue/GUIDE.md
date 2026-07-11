# Vue Integration Guide: API Client Patterns

**Location:** `platform/packages/vue`

This guide explains how the Vue 3 adapter integrates with `DynamiaClient` and provides composables, components, and reactive views for building Dynamia-based applications.

---

## Table of Contents

1. [Overview](#overview)
2. [Setup & Plugin Registration](#setup--plugin-registration)
3. [Composables Reference](#composables-reference)
4. [Common Patterns](#common-patterns)
5. [Testing](#testing)
6. [Real-World Examples](#real-world-examples)

---

## Overview

The Vue package provides three layers of API integration:

| Layer | Purpose | Files |
|-------|---------|-------|
| **Plugin** | Registers global `DynamiaClient` via Vue injection | `src/plugin.ts` |
| **Composables** | Reactive helpers for common tasks (navigation, forms, CRUD) | `src/composables/*.ts` |
| **Views** | Reactive Vue extensions of ui-core classes | `src/views/*.ts` |

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│ Application (e.g., App.vue)                                     │
├─────────────────────────────────────────────────────────────────┤
│ useDynamiaClient() ──┐                                           │
│ useNavigation(client) ├─→ Composables                           │
│ useCrud(options)  ────┘   │                                      │
├──────────────────────────┼──────────────────────────────────────┤
│                          ▼                                       │
│                   Reactive Views                                 │
│              (VueCrudView, VueTableView, etc.)                  │
├──────────────────────────┬──────────────────────────────────────┤
│                          │                                       │
│      client.http (from plugin injection)                        │
│       ├─ client.metadata.*                                      │
│       ├─ client.crud(path)                                      │
│       ├─ client.actions.*                                       │
│       └─ Extension APIs (ReportsApi, SaasApi, FilesApi)         │
│                                                                  │
│           ▼ All HTTP flows through HttpClient ▼                │
│         (Auth headers, fetch, error handling)                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Setup & Plugin Registration

### Step 1: Create DynamiaClient

```typescript
// lib/client.ts
import { DynamiaClient } from '@dynamia-tools/sdk';

export const client = new DynamiaClient({
  baseUrl: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  token: getAuthToken(), // Your auth logic
  // OR for session-based:
  // withCredentials: true,
});
```

### Step 2: Register Plugin

```typescript
// main.ts
import { createApp } from 'vue';
import { DynamiaVue } from '@dynamia-tools/vue';
import App from './App.vue';
import { client } from './lib/client';

const app = createApp(App);
app.use(DynamiaVue, { client });
app.mount('#app');
```

### Step 3: Use in Components

```typescript
// components/MyComponent.vue
<script setup lang="ts">
import { useDynamiaClient, useNavigation } from '@dynamia-tools/vue';

const client = useDynamiaClient();
const { nodes, currentPage, navigateTo } = useNavigation(client!);
</script>

<template>
  <nav>
    <button v-for="node in nodes" :key="node.id" @click="navigateTo(node.internalPath)">
      {{ node.label }}
    </button>
  </nav>
  <main>
    <p v-if="currentPage">{{ currentPage.label }}</p>
  </main>
</template>
```

---

## Composables Reference

### `useDynamiaClient()`

Injects the global `DynamiaClient` provided by the plugin.

**Returns:**
- `DynamiaClient | null` — The configured client, or null if plugin not initialized

**Example:**
```typescript
const client = useDynamiaClient();
if (client) {
  const app = await client.metadata.getApp();
  console.log(app.name);
}
```

**When to use:**
- In top-level components that need direct client access
- When calling extension APIs (ReportsApi, SaasApi, etc.)
- In custom loaders

---

### `useNavigation(client, options?)`

Loads and manages the application navigation tree.

**Parameters:**
- `client: DynamiaClient` — The injected or passed client
- `options?: UseNavigationOptions`
  - `autoSelectFirst?: boolean` — Auto-navigate to first page on load (default: `false`)

**Returns:**
```typescript
{
  tree: Ref<NavigationTree | null>;
  nodes: ComputedRef<NavigationNode[]>;        // Top-level modules
  currentModule: ComputedRef<NavigationNode | null>;
  currentGroup: ComputedRef<NavigationNode | null>;
  currentPage: ComputedRef<NavigationNode | null>;
  loading: Ref<boolean>;
  error: Ref<string | null>;
  currentPath: Ref<string | null>;
  navigateTo(path: string): void;
  reload(): Promise<void>;
  clearCache(): void;
}
```

**Example:**
```typescript
const client = useDynamiaClient();
const { nodes, currentModule, currentPage, navigateTo, loading, error } = useNavigation(client!, {
  autoSelectFirst: true,
});

// In template
<div v-if="loading">Loading...</div>
<div v-else-if="error">Error: {{ error }}</div>
<div v-else>
  <h1>{{ currentModule?.label }}</h1>
  <p>{{ currentPage?.label }}</p>
</div>
```

**When to use:**
- Main layout component for app navigation
- Page selector UI
- Breadcrumb navigation

---

### `useTable(options)`

Creates and manages a reactive table view.

**Parameters:**
```typescript
{
  descriptor: ViewDescriptor;              // Required
  entityMetadata?: EntityMetadata | null;
  loader?: (params) => Promise<{ rows; pagination }>;  // Optional
  autoLoad?: boolean;                      // Default: true
}
```

**Returns:**
```typescript
{
  view: VueTableView;
  rows: Ref<unknown[]>;
  columns: Ref<ColumnDefinition[]>;
  pagination: Ref<CrudPageable | null>;
  loading: Ref<boolean>;
  selectedItem: Ref<unknown | null>;
  sortField: Ref<string | null>;
  sortDir: Ref<'ASC' | 'DESC'>;
  searchQuery: Ref<string>;
  load(params?: Record<string, unknown>): Promise<void>;
  sort(field: string): void;
  search(query: string): void;
  selectRow(row: unknown): void;
  nextPage(): void;
  prevPage(): void;
}
```

**Example (with custom loader):**
```typescript
const client = useDynamiaClient();

const { view, rows, columns, pagination, load, sort, search } = useTable({
  descriptor,
  loader: async (params) => {
    const data = await client!.crud('store/books').findAll(params);
    return {
      rows: data.content,
      pagination: {
        page: data.page,
        pageSize: data.pageSize,
        total: data.total,
        totalPages: data.totalPages,
      },
    };
  },
  autoLoad: true,
});
```

**When to use:**
- Data tables, grids
- List pages with sorting/pagination/search
- Read-only data display

---

### `useCrud(options)`

Creates and manages a CRUD view (form + table/tree).

**Parameters:**
```typescript
{
  descriptor: ViewDescriptor;              // Required
  entityMetadata?: EntityMetadata | null;
  loader?: (params) => Promise<...>;
  nodeLoader?: (params) => Promise<...>;   // For tree view
  onSave?: (data, mode) => Promise<void>;
  onDelete?: (entity) => Promise<void>;
}
```

**Returns:**
```typescript
{
  view: VueCrudView;
  mode: Ref<'list' | 'create' | 'edit'>;
  form: VueFormView;
  dataSetView: VueTableView | VueTreeView;
  tableView: VueTableView | null;
  treeView: VueTreeView | null;
  showForm: ComputedRef<boolean>;
  showDataSet: ComputedRef<boolean>;
  loading: Ref<boolean>;
  error: Ref<string | null>;
  startCreate(): void;
  startEdit(entity: unknown): void;
  cancelEdit(): void;
  save(): Promise<void>;
  remove(entity: unknown): Promise<void>;
}
```

**Example (Full CRUD with custom handlers):**
```typescript
const client = useDynamiaClient();

const { view, mode, form, dataSetView, showForm, showDataSet, startCreate, save, remove } = useCrud({
  descriptor,
  loader: async (params) => {
    const data = await client!.crud('store/books').findAll(params);
    return { rows: data.content, pagination: {...} };
  },
  onSave: async (data, mode) => {
    if (mode === 'create') {
      await client!.crud('store/books').create(data);
    } else {
      await client!.crud('store/books').update(data.id, data);
    }
  },
  onDelete: async (entity) => {
    await client!.crud('store/books').delete(entity.id);
  },
});
```

**When to use:**
- Full CRUD pages (list + form + actions)
- Master-detail patterns
- Complex entity management UI

---

### `useForm(options)`

Creates and manages a reactive form view.

**Parameters:**
```typescript
{
  descriptor: ViewDescriptor;
  entityMetadata?: EntityMetadata | null;
  initialValue?: unknown;
}
```

**Returns:**
```typescript
{
  view: VueFormView;
  fields: Ref<FormFieldMetadata[]>;
  values: Reactive<Record<string, unknown>>;
  errors: Ref<Record<string, string>>;
  loading: Ref<boolean>;
  setFieldValue(name: string, value: unknown): void;
  validate(): boolean;
  getValue(): unknown;
}
```

**When to use:**
- Data entry forms
- Edit dialogs
- Configuration panels

---

### `useEntityPicker(options)`

Creates and manages an entity search/picker view.

**Parameters:**
```typescript
{
  descriptor: ViewDescriptor;
  entityMetadata?: EntityMetadata | null;
  searcher?: (query: string) => Promise<unknown[]>;
  initialValue?: unknown;
}
```

**Returns:**
```typescript
{
  view: VueEntityPickerView;
  searchResults: Ref<unknown[]>;
  selectedEntity: Ref<unknown | null>;
  searchQuery: Ref<string>;
  loading: Ref<boolean>;
  search(query: string): void;
  select(entity: unknown): void;
  clear(): void;
}
```

**Example:**
```typescript
const client = useDynamiaClient();

const { view, searchResults, search, select } = useEntityPicker({
  descriptor,
  searcher: async (query) => {
    const results = await client!.metadata.findEntityReferences('authors', query);
    return results;
  },
});
```

**When to use:**
- Entity selection fields (lookups, foreign keys)
- Autocomplete search UI
- Modal/dropdown pickers

---

## Common Patterns

### Pattern 1: Navigation + List + Detail

```typescript
<script setup lang="ts">
import { useDynamiaClient, useNavigation, useCrud } from '@dynamia-tools/vue';

const client = useDynamiaClient();
const { currentPage, navigateTo } = useNavigation(client!);

const { view, showForm, showDataSet, startCreate, startEdit, save } = useCrud({
  descriptor: currentPage.value?.descriptor,
  loader: async (params) => {
    const path = currentPage.value?.internalPath;
    if (!path) return { rows: [], pagination: null };
    const data = await client!.crud(path).findAll(params);
    return { rows: data.content, pagination: {...} };
  },
  onSave: async (data, mode) => {
    const path = currentPage.value?.internalPath;
    if (mode === 'create') {
      await client!.crud(path!).create(data);
    } else {
      await client!.crud(path!).update(data.id, data);
    }
    await view.dataSetView.load();
  },
});
</script>

<template>
  <div v-if="showDataSet" class="list">
    <button @click="startCreate">+ New</button>
    <table>...</table>
  </div>
  <div v-if="showForm" class="form">
    <form @submit.prevent="save">
      <!-- Form fields -->
    </form>
  </div>
</template>
```

### Pattern 2: Entity Picker Field

```typescript
<script setup lang="ts">
import { useDynamiaClient, useEntityPicker } from '@dynamia-tools/vue';

const client = useDynamiaClient();

const { searchResults, search, select, selectedEntity } = useEntityPicker({
  descriptor,
  searcher: async (q) => {
    return await client!.metadata.findEntityReferences('authors', q);
  },
});
</script>

<template>
  <div class="entity-picker">
    <input @input="search($event.target.value)" placeholder="Search...">
    <div v-if="searchResults.length">
      <button v-for="entity in searchResults" :key="entity.id" @click="select(entity)">
        {{ entity.name }}
      </button>
    </div>
    <div v-if="selectedEntity">Selected: {{ selectedEntity.name }}</div>
  </div>
</template>
```

### Pattern 3: Custom Extension API in Loader

```typescript
<script setup lang="ts">
import { useDynamiaClient, useTable } from '@dynamia-tools/vue';
import { ReportsApi } from '@dynamia-tools/reports-sdk';

const client = useDynamiaClient();

const { rows, load } = useTable({
  descriptor,
  loader: async (params) => {
    // Use extension SDK
    const reports = new ReportsApi(client!.http);
    const reportData = await reports.post('sales', 'monthly', {
      options: [{ name: 'year', value: params.year }],
    });
    return {
      rows: reportData.rows || [],
      pagination: null,
    };
  },
});
</script>
```

---

## Testing

### Unit Test: Composable with Mock Client

```typescript
import { describe, it, expect, vi } from 'vitest';
import { useTable } from '@dynamia-tools/vue';
import { DynamiaClient } from '@dynamia-tools/sdk';

describe('useTable', () => {
  it('loads data and updates rows', async () => {
    // Mock loader
    const mockLoader = vi.fn().mockResolvedValue({
      rows: [{ id: 1, name: 'Book 1' }],
      pagination: { page: 1, pageSize: 10, total: 100 },
    });

    const { rows, load } = useTable({
      descriptor: mockDescriptor,
      loader: mockLoader,
    });

    expect(rows.value).toEqual([]);
    await load();
    expect(rows.value).toEqual([{ id: 1, name: 'Book 1' }]);
    expect(mockLoader).toHaveBeenCalled();
  });
});
```

### Component Test: Navigation Integration

```typescript
import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import { DynamiaClient } from '@dynamia-tools/sdk';
import { DynamiaVue } from '@dynamia-tools/vue';
import MyApp from './App.vue';

describe('App with Navigation', () => {
  it('loads navigation on mount', async () => {
    const mockClient = new DynamiaClient({
      baseUrl: 'https://api.example.com',
      token: 'test',
      fetch: mockFetch,
    });

    const wrapper = mount(MyApp, {
      global: {
        plugins: [[DynamiaVue, { client: mockClient }]],
      },
    });

    await wrapper.vm.$nextTick();
    expect(wrapper.html()).toContain('Navigation loaded');
  });
});
```

---

## Real-World Examples

### Example 1: Dashboard with Reports

```typescript
<script setup lang="ts">
import { useDynamiaClient, useTable } from '@dynamia-tools/vue';
import { ReportsApi } from '@dynamia-tools/reports-sdk';

const client = useDynamiaClient();

// Sales Report Table
const { rows: salesRows, load: loadSalesReport } = useTable({
  descriptor: salesReportDescriptor,
  loader: async () => {
    const reports = new ReportsApi(client!.http);
    const data = await reports.post('sales', 'monthly', {
      options: [{ name: 'year', value: new Date().getFullYear() }],
    });
    return { rows: data.rows || [], pagination: null };
  },
  autoLoad: true,
});
</script>

<template>
  <section class="dashboard">
    <h1>Sales Dashboard</h1>
    <table>
      <tr v-for="row in salesRows" :key="row.id">
        <td>{{ row.month }}</td>
        <td>{{ row.amount }}</td>
      </tr>
    </table>
  </section>
</template>
```

### Example 2: Master-Detail with Custom Handlers

```typescript
<script setup lang="ts">
import { useDynamiaClient, useCrud } from '@dynamia-tools/vue';
import { ref } from 'vue';

const client = useDynamiaClient();

const { view, form, dataSetView, showForm, showDataSet, startCreate, save, remove } = useCrud({
  descriptor: bookDescriptor,
  entityMetadata: bookMetadata,
  loader: async (params) => {
    const result = await client!.crud('store/books').findAll(params);
    return {
      rows: result.content,
      pagination: {
        page: result.page,
        pageSize: result.pageSize,
        total: result.total,
        totalPages: result.totalPages,
      },
    };
  },
  onSave: async (data, mode) => {
    if (mode === 'create') {
      const created = await client!.crud('store/books').create(data);
      console.log('Created:', created);
    } else {
      const updated = await client!.crud('store/books').update(data.id, data);
      console.log('Updated:', updated);
    }
  },
  onDelete: async (entity) => {
    await client!.crud('store/books').delete(entity.id);
    console.log('Deleted:', entity.id);
  },
});
</script>

<template>
  <div class="crud-page">
    <!-- List View -->
    <div v-if="showDataSet" class="list-section">
      <button @click="startCreate" class="btn-primary">+ Add Book</button>
      <table class="books-table">
        <thead>
          <tr>
            <th v-for="col in dataSetView.columns" :key="col.name">
              {{ col.label }}
            </th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in dataSetView.rows" :key="row.id">
            <td v-for="col in dataSetView.columns" :key="col.name">
              {{ row[col.name] }}
            </td>
            <td>
              <button @click="startEdit(row)" class="btn-small">Edit</button>
              <button @click="remove(row)" class="btn-small btn-danger">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Form View -->
    <div v-if="showForm" class="form-section">
      <h2>{{ view.mode === 'create' ? 'New Book' : 'Edit Book' }}</h2>
      <form @submit.prevent="save" class="book-form">
        <div v-for="field in form.fields" :key="field.name" class="form-group">
          <label>{{ field.label }}</label>
          <input
            :type="field.fieldType || 'text'"
            :value="form.values[field.name]"
            @input="form.setFieldValue(field.name, $event.target.value)"
          />
        </div>
        <button type="submit" class="btn-primary">Save</button>
        <button type="button" @click="view.cancelEdit" class="btn-secondary">Cancel</button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.crud-page {
  padding: 2rem;
}

.list-section,
.form-section {
  margin: 1rem 0;
}

.books-table {
  width: 100%;
  border-collapse: collapse;
}

.books-table th,
.books-table td {
  border: 1px solid #ccc;
  padding: 0.5rem;
  text-align: left;
}

.btn-primary,
.btn-secondary,
.btn-small,
.btn-danger {
  padding: 0.5rem 1rem;
  cursor: pointer;
  border: none;
  border-radius: 4px;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-small {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}
</style>
```

---

## Best Practices

1. **Always inject the client at the top level** — Use `useDynamiaClient()` in top-level components, pass to child composables
2. **Use loaders for data fetching** — Don't call APIs directly in view components; use loader callbacks
3. **Handle errors gracefully** — Composables expose `error` and `loading` refs; use them in templates
4. **Cache navigation** — `useNavigation()` caches the tree; call `reload()` only when necessary
5. **Test with mocks** — Always provide mock loaders in tests; avoid network calls
6. **Type your data** — Use TypeScript generics: `useTable<BookDTO>(...)`, `useCrud<AuthorDTO>(...)`

---

## See Also

- [API Client Standards](../API_CLIENT_STANDARDS.md)
- [API Client Audit Report](../API_CLIENT_AUDIT_REPORT.md)
- [SDK README](../../platform/packages/sdk/README.md)
- [Demo App](../../examples/demo-vue-books/src/App.vue)

