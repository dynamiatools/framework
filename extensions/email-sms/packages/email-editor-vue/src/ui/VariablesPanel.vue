<script setup lang="ts">
import { computed, inject, ref } from 'vue';
import { ancestors } from '../model/tree.js';
import type { TemplateVariable } from './variables.js';
import type { EditorState } from './state.js';

const props = defineProps<{ variables: TemplateVariable[] }>();
const editor = inject<EditorState>('editor')!;
const t = inject<(key: string, values?: Record<string, string | number>) => string>('t')!;
const filter = ref('');

/** Variables that exist only inside the repeats that contain the selected block. */
const loopVariables = computed<TemplateVariable[]>(() => {
  const id = editor.selectedId.value;
  const repeats = id ? ancestors(editor.design.value, id).filter((b) => b.type === 'repeat') : [];
  if (repeats.length === 0) {
    return [];
  }
  const aliases = repeats.map((b) => String(b.props['alias'] ?? '')).filter(Boolean);
  return [...aliases.map((name) => ({ name })), ...['loop.number', 'loop.index', 'loop.count', 'loop.first', 'loop.last'].map((name) => ({ name }))];
});

const shown = computed(() => {
  const q = filter.value.trim().toLowerCase();
  const match = (v: TemplateVariable): boolean => q === '' || v.name.toLowerCase().includes(q) || (v.label ?? '').toLowerCase().includes(q);
  return { loop: loopVariables.value.filter(match), all: props.variables.filter(match) };
});

function insert(name: string): void {
  editor.insertText(`{{${name}}}`);
}
</script>

<template>
  <section class="vars" :aria-label="t('variables')">
    <h3>{{ t('variables') }}</h3>
    <input v-model="filter" type="search" :placeholder="t('searchVariables')" :aria-label="t('searchVariables')" />
    <p v-if="shown.loop.length === 0 && shown.all.length === 0" class="empty">{{ t('noVariables') }}</p>
    <template v-if="shown.loop.length > 0">
      <h4>{{ t('loopVariables') }}</h4>
      <ul>
        <li v-for="v in shown.loop" :key="`l-${v.name}`">
          <button type="button" data-variable :title="t('insertVariable', { name: v.name })" @mousedown.prevent @click="insert(v.name)">{{ v.name }}</button>
        </li>
      </ul>
    </template>
    <ul v-if="shown.all.length > 0">
      <li v-for="v in shown.all" :key="v.name">
        <button type="button" data-variable :title="[v.label, v.sample !== undefined && v.sample !== null ? String(v.sample) : ''].filter(Boolean).join(' · ') || t('insertVariable', { name: v.name })" @mousedown.prevent @click="insert(v.name)">
          <span class="name">{{ v.name }}</span>
          <span v-if="v.label" class="label">{{ v.label }}</span>
        </button>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.vars { display: flex; flex-direction: column; gap: 6px; margin-top: 18px; padding-top: 12px; border-top: 1px solid #d4d9e0; }
h3, h4 { margin: 0; font-size: 12px; text-transform: uppercase; letter-spacing: 0.04em; color: #5f6368; }
h4 { margin-top: 6px; text-transform: none; }
input { width: 100%; box-sizing: border-box; padding: 6px 8px; border: 1px solid #c4cad3; border-radius: 4px; font: inherit; }
ul { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 3px; max-height: 220px; overflow: auto; }
button { width: 100%; text-align: left; padding: 4px 8px; border: 1px solid #e3e6ea; background: #fff; border-radius: 4px; cursor: pointer; font: inherit; display: flex; gap: 8px; align-items: baseline; }
button:hover, button:focus-visible { border-color: #1a73e8; background: #f1f6fe; }
.name { font-family: ui-monospace, monospace; font-size: 12px; }
.label { color: #7a8594; font-size: 12px; }
.empty { color: #7a8594; font-size: 12px; margin: 0; }
</style>
