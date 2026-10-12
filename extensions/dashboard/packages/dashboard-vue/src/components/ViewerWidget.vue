<!-- ViewerWidget.vue: built-in renderer of `viewer` widgets; uses the DynamiaViewer component of @dynamia-tools/vue -->
<template>
  <DynamiaViewer
    ref="viewer"
    :descriptor-id="data?.descriptorId ?? undefined"
    :view-type="data?.viewType ?? undefined"
    :read-only="true"
    @ready="applyValue"
  />
</template>

<script setup lang="ts">
import { ref, resolveComponent, watch } from 'vue';
import type { ViewerWidgetData } from '@dynamia-tools/dashboard-sdk';

const props = defineProps<{
  /** The widget data: a view descriptor id (or view type) and the value to show */
  data: ViewerWidgetData | null;
}>();

// Registered globally by `app.use(DynamiaVue)` from @dynamia-tools/vue
const DynamiaViewer = resolveComponent('DynamiaViewer');
const viewer = ref<{ setValue(value: unknown): void } | null>(null);

function applyValue(): void {
  if (props.data) viewer.value?.setValue(props.data.value);
}

watch(() => props.data?.value, applyValue);
</script>
