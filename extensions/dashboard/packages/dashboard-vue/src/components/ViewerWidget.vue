<!-- ViewerWidget.vue: built-in renderer of `viewer` widgets; uses the DynamiaViewer component of @dynamia-tools/vue -->
<template>
  <DynamiaViewer
    :descriptor-id="data?.descriptorId ?? undefined"
    :view-type="data?.viewType ?? undefined"
    :source="rows"
    :value="single"
    :read-only="true"
  />
</template>

<script setup lang="ts">
import { computed, resolveComponent } from 'vue';
import type { ViewerWidgetData } from '@dynamia-tools/dashboard-sdk';

const props = defineProps<{
  /** The widget data: a view descriptor id (or view type) and the value to show */
  data: ViewerWidgetData | null;
}>();

// Registered globally by `app.use(DynamiaVue)` from @dynamia-tools/vue
const DynamiaViewer = resolveComponent('DynamiaViewer');

// A list is the data source of a table/tree view (its rows); anything else is the value of a form-like view.
const rows = computed(() => (Array.isArray(props.data?.value) ? props.data.value : undefined));
const single = computed(() => (Array.isArray(props.data?.value) ? undefined : props.data?.value));
</script>
