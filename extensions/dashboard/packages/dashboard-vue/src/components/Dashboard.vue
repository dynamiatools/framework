<!-- Dashboard.vue: renders a dashboard descriptor with the widgets served by the Dashboard extension -->
<template>
  <div class="dynamia-dashboard">
    <div v-if="loading" class="dynamia-dashboard-loading">
      <slot name="loading"><span>Loading...</span></slot>
    </div>
    <div v-else-if="error" class="dynamia-dashboard-error">
      <slot name="error" :error="error"><span>{{ error }}</span></slot>
    </div>
    <template v-else-if="layout">
      <div v-for="(row, index) in layout.rows" :key="index" class="dynamia-dashboard-row">
        <div
          v-for="cell in row"
          :key="cell.field"
          class="dynamia-dashboard-cell"
          :data-field="cell.field"
          :style="cellStyle(cell)"
        >
          <div v-if="widgets[cell.field]?.response?.titleVisible" class="dynamia-dashboard-title">
            {{ widgets[cell.field]?.response?.title }}
          </div>
          <div v-if="widgets[cell.field]?.loading" class="dynamia-dashboard-widget-loading">
            <slot name="widget-loading" :field="cell.field"><span>Loading...</span></slot>
          </div>
          <div v-else-if="widgets[cell.field]?.error" class="dynamia-dashboard-widget-error">
            <slot name="widget-error" :field="cell.field" :error="widgets[cell.field]?.error">
              <span>{{ widgets[cell.field]?.error }}</span>
            </slot>
          </div>
          <template v-else-if="widgets[cell.field]?.response">
            <component
              :is="rendererOf(cell.field)"
              v-if="rendererOf(cell.field)"
              :data="widgets[cell.field]!.response!.data"
              :response="widgets[cell.field]!.response"
            />
            <slot v-else name="unsupported" :field="cell.field" :response="widgets[cell.field]!.response">
              <span>No renderer registered for widget type "{{ widgets[cell.field]!.response!.type }}"</span>
            </slot>
          </template>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { CSSProperties } from 'vue';
import type { DynamiaClient } from '@dynamia-tools/sdk';
import { useDynamiaClient } from '@dynamia-tools/vue';
import type { DashboardLayoutCell, DashboardWidgetParams } from '@dynamia-tools/dashboard-sdk';
import { useDashboard } from '../composables/useDashboard.js';
import { WidgetRendererRegistry } from '../registry.js';

const props = defineProps<{
  /** The dashboard view descriptor id */
  id: string;
  /** DynamiaClient; defaults to the one provided by `app.use(DynamiaVue, { client })` */
  client?: DynamiaClient;
  /** Query parameters sent to every widget, e.g. a date range filter */
  params?: DashboardWidgetParams;
}>();

const client = props.client ?? useDynamiaClient();
if (!client) {
  throw new Error('DynamiaDashboard needs a DynamiaClient: pass the `client` prop or provide it with app.use(DynamiaVue, { client })');
}

const { layout, widgets, loading, error, reload, reloadWidget } = useDashboard(client, () => props.id, {
  params: () => props.params,
});

function rendererOf(field: string) {
  const type = widgets.value[field]?.response?.type;
  return type ? WidgetRendererRegistry.get(type) : undefined;
}

/** Spans for a 12-column grid; the CSS below applies xs/sm/md according to the viewport width */
function cellStyle(cell: DashboardLayoutCell): CSSProperties {
  return {
    '--dynamia-span-md': String(cell.span.md),
    '--dynamia-span-sm': String(cell.span.sm),
    '--dynamia-span-xs': String(cell.span.xs ?? 12),
  } as CSSProperties;
}

defineExpose({ reload, reloadWidget });
</script>

<style scoped>
.dynamia-dashboard-row {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}
.dynamia-dashboard-cell { grid-column: span var(--dynamia-span-xs); min-width: 0; }
@media (min-width: 576px) { .dynamia-dashboard-cell { grid-column: span var(--dynamia-span-sm); } }
@media (min-width: 768px) { .dynamia-dashboard-cell { grid-column: span var(--dynamia-span-md); } }
.dynamia-dashboard-title { font-weight: 600; margin-bottom: 0.5rem; }
</style>
