<!-- NavBreadcrumb.vue: Renders current page path as breadcrumb -->
<template>
  <nav class="dynamia-breadcrumb" aria-label="Breadcrumb">
    <ol class="dynamia-breadcrumb-list">
      <li v-if="module" class="dynamia-breadcrumb-item">
        <span class="dynamia-breadcrumb-module">{{ module.name }}</span>
      </li>
      <li v-if="group" class="dynamia-breadcrumb-item dynamia-breadcrumb-separator">
        <span>›</span>
        <span class="dynamia-breadcrumb-group">{{ group.name }}</span>
      </li>
      <li v-if="page" class="dynamia-breadcrumb-item dynamia-breadcrumb-separator">
        <span>›</span>
        <span class="dynamia-breadcrumb-page">{{ page.name }}</span>
      </li>
    </ol>
  </nav>
</template>

<script setup lang="ts">
import type { NavigationNode } from '@dynamia-tools/sdk';

defineProps<{
  /** Current navigation module node */
  module?: NavigationNode | null;
  /** Current navigation group node */
  group?: NavigationNode | null;
  /** Current navigation page node */
  page?: NavigationNode | null;
}>();
</script>
