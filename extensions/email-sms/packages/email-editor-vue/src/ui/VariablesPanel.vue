<script setup lang="ts">
import { Braces, Search } from 'lucide-vue-next';
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
    <h3 class="title"><Braces :size="14" aria-hidden="true" />{{ t('variables') }}</h3>
    <div class="search"><Search :size="14" aria-hidden="true" /><input v-model="filter" type="search" :placeholder="t('searchVariables')" :aria-label="t('searchVariables')" /></div>
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
.vars { display: flex; flex-direction: column; gap: 8px; margin-top: 22px; padding-top: 16px; border-top: 1px solid #e2e8f0; }
.title { display: flex; align-items: center; gap: 6px; margin: 0; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.06em; color: #94a3b8; }
h4 { margin: 6px 0 0; font-size: 12px; font-weight: 600; color: #475569; }
.search { position: relative; display: flex; align-items: center; }
.search svg { position: absolute; left: 9px; color: #94a3b8; pointer-events: none; }
.search input { width: 100%; box-sizing: border-box; padding: 6px 8px 6px 28px; border: 1px solid #cbd5e1; border-radius: 6px; font: inherit; font-size: 13px; }
.search input:focus { outline: 2px solid #bfdbfe; border-color: #3b82f6; }
ul { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 4px; max-height: 240px; overflow: auto; }
button { width: 100%; text-align: left; padding: 5px 9px; border: 1px solid #e2e8f0; background: #f8fafc; border-radius: 6px; cursor: pointer; font: inherit; display: flex; flex-wrap: wrap; gap: 4px 8px; align-items: baseline; transition: all 0.12s; }
button:hover, button:focus-visible { border-color: #3b82f6; background: #eff6ff; }
.name { font-family: ui-monospace, monospace; font-size: 12px; color: #1d4ed8; }
.label { color: #64748b; font-size: 12px; }
.empty { color: #64748b; font-size: 12px; margin: 0; }
</style>
