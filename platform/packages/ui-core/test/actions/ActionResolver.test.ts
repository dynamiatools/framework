import { describe, expect, it } from 'vitest';
import type { ActionMetadata, EntityMetadata, ViewDescriptor } from '@dynamia-tools/sdk';
import { CrudView, ActionResolver } from '../../src/index.js';

function createAction(action: Partial<ActionMetadata> & Pick<ActionMetadata, 'id' | 'name'>): ActionMetadata {
  return {
    description: '',
    ...action,
  };
}

function createMetadata(actions: ActionMetadata[]): EntityMetadata {
  return {
    id: 'Book',
    name: 'Book',
    className: 'mybookstore.domain.Book',
    actions,
    descriptors: [],
    actionsEndpoint: '/api/books/actions',
  };
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

describe('ActionResolver', () => {
  it('filters actions by applicable class and CRUD alias state', () => {
    const actions = [
      createAction({ id: 'NewAction', name: 'New', type: 'CrudAction', applicableStates: ['READ'] }),
      createAction({ id: 'SaveAction', name: 'Save', type: 'CrudAction', applicableStates: ['CREATE', 'UPDATE'] }),
      createAction({ id: 'AdminOnlyAction', name: 'Admin', type: 'ClassAction', applicableClasses: ['mybookstore.domain.Admin'] }),
    ];

    const resolved = ActionResolver.resolveActions(actions, {
      targetClass: 'mybookstore.domain.Book',
      crudState: 'list',
    });

    expect(resolved.map(action => action.id)).toEqual(['NewAction']);
  });

  it('treats "all" as a wildcard applicable class', () => {
    const resolved = ActionResolver.resolveActions([
      createAction({
        id: 'ExportAction',
        name: 'Export',
        type: 'ClassAction',
        applicableClasses: ['all'],
      }),
    ], {
      targetClass: 'mybookstore.domain.Book',
      crudState: 'READ',
    });

    expect(resolved).toHaveLength(1);
  });
});

describe('CrudView action helpers', () => {
  it('maps mode aliases to Java CRUD action states', () => {
    const metadata = createMetadata([]);
    const view = new CrudView(createDescriptor(), metadata);

    expect(view.getCrudActionState()).toBe('READ');

    view.startCreate();
    expect(view.getCrudActionState()).toBe('CREATE');

    view.startEdit({ id: 7, name: 'Domain-Driven Design' });
    expect(view.getCrudActionState()).toBe('UPDATE');
  });

  it('builds execution requests from the active CRUD context', () => {
    const metadata = createMetadata([]);
    const view = new CrudView(createDescriptor(), metadata);

    view.startEdit({ id: 9, name: 'Refactoring' });

    expect(view.buildActionExecutionRequest()).toEqual({
      data: { id: 9, name: 'Refactoring' },
      source: 'crud',
      dataType: 'mybookstore.domain.Book',
      dataId: 9,
      dataName: 'Refactoring',
    });
  });

  it('resolves the current entity actions using the CRUD state alias', () => {
    const metadata = createMetadata([
      createAction({ id: 'NewAction', name: 'New', type: 'CrudAction', applicableStates: ['READ'] }),
      createAction({ id: 'SaveAction', name: 'Save', type: 'CrudAction', applicableStates: ['CREATE', 'UPDATE'] }),
    ]);
    const view = new CrudView(createDescriptor(), metadata);

    expect(view.resolveActions().map(action => action.id)).toEqual(['NewAction']);

    view.startCreate();
    expect(view.resolveActions().map(action => action.id)).toEqual(['SaveAction']);
  });
});


