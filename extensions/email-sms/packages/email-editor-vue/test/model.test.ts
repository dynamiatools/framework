import { describe, expect, it } from 'vitest';
import {
  addColumn,
  canContain,
  createBlock,
  DesignError,
  duplicate,
  emptyDesign,
  find,
  History,
  insert,
  isFullDocumentDesign,
  move,
  parseDesign,
  remove,
  removeColumn,
  serializeDesign,
  updateProps,
  walk,
  type Design,
} from '../src/index.js';

const root = { parentId: null, slot: 'children', index: 0 } as const;

function sample(): Design {
  let d = emptyDesign();
  const columns = createBlock('columns');
  d = insert(d, root, createBlock('header'));
  d = insert(d, { ...root, index: 1 }, columns);
  const [left, right] = columns.children ?? [];
  d = insert(d, { parentId: left!.id, slot: 'children', index: 0 }, createBlock('text'));
  d = insert(d, { parentId: right!.id, slot: 'children', index: 0 }, createBlock('image'));
  d = insert(d, { ...root, index: 2 }, createBlock('footer'));
  return d;
}

describe('where blocks can go', () => {
  it('follows the email structure', () => {
    expect(canContain(null, 'children', 'columns')).toBe(true);
    expect(canContain(null, 'children', 'column')).toBe(false);
    expect(canContain('columns', 'children', 'column')).toBe(true);
    expect(canContain('columns', 'children', 'text')).toBe(false);
    expect(canContain('column', 'children', 'text')).toBe(true);
    expect(canContain('column', 'children', 'columns')).toBe(false);
    expect(canContain('column', 'children', 'repeat')).toBe(true);
    expect(canContain('repeat', 'children', 'columns')).toBe(true);
    expect(canContain('condition', 'elseChildren', 'text')).toBe(true);
    expect(canContain('repeat', 'elseChildren', 'text')).toBe(false);
    expect(canContain('text', 'children', 'text')).toBe(false);
  });

  it('is enforced by insert and move', () => {
    const d = sample();
    const columns = d.blocks[1]!;
    expect(() => insert(d, { parentId: columns.id, slot: 'children', index: 0 }, createBlock('text'))).toThrow();
    expect(() => insert(d, { parentId: columns.children![0]!.id, slot: 'children', index: 0 }, createBlock('columns'))).toThrow();
    expect(() => insert(d, { parentId: 'nope', slot: 'children', index: 0 }, createBlock('text'))).toThrow(/no block/);
    expect(() => move(d, d.blocks[0]!.id, { parentId: columns.id, slot: 'children', index: 0 })).toThrow();
  });
});

