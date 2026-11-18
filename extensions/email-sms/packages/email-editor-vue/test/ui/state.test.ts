import { describe, expect, it } from 'vitest';
import { createBlock, emptyDesign, find, insert, useEditorState, type Design } from '../../src/index.js';

const at = (index: number, parentId: string | null = null) => ({ parentId, slot: 'children', index }) as const;

function state(...types: Parameters<typeof createBlock>[0][]) {
  let d: Design = emptyDesign();
  types.forEach((type, i) => (d = insert(d, at(i), createBlock(type))));
  return useEditorState(d);
}

describe('adding blocks', () => {
  it('goes to the end of the email with nothing selected, and selects the new block', () => {
    const s = state('text');
    const id = s.addBlock('button');
    expect(s.design.value.blocks.map((b) => b.type)).toEqual(['text', 'button']);
    expect(s.selectedId.value).toBe(id);
  });

  it('goes inside a selected container and after a selected leaf', () => {
    const s = state('repeat', 'text');
    s.select(s.design.value.blocks[0]!.id);
    s.addBlock('spacer');
    expect(s.design.value.blocks[0]!.children!.map((b) => b.type)).toEqual(['spacer']);
    s.select(s.design.value.blocks[1]!.id);
    s.addBlock('divider');
    expect(s.design.value.blocks.map((b) => b.type)).toEqual(['repeat', 'text', 'divider']);
  });

  it('puts a block picked while a block inside a column is selected after it, and never nests columns', () => {
    const s = state('columns');
    const column = s.design.value.blocks[0]!.children![0]!;
    s.select(column.id);
    const text = s.addBlock('text')!;
    s.select(text);
    s.addBlock('button');
    expect(find(s.design.value, column.id)!.block.children!.map((b) => b.type)).toEqual(['text', 'button']);
    // columns cannot go inside a column, so the picked block goes to the end of the email instead
    s.addBlock('columns');
    expect(s.design.value.blocks.map((b) => b.type)).toEqual(['columns', 'columns']);
    expect(find(s.design.value, column.id)!.block.children!.map((b) => b.type)).toEqual(['text', 'button']);
    expect(s.addBlock('column')).toBeNull();
  });

  it('refuses a position that does not accept the block', () => {
    const s = state('columns');
    expect(s.addBlock('text', { parentId: s.design.value.blocks[0]!.id, slot: 'children', index: 0 })).toBeNull();
    expect(s.canUndo.value).toBe(false);
  });
});

describe('changing the design', () => {
  it('moves a block with the index adjusted for the removal', () => {
    const s = state('header', 'text', 'footer');
    const [a] = s.design.value.blocks;
    s.moveTo(a!.id, at(3));
    expect(s.design.value.blocks.map((b) => b.type)).toEqual(['text', 'footer', 'header']);
    s.moveTo(a!.id, at(2));
    expect(s.design.value.blocks.map((b) => b.type)).toEqual(['text', 'footer', 'header']);
    s.moveTo(a!.id, at(0));
    expect(s.design.value.blocks.map((b) => b.type)).toEqual(['header', 'text', 'footer']);
  });

  it('moves up and down, stopping at the ends', () => {
    const s = state('header', 'text', 'footer');
    const ids = s.design.value.blocks.map((b) => b.id);
    s.moveBy(ids[0]!, -1);
    expect(s.canUndo.value).toBe(false);
    s.moveBy(ids[0]!, 1);
    expect(s.design.value.blocks.map((b) => b.id)).toEqual([ids[1], ids[0], ids[2]]);
    s.moveBy(ids[2]!, 1);
    expect(s.design.value.blocks.map((b) => b.id)).toEqual([ids[1], ids[0], ids[2]]);
  });

  it('ignores a move the tree forbids', () => {
    const s = state('columns', 'text');
    const [columns, text] = s.design.value.blocks;
    s.moveTo(text!.id, { parentId: columns!.id, slot: 'children', index: 0 });
    s.moveTo(columns!.id, { parentId: columns!.children![0]!.id, slot: 'children', index: 0 });
    expect(s.design.value.blocks.map((b) => b.type)).toEqual(['columns', 'text']);
  });

  it('removes and clears the selection; duplicates and selects the copy', () => {
    const s = state('text', 'button');
    const [text] = s.design.value.blocks;
    s.select(text!.id);
    s.duplicateBlock(text!.id);
    expect(s.design.value.blocks.map((b) => b.type)).toEqual(['text', 'text', 'button']);
    expect(s.selectedId.value).not.toBe(text!.id);
    s.removeBlock(s.selectedId.value!);
    expect(s.selectedId.value).toBeNull();
    s.removeBlock('nope');
    expect(s.design.value.blocks).toHaveLength(2);
  });

  it('undoes and redoes, and a selection of a block that no longer exists is cleared', () => {
    const s = state('text');
    const id = s.addBlock('button')!;
    expect(s.selectedId.value).toBe(id);
    s.undo();
    expect(s.design.value.blocks).toHaveLength(1);
    expect(s.selectedId.value).toBeNull();
    expect(s.canRedo.value).toBe(true);
    s.redo();
    expect(s.design.value.blocks).toHaveLength(2);
  });

  it('counts quick typing in one field as one undo step, and notifies every change', () => {
    const changes: number[] = [];
    let d: Design = emptyDesign();
    d = insert(d, at(0), createBlock('text'));
    const s = useEditorState(d, (design) => changes.push(design.blocks.length));
    const id = s.design.value.blocks[0]!.id;
    s.update(id, { html: 'a' }, 'html');
    s.update(id, { html: 'ab' }, 'html');
    s.update(id, { html: 'abc' }, 'html');
    expect(changes).toHaveLength(3);
    s.undo();
    expect(find(s.design.value, id)!.block.props['html']).toBe('<p>Write your text here</p>');
  });

  it('changes the email settings', () => {
    const s = state();
    s.updateSettings({ width: 700 });
    expect(s.design.value.settings.width).toBe(700);
    s.undo();
    expect(s.design.value.settings.width).toBe(600);
  });

  it('load replaces the design and forgets the history', () => {
    const s = state('text');
    s.addBlock('button');
    s.load(emptyDesign());
    expect(s.design.value.blocks).toHaveLength(0);
    expect(s.canUndo.value).toBe(false);
  });
});

describe('dropping', () => {
  it('adds a new block or moves an existing one and ends the drag', () => {
    const s = state('header', 'text');
    s.dragging.value = 'button';
    s.drop({ kind: 'new', type: 'button' }, at(1));
    expect(s.design.value.blocks.map((b) => b.type)).toEqual(['header', 'button', 'text']);
    expect(s.dragging.value).toBeNull();
    s.drop({ kind: 'move', id: s.design.value.blocks[0]!.id }, at(3));
    expect(s.design.value.blocks.map((b) => b.type)).toEqual(['button', 'text', 'header']);
  });
});
