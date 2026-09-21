import { describe, it, expect, beforeEach } from 'vitest';
import type { ActionMetadata, ViewDescriptor } from '@dynamia-tools/sdk';
import { CrudView } from '../../src/view/CrudView.js';
import { ClientActionRegistry } from '../../src/actions/ClientAction.js';
import { registerBuiltinCrudActions } from '../../src/actions/builtinCrudActions.js';

function meta(id: string): ActionMetadata {
  return { id, name: id };
}

function createDescriptor(): ViewDescriptor {
  return {
    id: 'book-crud',
    beanClass: 'mybookstore.domain.Book',
    view: 'crud',
    fields: [],
    params: {},
  };
}

describe('registerBuiltinCrudActions — resolution', () => {
  beforeEach(() => {
    ClientActionRegistry.clear();
    registerBuiltinCrudActions();
  });

  it('resolves NewAction under its default id and every documented alias', () => {
    for (const id of ['NewAction', 'new', 'create', 'createaction']) {
      expect(ClientActionRegistry.resolve(meta(id))).not.toBeNull();
    }
  });

  it('resolves EditAction under its default id and alias', () => {
    for (const id of ['EditAction', 'edit']) {
      expect(ClientActionRegistry.resolve(meta(id))).not.toBeNull();
    }
  });

  it('resolves CancelAction under its default id and alias', () => {
    for (const id of ['CancelAction', 'cancel']) {
      expect(ClientActionRegistry.resolve(meta(id))).not.toBeNull();
    }
  });

  it('does not register Save/Delete — those stay server-flow-aware, not client-local', () => {
    for (const id of ['SaveAction', 'save', 'DeleteAction', 'delete']) {
      expect(ClientActionRegistry.resolve(meta(id))).toBeNull();
    }
  });

  it('is idempotent — calling it again does not throw or duplicate registrations oddly', () => {
    expect(() => registerBuiltinCrudActions()).not.toThrow();
    expect(ClientActionRegistry.resolve(meta('EditAction'))).not.toBeNull();
  });

  it('an app can override a builtin by registering its own ClientAction under the same id', () => {
    let customCalled = false;
    ClientActionRegistry.register('EditAction', { id: 'EditAction', execute: () => { customCalled = true; } });
    ClientActionRegistry.resolve(meta('EditAction'))!.execute({ action: meta('EditAction'), request: {} });
    expect(customCalled).toBe(true);
  });
});

describe('registerBuiltinCrudActions — execute behavior', () => {
  beforeEach(() => {
    ClientActionRegistry.clear();
    registerBuiltinCrudActions();
  });

  it('New: calls startCreate() on the CrudView', () => {
    const view = new CrudView(createDescriptor(), null);
    view.startEdit({ id: 1, name: 'existing' }); // put it in a non-'create' mode first
    ClientActionRegistry.resolve(meta('NewAction'))!.execute({ action: meta('NewAction'), request: {}, view });
    expect(view.getMode()).toBe('create');
  });

  it('Edit: prefers request.data over the current selection', () => {
    const view = new CrudView(createDescriptor(), null);
    const entity = { id: 42, name: 'from request' };
    ClientActionRegistry.resolve(meta('EditAction'))!.execute({
      action: meta('EditAction'),
      request: { data: entity },
      view,
    });
    expect(view.getMode()).toBe('edit');
    expect(view.formView.getValue()).toMatchObject(entity);
  });

  it('Edit: falls back to getActionData("READ") when request.data is absent', () => {
    const view = new CrudView(createDescriptor(), null);
    view.dataSetView.setSelected({ id: 7, name: 'selected row' });
    ClientActionRegistry.resolve(meta('EditAction'))!.execute({
      action: meta('EditAction'),
      request: {},
      view,
    });
    expect(view.getMode()).toBe('edit');
  });

  it('Edit: is a no-op when there is no entity to edit', () => {
    const view = new CrudView(createDescriptor(), null);
    ClientActionRegistry.resolve(meta('EditAction'))!.execute({ action: meta('EditAction'), request: {}, view });
    expect(view.getMode()).toBe('list');
  });

  it('Cancel: returns the CrudView to list mode', () => {
    const view = new CrudView(createDescriptor(), null);
    view.startCreate();
    ClientActionRegistry.resolve(meta('CancelAction'))!.execute({ action: meta('CancelAction'), request: {}, view });
    expect(view.getMode()).toBe('list');
  });

  it('is a no-op when the context view is not a CrudView (e.g. a plain View, or none at all)', () => {
    expect(() =>
      ClientActionRegistry.resolve(meta('NewAction'))!.execute({ action: meta('NewAction'), request: {} }),
    ).not.toThrow();
  });
});
