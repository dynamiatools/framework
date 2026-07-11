// useView: generic composable for creating any VueView subclass

import { onMounted } from 'vue';
import type { VueView } from '../views/VueView.js';

/**
 * Generic composable for creating and managing any VueView subclass.
 * Initializes on mount and cleans up on unmount.
 *
 * @param factory - Factory function that creates the VueView instance
 * @returns Object with view instance and lifecycle management
 */
export function useView<T extends VueView>(factory: () => T) {
  const view = factory();

  onMounted(async () => {
    await view.initialize();
  });

  return {
    /** The VueView instance */
    view,
    /** Reactive loading state */
    loading: view.isLoading,
    /** Reactive error message */
    error: view.errorMessage,
    /** Reactive initialized flag */
    initialized: view.isInitialized,
  };
}
