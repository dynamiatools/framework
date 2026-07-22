<template>
  <aside
    class="fixed inset-y-0 left-0 z-30 flex w-64 flex-col bg-slate-900 transition-transform duration-200 md:static md:translate-x-0"
    :class="mobileOpen ? 'translate-x-0' : '-translate-x-full'"
  >
    <div class="flex items-center gap-2 border-b border-white/10 px-4 py-4">
      <img v-if="logo" :src="logo" alt="" class="h-7 w-7 rounded" />
      <span class="truncate text-lg font-semibold text-white">{{ appName }}</span>
    </div>
    <div class="flex-1 overflow-y-auto">
      <DynamiaNavMenu :nodes="nodes" :current-path="currentPath" @navigate="onNavigate" />
    </div>
  </aside>

  <div v-if="mobileOpen" class="fixed inset-0 z-20 bg-black/50 md:hidden" @click="emit('close')" />
</template>

<script setup lang="ts">
import type { NavigationNode } from '@dynamia-tools/sdk';

defineProps<{
  nodes: NavigationNode[];
  currentPath?: string | null;
  appName: string;
  logo?: string | null;
  mobileOpen: boolean;
}>();

const emit = defineEmits<{
  navigate: [path: string];
  close: [];
}>();

function onNavigate(path: string): void {
  emit('navigate', path);
  emit('close');
}
</script>
