import type { Block, BlockType, Design, DesignSettings } from './types.js';

export const DEFAULT_SETTINGS: DesignSettings = {
  width: 600,
  backgroundColor: '#f4f4f4',
  contentBackground: '#ffffff',
  fontFamily: 'Arial, Helvetica, sans-serif',
  textColor: '#333333',
  linkColor: '#1a73e8',
  preheader: '',
};

/** The settings every new block of a type starts with. */
export function defaultProps(type: BlockType): Record<string, unknown> {
  const common = { hideOn: 'none' };
  switch (type) {
    case 'header':
      return { ...common, logoUrl: '', logoWidth: 120, logoAlt: '', title: '', align: 'center', backgroundColor: '', padding: '20px 24px' };
    case 'text':
      return { ...common, html: '<p>Write your text here</p>', align: 'left', color: '', fontSize: 16, lineHeight: 1.5, padding: '10px 24px' };
    case 'image':
      return { ...common, src: '', alt: '', href: '', width: 0, align: 'center', padding: '10px 24px' };
    case 'button':
      return { ...common, text: 'Click here', href: '#', backgroundColor: '#1a73e8', color: '#ffffff', borderRadius: 4, fontSize: 16, align: 'center', fullWidth: false, padding: '10px 24px' };
    case 'divider':
      return { ...common, color: '#dddddd', thickness: 1, padding: '10px 24px' };
    case 'spacer':
      return { ...common, height: 24 };
    case 'footer':
      return { ...common, html: '<p>Company name · Address</p>', align: 'center', color: '#888888', fontSize: 12, padding: '20px 24px' };
    case 'html':
      return { ...common, html: '', fullDocument: false };
    case 'columns':
      return { ...common, stackOnMobile: true, padding: '0px' };
    case 'column':
      return { ...common, width: 50 };
    case 'repeat':
      return { ...common, source: 'items', alias: 'item' };
    case 'condition':
      return { ...common, expression: 'condition' };
  }
}

let counter = 0;

/** A short id that is unique inside a design and across designs. */
export function newId(type: string): string {
  counter += 1;
  const random =
    typeof crypto !== 'undefined' && 'randomUUID' in crypto
      ? crypto.randomUUID().slice(0, 8)
      : Math.random().toString(36).slice(2, 10);
  return `${type}-${random}${counter.toString(36)}`;
}

/** A new block with its default settings; `columns` come with `columnCount` empty columns. */
export function createBlock(type: BlockType, props: Record<string, unknown> = {}, columnCount = 2): Block {
  const block: Block = { id: newId(type), type, props: { ...defaultProps(type), ...props } };
  if (type === 'columns') {
    const width = Math.floor(100 / columnCount);
    block.children = Array.from({ length: columnCount }, () => createBlock('column', { width }));
  } else if (type === 'column' || type === 'repeat') {
    block.children = [];
  } else if (type === 'condition') {
    block.children = [];
    block.elseChildren = [];
  }
  return block;
}

export function emptyDesign(): Design {
  return { version: 1, settings: { ...DEFAULT_SETTINGS }, blocks: [] };
}
