<script setup lang="ts">
import { computed, inject } from 'vue';
import { addColumn, find, removeColumn } from '../model/tree.js';
import { FIELDS, SETTINGS_FIELDS, type Field } from './fields.js';
import RichText from './RichText.vue';
import type { EditorState } from './state.js';

const editor = inject<EditorState>('editor')!;
const t = inject<(key: string, values?: Record<string, string | number>) => string>('t')!;

const block = computed(() => editor.selected.value);
const fields = computed<Field[]>(() => (block.value ? FIELDS[block.value.type] : []));
const columns = computed(() => (block.value?.type === 'columns' ? (block.value.children ?? []) : []));
const parentColumns = computed(() => (block.value?.type === 'column' ? (find(editor.design.value, block.value.id)?.parent ?? null) : null));

function read(field: Field, source: Record<string, unknown>): string {
  const value = source[field.key];
  return value === undefined || value === null ? '' : String(value);
}

function write(field: Field, source: 'block' | 'settings', raw: string | boolean): void {
  let value: unknown = raw;
  if (field.kind === 'number') {
    const n = Number(raw);
    if (raw === '' || !Number.isFinite(n)) {
      return;
    }
    value = Math.min(field.max ?? n, Math.max(field.min ?? n, n));
  }
  if (source === 'settings') {
    editor.updateSettings({ [field.key]: value }, field.key);
  } else if (block.value) {
    editor.update(block.value.id, { [field.key]: value }, field.key);
  }
}

function problem(field: Field, source: Record<string, unknown>): string | null {
  return field.invalid ? field.invalid(read(field, source)) : null;
}

/** The variables panel inserts into the text field the user last had the cursor in. */
function registerInserter(event: FocusEvent): void {
  const input = event.target as HTMLInputElement | HTMLTextAreaElement;
  editor.setInserter((text) => {
    const start = input.selectionStart ?? input.value.length;
    const end = input.selectionEnd ?? start;
    input.value = input.value.slice(0, start) + text + input.value.slice(end);
    input.setSelectionRange(start + text.length, start + text.length);
    input.dispatchEvent(new Event('input', { bubbles: true }));
    input.focus();
  });
}

const settings = computed(() => editor.design.value.settings as unknown as Record<string, unknown>);
const isDocument = computed(() => block.value?.type === 'html' && block.value.props['fullDocument'] === true);
</script>

