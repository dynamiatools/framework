// global-components.ts
// Augments Vue's GlobalComponents interface so TypeScript / Volar recognises
// the components registered by the DynamiaVue plugin (app.component(...)).

import type NavBreadcrumbComponent from './components/NavBreadcrumb.vue';
import type NavMenuComponent from './components/NavMenu.vue';
import type CrudPageComponent from './components/CrudPage.vue';
import type ViewerComponent from './components/Viewer.vue';
import type FormComponent from './components/Form.vue';
import type TableComponent from './components/Table.vue';
import type TreeComponent from './components/Tree.vue';
import type CrudComponent from './components/Crud.vue';
import type FieldComponent from './components/Field.vue';
import type ActionsComponent from './components/Actions.vue';

declare module 'vue' {
  export interface GlobalComponents {
    DynamiaViewer: typeof ViewerComponent;
    DynamiaForm: typeof FormComponent;
    DynamiaTable: typeof TableComponent;
    DynamiaTree: typeof TreeComponent;
    DynamiaCrud: typeof CrudComponent;
    DynamiaField: typeof FieldComponent;
    DynamiaActions: typeof ActionsComponent;
    DynamiaNavMenu: typeof NavMenuComponent;
    DynamiaNavBreadcrumb: typeof NavBreadcrumbComponent;
    DynamiaCrudPage: typeof CrudPageComponent;
  }
}

// Needed to make this a module (not a script) so the augmentation is scoped correctly.
export {};

