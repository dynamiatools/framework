// ViewType: open extension interface for view type identity

/**
 * Identity interface for a view type.
 * Anyone can implement this interface to register new view types.
 * This is intentionally NOT an enum so external modules can extend it.
 *
 * Example (custom view type):
 * <pre>{@code
 * const KanbanViewType: ViewType = { name: 'kanban' };
 * }</pre>
 */
export interface ViewType {
  /** Unique name identifier for this view type (e.g. 'form', 'table', 'crud') */
  readonly name: string;
}

/**
 * Built-in view types shipped with ui-core.
 * Plain objects satisfying ViewType — not enum values.
 * Third-party modules define their own ViewType instances the same way.
 */
export const ViewTypes = {
  Form: { name: 'form' },
  Table: { name: 'table' },
  Crud: { name: 'crud' },
  Tree: { name: 'tree' },
  Config: { name: 'config' },
  EntityPicker: { name: 'entitypicker' },
  EntityFilters: { name: 'entityfilters' },
  Export: { name: 'export' },
  Json: { name: 'json' },
} as const satisfies Record<string, ViewType>;
