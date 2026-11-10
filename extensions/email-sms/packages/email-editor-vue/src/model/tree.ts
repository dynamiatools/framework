import { createBlock, newId } from './defaults.js';
import { LEAF_TYPES, type Block, type BlockType, type Design, type Position, type Slot } from './types.js';

/**
 * Operations on the block tree. They never change their input: each returns a new design that shares the parts
 * that did not change, so a history of designs is cheap.
 */

const CONTENT: BlockType[] = [...LEAF_TYPES, 'repeat', 'condition'];

/**
 * What a place accepts. The email root, a repeat and the two sides of a condition take any content block and
 * `columns`; a `columns` block only takes `column` blocks; a `column` takes content blocks but not `columns` (email
 * clients cannot nest columns).
 */
export function canContain(parentType: BlockType | null, slot: Slot, child: BlockType): boolean {
  if (slot === 'elseChildren' && parentType !== 'condition') {
    return false;
  }
  switch (parentType) {
    case null:
    case 'repeat':
    case 'condition':
      return child === 'columns' || CONTENT.includes(child);
    case 'columns':
      return child === 'column';
    case 'column':
      return CONTENT.includes(child);
    default:
      return false; // leaf blocks hold nothing
  }
}

export interface Found {
  block: Block;
  parent: Block | null;
  slot: Slot;
  index: number;
}

/** Finds a block and where it lives, or null. */
export function find(design: Design, id: string): Found | null {
  const search = (list: Block[], parent: Block | null, slot: Slot): Found | null => {
    for (let index = 0; index < list.length; index++) {
      const block = list[index] as Block;
      if (block.id === id) {
        return { block, parent, slot, index };
      }
      const inside = search(block.children ?? [], block, 'children') ?? search(block.elseChildren ?? [], block, 'elseChildren');
      if (inside) {
        return inside;
      }
    }
    return null;
  };
  return search(design.blocks, null, 'children');
}

/** The blocks that contain a block, from its parent up to the top of the email. */
export function ancestors(design: Design, id: string): Block[] {
  const chain: Block[] = [];
  let found = find(design, id);
  while (found?.parent) {
    chain.push(found.parent);
    found = find(design, found.parent.id);
  }
  return chain;
}

/** Every block of the design, parents before children. */
export function walk(design: Design, visit: (block: Block, parent: Block | null) => void): void {
  const go = (list: Block[], parent: Block | null): void => {
    for (const block of list) {
      visit(block, parent);
      go(block.children ?? [], block);
      go(block.elseChildren ?? [], block);
    }
  };
  go(design.blocks, null);
}

/** Replaces the list at (parentId, slot) with `edit(list)`. */
function editList(design: Design, parentId: string | null, slot: Slot, edit: (list: Block[]) => Block[]): Design {
  if (parentId === null) {
    return { ...design, blocks: edit(design.blocks) };
  }
  const go = (list: Block[]): Block[] =>
    list.map((block) => {
      if (block.id === parentId) {
        return slot === 'children' ? { ...block, children: edit(block.children ?? []) } : { ...block, elseChildren: edit(block.elseChildren ?? []) };
      }
      const children = block.children ? go(block.children) : undefined;
      const elseChildren = block.elseChildren ? go(block.elseChildren) : undefined;
      if (children === block.children && elseChildren === block.elseChildren) {
        return block;
      }
      return {
        ...block,
        ...(children ? { children } : {}),
        ...(elseChildren ? { elseChildren } : {}),
      };
    });
  return { ...design, blocks: go(design.blocks) };
}

function typeOfParent(design: Design, parentId: string | null): BlockType | null {
  if (parentId === null) {
    return null;
  }
  const parent = find(design, parentId);
  if (!parent) {
    throw new Error(`There is no block ${parentId}`);
  }
  return parent.block.type;
}

function assertAllowed(design: Design, at: Position, child: BlockType): void {
  const parentType = typeOfParent(design, at.parentId);
  if (!canContain(parentType, at.slot, child)) {
    throw new Error(`A ${parentType ?? 'email'} cannot hold a ${child} in ${at.slot}`);
  }
}

