<template>
  <div class="insights">
    <header class="insights-head">
      <h1>{{ titles[page] }}</h1>
      <p v-if="page === 'reports'">Run, filter and export the reports stored in the database.</p>
      <p v-else-if="page === 'design'">Create and edit the report definitions, test their queries and move them between systems.</p>
      <p v-else>Live figures of the movie catalog, served by <code>/api/dashboard</code> and <code>/api/reports/v2</code>.</p>
    </header>

    <DynamiaDashboard v-if="page === 'dashboard'" id="moviesDashboard" />

    <!-- keyed by the node: the designer picks its first tab when it is created, and the node arrives after mounting -->
    <DynamiaReportDesigner v-else-if="page === 'design'" :key="designNode?.id ?? 'tools'" :node="designNode" />

    <div v-else class="reports-layout">
      <aside><DynamiaReportList @select="onSelect" /></aside>
      <section>
        <DynamiaReportViewer v-if="selected" :id="selected" :key="selected" />
        <p v-else class="hint">Choose a report on the left.</p>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { NavigationNode } from '@dynamia-tools/sdk'
import { useDynamiaClient } from '@dynamia-tools/vue'

defineProps<{ page: 'dashboard' | 'reports' | 'design' }>()
const titles = { dashboard: 'Dashboard', reports: 'Reports', design: 'Reports design' }
const selected = ref<number>()
const onSelect = (report: { id: number }) => (selected.value = report.id)

// The designer lists the report definitions with the platform CRUD: it needs the navigation node of that CrudPage,
// the invisible `insights/definitions` page (see InsightsModuleProvider).
const client = useDynamiaClient()
const designNode = ref<NavigationNode>()
onMounted(async () => {
  const { navigation } = (await client?.metadata.getNavigation()) ?? { navigation: [] }
  const find = (nodes: NavigationNode[]): NavigationNode | undefined => {
    for (const node of nodes) {
      if (node.type === 'CrudPage' && node.internalPath?.endsWith('insights/definitions')) return node
      const found = node.children && find(node.children)
      if (found) return found
    }
  }
  designNode.value = find(navigation)
})
</script>
