<!-- NavMenu.vue: Renders NavigationTree as a sidebar/menu -->
<template>
  <nav class="dynamia-nav-menu">
    <div
      v-for="module in modules"
      :key="module.id"
      class="dynamia-nav-module"
    >
      <div class="dynamia-nav-module-header">
        <span v-if="module.icon" :class="module.icon" class="dynamia-nav-icon" />
        <span class="dynamia-nav-module-name">{{ module.name }}</span>
      </div>
      <div
        v-for="group in module.groups"
        :key="group.id"
        class="dynamia-nav-group"
      >
        <div v-if="group.name" class="dynamia-nav-group-header">
          {{ group.name }}
        </div>
        <ul class="dynamia-nav-pages">
          <li
            v-for="page in group.pages"
            :key="page.id"
            class="dynamia-nav-page"
            :class="{ 'dynamia-nav-page-active': currentPath === page.virtualPath }"
            @click="$emit('navigate', page.virtualPath)"
          >
            <span v-if="page.icon" :class="page.icon" class="dynamia-nav-icon" />
            <span class="dynamia-nav-page-name">{{ page.name }}</span>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import type { NavigationModule } from '@dynamia-tools/sdk';

defineProps<{
  /** Navigation modules to display */
  modules: NavigationModule[];
  /** Currently active virtual path */
  currentPath?: string | null;
}>();

defineEmits<{
  /** Emitted when a page is clicked */
  navigate: [path: string];
}>();
</script>
