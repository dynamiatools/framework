<script setup lang="ts">
import { computed, inject, ref } from 'vue';
import { canContain } from '../model/tree.js';
import type { BlockType, Position, Slot } from '../model/types.js';
import { DRAG_MIME, type DragPayload, type EditorState } from './state.js';

const props = defineProps<{ at: Position; parentType: BlockType | null; label?: string | undefined }>();
const editor = inject<EditorState>('editor')!;
const over = ref(false);

/** A zone only exists while a block is being dragged that its parent accepts, or as the hint of an empty list. */
const accepts = computed(() => editor.dragging.value !== null && canContain(props.parentType, props.at.slot as Slot, editor.dragging.value));
const visible = computed(() => accepts.value || props.label !== undefined);

function onDragOver(event: DragEvent): void {
  if (accepts.value) {
    event.preventDefault();
    over.value = true;
  }
}

function onDrop(event: DragEvent): void {
  over.value = false;
  const raw = event.dataTransfer?.getData(DRAG_MIME);
  if (!raw || !accepts.value) {
    return;
  }
  event.preventDefault();
  event.stopPropagation();
  try {
    editor.drop(JSON.parse(raw) as DragPayload, props.at);
  } catch {
    editor.dragging.value = null;
  }
}
</script>

<template>
  <div
    v-if="visible"
    class="zone"
    :class="{ over, active: accepts, hint: label !== undefined }"
    data-zone
    @dragover="onDragOver"
    @dragleave="over = false"
    @drop="onDrop"
  >
    <span v-if="label !== undefined">{{ label }}</span>
  </div>
</template>

<style scoped>
.zone { min-height: 6px; margin: 2px 0; border-radius: 4px; transition: background 0.1s; }
.zone.active { min-height: 14px; background: rgba(26, 115, 232, 0.12); }
.zone.over { background: rgba(26, 115, 232, 0.4); }
.zone.hint { display: flex; align-items: center; justify-content: center; min-height: 44px; border: 1px dashed #b8bfc9; color: #7a8594; font-size: 13px; background: transparent; }
.zone.hint.active { background: rgba(26, 115, 232, 0.12); border-color: #1a73e8; }
.zone.hint.over { background: rgba(26, 115, 232, 0.3); }
</style>
