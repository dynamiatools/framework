// Viewer: universal view host that resolves ViewType → View → output

import type { ViewDescriptor, EntityMetadata, DynamiaClient } from '@dynamia-tools/sdk';
import type { ViewType } from '../view/ViewType.js';
import { ViewTypes } from '../view/ViewType.js';
import type { View, EventHandler } from '../view/View.js';
import type { ActionMetadata } from '@dynamia-tools/sdk';
import { ViewRendererRegistry } from './ViewRendererRegistry.js';

/** Configuration options for Viewer initialization */
export interface ViewerConfig {
  /** View type name or ViewType object */
  viewType?: string | ViewType | null;
  /** Entity class name (e.g. 'com.example.Book') */
  beanClass?: string | null;
  /** Pre-loaded view descriptor (skips fetch) */
  descriptor?: ViewDescriptor | null;
  /** Descriptor ID to fetch from backend */
  descriptorId?: string | null;
  /** Initial value for the view */
  value?: unknown;
  /** Initial source/data for the view */
  source?: unknown;
  /** Whether the view is in read-only mode */
  readOnly?: boolean;
  /** Dynamia client instance for API calls */
  client?: DynamiaClient | null;
}

/**
 * Universal view host that resolves ViewType → View → rendered output.
 * Primary abstraction for consumers — mirrors the ZK tools.dynamia.zk.viewers.ui.Viewer.
 *
 * Example:
 * <pre>{@code
 * const viewer = new Viewer({ viewType: 'form', beanClass: 'com.example.Book', client });
 * await viewer.initialize();
 * viewer.setValue(book);
 * }</pre>
 */
export class Viewer {
  viewType: string | ViewType | null;
  beanClass: string | null;
  descriptor: ViewDescriptor | null;
  descriptorId: string | null;
  value: unknown;
  source: unknown;
  readOnly: boolean;
  client: DynamiaClient | null;

  private _view: View | null = null;
  private _resolvedDescriptor: ViewDescriptor | null = null;
  private _resolvedViewType: ViewType | null = null;
  private _actions: ActionMetadata[] = [];
  private _pendingEvents: Array<{ event: string; handler: EventHandler }> = [];
  private _initialized = false;

  constructor(config: ViewerConfig = {}) {
    this.viewType = config.viewType ?? null;
    this.beanClass = config.beanClass ?? null;
    this.descriptor = config.descriptor ?? null;
    this.descriptorId = config.descriptorId ?? null;
    this.value = config.value;
    this.source = config.source;
    this.readOnly = config.readOnly ?? false;
    this.client = config.client ?? null;
  }

  /** The resolved View instance (available after initialize()) */
  get view(): View | null { return this._view; }

  /** The resolved ViewDescriptor (available after initialize()) */
  get resolvedDescriptor(): ViewDescriptor | null { return this._resolvedDescriptor; }

  /** The resolved ViewType (available after initialize()) */
  get resolvedViewType(): ViewType | null { return this._resolvedViewType; }

  /**
   * Initialize the viewer: resolve descriptor, create view, apply config.
   * Must be called before accessing view or rendering.
   */
  async initialize(): Promise<void> {
    // 1. Resolve descriptor
    await this._resolveDescriptor();

    // 2. Resolve view type
    this._resolvedViewType = this._resolveViewType();
    if (!this._resolvedViewType) throw new Error('Cannot resolve ViewType — set viewType, descriptor, or descriptorId');

    // 3. Create view
    if (ViewRendererRegistry.hasViewFactory(this._resolvedViewType)) {
      this._view = ViewRendererRegistry.createView(
        this._resolvedViewType,
        this._resolvedDescriptor!,
        null
      );
    } else {
      // Fall back to basic view creation based on view type
      this._view = this._createFallbackView(this._resolvedViewType, this._resolvedDescriptor!);
    }

    if (!this._view) throw new Error(`Cannot create view for ViewType '${this._resolvedViewType.name}'`);

    // 4. Apply pending event listeners
    for (const { event, handler } of this._pendingEvents) {
      this._view.on(event, handler);
    }
    this._pendingEvents = [];

    // 5. Apply value, source, readOnly
    if (this.value !== undefined) this._view.setValue(this.value);
    if (this.source !== undefined) this._view.setSource(this.source);
    this._view.setReadOnly(this.readOnly);

    // 6. Initialize the view
    await this._view.initialize();

    // 7. Load actions from entity metadata if available
    if (this.client && this.beanClass) {
      try {
        const entities = await this.client.metadata.getEntities();
        const entity = entities.entities.find(e => e.className === this.beanClass);
        if (entity) this._actions = entity.actions;
      } catch {
        // Ignore — actions are optional
      }
    }

    this._initialized = true;
  }

