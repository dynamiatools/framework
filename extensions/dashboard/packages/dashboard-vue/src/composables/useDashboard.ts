// useDashboard: loads a dashboard descriptor, computes its layout and loads each widget independently

import { getCurrentInstance, onMounted, ref, shallowRef, toValue, watch } from 'vue';
import type { MaybeRefOrGetter, Ref, ShallowRef } from 'vue';
import type { DynamiaClient, ViewDescriptor } from '@dynamia-tools/sdk';
import { DashboardApi, resolveDashboardLayout } from '@dynamia-tools/dashboard-sdk';
import type {
  DashboardLayout,
  DashboardLayoutCell,
  DashboardWidgetParams,
  DashboardWidgetResponse,
} from '@dynamia-tools/dashboard-sdk';

/** Options for {@link useDashboard}. */
export interface UseDashboardOptions {
  /** Query parameters sent to every widget (forwarded to the widget's `update(params)` on the server). */
  params?: MaybeRefOrGetter<DashboardWidgetParams | undefined>;
  /** Load as soon as the component is mounted (default `true`). Set `false` to call `load()` yourself. */
  immediate?: boolean;
}

/** Loading state of one widget. A failing widget does not affect the others. */
export interface WidgetState {
  response: DashboardWidgetResponse | null;
  loading: boolean;
  error: string | null;
}

function messageOf(e: unknown): string {
  return e instanceof Error ? e.message : String(e);
}

/**
 * Composable that loads a dashboard: the view descriptor (`client.metadata.getView`), its layout
 * (`resolveDashboardLayout`) and then each widget's data in parallel, with independent loading and error state
 * per widget. Reloads when `id` or `options.params` change.
 *
 * Example:
 * <pre>{@code
 * const { layout, widgets, loading, error, reloadWidget } = useDashboard(client, 'mainDashboard');
 * }</pre>
 *
 * @param client - DynamiaClient instance
 * @param id - the dashboard view descriptor id (may be a ref or getter)
 * @param options - optional params and load behavior
 */
export function useDashboard(client: DynamiaClient, id: MaybeRefOrGetter<string>, options: UseDashboardOptions = {}) {
  const api = new DashboardApi(client.http);
  const descriptor: ShallowRef<ViewDescriptor | null> = shallowRef(null);
  const layout: ShallowRef<DashboardLayout | null> = shallowRef(null);
  const widgets: Ref<Record<string, WidgetState>> = ref({});
  const loading = ref(false);
  const error: Ref<string | null> = ref(null);
  let generation = 0;

  async function loadWidget(cell: DashboardLayoutCell, run: number, params?: DashboardWidgetParams): Promise<void> {
    const state = widgets.value[cell.field];
    if (!state) return;
    state.loading = true;
    state.error = null;
    try {
      const response = await api.widget(toValue(id), cell.field, params ?? toValue(options.params));
      if (run === generation) state.response = response;
    } catch (e) {
      if (run === generation) state.error = messageOf(e);
    } finally {
      if (run === generation) state.loading = false;
    }
  }

  /** (Re)loads the descriptor, the layout and every widget. */
  async function load(): Promise<void> {
    const run = ++generation;
    loading.value = true;
    error.value = null;
    try {
      const loaded = await client.metadata.getView(toValue(id));
      if (run !== generation) return;
      const computed = resolveDashboardLayout(loaded);
      descriptor.value = loaded;
      layout.value = computed;
      const states: Record<string, WidgetState> = {};
      const cells = computed.rows.flat();
      for (const cell of cells) states[cell.field] = { response: null, loading: true, error: null };
      widgets.value = states;
      await Promise.all(cells.map((cell) => loadWidget(cell, run)));
    } catch (e) {
      if (run === generation) error.value = messageOf(e);
    } finally {
      if (run === generation) loading.value = false;
    }
  }

  /**
   * Reloads a single widget, optionally with different params (for example from a widget-level filter).
   * Does nothing when the field is not part of the loaded layout.
   */
  async function reloadWidget(field: string, params?: DashboardWidgetParams): Promise<void> {
    const cell = layout.value?.rows.flat().find((c) => c.field === field);
    if (cell) await loadWidget(cell, generation, params);
  }

  watch([() => toValue(id), () => toValue(options.params)], () => void load(), { deep: true });

  if (options.immediate !== false) {
    if (getCurrentInstance()) onMounted(load);
    else void load();
  }

  return {
    /** The dashboard view descriptor */
    descriptor,
    /** The computed layout: rows of cells on a 12-column grid */
    layout,
    /** Per-widget state, keyed by descriptor field name */
    widgets,
    /** True while the descriptor is being loaded */
    loading,
    /** Descriptor loading error message */
    error,
    load,
    /** Same as `load` */
    reload: load,
    reloadWidget,
  };
}
