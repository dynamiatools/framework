// @vitest-environment happy-dom
import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import { nextTick } from 'vue';
import { createBlock, DRAG_MIME, emptyDesign, EmailEditor, insert, type Design } from '../../src/index.js';

const at = (index: number) => ({ parentId: null, slot: 'children', index }) as const;

function withBlocks(...types: Parameters<typeof createBlock>[0][]): Design {
  return types.reduce((d, type, i) => insert(d, at(i), createBlock(type)), emptyDesign());
}

const types = (wrapper: ReturnType<typeof mount>) => wrapper.findAll('[data-canvas] > .list > [data-block]').map((b) => b.attributes('data-type'));

describe('EmailEditor', () => {
  it('shows a hint when the email is empty and adds blocks from the palette', async () => {
    const wrapper = mount(EmailEditor);
    expect(wrapper.text()).toContain('Drag a block here');
    await wrapper.get('[data-palette="text"]').trigger('click');
    await wrapper.get('[data-palette="button"]').trigger('click');
    expect(types(wrapper)).toEqual(['text', 'button']);
    expect(wrapper.text()).not.toContain('Drag a block here');
  });

  it('speaks Spanish when asked', () => {
    const wrapper = mount(EmailEditor, { props: { locale: 'es-CO' } });
    expect(wrapper.text()).toContain('Arrastra un bloque');
    expect(wrapper.get('[data-palette="button"]').text()).toContain('Botón');
  });

  it('selects a block on click and shows its toolbar; delete, duplicate and move work', async () => {
    const wrapper = mount(EmailEditor, { props: { design: withBlocks('header', 'text', 'footer') } });
    const text = wrapper.get('[data-type="text"]');
    await text.trigger('click');
    expect(text.classes()).toContain('selected');
    await text.get('[data-action="duplicate"]').trigger('click');
    expect(types(wrapper)).toEqual(['header', 'text', 'text', 'footer']);
    await wrapper.get('.selected [data-action="up"]').trigger('click');
    expect(types(wrapper)).toEqual(['header', 'text', 'text', 'footer']);
    await wrapper.get('.selected [data-action="down"]').trigger('click');
    await wrapper.get('.selected [data-action="delete"]').trigger('click');
    expect(types(wrapper)).toEqual(['header', 'text', 'footer']);
  });

  it('clicking the canvas background clears the selection', async () => {
    const wrapper = mount(EmailEditor, { props: { design: withBlocks('text') } });
    await wrapper.get('[data-type="text"]').trigger('click');
    expect(wrapper.find('.selected').exists()).toBe(true);
    await wrapper.get('.editor').trigger('click');
    expect(wrapper.find('.selected').exists()).toBe(false);
  });

  it('supports the keyboard: Delete removes the focused block, Alt+arrows move it', async () => {
    const wrapper = mount(EmailEditor, { props: { design: withBlocks('header', 'text') } });
    const header = wrapper.get('[data-type="header"]');
    await header.trigger('keydown', { key: 'ArrowDown', altKey: true });
    expect(types(wrapper)).toEqual(['text', 'header']);
    await wrapper.get('[data-type="header"]').trigger('keydown', { key: 'Delete' });
    expect(types(wrapper)).toEqual(['text']);
  });

  it('undo and redo buttons and shortcuts', async () => {
    const wrapper = mount(EmailEditor);
    expect(wrapper.get('[data-action="undo"]').attributes('disabled')).toBeDefined();
    await wrapper.get('[data-palette="text"]').trigger('click');
    await wrapper.get('[data-action="undo"]').trigger('click');
    expect(types(wrapper)).toEqual([]);
    await wrapper.get('[data-action="redo"]').trigger('click');
    expect(types(wrapper)).toEqual(['text']);
    await wrapper.get('.editor').trigger('keydown', { key: 'z', ctrlKey: true });
    expect(types(wrapper)).toEqual([]);
    await wrapper.get('.editor').trigger('keydown', { key: 'y', ctrlKey: true });
    expect(types(wrapper)).toEqual(['text']);
  });

  it('drops a palette block on a zone and a block on another position', async () => {
    const wrapper = mount(EmailEditor, { props: { design: withBlocks('header', 'footer') } });
    await wrapper.get('[data-palette="divider"]').trigger('dragstart', { dataTransfer: { setData() {}, effectAllowed: '' } });
    const zones = wrapper.findAll('[data-zone]');
    expect(zones.length).toBe(3);
    const payload = JSON.stringify({ kind: 'new', type: 'divider' });
    await zones[1]!.trigger('drop', { dataTransfer: { getData: (mime: string) => (mime === DRAG_MIME ? payload : '') } });
    expect(types(wrapper)).toEqual(['header', 'divider', 'footer']);
    expect(wrapper.findAll('[data-zone]')).toHaveLength(0);

    const move = JSON.stringify({ kind: 'move', id: wrapper.get('[data-type="header"]').attributes('data-block') });
    await wrapper.get('[data-type="footer"]').trigger('dragstart', { dataTransfer: { setData() {}, effectAllowed: '' } });
    await wrapper.findAll('[data-zone]').at(-1)!.trigger('drop', { dataTransfer: { getData: () => move } });
    expect(types(wrapper)).toEqual(['divider', 'footer', 'header']);
  });

  it('only offers zones where the dragged block can go', async () => {
    const wrapper = mount(EmailEditor, { props: { design: withBlocks('columns') } });
    await wrapper.get('[data-palette="columns"]').trigger('dragstart', { dataTransfer: { setData() {}, effectAllowed: '' } });
    // columns can go at the email level (2 zones) but never inside a column
    expect(wrapper.findAll('[data-zone]').filter((z) => z.classes().includes('active'))).toHaveLength(2);
    await wrapper.get('[data-palette="text"]').trigger('dragstart', { dataTransfer: { setData() {}, effectAllowed: '' } });
    await nextTick();
    expect(wrapper.findAll('[data-zone]').filter((z) => z.classes().includes('active')).length).toBeGreaterThan(2);
  });

  it('renders every kind of block without breaking', () => {
    const condition = createBlock('condition', { expression: 'vip' });
    condition.children = [createBlock('text')];
    condition.elseChildren = [createBlock('button')];
    const repeat = createBlock('repeat');
    repeat.children = [createBlock('image', { src: 'https://x.com/a.png' })];
    const design = ['header', 'text', 'button', 'divider', 'spacer', 'footer', 'html', 'columns'].reduce((d, t, i) => insert(d, at(i), createBlock(t as never)), emptyDesign());
    const full = insert(insert(design, at(8), condition), at(9), repeat);
    const wrapper = mount(EmailEditor, { props: { design: full } });
    expect(wrapper.text()).toContain('If vip');
    expect(wrapper.text()).toContain('For each item in items');
    expect(wrapper.text()).toContain('Otherwise');
    expect(wrapper.find('img').attributes('src')).toBe('https://x.com/a.png');
  });

  it('never runs scripts of a text block', () => {
    const d = insert(emptyDesign(), at(0), createBlock('text', { html: '<p>ok</p><img src=x onerror="window.__pwned=1"><script>window.__pwned=1</script>' }));
    const wrapper = mount(EmailEditor, { props: { design: d } });
    expect(wrapper.html()).not.toMatch(/onerror|<script/);
    expect((window as unknown as Record<string, unknown>)['__pwned']).toBeUndefined();
  });

  it('emits a change with the new design', async () => {
    const wrapper = mount(EmailEditor);
    await wrapper.get('[data-palette="spacer"]').trigger('click');
    const events = wrapper.emitted('change') as Design[][];
    expect(events).toHaveLength(1);
    expect(events[0]![0]!.blocks[0]!.type).toBe('spacer');
  });

  it('loads a new design from the prop', async () => {
    const wrapper = mount(EmailEditor, { props: { design: withBlocks('text') } });
    await wrapper.setProps({ design: withBlocks('header', 'footer') });
    expect(types(wrapper)).toEqual(['header', 'footer']);
  });
});
