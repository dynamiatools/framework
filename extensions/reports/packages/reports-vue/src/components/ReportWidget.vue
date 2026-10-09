<!-- ReportWidget.vue: renderer of the dashboard widgets of type `report` (a report shown as a compact table) -->
<template>
  <div class="dynamia-report-widget">
    <table v-if="data">
      <thead>
        <tr>
          <th v-for="column in data.columns" :key="column.name" :style="{ textAlign: alignOf(column) }">{{ column.label }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="data.rows.length === 0">
          <td :colspan="Math.max(data.columns.length, 1)" class="dynamia-report-widget-empty">{{ text.noResults }}</td>
        </tr>
        <tr v-for="(row, index) in data.rows" :key="index">
          <td v-for="column in data.columns" :key="column.name" :style="{ textAlign: alignOf(column) }">
            {{ formatCell(column, row[column.name]) }}
          </td>
        </tr>
      </tbody>
    </table>
    <small v-if="data && data.total > data.rows.length" class="dynamia-report-widget-more">
      {{ data.rows.length }} {{ text.of }} {{ data.total }} {{ text.rows }}
    </small>
    <small v-if="data?.truncated" class="dynamia-report-widget-truncated">{{ text.truncated }}</small>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ReportColumn, ReportRow } from '@dynamia-tools/reports-sdk';
import { formatCell } from '../format.js';
import { resolveLabels } from '../labels.js';
import type { ReportLabels } from '../labels.js';

/** Data of a `report` dashboard widget. Mirrors the Java `ReportWidgetData`. */
export interface ReportWidgetData {
  columns: ReportColumn[];
  rows: ReportRow[];
  total: number;
  truncated: boolean;
}

const props = defineProps<{
  /** The widget data served by the backend */
  data: ReportWidgetData | null;
  labels?: Partial<ReportLabels> | undefined;
}>();

const text = computed(() => resolveLabels(props.labels));

function alignOf(column: ReportColumn): 'left' | 'center' | 'right' {
  return column.align === 'RIGHT' ? 'right' : column.align === 'CENTER' ? 'center' : 'left';
}
</script>

<style scoped>
table { border-collapse: collapse; width: 100%; font-size: 0.9em; }
th, td { padding: 0.25rem 0.5rem; border-bottom: 1px solid rgba(128, 128, 128, 0.3); }
th { font-weight: 600; white-space: nowrap; }
.dynamia-report-widget-empty { text-align: center; opacity: 0.7; }
.dynamia-report-widget-more, .dynamia-report-widget-truncated { display: block; margin-top: 0.25rem; opacity: 0.75; }
</style>
