<script setup lang="ts">
import { Braces } from 'lucide-vue-next';
import { inject } from 'vue';
import type { BlockType } from '../model/types.js';
import { BLOCK_ICONS } from './icons.js';
import { DRAG_MIME, type EditorState } from './state.js';

const editor = inject<EditorState>('editor')!;
const t = inject<(key: string, values?: Record<string, string | number>) => string>('t')!;

/** The blocks the user can add, in groups. Dragging one into the email or clicking it adds a new block. */
const GROUPS: { title: string; items: BlockType[] }[] = [
  { title: 'groupContent', items: ['heading', 'text', 'quote', 'list', 'image', 'button', 'divider', 'spacer'] },
  { title: 'groupLayout', items: ['header', 'columns', 'table', 'footer'] },
  { title: 'groupLogic', items: ['repeat', 'condition', 'html'] },
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
  <nav class="palette de:flex de:flex-col de:gap-5" :aria-label="t('blocks')">
    <h2 class="de:sr-only">{{ t('blocks') }}</h2>
    <section v-for="group in GROUPS" :key="group.title">
      <h3 class="de:mb-2 de:flex de:items-center de:gap-1.5 de:text-[11px] de:font-semibold de:uppercase de:tracking-wider de:text-slate-400">
        <Braces v-if="group.title === 'groupLogic'" :size="12" aria-hidden="true" />{{ t(group.title) }}
      </h3>
      <div class="de:grid de:grid-cols-2 de:gap-2">
        <button
          v-for="type in group.items"
          :key="type"
          type="button"
          class="item de:group de:box-border de:[font-family:inherit] de:flex de:cursor-grab de:flex-col de:items-center de:gap-1.5 de:rounded-lg de:border de:border-slate-200 de:bg-white de:px-2 de:py-2.5 de:text-xs de:font-medium de:text-slate-600 de:shadow-sm de:transition de:hover:border-blue-400 de:hover:bg-blue-50 de:hover:text-blue-700 de:hover:shadow de:focus-visible:outline-2 de:focus-visible:outline-blue-500 de:active:cursor-grabbing"
          draggable="true"
          :data-palette="type"
          @click="editor.addBlock(type)"
          @dragstart="onDragStart($event, type)"
          @dragend="editor.dragging.value = null"
        >
          <component :is="BLOCK_ICONS[type]" :size="20" :stroke-width="1.75" class="de:text-slate-400 de:transition de:group-hover:text-blue-600" aria-hidden="true" />
          <span>{{ t(type) }}</span>
        </button>
      </div>
    </section>
  </nav>
</template>