<template>
  <div class="panel">
    <template v-if="block">
      <h3>{{ t(block.type) }}</h3>
      <p v-if="isDocument" class="note">{{ t('fullDocumentNote') }}</p>

      <div v-if="block.type === 'columns'" class="columns">
        <div v-for="(column, i) in columns" :key="column.id" class="row">
          <label :for="`w-${column.id}`">{{ t('columnWidth', { n: i + 1 }) }}</label>
          <input :id="`w-${column.id}`" type="number" min="5" max="100" :value="column.props['width']" @input="editor.update(column.id, { width: Math.min(100, Math.max(5, Number(($event.target as HTMLInputElement).value) || 50)) }, 'width')" />
          <button type="button" :disabled="columns.length <= 1" data-action="remove-column" :aria-label="t('removeColumn')" @click="editor.commitDesign(removeColumn(editor.design.value, column.id))">✕</button>
        </div>
        <button type="button" :disabled="columns.length >= 4" data-action="add-column" @click="editor.commitDesign(addColumn(editor.design.value, block.id))">+ {{ t('addColumn') }}</button>
      </div>

      <div v-if="parentColumns" class="row">
        <button type="button" :disabled="(parentColumns.children?.length ?? 0) <= 1" data-action="remove-this-column" @click="editor.commitDesign(removeColumn(editor.design.value, block.id))">{{ t('removeColumn') }}</button>
      </div>

      <div v-for="field in fields" :key="`${block.id}-${field.key}`" class="field">
        <label :for="`f-${field.key}`">{{ t(`f.${field.key}`) }}</label>
        <RichText v-if="field.kind === 'rich'" :model-value="read(field, block.props)" :label="t(`f.${field.key}`)" :data-field="field.key" @update:model-value="write(field, 'block', $event)" />
        <textarea v-else-if="field.kind === 'textarea' || field.kind === 'code'" :id="`f-${field.key}`" :data-field="field.key" rows="6" :class="{ code: field.kind === 'code' }" :value="read(field, block.props)" @focus="registerInserter($event)" @input="write(field, 'block', ($event.target as HTMLTextAreaElement).value)" />
        <select v-else-if="field.kind === 'select'" :id="`f-${field.key}`" :data-field="field.key" :value="read(field, block.props)" @change="write(field, 'block', ($event.target as HTMLSelectElement).value)">
          <option v-for="option in field.options" :key="option" :value="option">{{ t(`opt.${option}`) }}</option>
        </select>
        <input v-else-if="field.kind === 'checkbox'" :id="`f-${field.key}`" :data-field="field.key" type="checkbox" :checked="block.props[field.key] === true" @change="write(field, 'block', ($event.target as HTMLInputElement).checked)" />
        <span v-else-if="field.kind === 'color'" class="color">
          <input type="color" :aria-label="t(`f.${field.key}`)" :value="/^#[0-9a-f]{6}$/i.test(read(field, block.props)) ? read(field, block.props) : '#000000'" @input="write(field, 'block', ($event.target as HTMLInputElement).value)" />
          <input :id="`f-${field.key}`" :data-field="field.key" type="text" :value="read(field, block.props)" @input="write(field, 'block', ($event.target as HTMLInputElement).value)" />
        </span>
        <input v-else :id="`f-${field.key}`" :data-field="field.key" :type="field.kind === 'number' ? 'number' : 'text'" :inputmode="field.kind === 'url' ? 'url' : undefined" :min="field.min" :max="field.max" :step="field.step" :value="read(field, block.props)" @focus="field.kind !== 'number' && registerInserter($event)" @input="write(field, 'block', ($event.target as HTMLInputElement).value)" />
        <small v-if="problem(field, block.props)" class="error" role="alert" :data-error="field.key">{{ t(problem(field, block.props) as string) }}</small>
      </div>
    </template>

    <template v-else>
      <h3>{{ t('settings') }}</h3>
      <div v-for="field in SETTINGS_FIELDS" :key="field.key" class="field">
        <label :for="`s-${field.key}`">{{ t(`f.${field.key}`) }}</label>
        <select v-if="field.kind === 'select'" :id="`s-${field.key}`" :data-setting="field.key" :value="read(field, settings)" @change="write(field, 'settings', ($event.target as HTMLSelectElement).value)">
          <option v-for="option in field.options" :key="option" :value="option">{{ option.split(',')[0]?.replace(/'/g, '') }}</option>
        </select>
        <span v-else-if="field.kind === 'color'" class="color">
          <input type="color" :aria-label="t(`f.${field.key}`)" :value="/^#[0-9a-f]{6}$/i.test(read(field, settings)) ? read(field, settings) : '#ffffff'" @input="write(field, 'settings', ($event.target as HTMLInputElement).value)" />
          <input :id="`s-${field.key}`" :data-setting="field.key" type="text" :value="read(field, settings)" @input="write(field, 'settings', ($event.target as HTMLInputElement).value)" />
        </span>
        <input v-else :id="`s-${field.key}`" :data-setting="field.key" :type="field.kind === 'number' ? 'number' : 'text'" :min="field.min" :max="field.max" :value="read(field, settings)" @input="write(field, 'settings', ($event.target as HTMLInputElement).value)" />
        <small v-if="problem(field, settings)" class="error" role="alert">{{ t(problem(field, settings) as string) }}</small>
      </div>
      <p class="note">{{ t('selectBlock') }}</p>
    </template>
  </div>
</template>

<style scoped>
.panel { display: flex; flex-direction: column; gap: 10px; }
h3 { margin: 0; font-size: 13px; text-transform: uppercase; letter-spacing: 0.04em; color: #5f6368; }
.field { display: flex; flex-direction: column; gap: 3px; }
.row { display: flex; gap: 6px; align-items: center; }
.row label { flex: 1; }
label { font-size: 12px; color: #5f6368; }
input[type='text'], input[type='url'], input[type='number'], select, textarea { width: 100%; box-sizing: border-box; padding: 6px 8px; border: 1px solid #c4cad3; border-radius: 4px; font: inherit; background: #fff; }
input[type='number'] { max-width: 90px; }
textarea.code { font-family: ui-monospace, monospace; font-size: 12px; }
.color { display: flex; gap: 6px; align-items: center; }
.color input[type='color'] { width: 34px; height: 30px; padding: 0; border: 1px solid #c4cad3; border-radius: 4px; background: #fff; }
.error { color: #c5221f; font-size: 12px; }
.note { color: #7a8594; font-size: 12px; margin: 0; }
button { padding: 5px 10px; border: 1px solid #c4cad3; background: #fff; border-radius: 4px; cursor: pointer; font: inherit; }
button:disabled { opacity: 0.45; cursor: default; }
</style>
