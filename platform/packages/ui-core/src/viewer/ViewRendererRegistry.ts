// ViewRendererRegistry: central registry mapping ViewType to ViewRenderer and View factories

import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import type { ViewType } from '../view/ViewType.js';
import type { View } from '../view/View.js';
import type { ViewRenderer } from '../renderer/ViewRenderer.js';

type ViewFactory = (descriptor: ViewDescriptor, metadata: EntityMetadata | null) => View;

/**
 * Central registry that maps ViewType to ViewRenderer and View factory functions.
 * Used by Viewer to resolve the correct renderer and view for a given ViewType.
 * Both ui-core built-ins and external modules register here.
 *
 * Example (Vue plugin registration):
 * <pre>{@code
 * ViewRendererRegistry.register(ViewTypes.Form, new VueFormRenderer());
 * ViewRendererRegistry.registerViewFactory(ViewTypes.Form, (d, m) => new VueFormView(d, m));
 * }</pre>
 */
export class ViewRendererRegistry {
  private static readonly _renderers = new Map<string, ViewRenderer<View, unknown>>();
  private static readonly _factories = new Map<string, ViewFactory>();

  /**
   * Register a renderer for a ViewType.
   * @param type - The ViewType to register for
   * @param renderer - The renderer implementation
   */
  static register<TView extends View, TOutput>(
    type: ViewType,
    renderer: ViewRenderer<TView, TOutput>
  ): void {
    ViewRendererRegistry._renderers.set(type.name, renderer as ViewRenderer<View, unknown>);
  }

  /**
   * Retrieve a registered renderer for a ViewType.
   * @param type - The ViewType to look up
   * @returns The registered renderer
   * @throws Error if no renderer is registered for the ViewType
   */
  static getRenderer(type: ViewType): ViewRenderer<View, unknown> {
    const renderer = ViewRendererRegistry._renderers.get(type.name);
    if (!renderer) throw new Error(`No renderer registered for ViewType '${type.name}'`);
    return renderer;
  }

  /**
   * Check if a renderer is registered for a ViewType.
   * @param type - The ViewType to check
   * @returns true if a renderer is registered
   */
  static hasRenderer(type: ViewType): boolean {
    return ViewRendererRegistry._renderers.has(type.name);
  }

  /**
   * Register a factory function for creating View instances for a ViewType.
   * @param type - The ViewType to register for
   * @param factory - Factory function creating the correct View subclass
   */
  static registerViewFactory(
    type: ViewType,
    factory: ViewFactory
  ): void {
    ViewRendererRegistry._factories.set(type.name, factory);
  }

  /**
   * Create the correct View subclass for a ViewType.
   * @param type - The ViewType to create a View for
   * @param descriptor - The view descriptor
   * @param metadata - The entity metadata
   * @returns A new View instance for the given ViewType
   * @throws Error if no factory is registered for the ViewType
   */
  static createView(
    type: ViewType,
    descriptor: ViewDescriptor,
    metadata: EntityMetadata | null
  ): View {
    const factory = ViewRendererRegistry._factories.get(type.name);
    if (!factory) throw new Error(`No view factory registered for ViewType '${type.name}'`);
    return factory(descriptor, metadata);
  }

  /**
   * Check if a view factory is registered for a ViewType.
   * @param type - The ViewType to check
   * @returns true if a factory is registered
   */
  static hasViewFactory(type: ViewType): boolean {
    return ViewRendererRegistry._factories.has(type.name);
  }

  /** Clear all registered renderers and factories (useful for testing) */
  static clear(): void {
    ViewRendererRegistry._renderers.clear();
    ViewRendererRegistry._factories.clear();
  }
}
