<template>
  <div class="layout">
    <aside class="sidebar">
      <h1 class="brand">Dynamia Backoffice</h1>
      <DynamiaNavMenu :nodes="nodes" :current-path="currentPath" @navigate="navigateTo" />
    </aside>

    <section class="workspace">
      <header class="topbar">
        <DynamiaNavBreadcrumb :module="currentModule" :group="currentGroup" :page="activeNode" />
      </header>

      <main class="content">
        <div v-if="loading" class="panel-state">Loading navigation...</div>

        <div v-else-if="error" class="panel-state panel-state-error">
          <p>Could not load navigation from backend.</p>
          <p class="small">{{ error }}</p>
          <button type="button" @click="retry">Retry</button>
        </div>

        <DynamiaCrudPage 
          v-else-if="activeNode?.type === 'CrudPage'"
          :node="activeNode"
          :client="client"
        />

        <div v-else-if="activeNode" class="panel-state">
          <p>
            Selected node type <code>{{ activeNode.type }}</code> is not mapped in this demo.
          </p>
          <p class="small">Path: {{ activeNode.internalPath || 'N/A' }}</p>
        </div>

        <div v-else class="panel-state">
          Select a page from the navigation menu.
        </div>
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useNavigation } from '@dynamia-tools/vue';


import { client } from './lib/client';

const { nodes, currentPath, currentPage, currentModule, currentGroup, loading, error, navigateTo, reload } =
  useNavigation(client, {
    autoSelectFirst: true,
  });

const activeNode = computed(() => currentPage.value);

function retry(): void {
  void reload();
}
</script>

