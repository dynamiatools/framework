<!-- ReportViewer.vue: runs and shows one report: filters, table, charts and exports -->
<template>
  <section class="dynamia-report-viewer">
    <p v-if="loading" class="dynamia-report-loading" role="status">
      <slot name="loading">{{ text.loading }}</slot>
    </p>
    <p v-else-if="error" class="dynamia-report-error" role="alert">
      <slot name="error" :error="error">{{ error }}</slot>
    </p>

    <template v-else-if="definition">
      <header class="dynamia-report-header">
        <h2>{{ definition.report.title || definition.report.name }}</h2>
        <p v-if="definition.report.subtitle" class="dynamia-report-subtitle">{{ definition.report.subtitle }}</p>
        <p v-if="definition.report.description" class="dynamia-report-description">{{ definition.report.description }}</p>
      </header>

      <ReportFilters
        v-if="definition.filters.length > 0"
        :filters="definition.filters"
        :model-value="values"
        :load-options="filterOptions"
        :labels="labels"
        @update:model-value="applyValues"
        @submit="onSearch"
      />

      <div class="dynamia-report-actions">
        <button type="button" class="dynamia-report-run" :disabled="!canRun" @click="onSearch">
          {{ running ? text.running : text.run }}
        </button>
        <button v-if="definition.filters.length > 0" type="button" class="dynamia-report-reset" @click="reset">
          {{ text.reset }}
        </button>
        <span v-if="definition.exportFormats.length > 0" class="dynamia-report-exports">
          <button
            v-for="format in definition.exportFormats"
            :key="format"
            type="button"
            class="dynamia-report-export"
            :data-format="format"
            :disabled="exporting || missingRequired.length > 0"
            @click="exportAs(format)"
          >
            {{ exporting ? text.exporting : `${text.export} ${format.toUpperCase()}` }}
          </button>
        </span>
      </div>

      <p v-if="runError" class="dynamia-report-error" role="alert">{{ runError }}</p>
      <p v-if="result?.truncated" class="dynamia-report-truncated" role="status">{{ text.truncated }}</p>

      <template v-if="result">
        <ReportTable
          :columns="result.columns"
          :rows="result.rows"
          :total="result.total"
          :page="page"
          :size="size"
          :total-pages="totalPages"
          :sort-column="sort"
          :sort-direction="direction"
          :locale="locale"
          :labels="labels"
          @sort="sortBy"
          @page="setPage"
          @size="setPageSize"
        >
          <template v-if="$slots.cell" #cell="slotProps"><slot name="cell" v-bind="slotProps" /></template>
        </ReportTable>

        <div v-if="showCharts && charts.length > 0" class="dynamia-report-charts">
          <ReportChart v-for="chart in charts" :key="chart.index" :chart="chart" :labels="labels" />
        </div>
      </template>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import type { DynamiaClient } from '@dynamia-tools/sdk';
import { useDynamiaClient } from '@dynamia-tools/vue';
import type { ReportRunResult } from '@dynamia-tools/reports-sdk';
import { useReport } from '../composables/useReport.js';
import { resolveLabels } from '../labels.js';
import type { ReportLabels } from '../labels.js';
import ReportChart from './ReportChart.vue';
import ReportFilters from './ReportFilters.vue';
import ReportTable from './ReportTable.vue';

const props = withDefaults(
  defineProps<{
    /** The report id, from the catalog */
    id: number;
    /** DynamiaClient; defaults to the one provided by `app.use(DynamiaVue, { client })` */
    client?: DynamiaClient | undefined;
    /** Run the report when it is shown, if no required filter is missing (default `true`) */
    autoRun?: boolean;
    /** Rows per page (default 25) */
    pageSize?: number;
    /** Draw the charts of the report (default `true`) */
    showCharts?: boolean;
    /** Locale for numbers and dates, defaults to the browser's */
    locale?: string | undefined;
    labels?: Partial<ReportLabels> | undefined;
  }>(),
  { autoRun: true, pageSize: 25, showCharts: true },
);

const emit = defineEmits<{
  result: [result: ReportRunResult];
}>();

defineSlots<{
  loading?: () => unknown;
  error?: (props: { error: string }) => unknown;
  cell?: (props: { column: unknown; row: unknown; value: unknown }) => unknown;
}>();

const client = props.client ?? useDynamiaClient();
if (!client) {
  throw new Error('DynamiaReportViewer needs a DynamiaClient: pass the `client` prop or provide it with app.use(DynamiaVue, { client })');
}

const text = computed(() => resolveLabels(props.labels));
const {
  definition, values, result, charts, loading, running, exporting, error, runError, page, size, totalPages, sort,
  direction, missingRequired, canRun, run, search, sortBy, setPage, setPageSize, reset, filterOptions, exportAs,
} = useReport(client, () => props.id, { autoRun: props.autoRun, pageSize: props.pageSize });

function applyValues(next: Record<string, string>): void {
  for (const [key, value] of Object.entries(next)) values[key] = value;
}

function onSearch(): void {
  if (canRun.value) void search();
}

watch(result, (value) => {
  if (value) emit('result', value);
});

defineExpose({ run, search, reset, exportAs, values });
</script>

<style scoped>
.dynamia-report-header h2 { margin: 0 0 0.25rem; }
.dynamia-report-subtitle, .dynamia-report-description { margin: 0 0 0.5rem; opacity: 0.75; }
.dynamia-report-actions { display: flex; flex-wrap: wrap; gap: 0.5rem; margin: 1rem 0; }
.dynamia-report-exports { display: inline-flex; gap: 0.5rem; margin-left: auto; }
.dynamia-report-error { color: #ef4444; }
.dynamia-report-truncated { padding: 0.5rem 0.75rem; border-left: 4px solid #f59e0b; background: rgba(245, 158, 11, 0.12); }
.dynamia-report-charts { display: grid; grid-template-columns: repeat(auto-fit, minmax(18rem, 1fr)); gap: 1.5rem; margin-top: 1.5rem; }
</style>
