// VueFieldRenderer: implements FieldRenderer<Component> for individual fields

import type { Component } from 'vue';
import type { FieldRenderer, ResolvedField, FormView } from '@dynamia-tools/ui-core';

/**
 * Vue implementation of FieldRenderer.
 * Dispatches to the correct field component based on FieldComponent type.
 */
export class VueFieldRenderer implements FieldRenderer<Component> {
  readonly supportedComponent: string;

  constructor(supportedComponent: string) {
    this.supportedComponent = supportedComponent;
  }

  render(_field: ResolvedField, _view: FormView): Component {
    return { name: `Dynamia-${this.supportedComponent}` } as Component;
  }
}
