// DataSetViewRegistry: factory registry for DataSetView implementations.
// Mirrors the CrudDataSetViewBuilder SPI from the Java/ZK platform.

import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import type { DataSetView } from './DataSetView.js';
import { TableView } from './TableView.js';
import { TreeView } from './TreeView.js';

/** Factory function that produces a DataSetView for the given descriptor */
export type DataSetViewFactory = (
  descriptor: ViewDescriptor,
  entityMetadata: EntityMetadata | null,
) => DataSetView;

/**
 * Registry that maps a `dataSetViewType` string to a {@link DataSetViewFactory}.
 *
 * Built-in registrations (`'table'` and `'tree'`) are added at module load time.
 * Third-party code can call {@link registerDataSetView} to add new types.
 *
 * {@link CrudView} calls {@link resolveDataSetView} in its constructor using the
 * value of `descriptor.params['dataSetViewType']` (default `'table'`).
 *
 * Example — register a custom Kanban view:
 * <pre>{@code
 * registerDataSetView('kanban', (d, m) => new KanbanView(d, m));
 * }</pre>
 */
export class DataSetViewRegistry {
  private readonly _map = new Map<string, DataSetViewFactory>();

  /** Register a factory for a given type name */
  register(type: string, factory: DataSetViewFactory): void {
    this._map.set(type, factory);
  }

  /**
   * Resolve and instantiate a DataSetView for the given type.
   * @throws Error when the type has no registered factory.
   */
  resolve(type: string, descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null): DataSetView {
    const factory = this._map.get(type);
    if (!factory) {
      const known = [...this._map.keys()].join(', ');
      throw new Error(
        `No DataSetView factory registered for type "${type}". Known types: ${known}`,
      );
    }
    return factory(descriptor, entityMetadata);
  }

  /** Returns true when a factory is registered for the given type */
  has(type: string): boolean { return this._map.has(type); }
}

/** Singleton registry instance used by CrudView */
export const dataSetViewRegistry = new DataSetViewRegistry();

// ── Built-in registrations ────────────────────────────────────────────────────
dataSetViewRegistry.register('table', (d, m) => new TableView(d, m));
dataSetViewRegistry.register('tree',  (d, m) => new TreeView(d, m));

// ── Convenience functions ─────────────────────────────────────────────────────

/** Register a custom DataSetView factory for a given type name */
export function registerDataSetView(type: string, factory: DataSetViewFactory): void {
  dataSetViewRegistry.register(type, factory);
}

/**
 * Resolve a DataSetView instance from the singleton registry.
 * Used internally by {@link CrudView}.
 */
export function resolveDataSetView(
  type: string,
  descriptor: ViewDescriptor,
  entityMetadata: EntityMetadata | null,
): DataSetView {
  return dataSetViewRegistry.resolve(type, descriptor, entityMetadata);
}

