<!-- ReportList.vue: the reports the user can run, grouped, with search -->
<template>
  <div class="dynamia-report-list">
    <input
      v-model="query"
      type="search"
      class="dynamia-report-search"
      :placeholder="text.search"
      :aria-label="text.search"
    />
    <p v-if="loading" role="status"><slot name="loading">{{ text.loading }}</slot></p>
    <p v-else-if="error" class="dynamia-report-error" role="alert"><slot name="error" :error="error">{{ error }}</slot></p>
    <p v-else-if="visibleGroups.length === 0" class="dynamia-report-empty">{{ text.noReports }}</p>
    <section v-for="group in visibleGroups" :key="group.name" class="dynamia-report-group">
      <h3>{{ group.name }}</h3>
      <ul>
        <li v-for="report in group.reports" :key="report.id">
          <button type="button" class="dynamia-report-card" :data-report="report.id" @click="emit('select', report)">
            <slot name="report" :report="report">
              <strong>{{ report.title || report.name }}</strong>
              <span v-if="report.description">{{ report.description }}</span>
              <small>{{ report.chartable ? '▥' : '▤' }}</small>
            </slot>
          </button>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { DynamiaClient } from '@dynamia-tools/sdk';
import { useDynamiaClient } from '@dynamia-tools/vue';
import type { ReportSummary } from '@dynamia-tools/reports-sdk';
import { useReports } from '../composables/useReports.js';
import { resolveLabels } from '../labels.js';
import type { ReportLabels } from '../labels.js';

const props = defineProps<{
  /** DynamiaClient; defaults to the one provided by `app.use(DynamiaVue, { client })` */
  client?: DynamiaClient | undefined;
  labels?: Partial<ReportLabels> | undefined;
}>();

const emit = defineEmits<{
  /** A report was chosen */
  select: [report: ReportSummary];
}>();

const client = props.client ?? useDynamiaClient();
if (!client) {
  throw new Error('DynamiaReportList needs a DynamiaClient: pass the `client` prop or provide it with app.use(DynamiaVue, { client })');
}

const text = computed(() => resolveLabels(props.labels));
const query = ref('');
const { groups, loading, error, reload } = useReports(client);

/** Groups with the reports whose name, title or description contain the search text. */
const visibleGroups = computed(() => {
  const q = query.value.trim().toLowerCase();
  if (!q) return groups.value;
  return groups.value
    .map((group) => ({
      ...group,
      reports: group.reports.filter((r) =>
        [r.name, r.title, r.description].some((field) => field?.toLowerCase().includes(q)),
      ),
    }))
    .filter((group) => group.reports.length > 0);
});

defineExpose({ reload });
</script>

<style scoped>
.dynamia-report-search { width: 100%; max-width: 24rem; margin-bottom: 1rem; }
.dynamia-report-group ul { list-style: none; padding: 0; margin: 0 0 1.5rem; display: grid; grid-template-columns: repeat(auto-fill, minmax(15rem, 1fr)); gap: 0.75rem; }
.dynamia-report-card { width: 100%; height: 100%; text-align: left; display: flex; flex-direction: column; gap: 0.25rem; padding: 0.75rem; border: 1px solid rgba(128, 128, 128, 0.4); border-radius: 0.5rem; background: transparent; color: inherit; cursor: pointer; font: inherit; }
.dynamia-report-card:hover, .dynamia-report-card:focus-visible { border-color: currentColor; }
.dynamia-report-error { color: #ef4444; }
</style>
