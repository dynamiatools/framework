<template>
  <ThemeProvider>
    <SidebarProvider>
      <AppShell />
    </SidebarProvider>
  </ThemeProvider>
</template>

<script setup lang="ts">
import ThemeProvider from '@dynamia-tools/tailadmin-vue/components/layout/ThemeProvider.vue';
import SidebarProvider from '@dynamia-tools/tailadmin-vue/components/layout/SidebarProvider.vue';
import AppShell from './layout/AppShell.vue';
</script>
