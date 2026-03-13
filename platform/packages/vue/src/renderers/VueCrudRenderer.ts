// VueCrudRenderer: implements CrudRenderer<Component> for Vue

import type { Component } from 'vue';
import { ViewTypes } from '@dynamia-tools/ui-core';
import type { CrudRenderer } from '@dynamia-tools/ui-core';
import type { CrudView } from '@dynamia-tools/ui-core';

/**
 * Vue implementation of CrudRenderer.
 * Returns the Crud Vue component for rendering a CrudView.
 */
export class VueCrudRenderer implements CrudRenderer<Component> {
  readonly supportedViewType = ViewTypes.Crud;

  render(_view: CrudView): Component {
    return { name: 'DynamiaCrud' } as Component;
  }
}
