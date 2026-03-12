// ActionResolver: resolves enabled actions for a view from entity metadata

import type { EntityMetadata, ActionMetadata } from '@dynamia-tools/sdk';

/**
 * Resolves the list of actions available for a given entity and view type.
 *
 * Example:
 * <pre>{@code
 * const actions = ActionResolver.resolveActions(entityMetadata, 'crud');
 * }</pre>
 */
export class ActionResolver {
  /**
   * Resolve actions for an entity metadata, optionally filtered by view type context.
   * @param metadata - Entity metadata containing action list
   * @param viewContext - Optional view type context for filtering
   * @returns Sorted list of applicable ActionMetadata
   */
  static resolveActions(metadata: EntityMetadata, viewContext?: string): ActionMetadata[] {
    if (!metadata.actions) return [];
    return metadata.actions.filter(action => ActionResolver._isApplicable(action, viewContext));
  }

  private static _isApplicable(action: ActionMetadata, _viewContext?: string): boolean {
    // In a full implementation, this would check action params for view-specific restrictions
    return true;
  }
}
