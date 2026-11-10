import { DynamiaTemplateEngine } from '@dynamia-tools/template-engine';
import { describe, expect, it } from 'vitest';
import { attr, bodyOf, compile, createBlock, emptyDesign, insert, parseDesign, safeUrl, toMjml, type Block, type Design } from '../src/index.js';

const root = (index: number) => ({ parentId: null, slot: 'children', index }) as const;

function design(...blocks: Block[]): Design {
  return blocks.reduce((d, b, i) => insert(d, root(i), b), emptyDesign());
}

describe('toMjml', () => {
  it('wraps loose blocks in one section and column, and keeps their order', () => {
    const { mjml } = toMjml(design(createBlock('header', { title: 'Hola' }), createBlock('text', { html: '<p>Hi ${name}</p>' }), createBlock('button', { text: 'Pay', href: 'https://x.com/pay' })));
    expect(mjml.match(/<mj-section/g)).toHaveLength(1);
    expect(mjml).toContain('<mj-text align="center" font-size="24px"');
    expect(mjml).toContain('<p>Hi ${name}</p>');
    expect(mjml.indexOf('Hola')).toBeLessThan(mjml.indexOf('Hi ${name}'));
    expect(mjml.indexOf('Hi ${name}')).toBeLessThan(mjml.indexOf('Pay'));
    expect(mjml).toContain('<mj-body width="600px" background-color="#f4f4f4">');
  });

  it('draws columns with their widths and can keep them together on mobile', () => {
    const columns = createBlock('columns', {}, 3);
    const stacked = toMjml(design(columns)).mjml;
    expect(stacked).toContain('<mj-column width="33%">');
    expect(stacked).not.toContain('<mj-group>');
    const together = toMjml(design(createBlock('columns', { stackOnMobile: false }))).mjml;
    expect(together).toContain('<mj-group>');
  });

  it('hides blocks per device with a css class', () => {
    const { mjml } = toMjml(design(createBlock('text', { hideOn: 'mobile' }), createBlock('spacer', { hideOn: 'desktop' })));
    expect(mjml).toContain('css-class="hide-mobile"');
    expect(mjml).toContain('css-class="hide-desktop"');
    expect(mjml).toContain('.hide-mobile');
  });

  it('writes repeat and condition as engine tags around sections', () => {
    const repeat = createBlock('repeat', { source: 'order.lines', alias: 'line' });
    repeat.children = [createBlock('text', { html: '<p>${line.name}</p>' })];
    const condition = createBlock('condition', { expression: "vip && total > 100" });
    condition.children = [createBlock('text', { html: 'VIP' })];
    condition.elseChildren = [createBlock('text', { html: 'Regular' })];
    const { mjml, warnings } = toMjml(design(repeat, condition));
    expect(warnings).toEqual([]);
    expect(mjml).toContain('<mj-raw>{{#each order.lines as line}}</mj-raw><mj-section');
    expect(mjml).toContain('</mj-section><mj-raw>{{/each}}</mj-raw>');
    expect(mjml).toContain('<mj-raw>{{#if vip && total > 100}}</mj-raw>');
    expect(mjml).toContain('<mj-raw>{{else}}</mj-raw>');
    expect(mjml).toContain('<mj-raw>{{/if}}</mj-raw>');
  });

  it('allows repeat and condition inside a column', () => {
    const columns = createBlock('columns');
    const repeat = createBlock('repeat');
    repeat.children = [createBlock('text')];
    columns.children![0]!.children = [repeat];
    const { mjml } = toMjml(design(columns));
    expect(mjml).toMatch(/<mj-column width="50%"><mj-raw>\{\{#each items as item\}\}<\/mj-raw><mj-text/);
  });

  it('skips repeats and conditions that could inject tags', () => {
    for (const props of [{ source: 'a}} <script>', alias: 'x' }, { source: 'items', alias: 'a b' }, { source: '', alias: 'x' }]) {
      const repeat = createBlock('repeat', props);
      repeat.children = [createBlock('text', { html: 'INSIDE' })];
      const out = toMjml(design(repeat));
      expect(out.mjml).not.toContain('INSIDE');
      expect(out.mjml).not.toContain('<script>');
      expect(out.warnings).toHaveLength(1);
    }
    const condition = createBlock('condition', { expression: 'a}}<img onerror=x>{{#if b' });
    condition.children = [createBlock('text', { html: 'INSIDE' })];
    const out = toMjml(design(condition));
    expect(out.mjml).not.toContain('INSIDE');
    expect(out.warnings).toHaveLength(1);
  });

  it('validates attributes: colours, paddings, alignment, numbers and links', () => {
    const { mjml } = toMjml(
      design(
        createBlock('button', { href: 'javascript:alert(1)', backgroundColor: 'red;} body{display:none', color: '#fff', padding: '1px"><script>', align: 'evil', borderRadius: 9999, fontSize: 'x' }),
        createBlock('image', { src: 'data:image/png;base64,AAAA', alt: 'a"b' }),
        createBlock('image', { src: 'https://x.com/a.png?x=1&y="2"', alt: 'a"b<', href: ' JaVaScRiPt:alert(1)' }),
      ),
    );
    expect(mjml).toContain('href="#"');
    expect(mjml).toContain('background-color="#1a73e8"');
    expect(mjml).toContain('padding="10px 24px"');
    expect(mjml).toContain('align="left"');
    expect(mjml).toContain('border-radius="60px"');
    expect(mjml).toContain('font-size="16px"');
    expect(mjml).not.toContain('script');
    expect(mjml).not.toContain('javascript');
    expect(mjml).not.toContain('data:image');
    expect(mjml).toContain('src="https://x.com/a.png?x=1&amp;y=&quot;2&quot;"');
    expect(mjml).toContain('alt="a&quot;b&lt;"');
  });

  it('skips an image without a source and says so', () => {
    const out = toMjml(design(createBlock('image')));
    expect(out.mjml).not.toContain('<mj-image');
    expect(out.warnings[0]).toMatch(/no source/);
  });

  it('keeps variables with pipes and filters in text and attributes', () => {
    const { mjml } = toMjml(design(createBlock('text', { html: '<p>{{ total | currency:0 }} ${user.name|upper}</p>' }), createBlock('button', { href: '{{ order.url }}', text: 'Pay {{n}}' })));
    expect(mjml).toContain('{{ total | currency:0 }} ${user.name|upper}');
    expect(mjml).toContain('href="{{ order.url }}"');
  });

  it('puts the settings in the head', () => {
    const d = emptyDesign();
    d.settings = { ...d.settings, width: 700, backgroundColor: '#000000', fontFamily: 'Georgia, serif', preheader: 'Your {{x}}', linkColor: '#ff0000' };
    const { mjml } = toMjml(d);
    expect(mjml).toContain('width="700px" background-color="#000000"');
    expect(mjml).toContain('font-family="Georgia, serif"');
    expect(mjml).toContain('<mj-preview>Your {{x}}</mj-preview>');
    expect(mjml).toContain('a { color: #ff0000; }');
  });

  it('a whole document block inside a bigger design keeps only its body', () => {
    const html = createBlock('html', { html: '<html><head><style>x{}</style></head><body><p>BODY</p></body></html>', fullDocument: true });
    const out = toMjml(design(createBlock('text'), html));
    expect(out.mjml).toContain('<mj-raw><p>BODY</p></mj-raw>');
    expect(out.mjml).not.toContain('<style>x');
    expect(out.warnings[0]).toMatch(/whole document/);
  });
});

describe('helpers', () => {
  it('escape attributes, filter urls and read bodies', () => {
    expect(attr('a"&<>')).toBe('a&quot;&amp;&lt;&gt;');
    expect(safeUrl('https://x.com')).toBe('https://x.com');
    expect(safeUrl('mailto:a@x.com')).toBe('mailto:a@x.com');
    expect(safeUrl('{{ url }}')).toBe('{{ url }}');
    expect(safeUrl('java\tscript:alert(1)')).toBe('');
    expect(safeUrl(' DATA:text/html,x')).toBe('');
    expect(bodyOf('<html><body class="a"><p>x</p></body></html>')).toBe('<p>x</p>');
    expect(bodyOf('<p>x</p>')).toBe('<p>x</p>');
  });
});

describe('compile with MJML', () => {
  const engine = new DynamiaTemplateEngine();

  async function build(): Promise<Design> {
    const columns = createBlock('columns');
    columns.children![0]!.children = [createBlock('image', { src: 'https://x.com/logo.png', alt: 'Logo' })];
    columns.children![1]!.children = [createBlock('text', { html: '<p>Hi ${user.name|upper}</p>' })];
    const repeat = createBlock('repeat', { source: 'lines', alias: 'l' });
    repeat.children = [createBlock('text', { html: '<p>Row {{l.name}} {{l.price|currency:0}}</p>' })];
    const condition = createBlock('condition', { expression: 'paid' });
    condition.children = [createBlock('text', { html: 'PAID' })];
    condition.elseChildren = [createBlock('button', { text: 'Pay now', href: '{{payUrl}}' })];
    return design(createBlock('header', { title: 'Invoice' }), columns, repeat, condition, createBlock('footer'));
  }

  it('gives responsive table HTML that keeps the variables and the blocks', async () => {
    const result = await compile(await build());
    expect(result.errors).toEqual([]);
    expect(result.warnings).toEqual([]);
    expect(result.html).toContain('@media');
    expect(result.html).toContain('mso');
    expect(result.html).toContain('{{#each lines as l}}');
    expect(result.html).toContain('{{#if paid}}');
    expect(result.html).toContain('Hi ${user.name|upper}');
    expect(result.html).toContain('hide-mobile');
    expect(result.html.length).toBeGreaterThan(3000);
  }, 20_000);

  it('renders through the Dynamia engine like the server does', async () => {
    const { html } = await compile(await build());
    const data = { user: { name: 'ada' }, lines: [{ name: 'A', price: 1000 }, { name: 'B', price: 2500 }, { name: 'C', price: 30 }], paid: false, payUrl: 'https://pay.me/1' };
    const out = engine.evaluate(html, { ...data, locale: 'en-US' });
    expect(out).toContain('Hi ADA');
    expect(out.match(/Row /g)).toHaveLength(3);
    expect(out).toContain('Row B 2,500');
    expect(out).not.toContain('PAID');
    expect(out).toContain('href="https://pay.me/1"');
    expect(out).not.toContain('{{');
    expect(out).toContain('mj-column-per-50');

    const paid = engine.evaluate(html, { ...data, paid: true, lines: [] });
    expect(paid).toContain('PAID');
    expect(paid).not.toContain('Pay now');
    expect(paid).not.toContain('Row ');
  }, 20_000);

  it('escapes data when the template escapes values, and raw opts out', async () => {
    const d = design(createBlock('text', { html: '<p>${v}</p><p>${v|raw}</p>' }));
    const { html } = await compile(d);
    const escaping = new DynamiaTemplateEngine({ escapeHtml: true });
    const out = escaping.evaluate(html, { v: '<b>x</b>' });
    expect(out).toContain('<p>&lt;b&gt;x&lt;/b&gt;</p><p><b>x</b></p>');
  }, 20_000);

  it('returns a whole document design as it is, without MJML', async () => {
    const source = '<html><body><p>${x}</p></body></html>';
    const result = await compile(parseDesign({ version: 1, blocks: [{ id: 'html-1', type: 'html', props: { html: source, fullDocument: true } }] }));
    expect(result).toEqual({ html: source, mjml: '', errors: [], warnings: [] });
  });

  it('accepts the design as JSON text', async () => {
    const result = await compile(JSON.stringify(emptyDesign()));
    expect(result.html).toContain('<body');
  }, 20_000);
});
