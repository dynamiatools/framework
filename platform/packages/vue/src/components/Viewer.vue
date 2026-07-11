<!-- Viewer.vue: Universal view host component — primary component for consumers -->
<template>
  <div class="dynamia-viewer">
    <div v-if="loading" class="dynamia-viewer-loading">
      <slot name="loading">
        <span>Loading...</span>
      </slot>
    </div>
    <div v-else-if="error" class="dynamia-viewer-error">
      <slot name="error" :error="error">
        <span>{{ error }}</span>
      </slot>
    </div>
    <template v-else-if="view">
      <component
        :is="resolvedComponent"
        v-if="resolvedComponent"
        :view="view"
        :read-only="readOnly"
        v-bind="$attrs"
      />
      <slot v-else name="unsupported">
        <span>Unsupported view type: {{ viewType }}</span>
      </slot>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, shallowRef } from 'vue';
import type { Component } from 'vue';
import type { ViewerConfig, View } from '@dynamia-tools/ui-core';
import { ViewRendererRegistry } from '@dynamia-tools/ui-core';
import { VueViewer } from '../views/VueViewer.js';

/** Props for the Viewer component */
export interface ViewerProps extends Omit<ViewerConfig, 'client'> {
  /** View type name ('form', 'table', 'crud', etc.) */
  viewType?: string;
  /** Entity bean class name (e.g. 'com.example.Book') */
  beanClass?: string;
  /** Whether the view is in read-only mode */
  readOnly?: boolean;
}

const props = withDefaults(defineProps<ViewerProps>(), {
  readOnly: false,
});

const emit = defineEmits<{
  /** Emitted when the view is initialized */
  ready: [view: View];
  /** Emitted on error */
  error: [message: string];
}>();

const viewer = new VueViewer({
  viewType: props.viewType ?? null,
  beanClass: props.beanClass ?? null,
  readOnly: props.readOnly,
  ...(props.descriptor != null ? { descriptor: props.descriptor } : {}),
  ...(props.descriptorId != null ? { descriptorId: props.descriptorId } : {}),
});

const loading = viewer.loading;
const error = viewer.error;
const view = viewer.currentView;

const resolvedComponent = shallowRef<Component | null>(null);

onMounted(async () => {
  try {
    await viewer.initialize();
    if (viewer.resolvedViewType && ViewRendererRegistry.hasRenderer(viewer.resolvedViewType)) {
      const renderer = ViewRendererRegistry.getRenderer(viewer.resolvedViewType);
      resolvedComponent.value = renderer.render(view.value!) as Component;
    }
    if (view.value) emit('ready', view.value);
  } catch (e) {
    emit('error', String(e));
  }
});

onUnmounted(() => {
  viewer.destroy();
});

/** Get the primary value from the view */
const getValue = () => viewer.getValue();

/** Set the primary value on the view */
const setValue = (value: unknown) => viewer.setValue(value);

defineExpose({ viewer, getValue, setValue });
</script>
