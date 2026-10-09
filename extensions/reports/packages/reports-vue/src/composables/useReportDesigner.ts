// useReportDesigner: the designer tools: query preview, definition export/import and datasource test

import { getCurrentInstance, onMounted, ref } from 'vue';
import type { Ref } from 'vue';
import type { DynamiaClient } from '@dynamia-tools/sdk';
import { ReportsApi } from '@dynamia-tools/reports-sdk';
import type {
  DataSourceTestResult,
  ReportDesignerInfo,
  ReportPreviewRequest,
  ReportPreviewResult,
} from '@dynamia-tools/reports-sdk';
import { downloadBlob, exportFileName, messageOf } from '../format.js';

/** Options for {@link useReportDesigner}. */
export interface UseReportDesignerOptions {
  /** Load the designer info as soon as the component is mounted (default `true`). */
  immediate?: boolean;
  /** Saves an exported file. Defaults to a browser download. */
  download?: (blob: Blob, filename: string) => void;
}

/**
 * Composable with the report designer tools. The backend only lets users with a designer role use them
 * (`dynamia.reports.designer-roles`); `info.allowed` tells whether the current user is one.
 *
 * Example:
 * <pre>{@code
 * const designer = useReportDesigner(client);
 * await designer.preview({ queryLang: 'sql', queryScript: 'select * from customers' });
 * }</pre>
 *
 * @param client - DynamiaClient instance
 * @param options - load and download behavior
 */
export function useReportDesigner(client: DynamiaClient, options: UseReportDesignerOptions = {}) {
  const api = new ReportsApi(client.http);
  const save = options.download ?? downloadBlob;
  const info: Ref<ReportDesignerInfo | null> = ref(null);
  const busy = ref(false);
  const error: Ref<string | null> = ref(null);
  const previewResult: Ref<ReportPreviewResult | null> = ref(null);

  async function guard<T>(work: () => Promise<T>): Promise<T | undefined> {
    busy.value = true;
    error.value = null;
    try {
      return await work();
    } catch (e) {
      error.value = messageOf(e);
      return undefined;
    } finally {
      busy.value = false;
    }
  }

  /** Loads what the current user can do in the designer. */
  async function loadInfo(): Promise<void> {
    const loaded = await guard(() => api.designer());
    info.value = loaded ?? { allowed: false, previewLimit: 0 };
  }

  /** Runs a query being designed and keeps the first rows in `previewResult`. */
  async function preview(request: ReportPreviewRequest): Promise<ReportPreviewResult | undefined> {
    previewResult.value = null;
    const result = await guard(() => api.preview(request));
    if (result) previewResult.value = result;
    return result;
  }

  /** Downloads the definition of a report as a JSON file. */
  async function exportDefinition(id: number, name = `report-${id}`): Promise<void> {
    await guard(async () => {
      const definition = await api.exportDefinition(id);
      const blob = new Blob([JSON.stringify(definition, null, 2)], { type: 'application/json' });
      save(blob, exportFileName(name, 'json'));
    });
  }

  /**
   * Imports a definition file exported with {@link exportDefinition}.
   *
   * @returns the id of the new report, or `undefined` if the import failed (see `error`)
   */
  async function importDefinition(file: Blob): Promise<number | undefined> {
    return guard(async () => {
      let definition: Record<string, unknown>;
      try {
        definition = JSON.parse(await file.text()) as Record<string, unknown>;
      } catch {
        throw new Error('The file is not a valid JSON report definition');
      }
      return (await api.importDefinition(definition)).id;
    });
  }

  /** Tests the connection of a saved datasource. */
  function testDataSource(id: number): Promise<DataSourceTestResult | undefined> {
    return guard(() => api.testDataSource(id));
  }

  if (options.immediate !== false) {
    if (getCurrentInstance()) onMounted(loadInfo);
    else void loadInfo();
  }

  return { info, busy, error, previewResult, loadInfo, preview, exportDefinition, importDefinition, testDataSource };
}
