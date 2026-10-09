// useReport: the whole state of one report: definition, filter values, results, paging, sorting and export

import { computed, getCurrentInstance, onMounted, reactive, ref, shallowRef, toValue, watch } from 'vue';
import type { ComputedRef, MaybeRefOrGetter, Ref, ShallowRef } from 'vue';
import type { DynamiaClient } from '@dynamia-tools/sdk';
import { ReportsApi } from '@dynamia-tools/reports-sdk';
import type {
  ReportChartResult,
  ReportDefinition,
  ReportExportFormat,
  ReportFilterOptionItem,
  ReportFilterValue,
  ReportRunResult,
} from '@dynamia-tools/reports-sdk';
import { downloadBlob, exportFileName, messageOf, toApiValue, toInputValue } from '../format.js';

/** Options for {@link useReport}. */
export interface UseReportOptions {
  /** Rows per page (default 25). */
  pageSize?: number;
  /** Run the report after loading its definition when no required filter is missing (default `true`). */
  autoRun?: boolean;
  /** Load as soon as the component is mounted (default `true`). */
  immediate?: boolean;
  /** Saves an exported file. Defaults to a browser download; replace it in tests or in non-browser hosts. */
  download?: (blob: Blob, filename: string) => void;
}

/**
 * Composable with the state of one report. It loads the definition, keeps the filter form values (starting from the
 * defaults), runs the report with paging and sorting, loads filter options and exports files.
 *
 * Filter values are kept as the form controls use them (strings; dates as `yyyy-MM-dd`, date times as
 * `yyyy-MM-ddTHH:mm`) and converted for the API when running.
 *
 * Example:
 * <pre>{@code
 * const report = useReport(client, 12, { pageSize: 50 });
 * report.values.year = '2026';
 * await report.run();
 * }</pre>
 *
 * @param client - DynamiaClient instance
 * @param id - the report id (may be a ref or getter); changing it loads another report
 * @param options - paging and load behavior
 */
