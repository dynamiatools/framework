<script setup lang="ts">
import { inject } from 'vue';
import type { BlockType } from '../model/types.js';
import { DRAG_MIME, type EditorState } from './state.js';

const editor = inject<EditorState>('editor')!;
const t = inject<(key: string, values?: Record<string, string | number>) => string>('t')!;

const ITEMS: { type: BlockType; icon: string }[] = [
  { type: 'header', icon: '▀' }, { type: 'text', icon: 'T' }, { type: 'image', icon: '▣' }, { type: 'button', icon: '▭' },
  { type: 'divider', icon: '―' }, { type: 'spacer', icon: '↕' }, { type: 'columns', icon: '▥' }, { type: 'footer', icon: '▄' },
  { type: 'html', icon: '</>' }, { type: 'repeat', icon: '↻' }, { type: 'condition', icon: '?' },
];

function onDragStart(event: DragEvent, type: BlockType): void {
  event.dataTransfer?.setData(DRAG_MIME, JSON.stringify({ kind: 'new', type }));
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'copy';
  }
  editor.dragging.value = type;
}
</script>

<template>
  <nav class="palette" :aria-label="t('blocks')">
    <h3>{{ t('blocks') }}</h3>
    <button
      v-for="item in ITEMS"
      :key="item.type"
      type="button"
      class="item"
      draggable="true"
      :data-palette="item.type"
      @click="editor.addBlock(item.type)"
      @dragstart="onDragStart($event, item.type)"
      @dragend="editor.dragging.value = null"
    >
      <span class="icon" aria-hidden="true">{{ item.icon }}</span>
      <span>{{ t(item.type) }}</span>
    </button>
  </nav>
</template>

<style scoped>
.palette { display: flex; flex-direction: column; gap: 6px; }
h3 { margin: 0 0 4px; font-size: 13px; text-transform: uppercase; letter-spacing: 0.04em; color: #5f6368; }
.item { display: flex; align-items: center; gap: 10px; padding: 8px 10px; background: #fff; border: 1px solid #d4d9e0; border-radius: 6px; cursor: grab; font: inherit; text-align: left; }
.item:hover, .item:focus-visible { border-color: #1a73e8; background: #f1f6fe; }
.icon { width: 22px; text-align: center; font-weight: 700; color: #1a73e8; }
</style>
