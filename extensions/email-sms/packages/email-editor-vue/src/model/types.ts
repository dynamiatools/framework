/**
 * The document of the email editor: a version, some global settings and a tree of blocks. It is stored as JSON in
 * `EmailTemplate.design` and compiled to HTML (see the compiler) whenever it is saved. Everything is plain data.
 */

/** Blocks that print something. */
export const LEAF_TYPES = ['header', 'text', 'image', 'button', 'divider', 'spacer', 'footer', 'html'] as const;
/** Blocks that hold other blocks. */
export const CONTAINER_TYPES = ['columns', 'column', 'repeat', 'condition'] as const;
export const BLOCK_TYPES = [...LEAF_TYPES, ...CONTAINER_TYPES] as const;

export type LeafType = (typeof LEAF_TYPES)[number];
export type BlockType = (typeof BLOCK_TYPES)[number];

/** Where in its parent a block lives: `else` only exists in a `condition`. */
export type Slot = 'children' | 'elseChildren';

/** Where to put a block: inside `parentId` (null is the root of the email), in `slot`, at `index`. */
export interface Position {
  parentId: string | null;
  slot: Slot;
  index: number;
}

/** Hides a block on one kind of screen. */
export type HideOn = 'none' | 'desktop' | 'mobile';

export interface Block {
  id: string;
  type: BlockType;
  /** The settings of the block, see `defaultProps`; every block also accepts `hideOn`. */
  props: Record<string, unknown>;
  /** Blocks inside (columns hold `column` blocks; a condition holds the blocks of its `then` side). */
  children?: Block[];
  /** The `else` side of a `condition`. */
  elseChildren?: Block[];
}

export interface DesignSettings {
  /** Width of the email on desktop, in pixels. */
  width: number;
  /** Colour around the email. */
  backgroundColor: string;
  /** Colour of the email itself. */
  contentBackground: string;
  fontFamily: string;
  textColor: string;
  linkColor: string;
  /** Hidden text some clients show next to the subject. May contain variables. */
  preheader: string;
}

export interface Design {
  version: 1;
  settings: DesignSettings;
  blocks: Block[];
}

/** Thrown when a document is not a valid design. */
export class DesignError extends Error {
  constructor(readonly errors: string[]) {
    super(errors.join('; '));
    this.name = 'DesignError';
  }
}
