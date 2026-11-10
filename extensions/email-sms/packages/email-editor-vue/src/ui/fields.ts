import { ALIAS, COLOR, CONDITION, PADDING, PATH } from '../compiler/mjml.js';
import type { BlockType } from '../model/types.js';

/** How a setting is edited. */
export type FieldKind = 'text' | 'url' | 'number' | 'color' | 'select' | 'checkbox' | 'textarea' | 'rich' | 'code';

export interface Field {
  key: string;
  kind: FieldKind;
  /** For `select`: the values; their labels are the translations of `opt.<value>`. */
  options?: string[];
  min?: number;
  max?: number;
  step?: number;
  /** A colour that may be left empty. */
  optional?: boolean;
  /** Returns a message key when the value is not acceptable (the compiler would skip or ignore it). */
  invalid?: (value: string) => string | null;
}

const align: Field = { key: 'align', kind: 'select', options: ['left', 'center', 'right'] };
const padding: Field = { key: 'padding', kind: 'text', invalid: (v) => (PADDING.test(v.trim()) ? null : 'invalidPadding') };
const hideOn: Field = { key: 'hideOn', kind: 'select', options: ['none', 'desktop', 'mobile'] };
const color = (key: string, optional = false): Field => ({
  key,
  kind: 'color',
  optional,
  invalid: (v) => (v.trim() === '' && optional) || COLOR.test(v.trim()) ? null : 'invalidColor',
});
const number = (key: string, min: number, max: number, step = 1): Field => ({ key, kind: 'number', min, max, step });

/** The settings of every kind of block, in the order the panel shows them. */
export const FIELDS: Record<BlockType, Field[]> = {
  header: [{ key: 'logoUrl', kind: 'url' }, number('logoWidth', 10, 600), { key: 'logoAlt', kind: 'text' }, { key: 'title', kind: 'text' }, align, color('backgroundColor', true), padding, hideOn],
  text: [{ key: 'html', kind: 'rich' }, align, color('color', true), number('fontSize', 8, 72), number('lineHeight', 1, 3, 0.1), padding, hideOn],
  image: [{ key: 'src', kind: 'url' }, { key: 'alt', kind: 'text' }, { key: 'href', kind: 'url' }, number('width', 0, 1200), align, padding, hideOn],
  button: [{ key: 'text', kind: 'text' }, { key: 'href', kind: 'url' }, color('backgroundColor'), color('color'), number('borderRadius', 0, 60), number('fontSize', 8, 72), align, { key: 'fullWidth', kind: 'checkbox' }, padding, hideOn],
  divider: [color('color'), number('thickness', 1, 20), padding, hideOn],
  spacer: [number('height', 0, 400), hideOn],
  footer: [{ key: 'html', kind: 'rich' }, align, color('color', true), number('fontSize', 8, 72), padding, hideOn],
  html: [{ key: 'html', kind: 'code' }, hideOn],
  columns: [{ key: 'stackOnMobile', kind: 'checkbox' }, padding, hideOn],
  column: [number('width', 5, 100), hideOn],
  repeat: [
    { key: 'source', kind: 'text', invalid: (v) => (PATH.test(v.trim()) ? null : 'invalidPath') },
    { key: 'alias', kind: 'text', invalid: (v) => (ALIAS.test(v.trim()) ? null : 'invalidAlias') },
  ],
  condition: [{ key: 'expression', kind: 'text', invalid: (v) => (v.trim() !== '' && CONDITION.test(v.trim()) ? null : 'invalidCondition') }, hideOn],
};

/** The settings of the whole email. */
export const SETTINGS_FIELDS: Field[] = [
  number('width', 320, 900),
  color('backgroundColor'),
  color('contentBackground'),
  { key: 'fontFamily', kind: 'select', options: ['Arial, Helvetica, sans-serif', 'Georgia, serif', 'Verdana, Geneva, sans-serif', 'Tahoma, sans-serif', "'Trebuchet MS', sans-serif", "'Courier New', monospace"] },
  color('textColor'),
  color('linkColor'),
  { key: 'preheader', kind: 'text' },
];
