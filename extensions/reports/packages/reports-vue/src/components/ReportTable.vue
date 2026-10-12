<!-- ReportTable.vue: the rows of a report result with sorting and paging controls -->
<template>
  <div class="dynamia-report-table">
    <div class="dynamia-report-table-scroll">
      <table>
        <thead>
          <tr>
            <th
              v-for="column in columns"
              :key="column.name"
              :style="{ textAlign: alignOf(column), width: column.width ?? undefined }"
              :aria-sort="ariaSort(column)"
              :data-column="column.name"
            >
              <button type="button" class="dynamia-report-sort" @click="emit('sort', column.name)">
                {{ column.label }}
                <span v-if="sortColumn === column.name" aria-hidden="true">{{ sortDirection === 'asc' ? '▲' : '▼' }}</span>
              </button>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="rows.length === 0">
            <td :colspan="Math.max(columns.length, 1)" class="dynamia-report-empty">{{ text.noResults }}</td>
          </tr>
          <tr v-for="(row, index) in rows" :key="index">
            <td v-for="column in columns" :key="column.name" :style="{ textAlign: alignOf(column) }">
              <slot name="cell" :column="column" :row="row" :value="row[column.name]">
                {{ formatCell(column, row[column.name], locale) }}
              </slot>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-if="count > 0" class="dynamia-report-paging">
      <span class="dynamia-report-count">{{ count }} {{ text.rows }}</span>
      <label class="dynamia-report-size">
        {{ text.rowsPerPage }}
        <select :value="size" @change="emit('size', Number(($event.target as HTMLSelectElement).value))">
          <option v-for="option in sizes" :key="option" :value="option">{{ option }}</option>
        </select>
      </label>
      <button type="button" :disabled="current <= 0" @click="emit('page', current - 1)">{{ text.previous }}</button>
      <span>{{ text.page }} {{ current + 1 }} {{ text.of }} {{ pages }}</span>
      <button type="button" :disabled="current + 1 >= pages" @click="emit('page', current + 1)">{{ text.next }}</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ReportColumn, ReportRow } from '@dynamia-tools/reports-sdk';
import { formatCell } from '../format.js';
import { resolveLabels } from '../labels.js';
import type { ReportLabels } from '../labels.js';

const props = withDefaults(
  defineProps<{
    columns: ReportColumn[];
    rows: ReportRow[];
    total?: number;
    page?: number;
    size?: number;
    totalPages?: number;
    sortColumn?: string | null;
    sortDirection?: 'asc' | 'desc';
    /** Page sizes offered by the selector */
    sizes?: number[];
    /** Locale for numbers and dates, defaults to the browser's */
    locale?: string | undefined;
    labels?: Partial<ReportLabels> | undefined;
  }>(),
  { total: 0, page: 0, size: 25, totalPages: 1, sortColumn: null, sortDirection: 'asc', sizes: () => [10, 25, 50, 100] },
);

const emit = defineEmits<{
  sort: [column: string];
  page: [page: number];
  size: [size: number];
}>();

const text = computed(() => resolveLabels(props.labels));
const count = computed(() => props.total ?? 0);
const current = computed(() => props.page ?? 0);
const pages = computed(() => props.totalPages ?? 1);

function alignOf(column: ReportColumn): 'left' | 'center' | 'right' {
  return column.align === 'RIGHT' ? 'right' : column.align === 'CENTER' ? 'center' : 'left';
}

function ariaSort(column: ReportColumn): 'ascending' | 'descending' | 'none' {
  if (props.sortColumn !== column.name) return 'none';
  return props.sortDirection === 'asc' ? 'ascending' : 'descending';
}
</script>

<style scoped>
.dynamia-report-table-scroll { overflow-x: auto; }
table { border-collapse: collapse; width: 100%; }
th, td { padding: 0.4rem 0.6rem; border-bottom: 1px solid rgba(128, 128, 128, 0.3); }
th { white-space: nowrap; }
.dynamia-report-sort { background: none; border: 0; font: inherit; font-weight: 600; cursor: pointer; padding: 0; color: inherit; }
.dynamia-report-empty { text-align: center; opacity: 0.7; padding: 1.5rem; }
.dynamia-report-paging { display: flex; flex-wrap: wrap; gap: 0.75rem; align-items: center; margin-top: 0.75rem; }
.dynamia-report-count { margin-right: auto; opacity: 0.8; }
</style>
