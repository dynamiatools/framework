// EntityPickerView: entity selector (popup/inline search) view

import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import { View } from './View.js';
import { ViewTypes } from './ViewType.js';
import type { EntityPickerState } from '../types/state.js';

/**
 * View implementation for entity selection via search.
 *
 * Example:
 * <pre>{@code
 * const view = new EntityPickerView(descriptor, metadata);
 * await view.initialize();
 * await view.search('John');
 * view.select(results[0]);
 * }</pre>
 */
export class EntityPickerView extends View {
  protected override state: EntityPickerState;
  private _searcher?: (query: string) => Promise<unknown[]>;

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(ViewTypes.EntityPicker, descriptor, entityMetadata);
    this.state = { loading: false, error: null, initialized: false, searchQuery: '', searchResults: [], selectedEntity: null };
  }

  async initialize(): Promise<void> {
    this.state.initialized = true;
  }

  validate(): boolean { return this.state.selectedEntity !== null; }

  override getValue(): unknown { return this.state.selectedEntity; }
  override setValue(value: unknown): void { this.state.selectedEntity = value; }

  /** Set a search function for querying entities */
  setSearcher(searcher: (query: string) => Promise<unknown[]>): void { this._searcher = searcher; }

  /** Search for entities matching a query */
  async search(query: string): Promise<void> {
    this.state.searchQuery = query;
    if (!this._searcher) return;
    this.state.loading = true;
    try {
      this.state.searchResults = await this._searcher(query);
      this.emit('search-results', this.state.searchResults);
    } catch (e) {
      this.state.error = String(e);
    } finally {
      this.state.loading = false;
    }
  }

  /** Select an entity from search results */
  select(entity: unknown): void {
    this.state.selectedEntity = entity;
    this.emit('select', entity);
  }

  /** Clear the current selection */
  clear(): void {
    this.state.selectedEntity = null;
    this.state.searchQuery = '';
    this.state.searchResults = [];
    this.emit('clear');
  }

  getSearchResults(): unknown[] { return this.state.searchResults; }
  getSelectedEntity(): unknown { return this.state.selectedEntity; }
}