export function useReport(client: DynamiaClient, id: MaybeRefOrGetter<number>, options: UseReportOptions = {}) {
  const api = new ReportsApi(client.http);
  const save = options.download ?? downloadBlob;

  const definition: ShallowRef<ReportDefinition | null> = shallowRef(null);
  const values: Record<string, string> = reactive({});
  const result: ShallowRef<ReportRunResult | null> = shallowRef(null);
  const loading = ref(false);
  const running = ref(false);
  const exporting = ref(false);
  const error: Ref<string | null> = ref(null);
  const runError: Ref<string | null> = ref(null);
  const page = ref(0);
  const size = ref(options.pageSize ?? 25);
  const sort: Ref<string | null> = ref(null);
  const direction: Ref<'asc' | 'desc'> = ref('asc');
  let definitionRun = 0;
  let resultRun = 0;

  /** Names of the required filters that have no value yet. */
  const missingRequired: ComputedRef<string[]> = computed(() =>
    (definition.value?.filters ?? []).filter((f) => f.required && !values[f.name]).map((f) => f.name),
  );
  const canRun = computed(() => definition.value !== null && missingRequired.value.length === 0 && !running.value);
  const charts: ComputedRef<ReportChartResult[]> = computed(() => result.value?.charts ?? []);
  const totalPages = computed(() => (result.value && size.value > 0 ? Math.max(1, Math.ceil(result.value.total / size.value)) : 1));

  function resetValues(): void {
    for (const key of Object.keys(values)) delete values[key];
    for (const filter of definition.value?.filters ?? []) {
      values[filter.name] = toInputValue(filter, filter.defaultValue);
    }
  }

  function request(forExport = false) {
    const filters: Record<string, ReportFilterValue> = {};
    for (const filter of definition.value?.filters ?? []) {
      const value = toApiValue(filter, values[filter.name]);
      if (value !== undefined) filters[filter.name] = value;
    }
    const order = sort.value ? { sort: sort.value, direction: direction.value } : {};
    return forExport ? { filters, ...order } : { filters, page: page.value, size: size.value, ...order };
  }

  /** (Re)loads the definition and resets the filters to their defaults. */
  async function loadDefinition(): Promise<void> {
    const run = ++definitionRun;
    loading.value = true;
    error.value = null;
    result.value = null;
    runError.value = null;
    page.value = 0;
    sort.value = null;
    direction.value = 'asc';
    try {
      const loaded = await api.definition(toValue(id));
      if (run !== definitionRun) return;
      definition.value = loaded;
      resetValues();
    } catch (e) {
      if (run === definitionRun) {
        definition.value = null;
        error.value = messageOf(e);
      }
      return;
    } finally {
      if (run === definitionRun) loading.value = false;
    }
    if (options.autoRun !== false && canRun.value) await runReport();
  }

  /**
   * Runs the report with the current filters, paging and sorting.
   *
   * @param targetPage - page to load; defaults to the current page
   */
  async function runReport(targetPage?: number): Promise<void> {
    if (!definition.value) return;
    if (targetPage !== undefined) page.value = targetPage;
    const run = ++resultRun;
    running.value = true;
    runError.value = null;
    try {
      const data = await api.run(toValue(id), request());
      if (run === resultRun) result.value = data;
    } catch (e) {
      if (run === resultRun) runError.value = messageOf(e);
    } finally {
      if (run === resultRun) running.value = false;
    }
  }

  /** Runs the report from the first page; use it when the filters changed. */
  function search(): Promise<void> {
    return runReport(0);
  }

  /** Sorts by a column: first ascending, then descending when it is already the sort column. */
  function sortBy(column: string): Promise<void> {
    if (sort.value === column) {
      direction.value = direction.value === 'asc' ? 'desc' : 'asc';
    } else {
      sort.value = column;
      direction.value = 'asc';
    }
    return runReport(0);
  }

  function setPage(next: number): Promise<void> {
    return runReport(Math.max(0, Math.min(next, totalPages.value - 1)));
  }

  function setPageSize(next: number): Promise<void> {
    size.value = next;
    return runReport(0);
  }

  /** Clears the result and puts the filters back to their defaults. */
  function reset(): void {
    resultRun++;
    running.value = false;
    result.value = null;
    runError.value = null;
    page.value = 0;
    sort.value = null;
    direction.value = 'asc';
    resetValues();
  }

  /** Options of a filter with predefined values (enum, entity, query or static). */
  function filterOptions(filter: string, q?: string): Promise<ReportFilterOptionItem[]> {
    return api.filterOptions(toValue(id), filter, { q });
  }

  /**
   * Exports the report with the current filters and sorting (all rows, not only the current page) and saves the file.
   *
   * @param format - `xlsx`, `csv` or `pdf`
   */
  async function exportAs(format: ReportExportFormat): Promise<void> {
    if (!definition.value || missingRequired.value.length > 0) return;
    exporting.value = true;
    runError.value = null;
    try {
      const blob = await api.export(toValue(id), format, request(true));
      save(blob, exportFileName(definition.value.report.name, format));
    } catch (e) {
      runError.value = messageOf(e);
    } finally {
      exporting.value = false;
    }
  }

  watch(() => toValue(id), () => void loadDefinition());

  if (options.immediate !== false) {
    if (getCurrentInstance()) onMounted(loadDefinition);
    else void loadDefinition();
  }

  return {
    /** The report definition: filters, columns, charts and export formats */
    definition,
    /** Filter form values by filter name */
    values,
    /** The last result */
    result,
    /** Charts of the last result */
    charts,
    /** True while the definition is loading */
    loading,
    /** True while the report is running */
    running,
    exporting,
    /** Definition loading error */
    error,
    /** Error of the last run or export */
    runError,
    page,
    size,
    totalPages,
    sort,
    direction,
    missingRequired,
    canRun,
    loadDefinition,
    run: runReport,
    search,
    sortBy,
    setPage,
    setPageSize,
    reset,
    filterOptions,
    exportAs,
  };
}
