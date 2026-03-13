// useEntityPicker: composable for creating and managing a VueEntityPickerView

import { onMounted } from 'vue';
import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import { VueEntityPickerView } from '../views/VueEntityPickerView.js';

/** Options for useEntityPicker composable */
export interface UseEntityPickerOptions {
  /** Pre-loaded view descriptor */
  descriptor: ViewDescriptor;
  /** Entity metadata */
  entityMetadata?: EntityMetadata | null;
  /** Search function for querying entities */
  searcher?: (query: string) => Promise<unknown[]>;
  /** Initial selected value */
  initialValue?: unknown;
}

/**
 * Composable for creating and managing a VueEntityPickerView.
 *
 * Example:
 * <pre>{@code
 * const { view, searchResults, selectedEntity, search, select, clear } = useEntityPicker({
 *   descriptor,
 *   searcher: async (q) => await api.search(q),
 * });
 * }</pre>
 *
 * @param options - UseEntityPickerOptions
 * @returns Object with VueEntityPickerView and reactive state
 */
export function useEntityPicker(options: UseEntityPickerOptions) {
  const view = new VueEntityPickerView(options.descriptor, options.entityMetadata ?? null);

  if (options.searcher) view.setSearcher(options.searcher);

  onMounted(async () => {
    await view.initialize();
    if (options.initialValue !== undefined) view.setValue(options.initialValue);
  });

  return {
    view,
    searchResults: view.searchResults,
    selectedEntity: view.selectedEntity,
    searchQuery: view.searchQuery,
    loading: view.isLoading,
    search: (query: string) => view.search(query),
    select: (entity: unknown) => view.select(entity),
    clear: () => view.clear(),
  };
}
