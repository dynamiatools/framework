import { defineAsyncComponent, defineComponent, h, type Component } from 'vue';
import { FieldComponentRegistry } from '@dynamia-tools/ui-core';
import StarRating from '../components/fields/StarRating.vue';
import BadgeLabel from '../components/fields/BadgeLabel.vue';

/** `component: status` is the same label with a dot instead of a pill. */
const StatusLabel = defineComponent({
  name: 'StatusLabel',
  inheritAttrs: false,
  setup: (_props, { attrs }) => () => h(BadgeLabel as Component, { ...attrs, dot: true }),
});

/**
 * Registers the TailAdmin-based components this theme draws for the `component:` names of a view descriptor. Anything not
 * registered here keeps the basic component of `@dynamia-tools/vue`, or the one of the field type when the name is unknown.
 * The heavier ones are loaded when a view first draws them.
 */
export function registerThemeFieldComponents(): void {
  FieldComponentRegistry.register('rating', StarRating, ['stars']);
  FieldComponentRegistry.register('badge', BadgeLabel);
  FieldComponentRegistry.register('status', StatusLabel);
  FieldComponentRegistry.register('colorpicker', defineAsyncComponent(() => import('../components/fields/ColorField.vue')), ['colorbox']);
  FieldComponentRegistry.register('moneyinput', defineAsyncComponent(() => import('../components/fields/MoneyField.vue')), ['moneybox', 'money']);
  FieldComponentRegistry.register('quantityinput', defineAsyncComponent(() => import('../components/fields/QuantityField.vue')), ['quantitybox', 'quantity']);
}
