// @dynamia-tools/ui-core — Framework-agnostic view/viewer/renderer core for Dynamia Platform

// ── Registry base ─────────────────────────────────────────────────────────────
export { Registry } from './registry/Registry.js';
export type { KeyNormalizer } from './registry/Registry.js';

// ── Types ─────────────────────────────────────────────────────────────────────
export type { FieldComponent, ResolvedField } from './types/field.js';
export { FieldComponent as FieldComponents } from './types/field.js';
export type { ResolvedLayout, ResolvedGroup, ResolvedRow } from './types/layout.js';
export type {
  ViewState, DataSetViewState,
  FormState, TableState, CrudState, CrudMode, SortDirection,
  TreeState, TreeNode, EntityPickerState, ConfigState, ConfigParameter,
} from './types/state.js';
export type { Converter, ConverterRegistry } from './types/converters.js';
export type { Validator, ValidatorRegistry } from './types/validators.js';

// ── ViewType ──────────────────────────────────────────────────────────────────
export type { ViewType } from './view/ViewType.js';
export { ViewTypes } from './view/ViewType.js';

// ── View base + concrete views ────────────────────────────────────────────────
export type { EventHandler } from './view/View.js';
export { View } from './view/View.js';
export { DataSetView } from './view/DataSetView.js';
export { DataSetViewRegistry, dataSetViewRegistry, registerDataSetView, resolveDataSetView } from './view/DataSetViewRegistry.js';
export type { DataSetViewFactory } from './view/DataSetViewRegistry.js';
export { FormView } from './view/FormView.js';
export { TableView } from './view/TableView.js';
export { CrudView } from './view/CrudView.js';
export { TreeView } from './view/TreeView.js';
export type { TreeLoader } from './view/TreeView.js';
export { ConfigView } from './view/ConfigView.js';
export { EntityPickerView } from './view/EntityPickerView.js';

// ── Viewer + Registry ─────────────────────────────────────────────────────────
export { Viewer } from './viewer/Viewer.js';
export type { ViewerConfig } from './viewer/Viewer.js';
export { ViewRendererRegistry } from './viewer/ViewRendererRegistry.js';

// ── Renderer interfaces ───────────────────────────────────────────────────────
export type {
  ViewRenderer, FormRenderer, TableRenderer, CrudRenderer, TreeRenderer, FieldRenderer,
} from './renderer/ViewRenderer.js';

// ── Resolvers ─────────────────────────────────────────────────────────────────
export { FieldResolver } from './resolvers/FieldResolver.js';
export { LayoutEngine } from './resolvers/LayoutEngine.js';
export { ActionResolver } from './resolvers/ActionResolver.js';
export type { ActionResolutionContext } from './resolvers/ActionResolver.js';

// ── Actions ───────────────────────────────────────────────────────────────────
export { ActionRendererRegistry, getActionRendererKeyCandidates } from './actions/ActionRendererRegistry.js';
export type { ActionExecutionEvent, ActionExecutionErrorEvent, ActionTriggerPayload } from './actions/types.js';
export type { CrudActionState, CrudActionStateAlias } from './actions/crudActionState.js';
export {
  crudModeToActionState,
  normalizeCrudActionState,
  isCrudActionStateApplicable,
} from './actions/crudActionState.js';

// ── Client actions ────────────────────────────────────────────────────────────
export { ClientActionRegistry, registerClientAction, isClientActionApplicable } from './actions/ClientAction.js';
export type { ClientAction, ClientActionContext, ClientActionRegistryClass } from './actions/ClientAction.js';

// ── Utils ─────────────────────────────────────────────────────────────────────
export {
  currencyConverter, currencySimpleConverter, decimalConverter, dateConverter, dateTimeConverter,
  builtinConverters,
} from './utils/converters.js';
export { requiredValidator, constraintValidator, builtinValidators } from './utils/validators.js';

// ── Page resolvers ────────────────────────────────────────────────────────────
export { CrudPageResolver, NavigationPageTypes } from './page/CrudPageResolver.js';
export type { CrudPageContext, NavigationPageType } from './page/CrudPageResolver.js';

// ── Navigation resolvers ──────────────────────────────────────────────────────
export {
  containsPath,
  findNodeByPath,
  findFirstPage,
  resolveActivePath,
} from './navigation/NavigationResolver.js';
export type { ActiveNavigationPath } from './navigation/NavigationResolver.js';

