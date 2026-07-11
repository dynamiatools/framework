// plugin.ts: Vue plugin that registers all renderers, factories and global components

import type { App } from 'vue';
import type { DynamiaClient } from '@dynamia-tools/sdk';
import { ActionRendererRegistry, ViewRendererRegistry, ViewTypes } from '@dynamia-tools/ui-core';
import { DYNAMIA_CLIENT_KEY } from './composables/useDynamiaClient.js';

/**
 * Options accepted by the DynamiaVue plugin.
 *
 * @example
 * ```ts
 * app.use(DynamiaVue, { client: new DynamiaClient({ baseUrl: '...', token: '...' }) });
 * ```
 */
export interface DynamiaVueOptions {
  /**
   * Optional DynamiaClient instance.
   * When provided it is made available to all components via `inject(DYNAMIA_CLIENT_KEY)` /
   * the `useDynamiaClient()` composable — primarily used by EntityPicker and similar
   * components that perform direct HTTP calls.
   */
  client?: DynamiaClient;
}
import { VueFormView } from './views/VueFormView.js';
import { VueTableView } from './views/VueTableView.js';
import { VueCrudView } from './views/VueCrudView.js';
import { VueTreeView } from './views/VueTreeView.js';
import { VueConfigView } from './views/VueConfigView.js';
import { VueEntityPickerView } from './views/VueEntityPickerView.js';
import { VueFormRenderer } from './renderers/VueFormRenderer.js';
import { VueTableRenderer } from './renderers/VueTableRenderer.js';
import { VueCrudRenderer } from './renderers/VueCrudRenderer.js';
import { VueButtonActionRenderer } from './action-renderers/VueButtonActionRenderer.js';
import ViewerComponent from './components/Viewer.vue';
import FormComponent from './components/Form.vue';
import TableComponent from './components/Table.vue';
import TreeComponent from './components/Tree.vue';
import CrudComponent from './components/Crud.vue';
import FieldComponent from './components/Field.vue';
import ActionsComponent from './components/Actions.vue';
import NavMenuComponent from './components/NavMenu.vue';
import NavBreadcrumbComponent from './components/NavBreadcrumb.vue';
import CrudPageComponent from './components/CrudPage.vue';

/**
 * Vue plugin for Dynamia Tools.
 * Registers all built-in view renderers, factories and global Vue components.
 *
 * Usage:
 * <pre>{@code
 * import { DynamiaVue } from '@dynamia-tools/vue';
 * app.use(DynamiaVue);
 * }</pre>
 */
export const DynamiaVue = {
  install(app: App, options?: DynamiaVueOptions): void {
    // Provide the DynamiaClient to the entire component tree when supplied
    if (options?.client) {
      app.provide(DYNAMIA_CLIENT_KEY, options.client);
    }

    // Register view renderers
    ViewRendererRegistry.register(ViewTypes.Form, new VueFormRenderer());
    ViewRendererRegistry.register(ViewTypes.Table, new VueTableRenderer());
    ViewRendererRegistry.register(ViewTypes.Crud, new VueCrudRenderer());
    ActionRendererRegistry.register('default', VueButtonActionRenderer, [
      'button',
      'ButtonActionRenderer',
      'ToolbarbuttonActionRenderer',
      'MenuitemActionRenderer',
      'MenuActionRenderer',
    ]);

    // Register view factories
    ViewRendererRegistry.registerViewFactory(
      ViewTypes.Form,
      (d, m) => new VueFormView(d, m)
    );
    ViewRendererRegistry.registerViewFactory(
      ViewTypes.Table,
      (d, m) => new VueTableView(d, m)
    );
    ViewRendererRegistry.registerViewFactory(
      ViewTypes.Crud,
      (d, m) => new VueCrudView(d, m)
    );
    ViewRendererRegistry.registerViewFactory(
      ViewTypes.Tree,
      (d, m) => new VueTreeView(d, m)
    );
    ViewRendererRegistry.registerViewFactory(
      ViewTypes.Config,
      (d, m) => new VueConfigView(d, m)
    );
    ViewRendererRegistry.registerViewFactory(
      ViewTypes.EntityPicker,
      (d, m) => new VueEntityPickerView(d, m)
    );

    // Register global components
    app.component('DynamiaViewer', ViewerComponent);
    app.component('DynamiaForm', FormComponent);
    app.component('DynamiaTable', TableComponent);
    app.component('DynamiaTree', TreeComponent);
    app.component('DynamiaCrud', CrudComponent);
    app.component('DynamiaField', FieldComponent);
    app.component('DynamiaActions', ActionsComponent);
    app.component('DynamiaNavMenu', NavMenuComponent);
    app.component('DynamiaNavBreadcrumb', NavBreadcrumbComponent);
    app.component('DynamiaCrudPage', CrudPageComponent);
  },
};
