// VueConfigView: Vue-reactive extension of ConfigView

import { ref } from 'vue';
import type { Ref } from 'vue';
import { ConfigView } from '@dynamia-tools/ui-core';
import type { ConfigParameter } from '@dynamia-tools/ui-core';
import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';

/**
 * Vue-reactive extension of ConfigView.
 */
export class VueConfigView extends ConfigView {
  /** Reactive parameters list */
  readonly parameters: Ref<ConfigParameter[]> = ref([]);
  /** Reactive parameter values */
  readonly values: Ref<Record<string, unknown>> = ref({});
  /** Reactive loading state */
  readonly isLoading: Ref<boolean> = ref(false);

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(descriptor, entityMetadata);
  }

  override async loadParameters(): Promise<void> {
    this.isLoading.value = true;
    try {
      await super.loadParameters();
      this.parameters.value = super.getParameters();
    } finally {
      this.isLoading.value = false;
    }
  }

  override setParameterValue(name: string, value: unknown): void {
    super.setParameterValue(name, value);
    this.values.value[name] = value;
  }
}
