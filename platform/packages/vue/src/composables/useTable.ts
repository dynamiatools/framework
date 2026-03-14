// useTable: composable for creating and managing a VueTableView

import { onMounted } from 'vue';
import type { ViewDescriptor, CrudPageable, EntityMetadata } from '@dynamia-tools/sdk';
import { VueTableView } from '../views/VueTableView.js';

/** Options for useTable composable */
export interface UseTableOptions {
  /** Pre-loaded view descriptor */
  descriptor: ViewDescriptor;
  /** Entity metadata */
  entityMetadata?: EntityMetadata | null;
  /** Data loader function */
  loader?: (params: Record<string, unknown>) => Promise<{ rows: unknown[]; pagination: CrudPageable | null }>;
  /** Whether to load data on mount */
  autoLoad?: boolean;
}

/**
 * Composable for creating and managing a VueTableView.
 * Provides direct access to reactive table state.
 *
 * Example:
 * <pre>{@code
 * const { view, rows, columns, pagination, load, sort, search } = useTable({
 *   descriptor,
 *   loader: async (params) => { ... }
 * });
 * }</pre>
 *
 * @param options - UseTableOptions with descriptor and optional loader
 * @returns Object with VueTableView and reactive state
 */
export function useTable(options: UseTableOptions) {
  const view = new VueTableView(options.descriptor, options.entityMetadata ?? null);

  if (options.loader) {
    view.setLoader(options.loader);
  }

  onMounted(async () => {
    await view.initialize();
    if (options.autoLoad !== false) await view.load();
  });

  return {
    /** The VueTableView instance */
    view,
    /** Reactive table rows */
    rows: view.rows,
    /** Reactive column definitions */
    columns: view.columns,
    /** Reactive pagination info */
    pagination: view.pagination,
    /** Reactive loading state */
    loading: view.isLoading,
    /** Reactive selected item */
    selectedItem: view.selectedItem,
    /** Reactive sort field */
    sortField: view.sortField,
    /** Reactive sort direction */
    sortDir: view.sortDir,
    /** Reactive search query */
    searchQuery: view.searchQuery,
    /** Load data with optional params */
    load: (params?: Record<string, unknown>) => view.load(params),
    /** Sort by a field */
    sort: (field: string) => view.sort(field),
    /** Search with a query */
    search: (query: string) => view.search(query),
    /** Select a row */
    selectRow: (row: unknown) => view.selectRow(row),
    /** Go to next page */
    nextPage: () => view.nextPage(),
    /** Go to previous page */
    prevPage: () => view.prevPage(),
  };
}
