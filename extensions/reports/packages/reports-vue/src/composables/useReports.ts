// useReports: loads the catalog of reports the current user can run

import { getCurrentInstance, onMounted, ref } from 'vue';
import type { Ref } from 'vue';
import type { DynamiaClient } from '@dynamia-tools/sdk';
import { ReportsApi } from '@dynamia-tools/reports-sdk';
import type { ReportCatalogGroup } from '@dynamia-tools/reports-sdk';
import { messageOf } from '../format.js';

/** Options for {@link useReports}. */
export interface UseReportsOptions {
  /** Load as soon as the component is mounted (default `true`). Set `false` to call `load()` yourself. */
  immediate?: boolean;
}

/**
 * Composable that loads the report catalog: the active reports of the current account that the user is allowed to
 * run, grouped by report group.
 *
 * Example:
 * <pre>{@code
 * const { groups, loading, error, reload } = useReports(client);
 * }</pre>
 *
 * @param client - DynamiaClient instance
 * @param options - load behavior
 */
export function useReports(client: DynamiaClient, options: UseReportsOptions = {}) {
  const api = new ReportsApi(client.http);
  const groups: Ref<ReportCatalogGroup[]> = ref([]);
  const loading = ref(false);
  const error: Ref<string | null> = ref(null);

  async function load(): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      groups.value = await api.catalog();
    } catch (e) {
      error.value = messageOf(e);
    } finally {
      loading.value = false;
    }
  }

  if (options.immediate !== false) {
    if (getCurrentInstance()) onMounted(load);
    else void load();
  }

  return {
    /** Report groups with their reports */
    groups,
    loading,
    /** Error message of the last load */
    error,
    load,
    /** Same as `load` */
    reload: load,
  };
}
