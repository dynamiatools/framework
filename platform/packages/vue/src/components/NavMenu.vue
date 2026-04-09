<!-- NavMenu.vue: Renders NavigationTree as a sidebar/menu -->
<template>
  <nav class="dynamia-nav-menu">
    <div
      v-for="module in nodes"
      :key="module.id"
      class="dynamia-nav-module"
    >
      <div class="dynamia-nav-module-header">
        <span v-if="module.icon" :class="module.icon" class="dynamia-nav-icon" />
        <span class="dynamia-nav-module-name">{{ module.name }}</span>
      </div>
      <template v-for="child in module.children ?? []" :key="child.id">
        <!-- PageGroup node -->
        <div v-if="child.children?.length" class="dynamia-nav-group">
          <div v-if="child.name" class="dynamia-nav-group-header">
            {{ child.name }}
          </div>
          <ul class="dynamia-nav-pages">
            <li
              v-for="page in child.children"
              :key="page.id"
              class="dynamia-nav-page"
              :class="{ 'dynamia-nav-page-active': currentPath === page.internalPath }"
              @click="$emit('navigate', page.internalPath!)"
            >
              <span v-if="page.icon" :class="page.icon" class="dynamia-nav-icon" />
              <span class="dynamia-nav-page-name">{{ page.name }}</span>
            </li>
          </ul>
        </div>
        <!-- Direct page node (no children) -->
        <ul v-else class="dynamia-nav-pages">
          <li
            class="dynamia-nav-page"
            :class="{ 'dynamia-nav-page-active': currentPath === child.internalPath }"
            @click="$emit('navigate', child.internalPath!)"
          >
            <span v-if="child.icon" :class="child.icon" class="dynamia-nav-icon" />
            <span class="dynamia-nav-page-name">{{ child.name }}</span>
          </li>
        </ul>
      </template>
    </div>
  </nav>
</template>

<script setup lang="ts">
import type { NavigationNode } from '@dynamia-tools/sdk';

defineProps<{
  /** Top-level navigation nodes (modules) to display */
  nodes: NavigationNode[];
  /** Currently active virtual path */
  currentPath?: string | null;
}>();

defineEmits<{
  /** Emitted when a page is clicked, with its internalPath */
  navigate: [path: string];
}>();
</script>
