// useNavigation: composable for accessing and managing the application navigation tree

import { ref, computed, onMounted } from 'vue';
import type { Ref, ComputedRef } from 'vue';
import type { DynamiaClient, NavigationTree, NavigationNode } from '@dynamia-tools/sdk';
import { findFirstPage, resolveActivePath } from '@dynamia-tools/ui-core';

// Module-level cache (singleton, not Pinia)
let _cachedTree: NavigationTree | null = null;

export interface UseNavigationOptions {
  /** Automatically navigate to the first available page after loading the tree. */
  autoSelectFirst?: boolean;
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
 * @param options - Optional behavior flags for initialization
 * @returns Object with reactive navigation state
 */
export function useNavigation(client: DynamiaClient, options: UseNavigationOptions = {}) {
  const { autoSelectFirst = false } = options;
  const tree: Ref<NavigationTree | null> = ref(_cachedTree);
  const loading: Ref<boolean> = ref(false);
  const error: Ref<string | null> = ref(null);
  const currentPath: Ref<string | null> = ref(null);

  /** Top-level navigation nodes (type="Module") */
  const nodes: ComputedRef<NavigationNode[]> = computed(() => tree.value?.navigation ?? []);

  const activeContext = computed(() => resolveActivePath(tree.value, currentPath.value));

  /** Current top-level module node containing the active path */
  const currentModule: ComputedRef<NavigationNode | null> = computed(() => {
    return activeContext.value.module;
  });

  /** Current group node (second-level) containing the active path */
  const currentGroup: ComputedRef<NavigationNode | null> = computed(() => {
    return activeContext.value.group;
  });

  /** Current page node (leaf) matching the active path */
  const currentPage: ComputedRef<NavigationNode | null> = computed(() => {
    return activeContext.value.page;
  });

  function autoSelectFirstPage(): void {
    if (!autoSelectFirst || currentPath.value || !tree.value) return;
    const first = findFirstPage(tree.value.navigation);
    if (first?.internalPath) {
      currentPath.value = first.internalPath;
    }
  }

  async function loadNavigation(): Promise<void> {
    if (_cachedTree) {
      tree.value = _cachedTree;
      autoSelectFirstPage();
      return;
    }
    loading.value = true;
    error.value = null;
    try {
      const navTree = await client.metadata.getNavigation();
      _cachedTree = navTree;
      tree.value = navTree;
      autoSelectFirstPage();
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
