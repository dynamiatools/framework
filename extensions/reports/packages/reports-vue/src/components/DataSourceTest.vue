<!-- DataSourceTest.vue: tests the connection of a saved datasource -->
<template>
  <section class="dynamia-report-datasource-test">
    <h3>{{ text.testDatasource }}</h3>
    <label>
      {{ text.datasourceId }}
      <input v-model="id" type="number" min="1" />
    </label>
    <button type="button" :disabled="designer.busy.value || !id" @click="test">{{ text.testDatasource }}</button>
    <p v-if="result" :class="result.ok ? 'dynamia-report-ok' : 'dynamia-report-error'" role="status">{{ result.message }}</p>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { DataSourceTestResult } from '@dynamia-tools/reports-sdk';
import { resolveLabels } from '../labels.js';
import type { ReportLabels } from '../labels.js';
import type { useReportDesigner } from '../composables/useReportDesigner.js';

const props = defineProps<{
  designer: ReturnType<typeof useReportDesigner>;
  labels?: Partial<ReportLabels> | undefined;
}>();

const text = computed(() => resolveLabels(props.labels));
const id = ref('');
const result = ref<DataSourceTestResult | null>(null);

async function test(): Promise<void> {
  result.value = (await props.designer.testDataSource(Number(id.value))) ?? null;
}
</script>

<style scoped>
.dynamia-report-datasource-test { display: flex; flex-direction: column; gap: 0.75rem; align-items: flex-start; }
.dynamia-report-datasource-test label { display: flex; flex-direction: column; gap: 0.25rem; }
.dynamia-report-ok { color: #10b981; }
.dynamia-report-error { color: #ef4444; }
</style>
