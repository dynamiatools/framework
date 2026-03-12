// useViewer: primary composable for creating and managing a VueViewer

import { onMounted, onUnmounted } from 'vue';
import type { ViewerConfig } from '@dynamia-tools/ui-core';
import { VueViewer } from '../views/VueViewer.js';

/**
 * Primary composable for creating and managing a VueViewer.
 * Creates a VueViewer, initializes it on mount, and destroys it on unmount.
 *
 * Example:
 * <pre>{@code
 * const { viewer, loading, error } = useViewer({
 *   viewType: 'form',
 *   beanClass: 'com.example.Book',
 *   client,
 * });
 * }</pre>
 *
 * @param config - ViewerConfig with viewType, beanClass, descriptor, etc.
 * @returns Object with viewer instance and reactive state
 */
export function useViewer(config: ViewerConfig = {}) {
  const viewer = new VueViewer(config);

  onMounted(async () => {
    try {
      await viewer.initialize();
    } catch {
      // Error is already set on viewer.error
    }
  });

  onUnmounted(() => {
    viewer.destroy();
  });

  return {
    /** The VueViewer instance */
    viewer,
    /** Reactive loading state */
    loading: viewer.loading,
    /** Reactive error message */
    error: viewer.error,
    /** Reactive resolved view */
    view: viewer.currentView,
    /** Reactive resolved descriptor */
    descriptor: viewer.currentDescriptor,
    /** Get the primary value */
    getValue: () => viewer.getValue(),
    /** Set the primary value */
    setValue: (value: unknown) => viewer.setValue(value),
    /** Set read-only mode */
    setReadonly: (ro: boolean) => viewer.setReadonly(ro),
  };
}
