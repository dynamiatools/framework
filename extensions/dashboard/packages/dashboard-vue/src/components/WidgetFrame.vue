<!-- WidgetFrame.vue: default frame of a dashboard cell (title + content). Hosts replace it with the `frame` prop -->
<template>
  <div class="dynamia-widget">
    <div v-if="response?.titleVisible" class="dynamia-dashboard-title">{{ response.title }}</div>
    <slot />
  </div>
</template>

<script setup lang="ts">
import type { DashboardWidgetResponse } from '@dynamia-tools/dashboard-sdk';

/**
 * The contract of a widget frame. A custom frame (the `frame` prop of `DynamiaDashboard`) receives these props and
 * renders the widget content, or its loading and error state, in the default slot.
 */
defineProps<{
  /** Descriptor field name the widget is bound to */
  field: string;
  /** The loaded widget, `null` while it loads or if it failed; carries the title and the `titleVisible` flag */
  response: DashboardWidgetResponse | null;
  /** True while the widget data is being loaded */
  loading: boolean;
  /** The error message when the widget failed to load */
  error: string | null;
}>();
</script>
