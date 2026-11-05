// @vitest-environment happy-dom
import { afterEach, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import type { ActionMetadata } from '@dynamia-tools/sdk';
import RowActions from './RowActions.vue';

const action = (id: string): ActionMetadata => ({ id, name: id, className: id } as ActionMetadata);
const row = { id: 7 };

afterEach(() => { document.body.innerHTML = ''; });

describe('RowActions', () => {
  it('shows the edit button and emits the row', async () => {
    const wrapper = mount(RowActions, { props: { row, actions: [] } });
    await wrapper.get('.dynamia-row-action-edit').trigger('click');
    expect(wrapper.emitted('edit')?.[0]).toEqual([row]);
    expect(wrapper.find('.dynamia-row-action-more').exists()).toBe(false);
  });

  it('hides the edit button when showEdit is false', () => {
    const wrapper = mount(RowActions, { props: { row, actions: [], showEdit: false } });
    expect(wrapper.find('.dynamia-row-action-edit').exists()).toBe(false);
  });

  it('uses custom labels and falls back to the English defaults', () => {
    const custom = mount(RowActions, {
      props: { row, actions: [action('view')], editLabel: 'Editar', moreLabel: 'Más acciones' },
    });
    expect(custom.get('.dynamia-row-action-edit').attributes('aria-label')).toBe('Editar');
    expect(custom.get('.dynamia-row-action-more').attributes('aria-label')).toBe('Más acciones');

    const defaults = mount(RowActions, { props: { row, actions: [action('view')], editLabel: undefined } });
    expect(defaults.get('.dynamia-row-action-edit').attributes('aria-label')).toBe('Edit');
    expect(defaults.get('.dynamia-row-action-more').attributes('aria-label')).toBe('More actions');
  });

  it('opens the menu on the more button and marks Delete as dangerous', async () => {
    const wrapper = mount(RowActions, {
      props: { row, actions: [action('view'), action('delete')] },
      attachTo: document.body,
    });
    expect(document.body.querySelector('.dynamia-row-menu')).toBeNull();

    await wrapper.get('.dynamia-row-action-more').trigger('click');
    const menu = document.body.querySelector('.dynamia-row-menu') as HTMLElement;
    expect(menu).not.toBeNull();
    expect(menu.style.display).not.toBe('none');
    expect(menu.querySelectorAll('.dynamia-row-menu-item')).toHaveLength(2);
    expect(menu.querySelectorAll('.dynamia-row-menu-item-danger')).toHaveLength(1);
    wrapper.unmount();
  });
});
