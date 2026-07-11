// NavigationResolver: framework-agnostic helpers for resolving app navigation nodes

import type { NavigationTree, NavigationNode } from '@dynamia-tools/sdk';

export interface ActiveNavigationPath {
  module: NavigationNode | null;
  group: NavigationNode | null;
  page: NavigationNode | null;
}

/** Returns true if the node or any of its descendants has the given internal path. */
export function containsPath(node: NavigationNode, path: string): boolean {
  if (node.internalPath === path) return true;
  return node.children?.some((child) => containsPath(child, path)) ?? false;
}

/** Recursively finds a node by its internal path. */
export function findNodeByPath(nodes: NavigationNode[], path: string): NavigationNode | null {
  for (const node of nodes) {
    if (node.internalPath === path) return node;
    if (!node.children?.length) continue;
    const found = findNodeByPath(node.children, path);
    if (found) return found;
  }
  return null;
}

/** Returns the first leaf page with an internal path in depth-first order. */
export function findFirstPage(nodes: NavigationNode[]): NavigationNode | null {
  for (const node of nodes) {
    if (node.children?.length) {
      const nested = findFirstPage(node.children);
      if (nested) return nested;
      continue;
    }

    if (node.internalPath) return node;
  }

  return null;
}

/** Resolves current module, group and page nodes for an active navigation path. */
export function resolveActivePath(
  tree: NavigationTree | null,
  path: string | null,
): ActiveNavigationPath {
  if (!tree || !path) {
    return { module: null, group: null, page: null };
  }

  const module = tree.navigation.find((root) => containsPath(root, path)) ?? null;
  const group = module?.children?.find((child) => containsPath(child, path)) ?? null;
  const page = findNodeByPath(tree.navigation, path);

  return { module, group, page };
}

