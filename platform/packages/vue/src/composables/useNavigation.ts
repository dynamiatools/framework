// useNavigation: composable for accessing and managing the application navigation tree

import { ref, computed, onMounted } from 'vue';
import type { Ref, ComputedRef } from 'vue';
import type { DynamiaClient, NavigationTree, NavigationNode } from '@dynamia-tools/sdk';

// Module-level cache (singleton, not Pinia)
let _cachedTree: NavigationTree | null = null;

/** Recursively find a node by internalPath */
function findNodeByPath(nodes: NavigationNode[], path: string): NavigationNode | null {
  for (const node of nodes) {
    if (node.internalPath === path) return node;
    if (node.children) {
      const found = findNodeByPath(node.children, path);
      if (found) return found;
    }
  }
  return null;
}

/** Returns true if the node or any of its descendants has the given internalPath */
function containsPath(node: NavigationNode, path: string): boolean {
  if (node.internalPath === path) return true;
  return node.children?.some(c => containsPath(c, path)) ?? false;
}

/**
 * Composable for accessing the application navigation tree.
 * Fetches from the backend on first use and caches in memory.
 * Uses SDK types directly — no new navigation types.
 *
 * Example:
 * <pre>{@code
 * const { tree, nodes, currentModule, currentPage, navigateTo } = useNavigation(client);
 * }</pre>
 *
 * @param client - DynamiaClient instance
 * @returns Object with reactive navigation state
 */
export function useNavigation(client: DynamiaClient) {
  const tree: Ref<NavigationTree | null> = ref(_cachedTree);
  const loading: Ref<boolean> = ref(false);
  const error: Ref<string | null> = ref(null);
  const currentPath: Ref<string | null> = ref(null);

  /** Top-level navigation nodes (type="Module") */
  const nodes: ComputedRef<NavigationNode[]> = computed(() => tree.value?.navigation ?? []);

  /** Current top-level module node containing the active path */
  const currentModule: ComputedRef<NavigationNode | null> = computed(() => {
    if (!currentPath.value || !tree.value) return null;
    return tree.value.navigation.find(m => containsPath(m, currentPath.value!)) ?? null;
  });

  /** Current group node (second-level) containing the active path */
  const currentGroup: ComputedRef<NavigationNode | null> = computed(() => {
    const mod = currentModule.value;
    if (!mod?.children || !currentPath.value) return null;
    return mod.children.find(g => containsPath(g, currentPath.value!)) ?? null;
  });

  /** Current page node (leaf) matching the active path */
  const currentPage: ComputedRef<NavigationNode | null> = computed(() => {
    if (!currentPath.value || !tree.value) return null;
    return findNodeByPath(tree.value.navigation, currentPath.value);
  });

  async function loadNavigation(): Promise<void> {
    if (_cachedTree) { tree.value = _cachedTree; return; }
    loading.value = true;
    error.value = null;
    try {
      const navTree = await client.metadata.getNavigation();
      _cachedTree = navTree;
      tree.value = navTree;
    } catch (e) {
      error.value = String(e);
    } finally {
      loading.value = false;
    }
  }

  function navigateTo(path: string): void {
    currentPath.value = path;
  }

  function clearCache(): void {
    _cachedTree = null;
    tree.value = null;
  }

  onMounted(loadNavigation);

  return {
    tree,
    nodes,
    currentModule,
    currentGroup,
    currentPage,
    currentPath,
    loading,
    error,
    navigateTo,
    clearCache,
    reload: loadNavigation,
  };
}