function insertAt(list: Block[], index: number, block: Block): Block[] {
  const at = Math.max(0, Math.min(index, list.length));
  return [...list.slice(0, at), block, ...list.slice(at)];
}

/** Puts a block at a position. Throws when the parent does not accept that kind of block. */
export function insert(design: Design, at: Position, block: Block): Design {
  assertAllowed(design, at, block.type);
  return editList(design, at.parentId, at.slot, (list) => insertAt(list, at.index, block));
}

/** Removes a block with everything inside it. Unknown ids change nothing. */
export function remove(design: Design, id: string): Design {
  const found = find(design, id);
  if (!found) {
    return design;
  }
  return editList(design, found.parent?.id ?? null, found.slot, (list) => list.filter((b) => b.id !== id));
}

/**
 * Moves a block. `at.index` counts the destination list after the block was taken out of it. Throws when the move
 * would put a block inside itself or the destination does not accept it.
 */
export function move(design: Design, id: string, at: Position): Design {
  const found = find(design, id);
  if (!found) {
    return design;
  }
  if (at.parentId !== null && (at.parentId === id || find({ ...design, blocks: [found.block] }, at.parentId))) {
    throw new Error('A block cannot be moved inside itself');
  }
  assertAllowed(design, at, found.block.type);
  const without = remove(design, id);
  return editList(without, at.parentId, at.slot, (list) => insertAt(list, at.index, found.block));
}

/** Copies a block with new ids (the copy of a container copies what is inside) right after the original. */
export function duplicate(design: Design, id: string): { design: Design; id: string } | null {
  const found = find(design, id);
  if (!found || found.block.type === 'column') {
    return null;
  }
  const copy = cloneWithNewIds(found.block);
  return {
    design: editList(design, found.parent?.id ?? null, found.slot, (list) => insertAt(list, found.index + 1, copy)),
    id: copy.id,
  };
}

function cloneWithNewIds(block: Block): Block {
  return {
    ...block,
    id: newId(block.type),
    props: { ...block.props },
    ...(block.children ? { children: block.children.map(cloneWithNewIds) } : {}),
    ...(block.elseChildren ? { elseChildren: block.elseChildren.map(cloneWithNewIds) } : {}),
  };
}

/** Changes some settings of a block; a patch value of `undefined` removes the setting. */
export function updateProps(design: Design, id: string, patch: Record<string, unknown>): Design {
  const found = find(design, id);
  if (!found) {
    return design;
  }
  const props = { ...found.block.props, ...patch };
  for (const key of Object.keys(patch)) {
    if (patch[key] === undefined) {
      delete props[key];
    }
  }
  return editList(design, found.parent?.id ?? null, found.slot, (list) => list.map((b) => (b.id === id ? { ...b, props } : b)));
}

/** Adds a column to a `columns` block (up to 4) and splits the widths evenly. */
export function addColumn(design: Design, columnsId: string): Design {
  const found = find(design, columnsId);
  if (!found || found.block.type !== 'columns' || (found.block.children?.length ?? 0) >= 4) {
    return design;
  }
  const count = (found.block.children?.length ?? 0) + 1;
  const withNew = insert(design, { parentId: columnsId, slot: 'children', index: count - 1 }, createBlock('column'));
  return evenWidths(withNew, columnsId);
}

/** Removes a column (the last one cannot be removed) and splits the widths evenly. */
export function removeColumn(design: Design, columnId: string): Design {
  const found = find(design, columnId);
  if (!found || found.block.type !== 'column' || !found.parent || (found.parent.children?.length ?? 0) <= 1) {
    return design;
  }
  return evenWidths(remove(design, columnId), found.parent.id);
}

function evenWidths(design: Design, columnsId: string): Design {
  const columns = find(design, columnsId)?.block.children ?? [];
  const width = Math.floor(100 / columns.length);
  return columns.reduce((d, column) => updateProps(d, column.id, { width }), design);
}
