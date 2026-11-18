// @vitest-environment happy-dom
import { mount } from '@vue/test-utils';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ancestors, createBlock, emptyDesign, EmailEditor, insert, sampleData, type Block, type Design } from '../../src/index.js';

const at = (parentId: string | null, index = 0, slot: 'children' | 'elseChildren' = 'children') => ({ parentId, slot, index }) as const;
const design = (...blocks: Block[]): Design => blocks.reduce((d, b, i) => insert(d, at(null, i), b), emptyDesign());
const current = (wrapper: ReturnType<typeof mount>): Design => (wrapper.vm as unknown as { getDesign(): Design }).getDesign();

const exec = vi.fn((_name: string, _ui?: boolean, _value?: string) => true);
beforeEach(() => {
  exec.mockClear();
  (document as unknown as { execCommand: typeof exec }).execCommand = exec;
});

const VARIABLES = [
  { name: 'user.name', label: 'Customer name', sample: 'Ada' },
  { name: 'order.number', label: 'Order number' },
  { name: 'total' },
];

describe('rich text', () => {
  it('shows the block html and keeps it sanitized when it changes', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('text', { html: '<p>Hi {{name}}</p>' })) } });
    await wrapper.get('[data-type="text"]').trigger('click');
    const area = wrapper.get('[data-rich]');
    expect(area.element.innerHTML).toBe('<p>Hi {{name}}</p>');
    area.element.innerHTML = '<p>Bye <script>alert(1)</script><img src=x onerror="x()"></p>';
    await area.trigger('input');
    const html = String(current(wrapper).blocks[0]!.props['html']);
    expect(html).toBe('<p>Bye <img src="x"></p>');
    expect(html).not.toMatch(/script|onerror/);
  });

  it('toolbar buttons run editing commands and update the block', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('text')) } });
    await wrapper.get('[data-type="text"]').trigger('click');
    for (const cmd of ['bold', 'italic', 'underline', 'insertUnorderedList', 'insertOrderedList', 'removeFormat']) {
      await wrapper.get(`[data-cmd="${cmd}"]`).trigger('click');
      expect(exec).toHaveBeenLastCalledWith(cmd, false, undefined);
    }
  });

  it('adds a link only when the address is safe', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('text')) } });
    await wrapper.get('[data-type="text"]').trigger('click');
    await wrapper.get('[data-cmd="link"]').trigger('click');
    const input = wrapper.get('[data-link] input');
    await input.setValue('javascript:alert(1)');
    await wrapper.get('[data-action="apply-link"]').trigger('click');
    expect(exec).not.toHaveBeenCalledWith('createLink', false, expect.anything());
    await wrapper.get('[data-cmd="link"]').trigger('click');
    await wrapper.get('[data-link] input').setValue('https://dynamia.tools');
    await wrapper.get('[data-link] input').trigger('keydown', { key: 'Enter' });
    expect(exec).toHaveBeenCalledWith('createLink', false, 'https://dynamia.tools');
  });

  it('shows what undo restored without waiting for typing', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('text', { html: '<p>one</p>' })) } });
    await wrapper.get('[data-type="text"]').trigger('click');
    const area = wrapper.get('[data-rich]');
    area.element.innerHTML = '<p>two</p>';
    await area.trigger('input');
    await wrapper.get('[data-action="undo"]').trigger('click');
    await wrapper.vm.$nextTick();
    expect(current(wrapper).blocks[0]!.props['html']).toBe('<p>one</p>');
  });
});

describe('variables panel', () => {
  it('lists the variables, filters them and shows labels', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('text')), variables: VARIABLES } });
    expect(wrapper.findAll('[data-variable]')).toHaveLength(3);
    expect(wrapper.text()).toContain('Customer name');
    await wrapper.get('input[type="search"]').setValue('order');
    expect(wrapper.findAll('[data-variable]').map((b) => b.text())).toEqual(['order.numberOrder number']);
    await wrapper.get('input[type="search"]').setValue('zzz');
    expect(wrapper.text()).toContain('No variables available');
  });

  it('inserts {{name}} at the cursor of the plain text field that has the focus', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('button', { text: 'Pay now' })), variables: VARIABLES }, attachTo: document.body });
    await wrapper.get('[data-type="button"]').trigger('click');
    const field = wrapper.get('[data-field="text"]');
    (field.element as HTMLInputElement).focus();
    await field.trigger('focus');
    (field.element as HTMLInputElement).setSelectionRange(3, 3);
    await wrapper.findAll('[data-variable]')[0]!.trigger('click');
    expect(current(wrapper).blocks[0]!.props['text']).toBe('Pay{{user.name}} now');
    wrapper.unmount();
  });

  it('inserts into the rich text through the editing command', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('text')), variables: VARIABLES }, attachTo: document.body });
    await wrapper.get('[data-type="text"]').trigger('click');
    await wrapper.get('[data-rich]').trigger('focus');
    await wrapper.findAll('[data-variable]')[2]!.trigger('click');
    expect(exec).toHaveBeenCalledWith('insertText', false, '{{total}}');
    wrapper.unmount();
  });

  it('does nothing when no field has been focused', async () => {
    const wrapper = mount(EmailEditor, { props: { design: design(createBlock('spacer')), variables: VARIABLES } });
    await wrapper.findAll('[data-variable]')[0]!.trigger('click');
    expect(exec).not.toHaveBeenCalled();
  });

  it('offers the loop variables inside a repeat', async () => {
    const repeat = createBlock('repeat', { source: 'lines', alias: 'line' });
    const text = createBlock('text');
    let d = design(repeat);
    d = insert(d, at(repeat.id), text);
    const wrapper = mount(EmailEditor, { props: { design: d, variables: VARIABLES } });
    expect(wrapper.text()).not.toContain('loop.number');
    await wrapper.get('[data-type="text"]').trigger('click');
    const names = wrapper.findAll('[data-variable]').map((b) => b.text());
    expect(names).toContain('line');
    expect(names).toContain('loop.number');
    expect(names).toContain('loop.last');
  });
});

describe('helpers', () => {
  it('lists the ancestors of a block', () => {
    const columns = createBlock('columns');
    const text = createBlock('text');
    let d = design(columns);
    d = insert(d, at(columns.children![0]!.id), text);
    expect(ancestors(d, text.id).map((b) => b.type)).toEqual(['column', 'columns']);
    expect(ancestors(d, 'nope')).toEqual([]);
  });

  it('builds nested sample data from variable paths', () => {
    expect(sampleData([{ name: 'user.name', sample: 'Ada' }, { name: 'user.email' }, { name: 'total', sample: 10 }, { name: 'user.name.first' }])).toEqual({
      user: { name: 'Ada', email: '[user.email]' },
      total: 10,
    });
  });
});
