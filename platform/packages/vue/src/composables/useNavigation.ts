// useNavigation: composable for accessing and managing the application navigation tree

import { ref, computed, onMounted } from 'vue';
import type { Ref, ComputedRef } from 'vue';
import type { DynamiaClient, NavigationTree, NavigationModule, NavigationPage } from '@dynamia-tools/sdk';

// Module-level cache (singleton, not Pinia)
let _cachedTree: NavigationTree | null = null;

/**
 * Composable for accessing the application navigation tree.
 * Fetches from the backend on first use and caches in memory.
 * Uses SDK types directly — no new navigation types.
 *
 * Example:
 * <pre>{@code
 * const { tree, currentModule, navigateTo } = useNavigation(client);
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

  const modules: ComputedRef<NavigationModule[]> = computed(() => tree.value?.modules ?? []);

  const currentModule: ComputedRef<NavigationModule | null> = computed(() => {
    if (!currentPath.value || !tree.value) return null;
    return tree.value.modules.find(m =>
      m.groups.some(g => g.pages.some(p => p.virtualPath === currentPath.value))
    ) ?? null;
  });

  const currentPage: ComputedRef<NavigationPage | null> = computed(() => {
    if (!currentPath.value || !tree.value) return null;
    for (const m of tree.value.modules) {
      for (const g of m.groups) {
        for (const p of g.pages) {
          if (p.virtualPath === currentPath.value) return p;
        }
      }
    }
    return null;
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
    modules,
    currentModule,
    currentPage,
    currentPath,
    loading,
    error,
    navigateTo,
    clearCache,
    reload: loadNavigation,
  };
}
