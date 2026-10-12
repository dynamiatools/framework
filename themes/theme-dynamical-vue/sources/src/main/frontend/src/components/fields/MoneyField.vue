<!-- MoneyField.vue: `component: moneyinput`, the TailAdmin MoneyInput. Read only it is the formatted amount.
     params: `currency` (default USD), `locale` (default the browser language), `decimals` (default 2), `min`, `max`. -->
<template>
  <span v-if="readOnly" class="tabular-nums">{{ formatted }}</span>
  <MoneyInput
    v-else
    :model-value="amount"
    :currency="currency"
    :locale="locale"
    :decimal-precision="decimals"
    :min="num('min')"
    :max="num('max')"
    :invalid="field.resolvedRequired && amount === null"
    @update:model-value="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import MoneyInput from '@dynamia-tools/tailadmin-vue/components/ext/input/MoneyInput.vue';
import type { ResolvedField } from '@dynamia-tools/ui-core';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean | undefined;
  params?: Record<string, unknown> | undefined;
}>();

const emit = defineEmits<{ 'update:modelValue': [value: number | null] }>();

function num(name: string): number | undefined {
  const value = Number(props.params?.[name]);
  return props.params?.[name] !== undefined && Number.isFinite(value) ? value : undefined;
}

const currency = computed(() => String(props.params?.['currency'] ?? 'USD'));
const locale = computed(() => String(props.params?.['locale'] ?? navigator.language ?? 'en-US'));
const decimals = computed(() => num('decimals') ?? 2);
const amount = computed(() => {
  if (props.modelValue === null || props.modelValue === undefined || props.modelValue === '') return null;
  const value = Number(props.modelValue);
  return Number.isFinite(value) ? value : null;
});
const formatted = computed(() => (amount.value === null ? '' : new Intl.NumberFormat(locale.value, {
  style: 'currency', currency: currency.value, minimumFractionDigits: decimals.value, maximumFractionDigits: decimals.value,
}).format(amount.value)));
</script>
