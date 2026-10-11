<!-- ColorField.vue: `component: colorpicker`, the TailAdmin ColorPicker. Read only (a table cell) it is a swatch with
     the hex. params: `presets` (list of hex colors offered as swatches), `showText` (hex input, default true). -->
<template>
  <span v-if="readOnly" class="inline-flex items-center gap-2">
    <template v-if="color">
      <span class="size-4 rounded-full border border-black/10 dark:border-white/20" :style="{ backgroundColor: color }" />
      <span class="font-mono text-xs uppercase text-gray-500 dark:text-gray-400">{{ color }}</span>
    </template>
  </span>
  <ColorPicker
    v-else
    :model-value="color"
    :presets="presets"
    :show-text-input="params?.['showText'] !== false"
    @update:model-value="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import ColorPicker from '@dynamia-tools/tailadmin-vue/components/ext/input/ColorPicker.vue';
import type { ResolvedField } from '@dynamia-tools/ui-core';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean | undefined;
  params?: Record<string, unknown> | undefined;
}>();

const emit = defineEmits<{ 'update:modelValue': [value: string] }>();

const color = computed(() => (props.modelValue ? String(props.modelValue) : ''));
const presets = computed(() => (Array.isArray(props.params?.['presets']) ? (props.params['presets'] as string[]) : []));
</script>
