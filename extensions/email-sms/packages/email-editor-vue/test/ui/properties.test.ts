// @vitest-environment happy-dom
import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import { createBlock, emptyDesign, EmailEditor, find, insert, type Design } from '../../src/index.js';

const at = (index: number) => ({ parentId: null, slot: 'children', index }) as const;
const design = (...blocks: ReturnType<typeof createBlock>[]): Design => blocks.reduce((d, b, i) => insert(d, at(i), b), emptyDesign());

async function select(wrapper: ReturnType<typeof mount>, type: string) {
  await wrapper.get(`[data-type="${type}"]`).trigger('click');
}

function current(wrapper: ReturnType<typeof mount>): Design {
  return (wrapper.vm as unknown as { getDesign(): Design }).getDesign();
}

async function type(wrapper: ReturnType<typeof mount>, selector: string, value: string) {
  const input = wrapper.get(selector);
  (input.element as HTMLInputElement).value = value;
  await input.trigger('input');
}

describe('properties panel', () => {
  it('shows the settings of the email when nothing is selected and changes them', async () => {
    const wrapper = mount(EmailEditor);
    expect(wrapper.text()).toContain('Email settings');
    await type(wrapper, '[data-setting="width"]', '700');
    await type(wrapper, '[data-setting="backgroundColor"]', '#101010');
    await type(wrapper, '[data-setting="preheader"]', 'Hi {{name}}');
    expect(current(wrapper).settings.width).toBe(700);
    expect(current(wrapper).settings.backgroundColor).toBe('#101010');
    expect(current(wrapper).settings.preheader).toBe('Hi {{name}}');
    expect(wrapper.get('[data-canvas]').attributes('style')).toContain('max-width: 700px');
  });

  it('clamps numbers to their range and ignores empty or invalid ones', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('text')) } });
    await select(wrapper, 'text');
    await type(wrapper, '[data-field="fontSize"]', '500');
    expect(find(current(wrapper), current(wrapper).blocks[0]!.id)!.block.props['fontSize']).toBe(72);
    await type(wrapper, '[data-field="fontSize"]', '');
    expect(current(wrapper).blocks[0]!.props['fontSize']).toBe(72);
  });

  it('edits a block: select, checkbox and text fields', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('button')) } });
    await select(wrapper, 'button');
    await type(wrapper, '[data-field="text"]', 'Pay {{amount}}');
    await type(wrapper, '[data-field="href"]', '{{ payUrl }}');
    const align = wrapper.get('[data-field="align"]');
    (align.element as HTMLSelectElement).value = 'right';
    await align.trigger('change');
    const full = wrapper.get('[data-field="fullWidth"]');
    (full.element as HTMLInputElement).checked = true;
    await full.trigger('change');
    const props = current(wrapper).blocks[0]!.props;
    expect(props['text']).toBe('Pay {{amount}}');
    expect(props['href']).toBe('{{ payUrl }}');
    expect(props['align']).toBe('right');
    expect(props['fullWidth']).toBe(true);
    expect(wrapper.get('.v-button').text()).toBe('Pay {{amount}}');
  });

  it('hides a block per device', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('spacer')) } });
    await select(wrapper, 'spacer');
    const hide = wrapper.get('[data-field="hideOn"]');
    (hide.element as HTMLSelectElement).value = 'mobile';
    await hide.trigger('change');
    expect(current(wrapper).blocks[0]!.props['hideOn']).toBe('mobile');
    expect(wrapper.text()).toContain('Hidden on mobile');
  });

  it('warns about values the compiler would skip', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('repeat'), createBlock('condition'), createBlock('divider')) } });
    await select(wrapper, 'repeat');
    expect(wrapper.find('[data-error]').exists()).toBe(false);
    await type(wrapper, '[data-field="source"]', 'a}} <b>');
    expect(wrapper.get('[data-error="source"]').text()).toContain('Use a name');
    await type(wrapper, '[data-field="alias"]', 'a b');
    expect(wrapper.get('[data-error="alias"]').text()).toContain('single word');
    await select(wrapper, 'condition');
    await type(wrapper, '[data-field="expression"]', 'a}}{{b');
    expect(wrapper.find('[data-error="expression"]').exists()).toBe(true);
    await select(wrapper, 'divider');
    await type(wrapper, '[data-field="color"]', 'red;}');
    expect(wrapper.get('[data-error="color"]').text()).toContain('colour');
  });

  it('typing in one field is one undo step', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('header')) } });
    await select(wrapper, 'header');
    for (const v of ['H', 'He', 'Hel', 'Hell', 'Hello']) {
      await type(wrapper, '[data-field="title"]', v);
    }
    expect(current(wrapper).blocks[0]!.props['title']).toBe('Hello');
    await wrapper.get('[data-action="undo"]').trigger('click');
    expect(current(wrapper).blocks[0]!.props['title']).toBe('');
  });

  it('manages the columns of a columns block', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('columns')) } });
    await select(wrapper, 'columns');
    await wrapper.get('[data-action="add-column"]').trigger('click');
    expect(current(wrapper).blocks[0]!.children!.map((c) => c.props['width'])).toEqual([33, 33, 33]);
    await wrapper.get('[data-action="add-column"]').trigger('click');
    expect(wrapper.get('[data-action="add-column"]').attributes('disabled')).toBeDefined();
    const first = current(wrapper).blocks[0]!.children![0]!.id;
    await type(wrapper, `#w-${first}`, '40');
    expect(current(wrapper).blocks[0]!.children![0]!.props['width']).toBe(40);
    await wrapper.findAll('[data-action="remove-column"]')[3]!.trigger('click');
    expect(current(wrapper).blocks[0]!.children).toHaveLength(3);
    const stack = wrapper.get('[data-field="stackOnMobile"]');
    (stack.element as HTMLInputElement).checked = false;
    await stack.trigger('change');
    expect(current(wrapper).blocks[0]!.props['stackOnMobile']).toBe(false);
  });

  it('a selected column can be removed and sets its width', async () => {
    const d = design(createBlock('columns'));
    const wrapper = mount(EmailEditor, { props: { design: d } });
    await wrapper.get('[data-type="column"]').trigger('click');
    await type(wrapper, '[data-field="width"]', '70');
    expect(current(wrapper).blocks[0]!.children![0]!.props['width']).toBe(70);
    await wrapper.get('[data-action="remove-this-column"]').trigger('click');
    expect(current(wrapper).blocks[0]!.children).toHaveLength(1);
  });

  it('explains a whole document block', async () => {
    const d = design(createBlock('html', { html: '<html></html>', fullDocument: true }));
    const wrapper = mount(EmailEditor, { props: { design: d } });
    await select(wrapper, 'html');
    expect(wrapper.text()).toContain('whole HTML document');
  });
});
