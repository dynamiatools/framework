<!-- ReportFilters.vue: the filters form of a report, built from the filter definitions -->
<template>
  <form class="dynamia-report-filters" @submit.prevent="emit('submit')">
    <div
      v-for="filter in sortedFilters"
      :key="filter.name"
      class="dynamia-report-filter"
      :data-filter="filter.name"
    >
      <label v-if="!filter.hideLabel" :for="idOf(filter)">
        {{ filter.label }}<span v-if="filter.required" class="dynamia-report-required" :title="text.required"> *</span>
      </label>

      <template v-if="filter.dataType === 'BOOLEAN'">
        <select :id="idOf(filter)" :value="modelValue[filter.name] ?? ''" @change="set(filter, $event)">
          <option value="">{{ text.selectOption }}</option>
          <option value="true">{{ text.yes }}</option>
          <option value="false">{{ text.no }}</option>
        </select>
      </template>

      <template v-else-if="filter.optionsSource !== 'NONE'">
        <input
          v-if="filter.optionsSource === 'ENTITY'"
          type="search"
          class="dynamia-report-filter-search"
          :aria-label="filter.label"
          :placeholder="text.search"
          @input="searchOptions(filter, ($event.target as HTMLInputElement).value)"
        />
        <select :id="idOf(filter)" :value="modelValue[filter.name] ?? ''" @change="set(filter, $event)">
          <option value="">{{ text.selectOption }}</option>
          <option v-for="option in options[filter.name] ?? []" :key="String(option.value)" :value="String(option.value)">
            {{ option.label }}
          </option>
        </select>
      </template>

      <input
        v-else
        :id="idOf(filter)"
        :type="inputType(filter)"
        :step="filter.dataType === 'NUMBER' || filter.dataType === 'CURRENCY' ? 'any' : undefined"
        :value="modelValue[filter.name] ?? ''"
        :required="filter.required"
        @input="set(filter, $event)"
      />
    </div>
    <slot name="actions" />
  </form>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, watch } from 'vue';
import type { ReportFilterDefinition, ReportFilterOptionItem } from '@dynamia-tools/reports-sdk';
import { resolveLabels } from '../labels.js';
import type { ReportLabels } from '../labels.js';

const props = defineProps<{
  /** Filter definitions, from `ReportDefinition.filters` */
  filters: ReportFilterDefinition[];
  /** Form values by filter name */
  modelValue: Record<string, string>;
  /** Loads the options of a filter, see `useReport().filterOptions` */
  loadOptions?: ((filter: string, q?: string) => Promise<ReportFilterOptionItem[]>) | undefined;
  labels?: Partial<ReportLabels> | undefined;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, string>];
  submit: [];
}>();

const text = computed(() => resolveLabels(props.labels));
const sortedFilters = computed(() => [...props.filters].sort((a, b) => a.order - b.order));
const options = reactive<Record<string, ReportFilterOptionItem[]>>({});
const uid = Math.random().toString(36).slice(2, 8);

function idOf(filter: ReportFilterDefinition): string {
  return `report-filter-${uid}-${filter.name}`;
}

function inputType(filter: ReportFilterDefinition): string {
  switch (filter.dataType) {
    case 'NUMBER':
    case 'CURRENCY':
      return 'number';
    case 'DATE':
      return 'date';
    case 'DATE_TIME':
      return 'datetime-local';
    case 'TIME':
      return 'time';
    default:
      return 'text';
  }
}

function set(filter: ReportFilterDefinition, event: Event): void {
  const value = (event.target as HTMLInputElement | HTMLSelectElement).value;
  emit('update:modelValue', { ...props.modelValue, [filter.name]: value });
}

async function loadFor(filter: ReportFilterDefinition, q?: string): Promise<void> {
  if (!props.loadOptions) return;
  try {
    options[filter.name] = await props.loadOptions(filter.name, q);
  } catch {
    options[filter.name] = [];
  }
}

function searchOptions(filter: ReportFilterDefinition, q: string): void {
  void loadFor(filter, q);
}

function loadAll(): void {
  for (const filter of props.filters) {
    if (filter.optionsSource !== 'NONE' && filter.dataType !== 'BOOLEAN') void loadFor(filter);
  }
}

onMounted(loadAll);
watch(() => props.filters, loadAll);
</script>

<style scoped>
.dynamia-report-filters { display: flex; flex-wrap: wrap; gap: 0.75rem 1rem; align-items: flex-end; }
.dynamia-report-filter { display: flex; flex-direction: column; gap: 0.25rem; min-width: 12rem; }
.dynamia-report-filter label { font-size: 0.85em; opacity: 0.8; }
.dynamia-report-required { color: #ef4444; }
</style>
