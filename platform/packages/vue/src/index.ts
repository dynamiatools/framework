// @dynamia-tools/vue — Vue 3 adapter for Dynamia Platform UI

// ── Vue-reactive View classes ──────────────────────────────────────────────
export { VueView } from './views/VueView.js';
export { VueViewer } from './views/VueViewer.js';
export { VueFormView } from './views/VueFormView.js';
export { VueTableView } from './views/VueTableView.js';
export { VueCrudView } from './views/VueCrudView.js';
export { VueTreeView } from './views/VueTreeView.js';
export { VueConfigView } from './views/VueConfigView.js';
export { VueEntityPickerView } from './views/VueEntityPickerView.js';
export { VueDataSetViewRegistry, vueDataSetViewRegistry } from './views/VueDataSetViewRegistry.js';

// ── Renderers ──────────────────────────────────────────────────────────────
export { VueFormRenderer } from './renderers/VueFormRenderer.js';
export { VueTableRenderer } from './renderers/VueTableRenderer.js';
export { VueCrudRenderer } from './renderers/VueCrudRenderer.js';
export { VueFieldRenderer } from './renderers/VueFieldRenderer.js';
export { VueButtonActionRenderer } from './action-renderers/VueButtonActionRenderer.js';

// ── Actions ────────────────────────────────────────────────────────────────
export {
  isCancelCrudAction,
  isCreateCrudAction,
  isDeleteCrudAction,
  isEditCrudAction,
  isSaveCrudAction,
  matchesActionIdentity,
} from './actions/crudActionUtils.js';

// ── Client actions (re-exported from ui-core for convenience) ──────────────
export { ClientActionRegistry, registerClientAction, isClientActionApplicable } from '@dynamia-tools/ui-core';
export type { ClientAction, ClientActionContext } from '@dynamia-tools/ui-core';

// ── Composables ────────────────────────────────────────────────────────────
export { useViewer } from './composables/useViewer.js';
export { useView } from './composables/useView.js';
export { useForm } from './composables/useForm.js';
export type { UseFormOptions } from './composables/useForm.js';
export { useTable } from './composables/useTable.js';
export type { UseTableOptions } from './composables/useTable.js';
export { useCrud } from './composables/useCrud.js';
export type { UseCrudOptions } from './composables/useCrud.js';
export { useCrudPage } from './composables/useCrudPage.js';
export type { UseCrudPageOptions } from './composables/useCrudPage.js';
export { useEntityPicker } from './composables/useEntityPicker.js';
export type { UseEntityPickerOptions } from './composables/useEntityPicker.js';
export { useNavigation } from './composables/useNavigation.js';
export type { UseNavigationOptions } from './composables/useNavigation.js';
export { useDynamiaClient, DYNAMIA_CLIENT_KEY } from './composables/useDynamiaClient.js';

// ── Plugin ─────────────────────────────────────────────────────────────────
export { DynamiaVue } from './plugin.js';
export type { DynamiaVueOptions } from './plugin.js';

// ── Global component type augmentation (Volar / vue-tsc) ──────────────────
// Augments vue's GlobalComponents so TypeScript recognises the components
// registered by the DynamiaVue plugin without explicit local imports.
export * from './global-components.js';

// ── Vue Components (named exports for direct use) ──────────────────────────
export { default as DynamiaViewer } from './components/Viewer.vue';
export { default as FormComponent } from './components/Form.vue';
export { default as TableComponent } from './components/Table.vue';
export { default as TreeComponent } from './components/Tree.vue';
export { default as CrudComponent } from './components/Crud.vue';
export { default as DynamiaCrudPage } from './components/CrudPage.vue';
export { default as FieldComponent } from './components/Field.vue';
export { default as ActionsComponent } from './components/Actions.vue';
export { default as NavMenuComponent } from './components/NavMenu.vue';
export { default as NavBreadcrumbComponent } from './components/NavBreadcrumb.vue';
