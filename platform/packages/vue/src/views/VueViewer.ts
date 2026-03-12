// VueViewer: Vue-reactive extension of ui-core Viewer

import { ref, shallowRef } from 'vue';
import type { Ref, ShallowRef } from 'vue';
import { Viewer } from '@dynamia-tools/ui-core';
import type { ViewerConfig, View } from '@dynamia-tools/ui-core';
import type { ViewDescriptor } from '@dynamia-tools/sdk';

/**
 * Vue-reactive extension of ui-core Viewer.
 * Wraps Viewer with reactive state for Vue templates.
 *
 * Example:
 * <pre>{@code
 * const viewer = new VueViewer({ viewType: 'form', beanClass: 'com.example.Book', client });
 * await viewer.initialize();
 * // viewer.currentView.value is now reactive
 * }</pre>
 */
export class VueViewer extends Viewer {
  /** Reactive loading state */
  readonly loading: Ref<boolean> = ref(false);
  /** Reactive error message */
  readonly error: Ref<string | null> = ref(null);
  /** Reactive resolved view instance */
  readonly currentView: ShallowRef<View | null> = shallowRef(null);
  /** Reactive resolved descriptor */
  readonly currentDescriptor: ShallowRef<ViewDescriptor | null> = shallowRef(null);

  constructor(config: ViewerConfig = {}) {
    super(config);
  }

  override async initialize(): Promise<void> {
    this.loading.value = true;
    this.error.value = null;
    try {
      await super.initialize();
      this.currentView.value = super.view;
      this.currentDescriptor.value = super.resolvedDescriptor;
    } catch (e) {
      this.error.value = String(e);
      throw e;
    } finally {
      this.loading.value = false;
    }
  }

  override destroy(): void {
    super.destroy();
    this.currentView.value = null;
    this.currentDescriptor.value = null;
  }
}
