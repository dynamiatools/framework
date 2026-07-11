<!-- Textareabox.vue: Multi-line text input field component -->
<template>
  <textarea
    :id="field.name"
    v-model="proxyValue"
    :placeholder="params?.placeholder as string ?? ''"
    :readonly="readOnly"
    :required="field.resolvedRequired"
    :rows="params?.rows as number ?? 4"
    :maxlength="params?.maxlength as number ?? undefined"
    class="dynamia-textareabox"
  />
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

const emit = defineEmits<{ 'update:modelValue': [value: string] }>();

const proxyValue = computed({
  get: () => String(props.modelValue ?? ''),
  set: (val) => emit('update:modelValue', val),
});
</script>
