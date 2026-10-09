<!-- DefinitionTransfer.vue: export a report definition to a JSON file, or import one -->
<template>
  <section class="dynamia-report-transfer">
    <h3>{{ text.exportDefinition }}</h3>
    <label>
      {{ text.reportId }}
      <input v-model="exportId" type="number" min="1" />
    </label>
    <button type="button" :disabled="designer.busy.value || !exportId" @click="doExport">{{ text.exportDefinition }}</button>

    <h3>{{ text.importDefinition }}</h3>
    <input type="file" accept="application/json,.json" :aria-label="text.importDefinition" @change="doImport" />
    <p v-if="importedId" class="dynamia-report-imported" role="status">{{ text.importDone }} {{ importedId }}</p>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { DynamiaClient } from '@dynamia-tools/sdk';
import { resolveLabels } from '../labels.js';
import type { ReportLabels } from '../labels.js';
import type { useReportDesigner } from '../composables/useReportDesigner.js';

const props = defineProps<{
  designer: ReturnType<typeof useReportDesigner>;
  client?: DynamiaClient | undefined;
  labels?: Partial<ReportLabels> | undefined;
}>();

const emit = defineEmits<{
  /** A definition was imported; the payload is the id of the new report */
  imported: [id: number];
}>();

const text = computed(() => resolveLabels(props.labels));
const exportId = ref('');
const importedId = ref<number | null>(null);

async function doExport(): Promise<void> {
  await props.designer.exportDefinition(Number(exportId.value));
}

async function doImport(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  importedId.value = null;
  if (!file) return;
  const id = await props.designer.importDefinition(file);
  if (id !== undefined) {
    importedId.value = id;
    emit('imported', id);
  }
  input.value = '';
}
</script>

<style scoped>
.dynamia-report-transfer { display: flex; flex-direction: column; gap: 0.75rem; align-items: flex-start; }
.dynamia-report-transfer label { display: flex; flex-direction: column; gap: 0.25rem; }
</style>
