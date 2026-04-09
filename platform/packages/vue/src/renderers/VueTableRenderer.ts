// VueTableRenderer: implements TableRenderer<Component> for Vue

import type { Component } from 'vue';
import { ViewTypes } from '@dynamia-tools/ui-core';
import type { TableRenderer } from '@dynamia-tools/ui-core';
import type { TableView } from '@dynamia-tools/ui-core';

/**
 * Vue implementation of TableRenderer.
 * Returns the Table Vue component for rendering a TableView.
 */
export class VueTableRenderer implements TableRenderer<Component> {
  readonly supportedViewType = ViewTypes.Table;

  render(_view: TableView): Component {
    return { name: 'DynamiaTable' } as Component;
  }
}
