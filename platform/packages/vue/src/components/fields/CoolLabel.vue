<!-- CoolLabel.vue: Image + title + subtitle + description display component -->
<template>
  <div class="dynamia-cool-label">
    <img
      v-if="imageUrl"
      :src="imageUrl"
      :alt="title"
      class="dynamia-cool-label-image"
    />
    <div class="dynamia-cool-label-content">
      <div v-if="title" class="dynamia-cool-label-title">{{ title }}</div>
      <div v-if="subtitle" class="dynamia-cool-label-subtitle">{{ subtitle }}</div>
      <div v-if="description" class="dynamia-cool-label-description">{{ description }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ResolvedField } from '@dynamia-tools/ui-core';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean;
  params?: Record<string, unknown>;
}>();

function getField(key: string): string {
  if (!props.modelValue || typeof props.modelValue !== 'object') return '';
  const obj = props.modelValue as Record<string, unknown>;
  const fieldName = props.params?.[key] as string ?? key;
  return String(obj[fieldName] ?? '');
}

const imageUrl = computed(() => getField('image'));
const title = computed(() => getField('title') || getField('name'));
const subtitle = computed(() => getField('subtitle'));
const description = computed(() => getField('description'));
</script>
