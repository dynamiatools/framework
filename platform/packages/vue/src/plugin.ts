// plugin.ts: Vue plugin that registers all renderers, factories and global components

import type { App } from 'vue';
import { ViewRendererRegistry, ViewTypes } from '@dynamia-tools/ui-core';
import { VueFormView } from './views/VueFormView.js';
import { VueTableView } from './views/VueTableView.js';
import { VueCrudView } from './views/VueCrudView.js';
import { VueTreeView } from './views/VueTreeView.js';
import { VueConfigView } from './views/VueConfigView.js';
import { VueEntityPickerView } from './views/VueEntityPickerView.js';
import { VueFormRenderer } from './renderers/VueFormRenderer.js';
import { VueTableRenderer } from './renderers/VueTableRenderer.js';
import { VueCrudRenderer } from './renderers/VueCrudRenderer.js';
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
  install(app: App): void {
    // Register view renderers
    ViewRendererRegistry.register(ViewTypes.Form, new VueFormRenderer());
    ViewRendererRegistry.register(ViewTypes.Table, new VueTableRenderer());
    ViewRendererRegistry.register(ViewTypes.Crud, new VueCrudRenderer());

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
