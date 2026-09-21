import type { NavigationNode } from '@dynamia-tools/sdk';
import type { MenuGroup, MenuItem, SubItem } from '@dynamia-tools/tailadmin-vue/components/layout/AppSidebar.vue';
import { faIcon } from './faIcon.js';
import { toRoutePath } from '../router.js';

/** The collapsed sidebar shows icons only, so an item with no icon of its own still needs one. */
const FALLBACK_ICON = 'fa fa-circle';

/** Flattens a node's descendants to leaf pages (the package's menu has a single submenu level). */
function leafPages(node: NavigationNode): SubItem[] {
  if (!node.children?.length) {
    return node.internalPath ? [{ name: node.name, path: toRoutePath(node.internalPath) }] : [];
  }
  return node.children.flatMap(leafPages);
}

function toMenuItem(node: NavigationNode): MenuItem | null {
  const icon = faIcon(node.icon || FALLBACK_ICON);
  if (!node.children?.length) {
    return node.internalPath ? { name: node.name, path: toRoutePath(node.internalPath), ...(icon ? { icon } : {}) } : null;
  }
  const subItems = node.children.flatMap(leafPages);
  return subItems.length ? { name: node.name, subItems, ...(icon ? { icon } : {}) } : null;
}

/**
 * Maps the Dynamia navigation tree onto the package's `AppSidebar` menu model:
 * Module → `MenuGroup` (section title), PageGroup → item with `subItems`, Page → plain item.
 * Paths are the nodes' `internalPath` as route paths (see router.ts).
 */
export function toMenuGroups(nodes: NavigationNode[]): MenuGroup[] {
  return nodes
    .map(module => ({
      title: module.name,
      items: (module.children?.length ? module.children : [module])
        .map(toMenuItem)
        .filter((item): item is MenuItem => item !== null),
    }))
    .filter(group => group.items.length > 0);
}
