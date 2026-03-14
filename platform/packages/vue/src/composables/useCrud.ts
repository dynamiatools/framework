// useCrud: composable for creating and managing a VueCrudView

import { onMounted } from 'vue';
import type { ViewDescriptor, CrudPageable, EntityMetadata } from '@dynamia-tools/sdk';
import type { TreeNode } from '@dynamia-tools/ui-core';
import { VueCrudView } from '../views/VueCrudView.js';

/** Options for useCrud composable */
export interface UseCrudOptions {
  /** Pre-loaded view descriptor */
  descriptor: ViewDescriptor;
  /** Entity metadata */
  entityMetadata?: EntityMetadata | null;
  /** Data loader for TableView (ignored when dataSetViewType is tree) */
  loader?: (params: Record<string, unknown>) => Promise<{ rows: unknown[]; pagination: CrudPageable | null }>;
  /** Node loader for TreeView (ignored when dataSetViewType is table) */
  nodeLoader?: (params: Record<string, unknown>) => Promise<{ nodes: TreeNode[] }>;
  /** Save handler called on form submit */
  onSave?: (data: unknown, mode: 'create' | 'edit') => Promise<void>;
  /** Delete handler */
  onDelete?: (entity: unknown) => Promise<void>;
}

/**
 * Composable for creating and managing a {@link VueCrudView}.
 * Automatically selects the right DataSetView (table or tree) from the
 * descriptor param `dataSetViewType` and wires the appropriate loader.
 *
 * Example:
 * <pre>{@code
 * const { view, mode, form, dataSetView, startCreate, save } = useCrud({
 *   descriptor,
 *   loader: async (params) => fetchBooks(params),
 *   onSave: async (data, mode) => saveBook(data, mode),
 * });
 * }</pre>
 */
export function useCrud(options: UseCrudOptions) {
  const view = new VueCrudView(options.descriptor, options.entityMetadata ?? null);

  // Wire the appropriate loader based on the resolved DataSetView type
  if (options.loader && view.tableView) {
    view.tableView.setLoader(options.loader);
  }
  if (options.nodeLoader && view.treeView) {
    view.treeView.setLoader(options.nodeLoader);
  }

  if (options.onSave) {
    const handler = options.onSave;
    view.on('save', (payload) => {
      const { mode, data } = payload as { mode: 'create' | 'edit'; data: unknown };
      handler(data, mode).catch(console.error);
    });
  }

  if (options.onDelete) {
    const handler = options.onDelete;
    view.on('delete', (entity) => {
      handler(entity).catch(console.error);
    });
  }

  onMounted(async () => {
    await view.initialize();
    await view.dataSetView.load();
  });

  return {
    /** The VueCrudView instance */
    view,
    /** Reactive CRUD mode */
    mode: view.mode,
    /** The VueFormView */
    form: view.formView,
    /** The active DataSetView (VueTableView or VueTreeView) */
    dataSetView: view.dataSetView,
    /** The VueTableView, or null when using a tree */
    tableView: view.tableView,
    /** The VueTreeView, or null when using a table */
    treeView: view.treeView,
    /** Reactive show-form computed */
    showForm: view.showForm,
    /** Reactive show-dataset computed */
    showDataSet: view.showDataSet,
    /** Reactive loading state */
    loading: view.isLoading,
    /** Reactive error message */
    error: view.errorMessage,
    /** Start creating a new entity */
    startCreate: () => view.startCreate(),
    /** Start editing an entity */
    startEdit: (entity: unknown) => view.startEdit(entity),
    /** Cancel editing */
    cancelEdit: () => view.cancelEdit(),
    /** Save the current entity */
    save: () => view.save(),
    /** Delete an entity */
    remove: (entity: unknown) => view.delete(entity),
  };
}
