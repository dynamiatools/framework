import { computed, ref, shallowRef } from 'vue';
import { createBlock } from '../model/defaults.js';
import { History } from '../model/history.js';
import { canContain, duplicate, find, insert, move, remove, updateProps } from '../model/tree.js';
import type { BlockType, Design, Position } from '../model/types.js';

/** What a drag carries: a new block from the palette, or an existing block being moved. */
export type DragPayload = { kind: 'new'; type: BlockType } | { kind: 'move'; id: string };

/** The mime type used for drags inside the editor. */
export const DRAG_MIME = 'application/x-dynamia-email-block';

/**
 * The state of one editor: the design with its undo history, the selected block and the block being dragged. Every
 * change goes through here, so the canvas, the palette and the property panels never edit the design on their own.
 */
export function useEditorState(initial: Design, onChange: (design: Design) => void = () => undefined) {
  const history = new History<Design>(initial);
  const design = shallowRef<Design>(initial);
  const version = ref(0);
  const selectedId = ref<string | null>(null);
  const dragging = ref<BlockType | null>(null);

  const canUndo = computed(() => (version.value, history.canUndo));
  const canRedo = computed(() => (version.value, history.canRedo));

  function commit(next: Design, key: string | null = null): void {
    history.push(next, key);
    design.value = history.present;
    version.value++;
    onChange(design.value);
  }

  /** Makes a design built by the tree operations the new present, as one undo step. */
  function commitDesign(next: Design): void {
    if (next !== design.value) {
      commit(next);
    }
  }

  function select(id: string | null): void {
    selectedId.value = id;
  }

  /** Where `addBlock` puts a block: inside the selected container, after the selected block, or at the end. */
  function defaultPosition(type: BlockType): Position {
    const current = selectedId.value ? find(design.value, selectedId.value) : null;
    if (current) {
      if (canContain(current.block.type, 'children', type)) {
        return { parentId: current.block.id, slot: 'children', index: current.block.children?.length ?? 0 };
      }
      if (canContain(current.parent?.type ?? null, current.slot, type)) {
        return { parentId: current.parent?.id ?? null, slot: current.slot, index: current.index + 1 };
      }
    }
    return { parentId: null, slot: 'children', index: design.value.blocks.length };
  }

  /** Adds a new block and selects it. Returns its id, or null when the place does not accept it. */
  function addBlock(type: BlockType, at: Position = defaultPosition(type)): string | null {
    if (type === 'column' || !canContain(at.parentId === null ? null : (find(design.value, at.parentId)?.block.type ?? null), at.slot, type)) {
      return null;
    }
    const block = createBlock(type);
    commit(insert(design.value, at, block));
    selectedId.value = block.id;
    return block.id;
  }

  /** Handles a drop of a palette block or a moved block on a position. */
  function drop(payload: DragPayload, at: Position): void {
    if (payload.kind === 'new') {
      addBlock(payload.type, at);
    } else {
      moveTo(payload.id, at);
    }
    dragging.value = null;
  }

  /** `at.index` is the position in the list as it is now: moving down inside one list is adjusted for the removal. */
  function moveTo(id: string, at: Position): void {
    const found = find(design.value, id);
    if (!found) {
      return;
    }
    const sameList = (found.parent?.id ?? null) === at.parentId && found.slot === at.slot;
    const index = sameList && at.index > found.index ? at.index - 1 : at.index;
    if (sameList && index === found.index) {
      return;
    }
    try {
      commit(move(design.value, id, { ...at, index }));
      selectedId.value = id;
    } catch {
      // a move the tree does not allow (into itself, wrong kind of parent) changes nothing
    }
  }

  function moveBy(id: string, delta: -1 | 1): void {
    const found = find(design.value, id);
    if (!found) {
      return;
    }
    const length = (found.parent ? (found.slot === 'children' ? found.parent.children : found.parent.elseChildren)?.length : design.value.blocks.length) ?? 0;
    const target = found.index + delta;
    if (target < 0 || target >= length) {
      return;
    }
    commit(move(design.value, id, { parentId: found.parent?.id ?? null, slot: found.slot, index: target }));
  }

  function removeBlock(id: string): void {
    const next = remove(design.value, id);
    if (next !== design.value) {
      if (selectedId.value && !find(next, selectedId.value)) {
        selectedId.value = null;
      }
      commit(next);
    }
  }

  function duplicateBlock(id: string): void {
    const result = duplicate(design.value, id);
    if (result) {
      commit(result.design);
      selectedId.value = result.id;
    }
  }

  /** Changes the settings of a block; pass `key` when it comes from typing so quick edits count as one undo step. */
  function update(id: string, patch: Record<string, unknown>, key: string | null = null): void {
    commit(updateProps(design.value, id, patch), key === null ? null : `${id}:${key}`);
  }

  function updateSettings(patch: Partial<Design['settings']>, key: string | null = null): void {
    commit({ ...design.value, settings: { ...design.value.settings, ...patch } }, key === null ? null : `settings:${key}`);
  }

  function undo(): void {
    design.value = history.undo();
    version.value++;
    if (selectedId.value && !find(design.value, selectedId.value)) {
      selectedId.value = null;
    }
    onChange(design.value);
  }

  function redo(): void {
    design.value = history.redo();
    version.value++;
    onChange(design.value);
  }

  /** Replaces the whole design (after loading) and forgets the history. */
  function load(next: Design): void {
    history.reset(next);
    design.value = next;
    version.value++;
    selectedId.value = null;
  }

  const selected = computed(() => (selectedId.value ? (find(design.value, selectedId.value)?.block ?? null) : null));

  return { design, selectedId, selected, dragging, canUndo, canRedo, select, commitDesign, addBlock, drop, moveTo, moveBy, removeBlock, duplicateBlock, update, updateSettings, undo, redo, load };
}

export type EditorState = ReturnType<typeof useEditorState>;
