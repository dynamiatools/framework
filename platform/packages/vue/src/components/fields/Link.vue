<!-- Link.vue: Cell link that triggers an action -->
<template>
  <a
    href="#"
    class="dynamia-link"
    @click.prevent="handleClick"
  >
    <slot>{{ displayValue }}</slot>
  </a>
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

const emit = defineEmits<{
  click: [value: unknown, field: ResolvedField];
}>();

const displayValue = computed(() => String(props.modelValue ?? ''));

function handleClick() {
  const onClick = props.params?.['onClick'];
  if (typeof onClick === 'function') onClick(props.modelValue, props.field);
  emit('click', props.modelValue, props.field);
}
</script>
