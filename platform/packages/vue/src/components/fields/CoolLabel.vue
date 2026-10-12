<!-- CoolLabel.vue: Image + title + subtitle + description display component -->
<template>
  <div class="dynamia-cool-label">
    <img
      v-if="imageUrl && !imageFailed"
      :src="imageUrl"
      :alt="title"
      class="dynamia-cool-label-image"
      @error="imageFailed = true"
    />
    <div v-else class="dynamia-cool-label-image dynamia-cool-label-avatar" aria-hidden="true">{{ initial }}</div>
    <div class="dynamia-cool-label-content">
      <div v-if="title" class="dynamia-cool-label-title">{{ title }}</div>
      <div v-if="subtitle" class="dynamia-cool-label-subtitle">{{ subtitle }}</div>
      <div v-if="description" class="dynamia-cool-label-description">{{ description }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { ResolvedField } from '@dynamia-tools/ui-core';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean;
  params?: Record<string, unknown>;
}>();

/** Keys the value may carry for the image, in the order they are tried (descriptor `bindings` use `imageURL`). */
const IMAGE_KEYS = ['image', 'imageURL', 'imageUrl'];

function getField(key: string): string {
  if (!props.modelValue || typeof props.modelValue !== 'object') return '';
  const obj = props.modelValue as Record<string, unknown>;
  const fieldName = props.params?.[key] as string ?? key;
  return String(obj[fieldName] ?? '');
}

/** The photo is optional (and may not exist): without it, or when it fails to load, an initial is shown. */
const imageUrl = computed(() => IMAGE_KEYS.map(getField).find(Boolean) ?? '');
const imageFailed = ref(false);
const title = computed(() => getField('title') || getField('name'));
const subtitle = computed(() => getField('subtitle'));
const description = computed(() => getField('description'));
const initial = computed(() => (title.value.trim()[0] ?? '?').toUpperCase());
</script>
