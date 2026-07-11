// ActionResolver: resolves enabled actions for a view from entity metadata

import type { EntityMetadata, ActionMetadata } from '@dynamia-tools/sdk';
import type { CrudActionStateAlias } from '../actions/crudActionState.js';
import { isCrudActionStateApplicable } from '../actions/crudActionState.js';

export interface ActionResolutionContext {
  /** Optional view type or rendering context. Reserved for future filtering. */
  viewContext?: string;
  /** Fully-qualified class name of the active entity. */
  targetClass?: string | null;
  /** Current CRUD action state, supporting TS aliases like list/create/edit. */
  crudState?: CrudActionStateAlias | null;
  /** Optional action type allow-list. */
  actionTypes?: string[];
}

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
   * Resolve actions for entity metadata or a direct action array.
   * @param metadata - Entity metadata containing action list
   * @param context - Optional string view context or full resolution context
   * @returns Sorted list of applicable ActionMetadata
   */
  static resolveActions(
    metadata: EntityMetadata | ActionMetadata[] | null | undefined,
    context?: string | ActionResolutionContext,
  ): ActionMetadata[] {
    const actions = Array.isArray(metadata) ? metadata : metadata?.actions ?? [];
    const resolvedContext = typeof context === 'string' ? { viewContext: context } : (context ?? {});

    return actions.filter(action => ActionResolver._isApplicable(action, resolvedContext));
  }

  private static _isApplicable(action: ActionMetadata, context: ActionResolutionContext): boolean {
    return ActionResolver._matchesType(action, context)
      && ActionResolver._matchesClass(action, context.targetClass)
      && ActionResolver._matchesCrudState(action, context.crudState);
  }

  private static _matchesType(action: ActionMetadata, context: ActionResolutionContext): boolean {
    if (!context.actionTypes || context.actionTypes.length === 0) {
      return true;
    }

    return context.actionTypes.includes(action.type ?? 'Action');
  }

  private static _matchesClass(action: ActionMetadata, targetClass?: string | null): boolean {
    const applicableClasses = action.applicableClasses ?? [];
    if (applicableClasses.length === 0) {
      return true;
    }

    if (applicableClasses.some(className => className.toLowerCase() === 'all')) {
      return true;
    }

    if (!targetClass) {
      return false;
    }

    const normalizedTargetClass = targetClass.trim();
    const targetSimpleName = normalizedTargetClass.includes('.')
      ? normalizedTargetClass.substring(normalizedTargetClass.lastIndexOf('.') + 1)
      : normalizedTargetClass;

    return applicableClasses.some(className => {
      const normalizedClassName = className.trim();
      const simpleName = normalizedClassName.includes('.')
        ? normalizedClassName.substring(normalizedClassName.lastIndexOf('.') + 1)
        : normalizedClassName;

      return normalizedClassName === normalizedTargetClass || simpleName === targetSimpleName;
    });
  }

  private static _matchesCrudState(action: ActionMetadata, crudState?: CrudActionStateAlias | null): boolean {
    return isCrudActionStateApplicable(crudState, action.applicableStates);
  }
}
