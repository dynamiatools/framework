import { resolveIconClass } from '@dynamia-tools/ui-core';
import type { NavigationNode } from '@dynamia-tools/sdk';

/**
 * Recursively rewrites `icon` on a NavigationNode tree from a bare token (e.g. `"book"`) to a
 * Font Awesome class string (`"fa fa-book"`) — this theme's icon library choice; see
 * `resolveIconClass` in `@dynamia-tools/ui-core` for why that mapping isn't done server-side.
 */
export function withResolvedIcons(nodes: NavigationNode[]): NavigationNode[] {
  return nodes.map(node => ({
    ...node,
    icon: resolveIconClass(node.icon) || undefined,
    children: node.children ? withResolvedIcons(node.children) : node.children,
  }));
}