describe('tree operations', () => {
  it('insert, find and walk', () => {
    const d = sample();
    expect(d.blocks.map((b) => b.type)).toEqual(['header', 'columns', 'footer']);
    const text = find(d, d.blocks[1]!.children![0]!.children![0]!.id);
    expect(text?.parent?.type).toBe('column');
    expect(text?.index).toBe(0);
    const types: string[] = [];
    walk(d, (b) => types.push(b.type));
    expect(types).toEqual(['header', 'columns', 'column', 'text', 'column', 'image', 'footer']);
  });

  it('never changes its input and shares what did not change', () => {
    const d = sample();
    const before = serializeDesign(d);
    const next = insert(d, { ...root, index: 0 }, createBlock('spacer'));
    expect(serializeDesign(d)).toBe(before);
    expect(next.blocks[1]).toBe(d.blocks[0]);
    expect(next.blocks[2]).toBe(d.blocks[1]);
  });

  it('insert clamps the index', () => {
    const d = insert(emptyDesign(), { ...root, index: 99 }, createBlock('text'));
    expect(d.blocks).toHaveLength(1);
    expect(insert(d, { ...root, index: -5 }, createBlock('spacer')).blocks[0]!.type).toBe('spacer');
  });

  it('remove takes a block with its children and ignores unknown ids', () => {
    const d = sample();
    const without = remove(d, d.blocks[1]!.id);
    expect(without.blocks.map((b) => b.type)).toEqual(['header', 'footer']);
    expect(remove(d, 'nope')).toBe(d);
  });

  it('move reorders and changes parent, counting the index after the removal', () => {
    const d = sample();
    const footerId = d.blocks[2]!.id;
    const first = move(d, footerId, { ...root, index: 0 });
    expect(first.blocks.map((b) => b.type)).toEqual(['footer', 'header', 'columns']);
    const headerId = d.blocks[0]!.id;
    const inColumn = move(d, headerId, { parentId: d.blocks[1]!.children![0]!.id, slot: 'children', index: 1 });
    expect(inColumn.blocks.map((b) => b.type)).toEqual(['columns', 'footer']);
    expect(find(inColumn, headerId)?.parent?.type).toBe('column');
    const same = move(d, d.blocks[0]!.id, { ...root, index: 2 });
    expect(same.blocks.map((b) => b.type)).toEqual(['columns', 'footer', 'header']);
  });

  it('a block cannot be moved inside itself', () => {
    const d = sample();
    const columns = d.blocks[1]!;
    expect(() => move(d, columns.id, { parentId: columns.children![0]!.id, slot: 'children', index: 0 })).toThrow(/inside itself/);
    expect(() => move(d, columns.id, { parentId: columns.id, slot: 'children', index: 0 })).toThrow(/inside itself/);
  });

  it('duplicate copies containers with new ids right after the original', () => {
    const d = sample();
    const result = duplicate(d, d.blocks[1]!.id)!;
    expect(result.design.blocks.map((b) => b.type)).toEqual(['header', 'columns', 'columns', 'footer']);
    const ids = new Set<string>();
    walk(result.design, (b) => ids.add(b.id));
    let count = 0;
    walk(result.design, () => count++);
    expect(ids.size).toBe(count);
    expect(find(result.design, result.id)?.block.children).toHaveLength(2);
    expect(duplicate(d, d.blocks[1]!.children![0]!.id)).toBeNull();
    expect(duplicate(d, 'nope')).toBeNull();
  });

  it('updateProps merges and removes settings', () => {
    const d = sample();
    const id = d.blocks[0]!.id;
    const next = updateProps(d, id, { title: 'Hello', logoWidth: undefined });
    expect(next.blocks[0]!.props['title']).toBe('Hello');
    expect('logoWidth' in next.blocks[0]!.props).toBe(false);
    expect(d.blocks[0]!.props['title']).toBe('');
    expect(updateProps(d, 'nope', { a: 1 })).toBe(d);
  });

  it('columns can be added up to four and removed down to one, with even widths', () => {
    let d = insert(emptyDesign(), root, createBlock('columns'));
    const id = d.blocks[0]!.id;
    d = addColumn(addColumn(d, id), id);
    expect(d.blocks[0]!.children!.map((c) => c.props['width'])).toEqual([25, 25, 25, 25]);
    expect(addColumn(d, id)).toBe(d);
    d = removeColumn(d, d.blocks[0]!.children![0]!.id);
    expect(d.blocks[0]!.children!.map((c) => c.props['width'])).toEqual([33, 33, 33]);
    for (let i = 0; i < 5; i++) {
      d = removeColumn(d, d.blocks[0]!.children![0]!.id);
    }
    expect(d.blocks[0]!.children).toHaveLength(1);
  });

  it('a condition has a then and an else side', () => {
    let d = insert(emptyDesign(), root, createBlock('condition', { expression: 'vip' }));
    const id = d.blocks[0]!.id;
    d = insert(d, { parentId: id, slot: 'children', index: 0 }, createBlock('text'));
    d = insert(d, { parentId: id, slot: 'elseChildren', index: 0 }, createBlock('spacer'));
    expect(d.blocks[0]!.children!.map((b) => b.type)).toEqual(['text']);
    expect(d.blocks[0]!.elseChildren!.map((b) => b.type)).toEqual(['spacer']);
    expect(find(d, d.blocks[0]!.elseChildren![0]!.id)?.slot).toBe('elseChildren');
  });
});