  /** Clean up the viewer and its view */
  destroy(): void {
    this._view = null;
    this._resolvedDescriptor = null;
    this._resolvedViewType = null;
    this._actions = [];
    this._pendingEvents = [];
    this._initialized = false;
  }

  /** Get the primary value from the view */
  getValue(): unknown { return this._view?.getValue(); }

  /** Set the primary value on the view */
  setValue(value: unknown): void {
    this.value = value;
    if (this._view) this._view.setValue(value);
  }

  /** Get selected item (for dataset views like table) */
  getSelected(): unknown {
    return (this._view as unknown as { getSelectedRow?: () => unknown })?.getSelectedRow?.();
  }

  /** Set selected item */
  setSelected(value: unknown): void {
    (this._view as unknown as { selectRow?: (row: unknown) => void })?.selectRow?.(value);
  }

  /** Add an action to the viewer */
  addAction(action: ActionMetadata): void { this._actions.push(action); }

  /** Get all resolved actions */
  getActions(): ActionMetadata[] { return this._actions; }

  /**
   * Register an event listener.
   * If called before initialize(), the listener is buffered and applied after.
   */
  on(event: string, handler: EventHandler): void {
    if (this._view) {
      this._view.on(event, handler);
    } else {
      this._pendingEvents.push({ event, handler });
    }
  }

  /** Remove an event listener */
  off(event: string, handler: EventHandler): void {
    this._view?.off(event, handler);
  }

  /** Set read-only mode */
  setReadonly(readOnly: boolean): void {
    this.readOnly = readOnly;
    this._view?.setReadOnly(readOnly);
  }

  /** Whether the viewer is in read-only mode */
  isReadonly(): boolean { return this.readOnly; }

  /** Whether the viewer has been initialized */
  isInitialized(): boolean { return this._initialized; }

  private async _resolveDescriptor(): Promise<void> {
    if (this.descriptor) {
      // Use pre-loaded descriptor directly
      this._resolvedDescriptor = this.descriptor;
      return;
    }
    if (!this.client) {
      // No client — cannot fetch; descriptor must be provided
      if (!this.descriptor) throw new Error('Either provide a descriptor or a DynamiaClient to fetch it');
      return;
    }
    if (this.descriptorId) {
      // Fetch by ID
      const meta = await this.client.metadata.getEntities();
      for (const entity of meta.entities) {
        for (const d of entity.descriptors) {
          if (d.descriptor.id === this.descriptorId) {
            this._resolvedDescriptor = d.descriptor;
            if (!this.beanClass) this.beanClass = entity.className;
            return;
          }
        }
      }
      throw new Error(`Descriptor with id '${this.descriptorId}' not found`);
    }
    if (this.beanClass && this.viewType) {
      // Fetch by beanClass + viewType
      const typeName = typeof this.viewType === 'string' ? this.viewType : this.viewType.name;
      const meta = await this.client.metadata.getEntities();
      const entity = meta.entities.find(e => e.className === this.beanClass);
      if (entity) {
        const found = entity.descriptors.find(d => d.view === typeName || d.descriptor.viewTypeName === typeName);
        if (found) {
          this._resolvedDescriptor = found.descriptor;
          return;
        }
      }
    }
    // If we get here and still no descriptor, create a minimal one
    if (!this._resolvedDescriptor) {
      this._resolvedDescriptor = {
        id: `${this.beanClass ?? 'unknown'}-${typeof this.viewType === 'string' ? this.viewType : this.viewType?.name ?? 'form'}`,
        beanClass: this.beanClass ?? '',
        viewTypeName: typeof this.viewType === 'string' ? this.viewType : this.viewType?.name ?? 'form',
        fields: [],
        params: {},
      };
    }
  }

  private _resolveViewType(): ViewType | null {
    if (this.viewType) {
      if (typeof this.viewType === 'string') {
        const found = Object.values(ViewTypes).find(vt => vt.name === this.viewType);
        return found ?? { name: this.viewType as string };
      }
      return this.viewType;
    }
    if (this._resolvedDescriptor) {
      const typeName = this._resolvedDescriptor.viewTypeName;
      const found = Object.values(ViewTypes).find(vt => vt.name === typeName);
      return found ?? { name: typeName };
    }
    return null;
  }

  private _createFallbackView(_type: ViewType, _descriptor: ViewDescriptor): View | null {
    // Dynamically import concrete view classes to avoid circular deps at module level
    // Return null — callers should always register a factory
    return null;
  }
}
