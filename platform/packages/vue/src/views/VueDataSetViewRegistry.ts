// VueDataSetViewRegistry: Vue-reactive override of the core DataSetViewRegistry.
// Registers VueTableView and VueTreeView as the built-in factories so that
// VueCrudView receives reactive instances instead of plain core ones.

import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import type { DataSetView } from '@dynamia-tools/ui-core';
import type { DataSetViewFactory } from '@dynamia-tools/ui-core';
import { VueTableView } from './VueTableView.js';
import { VueTreeView } from './VueTreeView.js';

/**
 * Vue-aware DataSetView registry.
 * Same API as the core {@link DataSetViewRegistry} but factories return
 * Vue-reactive subclasses ({@link VueTableView}, {@link VueTreeView}).
 *
 * {@link VueCrudView} reads `descriptor.params['dataSetViewType']` and calls
 * {@link VueDataSetViewRegistry.resolve} to create the right reactive view.
 *
 * Example — register a custom Vue Kanban DataSetView:
 * <pre>{@code
 * vueDataSetViewRegistry.register('kanban', (d, m) => new VueKanbanView(d, m));
 * }</pre>
 */
class VueDataSetViewRegistry {
  private readonly _map = new Map<string, DataSetViewFactory>();

  register(type: string, factory: DataSetViewFactory): void {
    this._map.set(type, factory);
  }

  resolve(type: string, descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null): DataSetView {
    const factory = this._map.get(type);
    if (!factory) {
      const known = [...this._map.keys()].join(', ');
      throw new Error(
        `No Vue DataSetView factory registered for type "${type}". Known types: ${known}`,
      );
    }
    return factory(descriptor, entityMetadata);
  }

  has(type: string): boolean { return this._map.has(type); }
}

export const vueDataSetViewRegistry = new VueDataSetViewRegistry();
export { VueDataSetViewRegistry };

// ── Built-in Vue registrations ────────────────────────────────────────────────
vueDataSetViewRegistry.register('table', (d, m) => new VueTableView(d, m));
vueDataSetViewRegistry.register('tree',  (d, m) => new VueTreeView(d, m));

