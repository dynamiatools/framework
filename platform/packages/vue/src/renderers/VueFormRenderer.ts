// VueFormRenderer: implements FormRenderer<Component> for Vue

import type { Component } from 'vue';
import { ViewTypes } from '@dynamia-tools/ui-core';
import type { FormRenderer } from '@dynamia-tools/ui-core';
import type { FormView } from '@dynamia-tools/ui-core';

/**
 * Vue implementation of FormRenderer.
 * Returns the Form Vue component for rendering a FormView.
 */
export class VueFormRenderer implements FormRenderer<Component> {
  readonly supportedViewType = ViewTypes.Form;

  render(_view: FormView): Component {
    // Lazy import to avoid circular dependency at module initialization time
    return { name: 'DynamiaForm' } as Component;
  }
}
