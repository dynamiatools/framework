<!-- QueryPreview.vue: runs the query being designed and shows its first rows -->
<template>
  <section class="dynamia-report-preview">
    <h3>{{ text.preview }}</h3>
    <label>
      {{ text.queryLanguage }}
      <select v-model="queryLang">
        <option value="sql">SQL</option>
        <option value="jpql">JPQL</option>
      </select>
    </label>
    <label>
      {{ text.query }}
      <textarea v-model="queryScript" rows="8" spellcheck="false"></textarea>
    </label>
    <label>
      {{ text.datasourceId }}
      <input v-model="dataSourceId" type="number" min="1" />
    </label>
    <label>
      {{ text.parameters }}
      <textarea v-model="parameters" rows="2" spellcheck="false" placeholder='{"min": 100}'></textarea>
    </label>
    <p v-if="parameterError" class="dynamia-report-error" role="alert">{{ parameterError }}</p>
    <button type="button" :disabled="designer.busy.value || !queryScript.trim()" @click="run">{{ text.preview }}</button>

    <div v-if="designer.previewResult.value" class="dynamia-report-preview-result">
      <table>
        <thead>
          <tr><th v-for="column in designer.previewResult.value.columns" :key="column">{{ column }}</th></tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in designer.previewResult.value.rows" :key="index">
            <td v-for="column in designer.previewResult.value.columns" :key="column">{{ row[column] }}</td>
          </tr>
        </tbody>
      </table>
      <small v-if="designer.previewResult.value.truncated">{{ text.previewTruncated }}</small>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { resolveLabels } from '../labels.js';
import type { ReportLabels } from '../labels.js';
import type { useReportDesigner } from '../composables/useReportDesigner.js';

const props = defineProps<{
  designer: ReturnType<typeof useReportDesigner>;
  labels?: Partial<ReportLabels> | undefined;
}>();

const text = computed(() => resolveLabels(props.labels));
const queryLang = ref<'sql' | 'jpql'>('sql');
const queryScript = ref('');
const dataSourceId = ref('');
const parameters = ref('');
const parameterError = ref<string | null>(null);

async function run(): Promise<void> {
  parameterError.value = null;
  let parsed: Record<string, string | number | boolean | null> | undefined;
  if (parameters.value.trim()) {
    try {
      parsed = JSON.parse(parameters.value);
    } catch {
      parameterError.value = `${text.value.parameters}: invalid JSON`;
      return;
    }
  }
  await props.designer.preview({
    queryLang: queryLang.value,
    queryScript: queryScript.value,
    dataSourceId: dataSourceId.value ? Number(dataSourceId.value) : null,
    parameters: parsed,
  });
}
</script>

<style scoped>
.dynamia-report-preview { display: flex; flex-direction: column; gap: 0.75rem; }
.dynamia-report-preview label { display: flex; flex-direction: column; gap: 0.25rem; }
.dynamia-report-preview textarea { font-family: monospace; }
.dynamia-report-preview-result { overflow-x: auto; }
table { border-collapse: collapse; width: 100%; }
th, td { padding: 0.25rem 0.5rem; border-bottom: 1px solid rgba(128, 128, 128, 0.3); text-align: left; }
.dynamia-report-error { color: #ef4444; }
</style>
