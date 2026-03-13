// VueView: abstract Vue-reactive extension of ui-core View

import { ref } from 'vue';
import type { Ref } from 'vue';
import { View } from '@dynamia-tools/ui-core';
import type { ViewType } from '@dynamia-tools/ui-core';
import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';

/**
 * Abstract base class for all Vue-reactive views.
 * Extends ui-core View to add Vue reactivity primitives.
 */
export abstract class VueView extends View {
  /** Reactive loading state */
  readonly isLoading: Ref<boolean> = ref(false);
  /** Reactive error message */
  readonly errorMessage: Ref<string | null> = ref(null);
  /** Reactive initialized flag */
  readonly isInitialized: Ref<boolean> = ref(false);

  constructor(viewType: ViewType, descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(viewType, descriptor, entityMetadata);
  }

  protected setLoading(loading: boolean): void {
    this.isLoading.value = loading;
    this.state.loading = loading;
  }

  protected setError(error: string | null): void {
    this.errorMessage.value = error;
    this.state.error = error;
  }

  protected setInitialized(initialized: boolean): void {
    this.isInitialized.value = initialized;
    this.state.initialized = initialized;
  }
}
