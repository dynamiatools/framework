import { DEFAULT_SETTINGS, defaultProps, newId } from './defaults.js';
import { canContain } from './tree.js';
import { BLOCK_TYPES, DesignError, type Block, type BlockType, type Design, type DesignSettings, type Slot } from './types.js';

const isObject = (value: unknown): value is Record<string, unknown> => typeof value === 'object' && value !== null && !Array.isArray(value);

/**
 * Reads a design from JSON text or an already parsed value and returns it complete: missing settings get their
 * defaults, blocks without an id get one, missing block settings get theirs. Throws a {@link DesignError} listing
 * every problem when it is not a design (wrong version, unknown block types, blocks in places that do not accept them).
 */
export function parseDesign(source: string | unknown): Design {
  let raw: unknown = source;
  if (typeof source === 'string') {
    try {
      raw = JSON.parse(source);
    } catch {
      throw new DesignError(['The design is not valid JSON']);
    }
  }
  const errors: string[] = [];
  if (!isObject(raw)) {
    throw new DesignError(['The design must be a JSON object']);
  }
  if (raw['version'] !== 1) {
    errors.push(`Unsupported design version ${String(raw['version'])}`);
  }
  if (!Array.isArray(raw['blocks'])) {
    errors.push('The design needs a blocks array');
  }
  const settings = { ...DEFAULT_SETTINGS, ...(isObject(raw['settings']) ? (raw['settings'] as Partial<DesignSettings>) : {}) };
  const ids = new Set<string>();
  const blocks = Array.isArray(raw['blocks']) ? readList(raw['blocks'], null, 'children', 'blocks', ids, errors) : [];
  if (errors.length > 0) {
    throw new DesignError(errors);
  }
  return { version: 1, settings, blocks };
}

function readList(list: unknown[], parentType: BlockType | null, slot: Slot, path: string, ids: Set<string>, errors: string[]): Block[] {
  const out: Block[] = [];
  list.forEach((item, index) => {
    const where = `${path}[${index}]`;
    if (!isObject(item) || typeof item['type'] !== 'string' || !(BLOCK_TYPES as readonly string[]).includes(item['type'])) {
      errors.push(`${where} is not a known block`);
      return;
    }
    const type = item['type'] as BlockType;
    if (!canContain(parentType, slot, type)) {
      errors.push(`${where}: a ${parentType ?? 'email'} cannot hold a ${type}`);
      return;
    }
    let id = typeof item['id'] === 'string' && item['id'] !== '' ? item['id'] : newId(type);
    if (ids.has(id)) {
      errors.push(`${where}: the id ${id} is repeated`);
      id = newId(type);
    }
    ids.add(id);
    const props = { ...defaultProps(type), ...(isObject(item['props']) ? item['props'] : {}) };
    const block: Block = { id, type, props };
    if (type === 'columns' || type === 'column' || type === 'repeat' || type === 'condition') {
      block.children = Array.isArray(item['children']) ? readList(item['children'], type, 'children', `${where}.children`, ids, errors) : [];
    } else if (item['children'] !== undefined) {
      errors.push(`${where}: a ${type} cannot have children`);
    }
    if (type === 'condition') {
      block.elseChildren = Array.isArray(item['elseChildren']) ? readList(item['elseChildren'], type, 'elseChildren', `${where}.elseChildren`, ids, errors) : [];
    }
    if (type === 'columns' && (block.children?.length ?? 0) === 0) {
      errors.push(`${where}: columns need at least one column`);
    }
    out.push(block);
  });
  return out;
}

/** The text stored in `EmailTemplate.design`. */
export function serializeDesign(design: Design): string {
  return JSON.stringify(design);
}

/** True for the design a migrated template has: one HTML block that holds a whole HTML document, sent as it is. */
export function isFullDocumentDesign(design: Design): boolean {
  const only = design.blocks.length === 1 ? design.blocks[0] : undefined;
  return only !== undefined && only.type === 'html' && only.props['fullDocument'] === true;
}
