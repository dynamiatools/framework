<!-- Table.vue: Thin wrapper for rendering a TableView with columns, rows, and pagination -->
<template>
  <div class="dynamia-table">
    <div v-if="view.isLoading.value" class="dynamia-table-loading">
      <slot name="loading"><span>Loading...</span></slot>
    </div>
    <table v-else class="dynamia-table-grid">
      <thead>
        <tr>
          <th
            v-for="col in view.columns.value"
            :key="col.name"
            class="dynamia-table-header"
            @click="view.sort(col.name)"
          >
            {{ col.resolvedLabel }}
            <span v-if="view.sortField.value === col.name">
              {{ view.sortDir.value === 'asc' ? '↑' : '↓' }}
            </span>
          </th>
          <th v-if="$slots['actions']" class="dynamia-table-actions-header">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(row, index) in view.rows.value"
          :key="index"
          class="dynamia-table-row"
          :class="{ 'dynamia-table-row-selected': view.selectedRow.value === row }"
          @click="view.selectRow(row)"
        >
          <td
            v-for="col in view.columns.value"
            :key="col.name"
            class="dynamia-table-cell"
          >
            <slot :name="`cell-${col.name}`" :row="row" :col="col">
              {{ getCellValue(row, col.name) }}
            </slot>
          </td>
          <td v-if="$slots['actions']" class="dynamia-table-actions-cell">
            <slot name="actions" :row="row" />
          </td>
        </tr>
        <tr v-if="view.rows.value.length === 0">
          <td :colspan="view.columns.value.length" class="dynamia-table-empty">
            <slot name="empty"><span>No records found</span></slot>
          </td>
        </tr>
      </tbody>
    </table>
    <!-- Pagination -->
    <div v-if="view.pagination.value" class="dynamia-table-pagination">
      <button :disabled="view.pagination.value.page <= 1" @click="view.prevPage()">←</button>
      <span>{{ view.pagination.value.page }} / {{ view.pagination.value.pagesNumber }}</span>
      <button :disabled="view.pagination.value.page >= view.pagination.value.pagesNumber" @click="view.nextPage()">→</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { VueTableView } from '../views/VueTableView.js';

defineProps<{
  /** The VueTableView instance to render */
  view: VueTableView;
  /** Whether the table is in read-only mode */
  readOnly?: boolean;
}>();

function getCellValue(row: unknown, field: string): unknown {
  if (row && typeof row === 'object') {
    return (row as Record<string, unknown>)[field];
  }
  return '';
}
</script>
