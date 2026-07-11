import { describe, expect, it } from 'vitest';
import type { NavigationNode, NavigationTree } from '@dynamia-tools/sdk';
import {
  containsPath,
  findFirstPage,
  findNodeByPath,
  resolveActivePath,
} from '../../src';

function createTree(): NavigationTree {
  const booksCrud: NavigationNode = {
    id: 'books-crud',
    name: 'Books',
    type: 'CrudPage',
    internalPath: '/pages/store/books',
  };

  const authorsPage: NavigationNode = {
    id: 'authors-page',
    name: 'Authors',
    type: 'Page',
    internalPath: '/pages/store/authors',
  };

  const storeGroup: NavigationNode = {
    id: 'store-group',
    name: 'Store',
    type: 'PageGroup',
    children: [booksCrud, authorsPage],
  };

  const dashboardPage: NavigationNode = {
    id: 'dashboard-page',
    name: 'Dashboard',
    type: 'Page',
    internalPath: '/pages/dashboard',
  };

  const storeModule: NavigationNode = {
    id: 'store-module',
    name: 'Store Module',
    type: 'Module',
    children: [storeGroup, dashboardPage],
  };

  const adminModule: NavigationNode = {
    id: 'admin-module',
    name: 'Admin Module',
    type: 'Module',
    children: [
      {
        id: 'settings-page',
        name: 'Settings',
        type: 'Page',
        internalPath: '/pages/admin/settings',
      },
    ],
  };

  return {
    navigation: [storeModule, adminModule],
  };
}

describe('NavigationResolver.containsPath', () => {
  const tree = createTree();
  const module = tree.navigation.find((node) => node.id === 'store-module');
  if (!module) throw new Error('Test fixture is missing store-module');

  const group = module.children?.find((node) => node.id === 'store-group');
  if (!group) throw new Error('Test fixture is missing store-group');

  it('returns true when path matches descendant node', () => {
    expect(containsPath(module, '/pages/store/books')).toBe(true);
  });

  it('returns true when path matches node directly', () => {
    expect(containsPath(group, '/pages/store/authors')).toBe(true);
  });

  it('returns false when path is not present', () => {
    expect(containsPath(module, '/pages/unknown')).toBe(false);
  });
});

describe('NavigationResolver.findNodeByPath', () => {
  const tree = createTree();

  it('finds nested page by internalPath', () => {
    const node = findNodeByPath(tree.navigation, '/pages/store/books');
    expect(node?.id).toBe('books-crud');
  });

  it('returns null when no node matches the path', () => {
    const node = findNodeByPath(tree.navigation, '/pages/missing');
    expect(node).toBeNull();
  });
});

describe('NavigationResolver.findFirstPage', () => {
  it('returns first leaf page in depth-first order', () => {
    const tree = createTree();
    const first = findFirstPage(tree.navigation);
    expect(first?.internalPath).toBe('/pages/store/books');
  });

  it('returns null for an empty tree', () => {
    expect(findFirstPage([])).toBeNull();
  });

  it('ignores nodes without internalPath', () => {
    const tree: NavigationTree = {
      navigation: [
        {
          id: 'module-only',
          name: 'Module Only',
          type: 'Module',
          children: [
            {
              id: 'group-only',
              name: 'Group Only',
              type: 'PageGroup',
              children: [
                { id: 'no-path', name: 'No Path', type: 'Page' },
                { id: 'with-path', name: 'With Path', type: 'Page', internalPath: '/pages/ok' },
              ],
            },
          ],
        },
      ],
    };

    expect(findFirstPage(tree.navigation)?.id).toBe('with-path');
  });
});

describe('NavigationResolver.resolveActivePath', () => {
  const tree = createTree();

  it('returns null context when tree is null', () => {
    expect(resolveActivePath(null, '/pages/store/books')).toEqual({
      module: null,
      group: null,
      page: null,
    });
  });

  it('returns null context when path is null', () => {
    expect(resolveActivePath(tree, null)).toEqual({
      module: null,
      group: null,
      page: null,
    });
  });

  it('resolves module, group and page for nested page path', () => {
    const result = resolveActivePath(tree, '/pages/store/books');

    expect(result.module?.id).toBe('store-module');
    expect(result.group?.id).toBe('store-group');
    expect(result.page?.id).toBe('books-crud');
  });

  it('returns module and uses direct child as group when page hangs from module', () => {
    const result = resolveActivePath(tree, '/pages/dashboard');

    expect(result.module?.id).toBe('store-module');
    expect(result.group?.id).toBe('dashboard-page');
    expect(result.page?.id).toBe('dashboard-page');
  });

  it('returns null context for unknown path', () => {
    const result = resolveActivePath(tree, '/pages/does-not-exist');

    expect(result.module).toBeNull();
    expect(result.group).toBeNull();
    expect(result.page).toBeNull();
  });
});


