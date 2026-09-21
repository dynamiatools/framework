<template>
  <header class="flex items-center justify-between gap-4 border-b border-slate-200 bg-white px-4 py-3">
    <div class="flex min-w-0 items-center gap-3">
      <button
        type="button"
        class="rounded-md p-2 text-slate-500 hover:bg-slate-100 md:hidden"
        aria-label="Toggle navigation"
        @click="emit('toggle-sidebar')"
      >
        ☰
      </button>
      <DynamiaNavBreadcrumb :module="module" :group="group" :page="page" />
    </div>

    <div class="flex shrink-0 items-center gap-3">
      <select
        class="rounded-md border border-slate-200 bg-transparent px-2 py-1 text-sm text-slate-600"
        :value="skin"
        aria-label="Skin"
        @change="emit('change-skin', ($event.target as HTMLSelectElement).value)"
      >
        <option value="blue">Blue</option>
        <option value="dynamia">Dynamia</option>
        <option value="dark">Dark</option>
      </select>

      <button
        type="button"
        class="rounded-md px-3 py-1.5 text-sm font-medium"
        :style="{ backgroundColor: 'var(--skin-primary)', color: 'var(--skin-primary-fg)' }"
        @click="emit('logout')"
      >
        Sign out
      </button>
    </div>
  </header>
</template>

<script setup lang="ts">
import type { NavigationNode } from '@dynamia-tools/sdk';

defineProps<{
  module?: NavigationNode | null;
  group?: NavigationNode | null;
  page?: NavigationNode | null;
  skin: string;
}>();

const emit = defineEmits<{
  'toggle-sidebar': [];
  'change-skin': [skin: string];
  logout: [];
}>();
</script>
