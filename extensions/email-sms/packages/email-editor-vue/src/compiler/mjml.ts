import { isFullDocumentDesign } from '../model/validate.js';
import type { Block, Design } from '../model/types.js';

/**
 * Turns a design into MJML. Variables (`${x}`, `{{x}}|filter`) pass through untouched inside text and attributes, and
 * `repeat` and `condition` become `{{#each}}` and `{{#if}}` tags around the blocks, written with `mj-raw` so MJML does
 * not touch them. Everything that ends up in an attribute or a tag is validated, so a design cannot inject markup or
 * a `javascript:` link through its settings.
 */

export interface MjmlResult {
  mjml: string;
  warnings: string[];
}

export const COLOR = /^(#[0-9a-fA-F]{3,8}|[a-zA-Z]{3,20}|rgba?\([\d\s.,%]+\)|transparent)$/;
export const PADDING = /^\d{1,3}(px|%)?(\s+\d{1,3}(px|%)?){0,3}$/;
export const PATH = /^[A-Za-z_][\w.]*$/;
export const ALIAS = /^[A-Za-z_]\w*$/;
export const CONDITION = /^[\w.'"!=<>&| \-]+$/;
const ALIGN = ['left', 'center', 'right'];

/** Escapes a value for a double quoted attribute. */
export function attr(value: unknown): string {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

/** Escapes a plain text setting (a title, a button label, the preheader) so `<` or `&` cannot break the MJML; `{{ }}` and `${ }` are untouched. */
export function plain(value: unknown): string {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

/** Keeps http(s), mailto, tel, anchors and relative links, and links made of variables; drops javascript: and data:. */
export function safeUrl(url: unknown): string {
  const value = String(url ?? '').trim();
  if (/^(javascript|vbscript|data|file):/i.test(value.replace(/[\u0000- ]/g, ''))) {
    return '';
  }
  return value;
}

function color(value: unknown, fallback = ''): string {
  const v = String(value ?? '').trim();
  return COLOR.test(v) ? v : fallback;
}

function padding(value: unknown, fallback: string): string {
  const v = String(value ?? '').trim();
  if (!PADDING.test(v)) {
    return fallback;
  }
  return v
    .split(/\s+/)
    .map((token) => (/^\d+$/.test(token) ? `${token}px` : token))
    .join(' ');
}

function align(value: unknown): string {
  return ALIGN.includes(String(value)) ? String(value) : 'left';
}

function num(value: unknown, fallback: number, min = 0, max = 2000): number {
  const n = typeof value === 'number' ? value : Number(value);
  return Number.isFinite(n) ? Math.min(max, Math.max(min, n)) : fallback;
}

function cssClass(block: Block): string {
  const hide = block.props['hideOn'];
  return hide === 'mobile' ? ' css-class="hide-mobile"' : hide === 'desktop' ? ' css-class="hide-desktop"' : '';
}

const LEAVES = ['header', 'text', 'image', 'button', 'divider', 'spacer', 'footer', 'html'];

/** The body of an HTML document (what is between `<body>` and `</body>`), or the text as it is. */
export function bodyOf(html: string): string {
  const m = /<body[^>]*>([\s\S]*)<\/body>/i.exec(html);
  return m ? (m[1] ?? '') : html;
}

class Emitter {
  readonly warnings: string[] = [];

  constructor(private readonly design: Design) {}

  // ----------------------------------------------------------- leaves

  private leaf(block: Block): string {
    const p = block.props;
    const hide = cssClass(block);
    switch (block.type) {
      case 'header': {
        const bg = color(p['backgroundColor']);
        const container = bg ? ` container-background-color="${bg}"` : '';
        const pad = padding(p['padding'], '20px 24px');
        const logo = safeUrl(p['logoUrl']);
        const title = plain(p['title']);
        return (
          (logo
            ? `<mj-image src="${attr(logo)}" alt="${attr(p['logoAlt'])}" width="${num(p['logoWidth'], 120, 10, 600)}px" align="${align(p['align'])}" padding="${pad}"${container}${hide} />`
            : '') +
          (title
            ? `<mj-text align="${align(p['align'])}" font-size="24px" font-weight="bold" padding="${pad}"${container}${hide}>${title}</mj-text>`
            : '')
        );
      }
      case 'text':
        return `<mj-text align="${align(p['align'])}"${colorAttr('color', p['color'])} font-size="${num(p['fontSize'], 16, 8, 72)}px" line-height="${num(p['lineHeight'], 1.5, 1, 3)}" padding="${padding(p['padding'], '10px 24px')}"${hide}>${String(p['html'] ?? '')}</mj-text>`;
      case 'footer':
        return `<mj-text align="${align(p['align'])}"${colorAttr('color', p['color'])} font-size="${num(p['fontSize'], 12, 8, 72)}px" padding="${padding(p['padding'], '20px 24px')}"${hide}>${String(p['html'] ?? '')}</mj-text>`;
      case 'image': {
        const src = safeUrl(p['src']);
        if (!src) {
          this.warnings.push(`The image ${block.id} has no source and was skipped`);
          return '';
        }
        const href = safeUrl(p['href']);
        const width = num(p['width'], 0, 0, 1200);
        return `<mj-image src="${attr(src)}" alt="${attr(p['alt'])}"${href ? ` href="${attr(href)}"` : ''}${width > 0 ? ` width="${width}px"` : ''} align="${align(p['align'])}" padding="${padding(p['padding'], '10px 24px')}"${hide} />`;
      }
      case 'button':
        return `<mj-button href="${attr(safeUrl(p['href']) || '#')}" background-color="${color(p['backgroundColor'], '#1a73e8')}" color="${color(p['color'], '#ffffff')}" border-radius="${num(p['borderRadius'], 4, 0, 60)}px" font-size="${num(p['fontSize'], 16, 8, 72)}px" align="${align(p['align'])}" padding="${padding(p['padding'], '10px 24px')}"${p['fullWidth'] === true ? ' width="100%"' : ''}${hide}>${plain(p['text'])}</mj-button>`;
      case 'divider':
        return `<mj-divider border-color="${color(p['color'], '#dddddd')}" border-width="${num(p['thickness'], 1, 1, 20)}px" padding="${padding(p['padding'], '10px 24px')}"${hide} />`;
      case 'spacer':
        return `<mj-spacer height="${num(p['height'], 24, 0, 400)}px"${hide} />`;
      case 'html': {
        const html = String(p['html'] ?? '');
        if (p['fullDocument'] === true) {
          this.warnings.push(`The HTML block ${block.id} is a whole document; only its body was kept (styles in its head are lost)`);
        }
        return `<mj-raw>${p['fullDocument'] === true ? bodyOf(html) : html}</mj-raw>`;
      }
      default:
        return '';
    }
  }

  // ------------------------------------------------- control blocks

  private open(block: Block): { open: string; close: string; elseTag: string } | null {
    if (block.type === 'repeat') {
      const source = String(block.props['source'] ?? '').trim();
      const alias = String(block.props['alias'] ?? '').trim();
      if (!PATH.test(source) || !ALIAS.test(alias)) {
        this.warnings.push(`The repeat ${block.id} has an invalid source or name and was skipped`);
        return null;
      }
      return { open: `<mj-raw>{{#each ${source} as ${alias}}}</mj-raw>`, close: '<mj-raw>{{/each}}</mj-raw>', elseTag: '' };
    }
    const expression = String(block.props['expression'] ?? '').trim();
    if (expression === '' || !CONDITION.test(expression)) {
      this.warnings.push(`The condition ${block.id} has an invalid expression and was skipped`);
      return null;
    }
    return { open: `<mj-raw>{{#if ${expression}}}</mj-raw>`, close: '<mj-raw>{{/if}}</mj-raw>', elseTag: '<mj-raw>{{else}}</mj-raw>' };
  }

  /** Blocks inside a column. */
  columnLevel(blocks: Block[]): string {
    return blocks
      .map((block) => {
        if (LEAVES.includes(block.type)) {
          return this.leaf(block);
        }
        if (block.type === 'repeat' || block.type === 'condition') {
          const tags = this.open(block);
          if (!tags) {
            return '';
          }
          return (
            tags.open +
            this.columnLevel(block.children ?? []) +
            (block.type === 'condition' && (block.elseChildren?.length ?? 0) > 0 ? tags.elseTag + this.columnLevel(block.elseChildren ?? []) : '') +
            tags.close
          );
        }
        return '';
      })
      .join('');
  }

  /** Blocks directly in the email, in a repeat or in a condition: each needs to be a section. */
  sectionLevel(blocks: Block[]): string {
    let out = '';
    let run: Block[] = [];
    const bg = color(this.design.settings.contentBackground, '#ffffff');
    const flush = (): void => {
      if (run.length > 0) {
        out += `<mj-section background-color="${bg}" padding="0"><mj-column>${this.columnLevel(run)}</mj-column></mj-section>`;
        run = [];
      }
    };
    for (const block of blocks) {
      if (block.type === 'columns') {
        flush();
        out += this.columns(block, bg);
      } else if (block.type === 'repeat' || block.type === 'condition') {
        flush();
        const tags = this.open(block);
        if (tags) {
          out +=
            tags.open +
            this.sectionLevel(block.children ?? []) +
            (block.type === 'condition' && (block.elseChildren?.length ?? 0) > 0 ? tags.elseTag + this.sectionLevel(block.elseChildren ?? []) : '') +
            tags.close;
        }
      } else if (LEAVES.includes(block.type)) {
        run.push(block);
      }
    }
    flush();
    return out;
  }

  private columns(block: Block, bg: string): string {
    const columns = (block.children ?? []).map((column) => {
      const width = num(column.props['width'], 50, 5, 100);
      return `<mj-column width="${width}%"${cssClass(column)}>${this.columnLevel(column.children ?? [])}</mj-column>`;
    });
    const inner = block.props['stackOnMobile'] === false ? `<mj-group>${columns.join('')}</mj-group>` : columns.join('');
    return `<mj-section background-color="${bg}" padding="${padding(block.props['padding'], '0')}"${cssClass(block)}>${inner}</mj-section>`;
  }

  head(): string {
    const s = this.design.settings;
    const family = String(s.fontFamily).replace(/[^\w ,'"-]/g, '');
    const preheader = plain(s.preheader).trim();
    return (
      '<mj-head>' +
      `<mj-attributes><mj-all font-family="${attr(family)}" /><mj-text color="${color(s.textColor, '#333333')}" /></mj-attributes>` +
      `<mj-style>a { color: ${color(s.linkColor, '#1a73e8')}; } @media only screen and (max-width: 480px) { .hide-mobile { display: none !important; max-height: 0 !important; overflow: hidden !important; } } @media only screen and (min-width: 481px) { .hide-desktop { display: none !important; } }</mj-style>` +
      (preheader ? `<mj-preview>${preheader}</mj-preview>` : '') +
      '</mj-head>'
    );
  }
}

function colorAttr(name: string, value: unknown): string {
  const c = color(value);
  return c ? ` ${name}="${c}"` : '';
}

/**
 * @returns the MJML of the design plus warnings about what was skipped. A design that is a single whole HTML document
 * has no MJML: use {@link isFullDocumentDesign} first.
 */
export function toMjml(design: Design): MjmlResult {
  const emitter = new Emitter(design);
  const body = emitter.sectionLevel(design.blocks);
  const width = num(design.settings.width, 600, 320, 900);
  const mjml =
    '<mjml>' + emitter.head() + `<mj-body width="${width}px" background-color="${color(design.settings.backgroundColor, '#f4f4f4')}">${body}</mj-body></mjml>`;
  return { mjml, warnings: emitter.warnings };
}

export { isFullDocumentDesign };
