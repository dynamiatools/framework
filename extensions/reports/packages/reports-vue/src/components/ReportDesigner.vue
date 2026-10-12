<!-- ReportDesigner.vue: designs reports without ZK: the reports CRUD plus the designer tools -->
<template>
  <div class="dynamia-report-designer">
    <div class="dynamia-report-designer-tabs" role="tablist">
      <button
        v-if="node"
        type="button"
        role="tab"
        :aria-selected="tab === 'reports'"
        :class="{ active: tab === 'reports' }"
        @click="tab = 'reports'"
      >
        {{ text.designerReports }}
      </button>
      <button
        type="button"
        role="tab"
        :aria-selected="tab === 'tools'"
        :class="{ active: tab === 'tools' }"
        @click="tab = 'tools'"
      >
        {{ text.designerTools }}
      </button>
    </div>

    <div v-if="tab === 'reports' && node" role="tabpanel" class="dynamia-report-designer-crud">
      <!-- The platform CRUD renders the report form (filters, fields and charts included) from the view descriptors -->
      <DynamiaCrudPage :node="node" :client="client" />
    </div>

    <div v-else role="tabpanel" class="dynamia-report-designer-tools">
      <p v-if="designer.info.value && !designer.info.value.allowed" class="dynamia-report-error" role="alert">
        {{ text.designerNotAllowed }}
      </p>
      <template v-else>
        <QueryPreview :designer="designer" :labels="labels" />
        <DefinitionTransfer :designer="designer" :client="client" :labels="labels" />
        <DataSourceTest :designer="designer" :labels="labels" />
      </template>
      <p v-if="designer.error.value" class="dynamia-report-error" role="alert">{{ designer.error.value }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { DynamiaClient, NavigationNode } from '@dynamia-tools/sdk';
import { DynamiaCrudPage, useDynamiaClient } from '@dynamia-tools/vue';
import { useReportDesigner } from '../composables/useReportDesigner.js';
import { resolveLabels } from '../labels.js';
import type { ReportLabels } from '../labels.js';
import DataSourceTest from './DataSourceTest.vue';
import DefinitionTransfer from './DefinitionTransfer.vue';
import QueryPreview from './QueryPreview.vue';

const props = defineProps<{
  /** DynamiaClient; defaults to the one provided by `app.use(DynamiaVue, { client })` */
  client?: DynamiaClient | undefined;
  /**
   * The navigation node of the reports design page (a `CrudPage` of the report entity). When it is not given only the
   * tools are shown.
   */
  node?: NavigationNode | undefined;
  labels?: Partial<ReportLabels> | undefined;
}>();

const client = props.client ?? useDynamiaClient();
if (!client) {
  throw new Error('DynamiaReportDesigner needs a DynamiaClient: pass the `client` prop or provide it with app.use(DynamiaVue, { client })');
}

const text = computed(() => resolveLabels(props.labels));
const tab = ref<'reports' | 'tools'>(props.node ? 'reports' : 'tools');
const designer = useReportDesigner(client);
</script>

<style scoped>
.dynamia-report-designer-tabs { display: flex; gap: 0.5rem; margin-bottom: 1rem; border-bottom: 1px solid rgba(128, 128, 128, 0.4); }
.dynamia-report-designer-tabs button { background: none; border: 0; padding: 0.5rem 1rem; cursor: pointer; font: inherit; color: inherit; border-bottom: 2px solid transparent; }
.dynamia-report-designer-tabs button.active { border-bottom-color: currentColor; font-weight: 600; }
.dynamia-report-designer-tools { display: flex; flex-direction: column; gap: 1.5rem; }
.dynamia-report-error { color: #ef4444; }
</style>
