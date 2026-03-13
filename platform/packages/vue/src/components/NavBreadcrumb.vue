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
import type { NavigationModule, NavigationGroup, NavigationPage } from '@dynamia-tools/sdk';

defineProps<{
  /** Current navigation module */
  module?: NavigationModule | null;
  /** Current navigation group */
  group?: NavigationGroup | null;
  /** Current navigation page */
  page?: NavigationPage | null;
}>();
</script>
