// VueEntityPickerView: Vue-reactive extension of EntityPickerView

import { ref } from 'vue';
import type { Ref } from 'vue';
import { EntityPickerView } from '@dynamia-tools/ui-core';
import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';

/**
 * Vue-reactive extension of EntityPickerView.
 */
export class VueEntityPickerView extends EntityPickerView {
  /** Reactive search results */
  readonly searchResults: Ref<unknown[]> = ref([]);
  /** Reactive selected entity */
  readonly selectedEntity: Ref<unknown> = ref(null);
  /** Reactive search query */
  readonly searchQuery: Ref<string> = ref('');
  /** Reactive loading state */
  readonly isLoading: Ref<boolean> = ref(false);

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(descriptor, entityMetadata);
  }

  override async search(query: string): Promise<void> {
    this.searchQuery.value = query;
    this.isLoading.value = true;
    try {
      await super.search(query);
      this.searchResults.value = super.getSearchResults();
    } finally {
      this.isLoading.value = false;
    }
  }

  override select(entity: unknown): void {
    super.select(entity);
    this.selectedEntity.value = entity;
  }

  override clear(): void {
    super.clear();
    this.selectedEntity.value = null;
    this.searchQuery.value = '';
    this.searchResults.value = [];
  }
}
