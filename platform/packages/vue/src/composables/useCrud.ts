// useCrud: composable for creating and managing a VueCrudView

import { onMounted } from 'vue';
import type { ViewDescriptor, CrudPageable, EntityMetadata } from '@dynamia-tools/sdk';
import { VueCrudView } from '../views/VueCrudView.js';

/** Options for useCrud composable */
export interface UseCrudOptions {
  /** Pre-loaded view descriptor */
  descriptor: ViewDescriptor;
  /** Entity metadata */
  entityMetadata?: EntityMetadata | null;
  /** Data loader function for the table */
  loader?: (params: Record<string, unknown>) => Promise<{ rows: unknown[]; pagination: CrudPageable | null }>;
  /** Save handler called on form submit */
  onSave?: (data: unknown, mode: 'create' | 'edit') => Promise<void>;
  /** Delete handler */
  onDelete?: (entity: unknown) => Promise<void>;
}

/**
 * Composable for creating and managing a VueCrudView.
 * Provides direct access to reactive CRUD state.
 *
 * Example:
 * <pre>{@code
 * const { view, mode, form, table, startCreate, startEdit, cancelEdit, save, remove } = useCrud({
 *   descriptor,
 *   loader: async (params) => { ... },
 *   onSave: async (data, mode) => { ... },
 * });
 * }</pre>
 *
 * @param options - UseCrudOptions with descriptor and handlers
 * @returns Object with VueCrudView and reactive state
 */
export function useCrud(options: UseCrudOptions) {
  const view = new VueCrudView(options.descriptor, options.entityMetadata ?? null);

  if (options.loader) {
    view.tableView.setLoader(options.loader);
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
    await view.tableView.load();
  });

  return {
    /** The VueCrudView instance */
    view,
    /** Reactive CRUD mode */
    mode: view.mode,
    /** The VueFormView */
    form: view.formView,
    /** The VueTableView */
    table: view.tableView,
    /** Reactive show-form computed */
    showForm: view.showForm,
    /** Reactive show-table computed */
    showTable: view.showTable,
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