describe('history', () => {
  it('undoes and redoes, and a new change drops the redo', () => {
    const h = new History<number>(0);
    h.push(1);
    h.push(2);
    expect(h.undo()).toBe(1);
    expect(h.canRedo).toBe(true);
    expect(h.redo()).toBe(2);
    h.undo();
    h.push(3);
    expect(h.canRedo).toBe(false);
    expect(h.undo()).toBe(1);
    expect(h.undo()).toBe(0);
    expect(h.canUndo).toBe(false);
    expect(h.undo()).toBe(0);
  });

  it('coalesces quick changes with the same key into one step', () => {
    let t = 0;
    const h = new History<string>('', 100, 800, () => t);
    h.push('a', 'typing');
    t = 100;
    h.push('ab', 'typing');
    t = 200;
    h.push('abc', 'typing');
    expect(h.undo()).toBe('');
    t = 5000;
    h.push('x', 'typing');
    t = 10_000;
    h.push('xy', 'typing');
    expect(h.undo()).toBe('x');
  });

  it('keeps at most `limit` steps and ignores the same state', () => {
    const h = new History<number>(0, 3);
    [1, 2, 3, 4, 5].forEach((n) => h.push(n));
    h.push(5);
    const seen = [h.undo(), h.undo(), h.undo(), h.undo()];
    expect(seen).toEqual([4, 3, 2, 2]);
    h.reset(9);
    expect(h.canUndo).toBe(false);
    expect(h.present).toBe(9);
  });
});

describe('reading a design', () => {
  it('fills settings, ids and block props', () => {
    const d = parseDesign({ version: 1, blocks: [{ type: 'text', props: { html: '<p>x</p>' } }] });
    expect(d.settings.width).toBe(600);
    expect(d.blocks[0]!.id).toMatch(/^text-/);
    expect(d.blocks[0]!.props['fontSize']).toBe(16);
    expect(d.blocks[0]!.props['html']).toBe('<p>x</p>');
  });

  it('round trips through JSON', () => {
    const d = sample();
    expect(parseDesign(serializeDesign(d))).toEqual(d);
  });

  it('reads the design a migrated template has', () => {
    const json = '{"version":1,"blocks":[{"id":"html-1","type":"html","props":{"html":"<html>${x}</html>","fullDocument":true}}]}';
    const d = parseDesign(json);
    expect(isFullDocumentDesign(d)).toBe(true);
    expect(d.blocks[0]!.id).toBe('html-1');
    expect(isFullDocumentDesign(sample())).toBe(false);
  });

  it('lists every problem', () => {
    const bad = {
      version: 2,
      blocks: [{ type: 'wat' }, { type: 'column' }, { type: 'text', children: [] }, { type: 'columns', children: [{ type: 'text' }] }, { type: 'columns', children: [] }],
    };
    let error: DesignError | null = null;
    try {
      parseDesign(bad);
    } catch (e) {
      error = e as DesignError;
    }
    expect(error).toBeInstanceOf(DesignError);
    expect(error!.errors.length).toBeGreaterThanOrEqual(6);
    expect(error!.errors.join('|')).toMatch(/version 2/);
    expect(error!.errors.join('|')).toMatch(/not a known block/);
    expect(error!.errors.join('|')).toMatch(/cannot hold/);
    expect(error!.errors.join('|')).toMatch(/at least one column/);
  });

  it('rejects text that is not a design', () => {
    expect(() => parseDesign('nope')).toThrow(/not valid JSON/);
    expect(() => parseDesign('[]')).toThrow(/JSON object/);
    expect(() => parseDesign({ version: 1 })).toThrow(/blocks array/);
  });

  it('repaired duplicated ids are reported', () => {
    expect(() =>
      parseDesign({ version: 1, blocks: [{ id: 'a', type: 'spacer' }, { id: 'a', type: 'spacer' }] }),
    ).toThrow(/repeated/);
  });
});
