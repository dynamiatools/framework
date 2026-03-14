// useCrudPage: composable that auto-generates a full CRUD page from a NavigationNode of type "CrudPage"

import { ref, onMounted } from 'vue';
import type { Ref } from 'vue';
import type { NavigationNode, DynamiaClient } from '@dynamia-tools/sdk';
import { CrudPageResolver } from '@dynamia-tools/ui-core';
import { VueCrudView } from '../views/VueCrudView.js';

/** Options for the {@link useCrudPage} composable */
export interface UseCrudPageOptions {
  /** A NavigationNode whose `type` is `"CrudPage"` */
  node: NavigationNode;
  /** DynamiaClient instance used for all API calls */
  client: DynamiaClient;
}

/**
 * Composable that auto-generates a fully-wired CRUD page from a `CrudPage`
 * {@link NavigationNode}.
 *
 * Given a node like:
 * ```json
 * { "type": "CrudPage", "file": "mybookstore.domain.Book", "internalPath": "library/books" }
 * ```
 * the composable will:
 * 1. Load entity metadata and the CRUD view descriptor from the backend.
 * 2. Create a {@link VueCrudView} configured with the resolved descriptor.
 * 3. Wire up the table data loader using `client.crud(internalPath).findAll()`.
 * 4. Wire up save (create / update) and delete handlers through the same resource API.
 * 5. Initialize the view and load the first page of data.
 *
 * Example:
 * <pre>{@code
 * const { view, loading, error } = useCrudPage({ node, client });
 * }</pre>
 *
 * @param options - {@link UseCrudPageOptions}
 * @returns Reactive `view`, `loading`, `error` refs and a `reload` function.
 */
export function useCrudPage(options: UseCrudPageOptions) {
  const { node, client } = options;

  const loading: Ref<boolean> = ref(false);
  const error: Ref<string | null> = ref(null);
  const view: Ref<VueCrudView | null> = ref(null);

  async function initialize(): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      // 1. Resolve entity metadata + view descriptor
      const context = await CrudPageResolver.resolve(node, client);

      // 2. Create Vue-reactive CRUD view
      const crudView = new VueCrudView(context.descriptor, context.entityMetadata);

      // 3. CRUD resource API bound to the node's virtual path
      const api = client.crud(context.virtualPath);

      // 4. Wire table loader
      crudView.tableView.setLoader(async (params) => {
        const result = await api.findAll(
          params as Record<string, string | number | boolean | undefined | null>,
        );
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

      // 5. Wire save handler (create or update)
      crudView.on('save', (payload) => {
        const { mode, data } = payload as { mode: 'create' | 'edit'; data: Record<string, unknown> };
        const persist = async () => {
          crudView.isLoading.value = true;
          try {
            if (mode === 'create') {
              await api.create(data);
            } else {
              const id = data['id'] as string | number | undefined;
              if (id == null) throw new Error(`Cannot update entity: "id" field is missing`);
              await api.update(id, data);
            }
            await crudView.tableView.load();
          } catch (e) {
            crudView.errorMessage.value = String(e);
          } finally {
            crudView.isLoading.value = false;
          }
        };
        void persist();
      });

      // 6. Wire delete handler
      crudView.on('delete', (entity) => {
        const rec = entity as Record<string, unknown>;
        const id = rec['id'] as string | number | undefined;
        const remove = async () => {
          crudView.isLoading.value = true;
          try {
            if (id == null) throw new Error(`Cannot delete entity: "id" field is missing`);
            await api.delete(id);
            await crudView.tableView.load();
          } catch (e) {
            crudView.errorMessage.value = String(e);
          } finally {
            crudView.isLoading.value = false;
          }
        };
        void remove();
      });

      // 7. Initialize and load first page
      await crudView.initialize();
      await crudView.tableView.load();

      view.value = crudView;
    } catch (e) {
      error.value = String(e);
    } finally {
      loading.value = false;
    }
  }

  onMounted(initialize);

  return {
    /** Reactive VueCrudView — null until initialization completes */
    view,
    /** True while loading metadata or data */
    loading,
    /** Error message if initialization or a CRUD operation failed */
    error,
    /** Re-run full initialization (re-fetches metadata and first page) */
    reload: initialize,
  };
}

